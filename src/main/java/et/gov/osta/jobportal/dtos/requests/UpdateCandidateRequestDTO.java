package et.gov.osta.jobportal.dtos.requests;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;

public record UpdateCandidateRequestDTO(

        @Size(min = 3, max = 55, message = "Length is limited to >3 and <55 characters.")
        @Pattern(regexp = "^[A-Za-z]+$", message = "Name can only contains alphabets")
        String firstName,

        @Size(min = 3, max = 55, message = "Length is limited to >3 and <55 characters.")
        @Pattern(regexp = "^[A-Za-z]+$", message = "Name can only contains alphabets")
        String lastName,
        Set<String> phoneNumbers
    ){}
