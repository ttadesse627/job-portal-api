package et.gov.osta.jobportal.domain.entities;

import et.gov.osta.jobportal.domain.enums.ResumeParseStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Resume {
    @Id
    @GeneratedValue
    private Long id;

    private String fileUrl;
    @Enumerated(EnumType.STRING)
    private ResumeParseStatus parseStatus;
    private String parserProvider;
    private String parsedFullName;
    private String parsedEmail;
    private String parsedPhone;
    @Column(columnDefinition = "TEXT")
    private String parsedSkills;
    @Column(columnDefinition = "TEXT")
    private String parseError;

    @OneToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;
}
