package et.gov.osta.jobportal.domain.entities;

import jakarta.persistence.*;

@Entity
public class Resume {
    @Id
    @GeneratedValue
    private Long id;

    private String fileUrl;

    @OneToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;
}
