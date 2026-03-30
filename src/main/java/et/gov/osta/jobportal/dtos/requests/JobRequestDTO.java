package et.gov.osta.jobportal.dtos.requests;

import et.gov.osta.jobportal.domain.enums.JobType;

import java.math.BigDecimal;

public record JobRequestDTO(
        String title,
        String position,
        String location,
        JobType jobType,
        String description,
        String companyName,
        BigDecimal salaryMin,
        BigDecimal salaryMax
) {}
