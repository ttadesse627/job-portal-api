package et.gov.osta.jobportal.domain.repositories;

import et.gov.osta.jobportal.domain.entities.Employer;
import et.gov.osta.jobportal.domain.entities.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {}
