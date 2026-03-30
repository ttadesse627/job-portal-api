package et.gov.osta.jobportal.controllers;

import et.gov.osta.jobportal.dtos.responses.UserResponseDTO;
import et.gov.osta.jobportal.helpers.CustomUserDetails;
import et.gov.osta.jobportal.services.UserService;
import et.gov.osta.jobportal.utils.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PagedResponse<UserResponseDTO>> getAll(
            @RequestParam(required = false) String companyName,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean sortAsc
    ) {
        return ResponseEntity.ok(userService.getAll(
                companyName,
                pageNumber,
                pageSize,
                sortBy,
                sortAsc
            ));
    }

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Long id) {

        return ResponseEntity.ok(userService.getById(id));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public String getCurrentUser(@AuthenticationPrincipal CustomUserDetails user) {
        return "User ID: " + user.getId();
    }
}
