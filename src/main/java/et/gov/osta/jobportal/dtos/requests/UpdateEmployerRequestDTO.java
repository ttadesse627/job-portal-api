package et.gov.osta.jobportal.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateEmployerRequestDTO(
        @NotBlank(message = "Company name should not be blank.")
        String companyName,
        Set<String> phoneNumbers
){}
