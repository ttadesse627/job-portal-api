package et.gov.osta.jobportal.helpers;

import et.gov.osta.jobportal.domain.repositories.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("candidateSecurity")
@RequiredArgsConstructor
public class CandidateSecurity {

    private final CandidateRepository candidateRepository;

    public boolean isOwner(Long candidateId, Long userId) {
        return candidateRepository.findById(candidateId)
                .map(candidate -> candidate.getUser() != null && candidate.getUser().getId().equals(userId))
                .orElse(false);
    }
}
