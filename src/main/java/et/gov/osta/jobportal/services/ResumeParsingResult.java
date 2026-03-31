package et.gov.osta.jobportal.services;

import et.gov.osta.jobportal.domain.enums.ResumeParseStatus;

public record ResumeParsingResult(
        ResumeParseStatus status,
        String provider,
        String fullName,
        String email,
        String phone,
        String skills,
        String error
) {
    public static ResumeParsingResult skipped(String provider, String reason) {
        return new ResumeParsingResult(ResumeParseStatus.SKIPPED, provider, null, null, null, null, reason);
    }

    public static ResumeParsingResult success(
            String provider,
            String fullName,
            String email,
            String phone,
            String skills
    ) {
        return new ResumeParsingResult(ResumeParseStatus.SUCCESS, provider, fullName, email, phone, skills, null);
    }

    public static ResumeParsingResult failed(String provider, String reason) {
        return new ResumeParsingResult(ResumeParseStatus.FAILED, provider, null, null, null, null, reason);
    }
}
