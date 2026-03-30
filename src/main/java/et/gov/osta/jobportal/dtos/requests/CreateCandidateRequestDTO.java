package et.gov.osta.jobportal.dtos.requests;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public record CreateCandidateRequestDTO(
        @NotBlank(message = "First name should not be blank.")
        @Size(min = 3, max = 55, message = "Length is limited to >3 and <55 characters.")
        String firstName,

        @NotBlank(message = "Last name should not be blank.")
        @Size(min = 3, max = 55, message = "Length is limited to >3 and <55 characters.")
        String lastName,

        Set<String> phoneNumbers,

        @Email(message = "Invalid email!")
        @NotBlank(message = "Email should not be blank.")
        String email,

        @Size(min = 6, max = 10, message = "password length should be longer than 6 and shorter than 10 characters.")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!\"#$%&'()*+,\\-./:;<=>?@\\[\\\\\\]^_`{|}~]).+$")
        String password,
        MultipartFile resume
    ){}
