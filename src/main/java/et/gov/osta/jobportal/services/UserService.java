package et.gov.osta.jobportal.services;

import et.gov.osta.jobportal.domain.entities.AppUser;
import et.gov.osta.jobportal.domain.enums.Role;
import et.gov.osta.jobportal.domain.repositories.UserRepository;
import et.gov.osta.jobportal.dtos.requests.CreateUserRequestDTO;
import et.gov.osta.jobportal.dtos.responses.UserResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public AppUser save(CreateUserRequestDTO userRequestDTO, Role role)
    {
        AppUser user = new AppUser();
        user.setEmail(userRequestDTO.email());
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(userRequestDTO.password()));

        return userRepository.save(user);
    }

    public UserResponseDTO create(CreateUserRequestDTO userRequestDTO, Role role)
    {
        return mapToResponse(save(userRequestDTO, role));
    }

    public UserResponseDTO getById(Long id)
    {
        return userRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("The user with id "+id+"doesn't exist!"));
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("The user with email "+email+" doesn't exist!"));
        return User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
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
