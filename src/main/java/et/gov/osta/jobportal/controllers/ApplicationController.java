package et.gov.osta.jobportal.controllers;

import et.gov.osta.jobportal.domain.enums.ApplicationStatus;
import et.gov.osta.jobportal.dtos.requests.ApplyRequestDTO;
import et.gov.osta.jobportal.dtos.responses.ApplicationResponseDTO;
import et.gov.osta.jobportal.helpers.CustomUserDetails;
import et.gov.osta.jobportal.services.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@Validated
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/jobs/{jobId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApplicationResponseDTO> apply(
            @PathVariable Long jobId,
            @RequestBody ApplyRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(
                applicationService.apply(jobId, user.getId(), dto)
        );
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Page<ApplicationResponseDTO>> myApplications(
            @AuthenticationPrincipal CustomUserDetails user,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                applicationService.getMyApplications(user.getId(), pageable)
        );
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public ResponseEntity<Page<ApplicationResponseDTO>> getApplicants(
            @PathVariable Long jobId,
            @AuthenticationPrincipal CustomUserDetails user,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                applicationService.getApplicantsForJob(jobId, user.getId(), user.getRole(), pageable)
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public void updateStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        applicationService.updateStatus(id, status, user.getId(), user.getRole());
    }

    @PatchMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('CANDIDATE')")
    public void withdraw(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        applicationService.withdraw(id, user.getId());
    }
}
