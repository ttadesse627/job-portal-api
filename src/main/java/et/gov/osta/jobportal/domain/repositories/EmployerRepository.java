package et.gov.osta.jobportal.domain.repositories;

import et.gov.osta.jobportal.domain.entities.AppUser;
import et.gov.osta.jobportal.domain.entities.Employer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployerRepository extends JpaRepository<Employer, Long> {

    Page<Employer> findByCompanyNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Employer> findByUser_Id(Long userId);
    Employer getById(Long id);

    List<Employer> user(AppUser user);
}
