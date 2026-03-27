package et.gov.osta.jobportal.services;

import et.gov.osta.jobportal.domain.entities.AppUser;
import et.gov.osta.jobportal.domain.enums.Role;
import et.gov.osta.jobportal.domain.repositories.UserRepository;
import et.gov.osta.jobportal.dtos.requests.CreateUserRequestDTO;
import et.gov.osta.jobportal.dtos.responses.UserResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO create(CreateUserRequestDTO userRequestDTO, Role role)
    {
        AppUser user = new AppUser();
        user.setEmail(userRequestDTO.email());
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(userRequestDTO.password()));

        return mapToResponse(userRepository.save(user));
    }

    public UserResponseDTO mapToResponse(AppUser user)
    {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                Objects.requireNonNull(user.getRole()).name()
        );
    }
}
