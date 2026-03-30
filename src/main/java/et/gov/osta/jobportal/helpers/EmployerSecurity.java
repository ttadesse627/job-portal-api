package et.gov.osta.jobportal.helpers;

import et.gov.osta.jobportal.domain.repositories.EmployerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("employerSecurity")
@RequiredArgsConstructor
public class EmployerSecurity {

    private final EmployerRepository employerRepository;

    public boolean isOwner(Long employerId, Long userId) {
        return employerRepository.findById(employerId)
                .map(employer -> employer.getUser() != null && employer.getUser().getId().equals(userId))
                .orElse(false);
    }
}
