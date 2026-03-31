package et.gov.osta.jobportal.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "resume-parser.apy-hub")
public class ResumeParserProperties {

    private boolean enabled = true;
    private String baseUrl = "https://sharpapi.com";
    private String apiKey = "";
    private String language = "English";
    private int pollAttempts = 10;
    private long pollDelayMillis = 2000;
}
