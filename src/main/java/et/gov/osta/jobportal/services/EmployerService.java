package et.gov.osta.jobportal.services;

import et.gov.osta.jobportal.domain.entities.Employer;
import et.gov.osta.jobportal.domain.enums.Role;
import et.gov.osta.jobportal.domain.repositories.EmployerRepository;
import et.gov.osta.jobportal.dtos.requests.CreateEmployerRequestDTO;
import et.gov.osta.jobportal.dtos.requests.UpdateEmployerRequestDTO;
import et.gov.osta.jobportal.dtos.responses.EmployerResponseDTO;
import et.gov.osta.jobportal.exceptions.ResourceNotFoundException;
import et.gov.osta.jobportal.utils.PagedResponse;
import et.gov.osta.jobportal.utils.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Transactional
@Service
public class EmployerService {

    private final EmployerRepository employerRepository;
    private final UserService userService;

    public Long create(CreateEmployerRequestDTO employerRequestDTO){

        Employer employer = new Employer();
        employer.setCompanyName(employerRequestDTO.companyName());
        var savedUser = userService.save(employerRequestDTO.user(), Role.EMPLOYER);
        employer.setUser(savedUser);

        return employerRepository.saveAndFlush(employer).getId();
    }

    public EmployerResponseDTO getById(Long id){

        return mapToResponse(employerRepository.findById((id)).orElseThrow(
                ()-> new ResourceNotFoundException("Employer with id " + id + " not found")));
    }

    public PagedResponse<EmployerResponseDTO> getAll(
            String companyName,
            int pageNumber,
            int pageSize,
            String sortBy,
            boolean sortAsc
    ){
        Sort sort = sortAsc
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Employer> page = (companyName != null && !companyName.isBlank())?
                employerRepository.findByCompanyNameContainingIgnoreCase(companyName, pageable)
                :employerRepository.findAll(pageable);

        return PaginationUtils.toPagedResponse(page, this::mapToResponse);
    }

    public EmployerResponseDTO update(Long id, UpdateEmployerRequestDTO updateRequestDTO){
        Employer employerEntity = employerRepository.findById((id)).orElseThrow(
                ()-> new ResourceNotFoundException("Employer with id " + id + " not found"));

        employerEntity.setCompanyName(updateRequestDTO.companyName());
        userService.update(employerEntity.getUser().getId(), updateRequestDTO.phoneNumbers());

        return mapToResponse(employerRepository.save(employerEntity));
    }


    public EmployerResponseDTO mapToResponse(Employer employer){
        return new EmployerResponseDTO(
                employer.getId(),
                employer.getCompanyName(),
                employer.getUser().getEmail()
            );
    }

}
