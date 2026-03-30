package et.gov.osta.jobportal.services;

import et.gov.osta.jobportal.domain.entities.Application;
import et.gov.osta.jobportal.domain.entities.Candidate;
import et.gov.osta.jobportal.domain.entities.Job;
import et.gov.osta.jobportal.domain.enums.Role;
import et.gov.osta.jobportal.domain.enums.ApplicationStatus;
import et.gov.osta.jobportal.domain.repositories.ApplicationRepository;
import et.gov.osta.jobportal.domain.repositories.CandidateRepository;
import et.gov.osta.jobportal.domain.repositories.JobRepository;
import et.gov.osta.jobportal.dtos.requests.ApplyRequestDTO;
import et.gov.osta.jobportal.dtos.responses.ApplicationResponseDTO;
import et.gov.osta.jobportal.exceptions.BadRequestException;
import et.gov.osta.jobportal.exceptions.ResourceNotFoundException;
import et.gov.osta.jobportal.exceptions.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;

    public ApplicationResponseDTO apply(Long jobId, Long userId, ApplyRequestDTO dto) {
        Candidate candidate = getCandidateForUser(userId);

        if (applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), jobId)) {
            throw new BadRequestException("Already applied to this job");
        }

        Job job = getJobOrThrow(jobId);

        Application app = new Application();
        app.setJob(job);
        app.setCandidate(candidate);
        app.setStatus(ApplicationStatus.APPLIED);
        app.setAppliedAt(LocalDateTime.now());
        app.setCoverLetter(dto.coverLetter());

        return mapToDTO(applicationRepository.save(app));
    }

    public Page<ApplicationResponseDTO> getMyApplications(Long userId, Pageable pageable) {
        Candidate candidate = getCandidateForUser(userId);

        return applicationRepository.findByCandidateId(candidate.getId(), pageable)
                .map(this::mapToDTO);
    }

    public Page<ApplicationResponseDTO> getApplicantsForJob(
            Long jobId,
            Long userId,
            Role role,
            Pageable pageable
    ) {
        Job job = getJobOrThrow(jobId);
        validateEmployerAccess(job, userId, role);

        return applicationRepository.findByJobId(jobId, pageable)
                .map(this::mapToDTO);
    }

    public void updateStatus(Long applicationId, ApplicationStatus status, Long userId, Role role) {
        Application app = getApplicationOrThrow(applicationId);
        validateEmployerAccess(app.getJob(), userId, role);

        app.setStatus(status);
        applicationRepository.save(app);
    }

    public void withdraw(Long applicationId, Long userId) {
        Application app = getApplicationOrThrow(applicationId);

        if (app.getCandidate() == null || app.getCandidate().getUser() == null) {
            throw new UnauthorizedException("Application owner is invalid");
        }

        if (!app.getCandidate().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not allowed to withdraw this application");
        }

        app.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(app);
    }

    private Candidate getCandidateForUser(Long userId) {
        return candidateRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
    }

    private Job getJobOrThrow(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    private Application getApplicationOrThrow(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    private void validateEmployerAccess(Job job, Long userId, Role role) {
        if (role == Role.ADMIN) {
            return;
        }

        if (job.getEmployer() == null || job.getEmployer().getUser() == null) {
            throw new UnauthorizedException("Job owner is invalid");
        }

        if (!job.getEmployer().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not allowed to manage applications for this job");
        }
    }

    private ApplicationResponseDTO mapToDTO(Application app) {
        return new ApplicationResponseDTO(
                app.getId(),
                app.getJob().getId(),
                app.getJob().getTitle(),
                app.getStatus(),
                app.getAppliedAt()
        );
    }
}
