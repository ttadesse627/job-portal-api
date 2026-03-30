package et.gov.osta.jobportal.dtos.requests;

import et.gov.osta.jobportal.domain.enums.JobType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record JobRequestDTO(
        @NotBlank(message = "Job title is required.")
        String title,

        @NotBlank(message = "Position is required.")
        String position,

        @NotBlank(message = "Location is required.")
        String location,

        @NotNull(message = "Job type is required.")
        JobType jobType,

        @NotBlank(message = "Description is required.")
        String description,

        @NotBlank(message = "Company name is required.")
        String companyName,
        BigDecimal salaryMin,
        BigDecimal salaryMax,

        @NotNull(message = "Application deadline is required.")
        @FutureOrPresent(message = "Application deadline must be today or later.")
        LocalDate deadline
) {}
