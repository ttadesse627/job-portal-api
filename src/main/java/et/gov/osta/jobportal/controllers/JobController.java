package et.gov.osta.jobportal.controllers;

import et.gov.osta.jobportal.domain.enums.JobType;
import et.gov.osta.jobportal.dtos.requests.JobRequestDTO;
import et.gov.osta.jobportal.dtos.requests.JobResponseDTO;
import et.gov.osta.jobportal.helpers.CustomUserDetails;
import et.gov.osta.jobportal.services.JobService;
import et.gov.osta.jobportal.utils.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/jobs")
@Validated
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobResponseDTO> create(
            @RequestBody JobRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(jobService.create(dto, user.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobResponseDTO> update(
            @PathVariable Long id,
            @RequestBody JobRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(jobService.update(id, dto, user.getId()));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('EMPLOYER')")
    public void publish(@PathVariable Long id,
                        @AuthenticationPrincipal CustomUserDetails user) {
        jobService.publish(id, user.getId());
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('EMPLOYER')")
    public void close(@PathVariable Long id,
                      @AuthenticationPrincipal CustomUserDetails user) {
        jobService.close(id, user.getId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal CustomUserDetails user) {
        jobService.delete(id, user.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponseDTO> viewDetails(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.view(id));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<JobResponseDTO>> getJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean sortAsc
    ) {
        return ResponseEntity.ok(
                jobService.getJobs(
                        title,
                        position,
                        location,
                        jobType,
                        startDate,
                        endDate,
                        pageNumber,
                        pageSize,
                        sortBy,
                        sortAsc
                )
        );
    }
}
