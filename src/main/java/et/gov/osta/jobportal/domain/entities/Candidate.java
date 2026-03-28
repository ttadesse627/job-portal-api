package et.gov.osta.jobportal.domain.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "candidates")
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @OneToOne(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Resume resume;

    @ManyToMany(mappedBy = "candidates")
    private List<Job> appliedJobs = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    public void setResume(Resume resume){
        this.resume = resume;
        if (resume != null){
            resume.setCandidate(this);
        }
    }
}
