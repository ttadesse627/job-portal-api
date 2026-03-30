package et.gov.osta.jobportal.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "employers")
public class Employer {
    @Id @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String companyName;

    @OneToMany(mappedBy = "employer")
    private List<Job> jobs;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;
}
