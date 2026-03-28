package et.gov.osta.jobportal.dtos.responses;

public record CandidateResponseDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String resumeUrl
){}
