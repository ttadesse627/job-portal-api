package et.gov.osta.jobportal.domain.repositories;

import et.gov.osta.jobportal.domain.entities.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Page<Candidate> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);

    Optional<Candidate> findByUser_Id(Long userId);
}
