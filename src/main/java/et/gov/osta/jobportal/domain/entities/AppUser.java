package et.gov.osta.jobportal.domain.entities;

import et.gov.osta.jobportal.domain.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "user",  cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Phone> phones = new ArrayList<>();

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Candidate candidate;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Employer employer;

    public void addPhone(Phone phone) {
        this.phones.add(phone);
        phone.setUser(this);
    }

    public void removePhone(Phone phone) {
        this.phones.remove(phone);
        phone.setUser(null);
    }

}
