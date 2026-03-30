package et.gov.osta.jobportal.dtos.requests;

import et.gov.osta.jobportal.domain.enums.JobStatus;
import et.gov.osta.jobportal.domain.enums.JobType;

import java.time.LocalDateTime;

public record JobResponseDTO(
        Long id,
        String title,
        String position,
        String location,
        JobType jobType,
        String companyName,
        JobStatus status,
        LocalDateTime postedAt
) {}
