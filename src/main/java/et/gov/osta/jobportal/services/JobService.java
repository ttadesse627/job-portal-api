package et.gov.osta.jobportal.services;

import et.gov.osta.jobportal.domain.entities.Employer;
import et.gov.osta.jobportal.domain.entities.Job;
import et.gov.osta.jobportal.domain.enums.JobStatus;
import et.gov.osta.jobportal.domain.enums.JobType;
import et.gov.osta.jobportal.helpers.JobSpecification;
import et.gov.osta.jobportal.domain.repositories.EmployerRepository;
import et.gov.osta.jobportal.domain.repositories.JobRepository;
import et.gov.osta.jobportal.dtos.requests.JobRequestDTO;
import et.gov.osta.jobportal.dtos.requests.JobResponseDTO;
import et.gov.osta.jobportal.exceptions.ResourceNotFoundException;
import et.gov.osta.jobportal.exceptions.UnauthorizedException;
import et.gov.osta.jobportal.utils.PagedResponse;
import et.gov.osta.jobportal.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final EmployerRepository employerRepository;

    public JobResponseDTO create(JobRequestDTO dto, Long userId) {
        Employer employer = getEmployerForUser(userId);

        Job job = new Job();
        job.setTitle(dto.title());
        job.setPosition(dto.position());
        job.setLocation(dto.location());
        job.setJobType(dto.jobType());
        job.setDescription(dto.description());
        job.setCompanyName(dto.companyName());
        job.setSalaryMin(dto.salaryMin());
        job.setSalaryMax(dto.salaryMax());
        job.setDeadline(dto.deadline());
        job.setStatus(JobStatus.DRAFT);
        job.setEmployer(employer);

        return mapToDTO(jobRepository.save(job));
    }

    public JobResponseDTO update(Long id, JobRequestDTO dto, Long userId) {
        Job job = getJobOrThrow(id);
        validateOwnership(job, userId);

        job.setTitle(dto.title());
        job.setPosition(dto.position());
        job.setLocation(dto.location());
        job.setJobType(dto.jobType());
        job.setDescription(dto.description());
        job.setCompanyName(dto.companyName());
        job.setSalaryMin(dto.salaryMin());
        job.setSalaryMax(dto.salaryMax());
        job.setDeadline(dto.deadline());

        return mapToDTO(jobRepository.save(job));
    }

    public void publish(Long id, Long userId) {
        Job job = getJobOrThrow(id);
        validateOwnership(job, userId);

        job.setStatus(JobStatus.ACTIVE);
        job.setPostedDate(LocalDateTime.now());

        jobRepository.save(job);
    }

    public void close(Long id, Long userId) {
        Job job = getJobOrThrow(id);
        validateOwnership(job, userId);

        job.setStatus(JobStatus.CLOSED);
        jobRepository.save(job);
    }

    public PagedResponse<JobResponseDTO> getJobs(
            String title,
            String position,
            String location,
            JobType jobType,
            LocalDate startDate,
            LocalDate endDate,
            int pageNumber,
            int pageSize,
            String sortBy,
            boolean sortAsc
    ) {
        Sort sort = sortAsc
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        var pagedJobs = jobRepository.findAll(
                JobSpecification.filter(title, position, location, jobType, startDate, endDate),
                pageable
        );
        return PaginationUtils.toPagedResponse(pagedJobs, this::mapToDTO);
    }

    public void delete(Long id, Long userId) {
        Job job = getJobOrThrow(id);
        validateOwnership(job, userId);

        jobRepository.delete(job);
    }

    public JobResponseDTO view(Long id) {
        return mapToDTO(getJobOrThrow(id));
    }

    private Job getJobOrThrow(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    private Employer getEmployerForUser(Long userId) {
        return employerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));
    }

    private void validateOwnership(Job job, Long userId) {
        if (job.getEmployer() == null || job.getEmployer().getUser() == null) {
            throw new UnauthorizedException("Job owner is invalid");
        }

        if (!job.getEmployer().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not allowed to modify this job");
        }
    }

    private JobResponseDTO mapToDTO(Job job) {
        return new JobResponseDTO(
                job.getId(),
                job.getTitle(),
                job.getPosition(),
                job.getLocation(),
                job.getJobType(),
                job.getCompanyName(),
                job.getStatus(),
                job.getPostedDate()
        );
    }
}
