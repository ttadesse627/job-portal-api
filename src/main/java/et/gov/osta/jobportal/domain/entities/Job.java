package et.gov.osta.jobportal.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class Job {
    @Id @GeneratedValue
    private Long id;

    private String title;
    private String description;
    private LocalDate deadline;

    @ManyToOne
    private Employer employer;

    @ManyToMany
    @JoinTable(
            name = "job_applications",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "candidate_id")
    )
    private List<Candidate> candidates;
}
