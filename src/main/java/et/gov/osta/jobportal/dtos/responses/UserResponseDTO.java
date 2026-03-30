package et.gov.osta.jobportal.dtos.responses;

import java.util.Set;

public record UserResponseDTO(Long Id, String email, Set<String> phoneNumbers, String role) {}
