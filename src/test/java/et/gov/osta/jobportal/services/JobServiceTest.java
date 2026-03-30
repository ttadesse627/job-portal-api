package et.gov.osta.jobportal.services;

import et.gov.osta.jobportal.domain.entities.AppUser;
import et.gov.osta.jobportal.domain.entities.Employer;
import et.gov.osta.jobportal.domain.entities.Job;
import et.gov.osta.jobportal.domain.enums.JobStatus;
import et.gov.osta.jobportal.domain.enums.JobType;
import et.gov.osta.jobportal.domain.repositories.EmployerRepository;
import et.gov.osta.jobportal.domain.repositories.JobRepository;
import et.gov.osta.jobportal.dtos.requests.JobRequestDTO;
import et.gov.osta.jobportal.exceptions.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private EmployerRepository employerRepository;

    @InjectMocks
    private JobService jobService;

    @Test
    void createAssociatesJobWithEmployerResolvedByAuthenticatedUser() {
        Employer employer = employer(10L, 20L);
        JobRequestDTO request = jobRequest("QA Backend Engineer", "Backend Engineer");

        when(employerRepository.findByUser_Id(20L)).thenReturn(Optional.of(employer));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> {
            Job savedJob = invocation.getArgument(0);
            savedJob.setId(99L);
            return savedJob;
        });

        var response = jobService.create(request, 20L);

        assertEquals(99L, response.id());
        assertEquals(JobStatus.DRAFT, response.status());
        assertEquals("QA Portal Labs", response.companyName());

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        assertEquals(LocalDate.now().plusDays(14), captor.getValue().getDeadline());
    }

    @Test
    void updateAllowsEmployerOwnerMatchedByUserId() {
        Job existingJob = job(1L, 10L, 20L, JobStatus.DRAFT);
        JobRequestDTO request = jobRequest("Updated Title", "Platform Engineer");

        when(jobRepository.findById(1L)).thenReturn(Optional.of(existingJob));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = jobService.update(1L, request, 20L);

        assertEquals("Updated Title", response.title());
        assertEquals("Platform Engineer", response.position());
        assertEquals(LocalDate.now().plusDays(14), existingJob.getDeadline());
    }

    @Test
    void publishSetsActiveStatusAndPostedDateForOwner() {
        Job existingJob = job(1L, 10L, 20L, JobStatus.DRAFT);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(existingJob));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        jobService.publish(1L, 20L);

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        assertEquals(JobStatus.ACTIVE, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getPostedDate());
    }

    @Test
    void closeSetsClosedStatusForOwner() {
        Job existingJob = job(1L, 10L, 20L, JobStatus.ACTIVE);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(existingJob));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        jobService.close(1L, 20L);

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        assertEquals(JobStatus.CLOSED, captor.getValue().getStatus());
    }

    @Test
    void deleteRejectsNonOwnerUserId() {
        Job existingJob = job(1L, 10L, 20L, JobStatus.DRAFT);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(existingJob));

        assertThrows(UnauthorizedException.class, () -> jobService.delete(1L, 999L));
        verify(jobRepository, never()).delete(any(Job.class));
    }

    private JobRequestDTO jobRequest(String title, String position) {
        return new JobRequestDTO(
                title,
                position,
                "Addis Ababa",
                JobType.FULL_TIME,
                "Testing the job workflow",
                "QA Portal Labs",
                BigDecimal.valueOf(20000),
                BigDecimal.valueOf(30000),
                LocalDate.now().plusDays(14)
        );
    }

    private Employer employer(Long employerId, Long userId) {
        Employer employer = new Employer();
        employer.setId(employerId);
        employer.setCompanyName("QA Portal Labs");
        employer.setUser(user(userId));
        return employer;
    }

    private Job job(Long jobId, Long employerId, Long userId, JobStatus status) {
        Job job = new Job();
        job.setId(jobId);
        job.setEmployer(employer(employerId, userId));
        job.setStatus(status);
        job.setCompanyName("QA Portal Labs");
        job.setTitle("Original Title");
        job.setPosition("Original Position");
        job.setLocation("Addis Ababa");
        job.setJobType(JobType.FULL_TIME);
        job.setDeadline(LocalDate.now().plusDays(7));
        return job;
    }

    private AppUser user(Long userId) {
        AppUser user = new AppUser();
        user.setId(userId);
        user.setEmail("qa.employer@example.com");
        return user;
    }
}
