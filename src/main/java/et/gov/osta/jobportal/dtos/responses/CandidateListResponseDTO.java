package et.gov.osta.jobportal.dtos.responses;

public record CandidateListResponseDTO(
        Long id,
        String firstName,
        String lastName,
        String email
){}
