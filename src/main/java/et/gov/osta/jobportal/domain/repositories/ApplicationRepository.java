package et.gov.osta.jobportal.domain.repositories;

import et.gov.osta.jobportal.domain.entities.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);

    Page<Application> findByCandidateId(Long candidateId, Pageable pageable);

    Page<Application> findByJobId(Long jobId, Pageable pageable);
}
