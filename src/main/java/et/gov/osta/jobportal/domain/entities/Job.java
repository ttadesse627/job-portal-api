package et.gov.osta.jobportal.domain.entities;

import et.gov.osta.jobportal.domain.enums.JobStatus;
import et.gov.osta.jobportal.domain.enums.JobType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "jobs")
public class Job {
    @Id @GeneratedValue
    private Long id;

    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
    private LocalDateTime postedDate;
    private LocalDate deadline;
    private String position;
    private String location;
    private String companyName;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    private BigDecimal salaryMin;
    private BigDecimal salaryMax;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @ManyToOne
    @JoinColumn(name = "employer_id")
    private Employer employer;

    @ManyToMany
    @JoinTable(
            name = "job_applications",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "candidate_id")
    )
    private List<Candidate> candidates;
}
