package et.gov.osta.jobportal.controllers;

import et.gov.osta.jobportal.dtos.requests.CreateCandidateRequestDTO;
import et.gov.osta.jobportal.dtos.requests.UpdateCandidateRequestDTO;
import et.gov.osta.jobportal.dtos.responses.CandidateResponseDTO;
import et.gov.osta.jobportal.services.CandidateService;
import et.gov.osta.jobportal.utils.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("api/candidates")
@Validated
@RequiredArgsConstructor
public class CandidateController {
    private final CandidateService candidateService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> create(
            @Valid @ModelAttribute CreateCandidateRequestDTO candidate) {
        Long newCandidateId = candidateService.create(candidate, candidate.resume());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newCandidateId)
                .toUri();

        return ResponseEntity.created(location).body(newCandidateId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PagedResponse<CandidateResponseDTO>> getAll(
            @RequestParam(required = false) String firstName,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean sortAsc
    ) {
        return ResponseEntity.ok(candidateService.getAll(
                firstName,
                pageNumber,
                pageSize,
                sortBy,
                sortAsc
        ));

    }

    @PreAuthorize("hasRole('ADMIN') or @candidateSecurity.isOwner(#id, authentication.principal.id)")
    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponseDTO> getById(@PathVariable Long id) {

        return ResponseEntity.ok(candidateService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN') or @candidateSecurity.isOwner(#id, authentication.principal.id)")
    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponseDTO> update(@PathVariable Long id, @RequestBody UpdateCandidateRequestDTO updateRequest) {
        return ResponseEntity.ok(candidateService.update(id, updateRequest));
    }
}
