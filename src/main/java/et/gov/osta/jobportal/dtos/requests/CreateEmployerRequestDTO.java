package et.gov.osta.jobportal.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateEmployerRequestDTO(
        @NotBlank(message = "Company name should not be blank.")
        String companyName,

        @NotEmpty(message = "Email and password should be set.")
        CreateUserRequestDTO user
){}
