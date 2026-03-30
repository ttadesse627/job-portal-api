package et.gov.osta.jobportal.helpers;

import et.gov.osta.jobportal.domain.entities.AppUser;
import et.gov.osta.jobportal.domain.repositories.JobRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JobSecurity {

    private final JobRepository jobRepository;

    public JobSecurity(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public boolean isOwner(Long jobId, Authentication auth) {
        AppUser user = (AppUser) auth.getPrincipal();
        if (user == null) return false;
        return jobRepository.findById(jobId)
                .map(job -> job.getEmployer().getUser().getId().equals(user.getId()))
                .orElse(false);
    }
}
