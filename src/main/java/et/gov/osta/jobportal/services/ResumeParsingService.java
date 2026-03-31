package et.gov.osta.jobportal.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import et.gov.osta.jobportal.configs.ResumeParserProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeParsingService {

    private static final String PROVIDER = "ApyHub SharpAPI";
    private static final String RATE_LIMIT_FALLBACK_MESSAGE =
            "Rate limited by resume parser provider; parsing deferred";

    private final ResumeParserProperties properties;
    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    public ResumeParsingResult parse(Path resumePath) {
        if (!properties.isEnabled() || properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            return ResumeParsingResult.skipped(PROVIDER, "Parser disabled or API key missing");
        }

        try {
            JsonNode jobResponse = readJson(submitJob(resumePath));
            String statusUrl = firstNonBlank(
                    text(jobResponse, "status_url"),
                    text(jobResponse.path("data"), "status_url")
            );
            if (statusUrl == null) {
                return ResumeParsingResult.failed(PROVIDER, "Missing status URL from provider");
            }

            JsonNode data = pollForResult(statusUrl);
            if (data == null) {
                return ResumeParsingResult.failed(PROVIDER, "Timed out waiting for provider result");
            }

            return ResumeParsingResult.success(
                    PROVIDER,
                    extractFullName(data),
                    firstNonBlank(
                            text(data, "candidate_email"),
                            text(data, "email"),
                            text(data, "mail"),
                            text(data.path("basics"), "email")
                    ),
                    firstNonBlank(
                            text(data, "candidate_phone"),
                            text(data, "phone"),
                            text(data, "phone_number"),
                            text(data, "mobile"),
                            text(data.path("basics"), "phone")
                    ),
                    extractSkills(data)
            );
        } catch (RestClientException exception) {
            if (isRateLimitError(exception)) {
                return ResumeParsingResult.skipped(PROVIDER, RATE_LIMIT_FALLBACK_MESSAGE);
            }
            return ResumeParsingResult.failed(PROVIDER, exception.getMessage());
        } catch (JsonProcessingException exception) {
            return ResumeParsingResult.failed(PROVIDER, "Could not parse response: " + exception.getOriginalMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return ResumeParsingResult.failed(PROVIDER, "Interrupted while waiting for provider result");
        }
    }

    private String submitJob(Path resumePath) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(resumePath));
        body.add("language", properties.getLanguage());

        return restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build()
                .post()
                .uri("/sharpapi/api/v1/hr/parse_resume")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Accept", "application/json")
                .header("apy-token", properties.getApiKey())
                .body(body)
                .retrieve()
                .body(String.class);
    }

    private JsonNode pollForResult(String statusUrl) throws JsonProcessingException, InterruptedException {
        String resolvedStatusUrl = resolveStatusUrl(statusUrl);

        for (int attempt = 0; attempt < properties.getPollAttempts(); attempt++) {
            String responseBody = restClientBuilder
                    .build()
                    .get()
                    .uri(resolvedStatusUrl)
                    .header("Accept", "application/json")
                    .header("apy-token", properties.getApiKey())
                    .retrieve()
                    .body(String.class);

            JsonNode response = readJson(responseBody);
            JsonNode resultNode = extractResultNode(response);
            if (resultNode != null) {
                return resultNode;
            }

            String status = firstNonBlank(
                    text(response.path("data").path("attributes"), "status"),
                    text(response.path("attributes"), "status"),
                    text(response, "status")
            );
            if (status != null && ("failed".equalsIgnoreCase(status) || "error".equalsIgnoreCase(status))) {
                return null;
            }

            Thread.sleep(properties.getPollDelayMillis());
        }

        return null;
    }

    private String resolveStatusUrl(String statusUrl) {
        if (statusUrl == null || statusUrl.isBlank()) {
            return statusUrl;
        }

        String apiBaseUrl = properties.getBaseUrl().endsWith("/")
                ? properties.getBaseUrl().substring(0, properties.getBaseUrl().length() - 1)
                : properties.getBaseUrl();

        return statusUrl
                .replace("https://apyhub.com/services/provider", apiBaseUrl)
                .replace("https://apyhub.com", apiBaseUrl)
                .replace("/services/provider", "");
    }

    private JsonNode readJson(String responseBody) throws JsonProcessingException {
        if (responseBody == null || responseBody.isBlank()) {
            return objectMapper.createObjectNode();
        }

        return objectMapper.readTree(responseBody);
    }

    private JsonNode extractResultNode(JsonNode response) {
        if (response == null || response.isMissingNode()) {
            return null;
        }

        JsonNode dataAttributesResult = response.path("data").path("attributes").path("result");
        if (!dataAttributesResult.isMissingNode() && !dataAttributesResult.isNull()) {
            return dataAttributesResult;
        }

        JsonNode attributesResult = response.path("attributes").path("result");
        if (!attributesResult.isMissingNode() && !attributesResult.isNull()) {
            return attributesResult;
        }

        JsonNode directResult = response.path("result");
        if (!directResult.isMissingNode() && !directResult.isNull()) {
            return directResult;
        }

        return null;
    }

    private String extractFullName(JsonNode data) {
        if (data == null || data.isMissingNode()) {
            return null;
        }

        JsonNode nameNode = data.path("name");
        String direct = null;
        if (nameNode.isTextual()) {
            direct = nameNode.asText().trim();
        } else {
            direct = firstNonBlank(
                    text(nameNode, "raw"),
                    text(nameNode, "text"),
                    text(nameNode, "full"),
                    text(data.path("candidateName"), "raw"),
                    text(data.path("candidateName"), "text")
            );
        }
        if (direct == null) {
            direct = firstNonBlank(
                    text(data, "candidate_name"),
                    text(data.path("basics"), "name"),
                    text(data.path("basics"), "fullName")
            );
        }
        if (direct != null) {
            return direct;
        }

        return joinNonBlank(
                firstNonBlank(text(data.path("name"), "first"), text(data.path("candidateName"), "first")),
                firstNonBlank(text(data.path("name"), "last"), text(data.path("candidateName"), "last"))
        );
    }

    private String extractFirstValue(JsonNode data, String arrayField, String... candidateFields) {
        if (data == null || data.isMissingNode()) {
            return null;
        }

        JsonNode values = data.path(arrayField);
        if (!values.isArray()) {
            return null;
        }

        for (JsonNode item : values) {
            if (item.isTextual()) {
                String value = item.asText().trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }

            for (String field : candidateFields) {
                String value = text(item, field);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    private String extractSkills(JsonNode data) {
        if (data == null || data.isMissingNode()) {
            return null;
        }

        JsonNode skills = data.path("skills");
        if (!skills.isArray()) {
            skills = data.path("skill");
        }
        if (!skills.isArray()) {
            skills = data.path("hard_skills");
        }
        if (!skills.isArray()) {
            skills = data.path("soft_skills");
        }
        if (!skills.isArray()) {
            skills = data.path("basics").path("skills");
        }
        if (!skills.isArray()) {
            skills = flattenPositionSkills(data.path("positions"));
        }
        if (!skills.isArray()) {
            return null;
        }

        List<String> extracted = new ArrayList<>();
        for (JsonNode item : skills) {
            String value;
            if (item.isTextual()) {
                value = item.asText().trim();
            } else {
                value = firstNonBlank(
                        text(item, "name"),
                        text(item, "raw"),
                        text(item, "value")
                );
            }

            if (value != null && extracted.stream().noneMatch(existing -> existing.equalsIgnoreCase(value))) {
                extracted.add(value);
            }

            if (extracted.size() == 10) {
                break;
            }
        }

        return extracted.isEmpty() ? null : String.join(", ", extracted);
    }

    private JsonNode flattenPositionSkills(JsonNode positions) {
        if (!positions.isArray()) {
            return objectMapper.createArrayNode();
        }

        var aggregated = objectMapper.createArrayNode();
        for (JsonNode position : positions) {
            JsonNode skills = position.path("skills");
            if (!skills.isArray()) {
                continue;
            }

            for (JsonNode skill : skills) {
                aggregated.add(skill);
            }
        }
        return aggregated;
    }

    private String text(JsonNode node, String field) {
        if (node == null || node.isMissingNode()) {
            return null;
        }

        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }

        String text = value.asText().trim();
        return text.isEmpty() ? null : text;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String joinNonBlank(String first, String second) {
        if (first == null && second == null) {
            return null;
        }

        if (first == null) {
            return second;
        }

        if (second == null) {
            return first;
        }

        return first + " " + second;
    }

    private boolean isRateLimitError(RestClientException exception) {
        String message = exception.getMessage();
        if (message == null) {
            return false;
        }

        String lowered = message.toLowerCase();
        return lowered.contains("429") || lowered.contains("too many requests") || lowered.contains("rate limit");
    }
}
