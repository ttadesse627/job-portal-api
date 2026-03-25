package et.gov.osta.jobportal.domain.repositories;

import et.gov.osta.jobportal.domain.entities.Candidate;
import et.gov.osta.jobportal.domain.entities.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {}
