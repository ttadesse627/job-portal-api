package et.gov.osta.jobportal.dtos.responses;

import et.gov.osta.jobportal.domain.enums.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationResponseDTO(
        Long id,
        Long jobId,
        String jobTitle,
        ApplicationStatus status,
        LocalDateTime appliedAt
) {}
