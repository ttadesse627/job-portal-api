package et.gov.osta.jobportal.services;

import et.gov.osta.jobportal.domain.entities.AppUser;
import et.gov.osta.jobportal.domain.entities.Phone;
import et.gov.osta.jobportal.domain.enums.Role;
import et.gov.osta.jobportal.domain.repositories.UserRepository;
import et.gov.osta.jobportal.dtos.requests.CreateUserRequestDTO;
import et.gov.osta.jobportal.dtos.responses.UserResponseDTO;
import et.gov.osta.jobportal.exceptions.BadRequestException;
import et.gov.osta.jobportal.helpers.CustomUserDetails;
import et.gov.osta.jobportal.utils.PagedResponse;
import et.gov.osta.jobportal.utils.PaginationUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Primary
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUser save(CreateUserRequestDTO userRequestDTO, Role role)
    {
        if (userExists(userRequestDTO.email())){
            throw new BadRequestException("Email already exists.");
        }
        AppUser user = new AppUser();
        user.setEmail(userRequestDTO.email());
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(userRequestDTO.password()));

        if (userRequestDTO.phoneNumbers() != null){
            user.getPhones().clear();
            userRequestDTO.phoneNumbers().forEach(
                    num -> user.addPhone(new Phone(null, num, null)));
        }

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

    public PagedResponse<UserResponseDTO> getAll(
            String email,
            int pageNumber,
            int pageSize,
            String sortBy,
            boolean sortAsc
    ){
        Sort sort = sortAsc
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<AppUser> page = (email != null && !email.isBlank())?
                userRepository.findByEmailContainingIgnoreCase(email, pageable)
                :userRepository.findAll(pageable);

        return PaginationUtils.toPagedResponse(page, this::mapToResponse);
    }

    public UserResponseDTO update(Long id, Set<String> phoneNumbers){
        AppUser user = userRepository.findById((id)).orElseThrow(
                ()-> new RuntimeException("Employer with this id does not exist."));

        user.getPhones().clear();
        phoneNumbers.forEach(
                num -> user.addPhone(new Phone(null, num, null)));

        return mapToResponse(userRepository.save(user));
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String email)
            throws UsernameNotFoundException {

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return new CustomUserDetails(user);
    }

    public boolean userExists(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    public UserResponseDTO mapToResponse(AppUser user)
    {

        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getPhones() != null ?
                        user.getPhones().stream()
                                .map(Phone::getNumber)
                                .collect(Collectors.toSet()) :
                        new HashSet<>(),
                Objects.requireNonNull(user.getRole()).name()
        );
    }
}
