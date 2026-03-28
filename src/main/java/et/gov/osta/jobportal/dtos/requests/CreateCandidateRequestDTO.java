package et.gov.osta.jobportal.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCandidateRequestDTO(
        @NotBlank(message = "First name should not be blank.")
        @Size(min = 3, max = 55, message = "Length is limited to >3 and <55 characters.")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])", message = "Name can only contains alphabets")
        String firstName,

        @NotBlank(message = "Last name should not be blank.")
        @Size(min = 3, max = 55, message = "Length is limited to >3 and <55 characters.")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])", message = "Name can only contains alphabets")
        String lastName,

        @NotEmpty(message = "You have to set user email and password.")
        CreateUserRequestDTO user
    ){}
