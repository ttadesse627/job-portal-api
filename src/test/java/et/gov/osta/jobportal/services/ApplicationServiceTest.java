package et.gov.osta.jobportal.services;

import et.gov.osta.jobportal.domain.entities.AppUser;
import et.gov.osta.jobportal.domain.entities.Application;
import et.gov.osta.jobportal.domain.entities.Candidate;
import et.gov.osta.jobportal.domain.entities.Employer;
import et.gov.osta.jobportal.domain.entities.Job;
import et.gov.osta.jobportal.domain.enums.ApplicationStatus;
import et.gov.osta.jobportal.domain.enums.Role;
import et.gov.osta.jobportal.domain.repositories.ApplicationRepository;
import et.gov.osta.jobportal.domain.repositories.CandidateRepository;
import et.gov.osta.jobportal.domain.repositories.JobRepository;
import et.gov.osta.jobportal.dtos.requests.ApplyRequestDTO;
import et.gov.osta.jobportal.exceptions.BadRequestException;
import et.gov.osta.jobportal.exceptions.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CandidateRepository candidateRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void applyResolvesCandidateFromAuthenticatedUserId() {
        Candidate candidate = candidate(1L, 3L);
        Job job = job(2L, 2L);

        when(candidateRepository.findByUser_Id(3L)).thenReturn(Optional.of(candidate));
        when(applicationRepository.existsByCandidateIdAndJobId(1L, 2L)).thenReturn(false);
        when(jobRepository.findById(2L)).thenReturn(Optional.of(job));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> {
            Application app = invocation.getArgument(0);
            app.setId(7L);
            return app;
        });

        var response = applicationService.apply(2L, 3L, new ApplyRequestDTO("QA application"));

        assertEquals(7L, response.id());
        assertEquals(ApplicationStatus.APPLIED, response.status());
        assertEquals(2L, response.jobId());
    }

    @Test
    void getMyApplicationsUsesCandidateResolvedFromUserId() {
        Candidate candidate = candidate(1L, 3L);
        Application application = application(8L, candidate, job(2L, 2L), ApplicationStatus.APPLIED);

        when(candidateRepository.findByUser_Id(3L)).thenReturn(Optional.of(candidate));
        when(applicationRepository.findByCandidateId(1L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(application)));

        var page = applicationService.getMyApplications(3L, Pageable.unpaged());

        assertEquals(1, page.getTotalElements());
        assertEquals(8L, page.getContent().getFirst().id());
    }

    @Test
    void applyRejectsExpiredJobDeadline() {
        Candidate candidate = candidate(1L, 3L);
        Job job = job(2L, 2L);
        job.setDeadline(LocalDate.now().minusDays(1));

        when(candidateRepository.findByUser_Id(3L)).thenReturn(Optional.of(candidate));
        when(applicationRepository.existsByCandidateIdAndJobId(1L, 2L)).thenReturn(false);
        when(jobRepository.findById(2L)).thenReturn(Optional.of(job));

        assertThrows(BadRequestException.class, () ->
                applicationService.apply(2L, 3L, new ApplyRequestDTO("Late application")));
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void getApplicantsRejectsEmployerWhoDoesNotOwnJob() {
        Job job = job(2L, 2L);

        when(jobRepository.findById(2L)).thenReturn(Optional.of(job));

        assertThrows(UnauthorizedException.class, () ->
                applicationService.getApplicantsForJob(2L, 99L, Role.EMPLOYER, Pageable.unpaged()));
        verify(applicationRepository, never()).findByJobId(any(), any());
    }

    @Test
    void updateStatusAllowsAdmin() {
        Application application = application(8L, candidate(1L, 3L), job(2L, 2L), ApplicationStatus.APPLIED);

        when(applicationRepository.findById(8L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        applicationService.updateStatus(8L, ApplicationStatus.SHORTLISTED, 1L, Role.ADMIN);

        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        assertEquals(ApplicationStatus.SHORTLISTED, captor.getValue().getStatus());
    }

    @Test
    void withdrawAllowsCandidateOwnerMatchedByUserId() {
        Candidate candidate = candidate(1L, 3L);
        Application application = application(8L, candidate, job(2L, 2L), ApplicationStatus.APPLIED);

        when(applicationRepository.findById(8L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        applicationService.withdraw(8L, 3L);

        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        assertEquals(ApplicationStatus.WITHDRAWN, captor.getValue().getStatus());
    }

    @Test
    void withdrawRejectsDifferentUser() {
        Candidate candidate = candidate(1L, 3L);
        Application application = application(8L, candidate, job(2L, 2L), ApplicationStatus.APPLIED);

        when(applicationRepository.findById(8L)).thenReturn(Optional.of(application));

        assertThrows(UnauthorizedException.class, () -> applicationService.withdraw(8L, 99L));
        verify(applicationRepository, never()).save(any(Application.class));
    }

    private Application application(Long id, Candidate candidate, Job job, ApplicationStatus status) {
        Application application = new Application();
        application.setId(id);
        application.setCandidate(candidate);
        application.setJob(job);
        application.setStatus(status);
        return application;
    }

    private Candidate candidate(Long candidateId, Long userId) {
        Candidate candidate = new Candidate();
        candidate.setId(candidateId);
        candidate.setUser(user(userId));
        candidate.setFirstName("Abel");
        candidate.setLastName("Kassa");
        return candidate;
    }

    private Job job(Long jobId, Long employerUserId) {
        Job job = new Job();
        job.setId(jobId);
        job.setTitle("Applications QA Job");
        job.setEmployer(employer(employerUserId));
        job.setDeadline(LocalDate.now().plusDays(7));
        return job;
    }

    private Employer employer(Long userId) {
        Employer employer = new Employer();
        employer.setId(1L);
        employer.setCompanyName("QA Portal Labs");
        employer.setUser(user(userId));
        return employer;
    }

    private AppUser user(Long userId) {
        AppUser user = new AppUser();
        user.setId(userId);
        return user;
    }
}
