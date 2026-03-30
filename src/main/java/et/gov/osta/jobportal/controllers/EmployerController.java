package et.gov.osta.jobportal.controllers;

import et.gov.osta.jobportal.dtos.requests.CreateEmployerRequestDTO;
import et.gov.osta.jobportal.dtos.requests.UpdateEmployerRequestDTO;
import et.gov.osta.jobportal.dtos.responses.EmployerResponseDTO;
import et.gov.osta.jobportal.services.EmployerService;
import et.gov.osta.jobportal.utils.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("api/employers")
@Validated
@RequiredArgsConstructor
public class EmployerController {
    private final EmployerService employerService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Returned Hello!");
    }

    @PostMapping
    public ResponseEntity<Long> create(@RequestBody CreateEmployerRequestDTO employerRequestDTO) {
        Long newEmployerId = employerService.create(employerRequestDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newEmployerId)
                .toUri();

        return ResponseEntity.created(location).body(newEmployerId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PagedResponse<EmployerResponseDTO>> getAll(
            @RequestParam(required = false) String companyName,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean sortAsc
    ) {
        return ResponseEntity.ok(employerService.getAll(
                companyName,
                pageNumber,
                pageSize,
                sortBy,
                sortAsc
            ));
    }

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @GetMapping("/{id}")
    public ResponseEntity<EmployerResponseDTO> getById(@PathVariable Long id) {

        return ResponseEntity.ok(employerService.getById(id));
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployerResponseDTO> update(@PathVariable Long id, @RequestBody UpdateEmployerRequestDTO updateRequest) {
        return ResponseEntity.ok(employerService.update(id, updateRequest));
    }
}
