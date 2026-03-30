package et.gov.osta.jobportal.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Setter
@Getter
public class ApiError {

    private LocalDateTime timestamp;
    private int status;
    private String error;     // e.g. "Bad Request"
    private String message;   // human-readable
    private String path;      // request URI
    private Map<String, String> validationErrors; // for field errors
}