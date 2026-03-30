package et.gov.osta.jobportal.dtos.requests;

import jakarta.validation.constraints.*;

import java.util.Set;

public record CreateUserRequestDTO(

        Set<String> phoneNumbers,

        @Email(message = "Invalid email!")
        @NotBlank(message = "Last name should not be blank.")
        String email,

        @Size(min = 6, max = 10, message = "password length should be longer than 6 and shorter than 10 characters.")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!\"#$%&'()*+,\\-./:;<=>?@\\[\\\\\\]^_`{|}~]).+$")
        String password
    ){}
