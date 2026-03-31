package et.gov.osta.jobportal.services;

import et.gov.osta.jobportal.domain.entities.Candidate;
import et.gov.osta.jobportal.domain.entities.Resume;
import et.gov.osta.jobportal.domain.enums.Role;
import et.gov.osta.jobportal.domain.repositories.CandidateRepository;
import et.gov.osta.jobportal.dtos.requests.CreateCandidateRequestDTO;
import et.gov.osta.jobportal.dtos.requests.CreateUserRequestDTO;
import et.gov.osta.jobportal.dtos.requests.UpdateCandidateRequestDTO;
import et.gov.osta.jobportal.dtos.responses.CandidateResponseDTO;
import et.gov.osta.jobportal.exceptions.BadRequestException;
import et.gov.osta.jobportal.utils.PagedResponse;
import et.gov.osta.jobportal.utils.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@RequiredArgsConstructor
@Transactional
@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final UserService userService;
    private final ResumeParsingService resumeParsingService;

    public Long create(CreateCandidateRequestDTO candidateRequestDTO, MultipartFile resumeFile){
        if (resumeFile == null || resumeFile.isEmpty()) {
            throw new BadRequestException("Resume file is required!");
        }
        Candidate candidate = new Candidate();

        candidate.setFirstName(candidateRequestDTO.firstName());
        candidate.setLastName(candidateRequestDTO.lastName());

        CreateUserRequestDTO userRequestDTO = new CreateUserRequestDTO(
                candidateRequestDTO.phoneNumbers(),
                candidateRequestDTO.email(),
                candidateRequestDTO.password()
            );

        var savedUser = userService.save(userRequestDTO, Role.CANDIDATE);

        candidate.setUser(savedUser);

        var savedCandidate = candidateRepository.saveAndFlush(candidate);
        Path resumePath = null;

        try {
            String resumeUrl = saveResume(resumeFile, savedCandidate.getId());
            resumePath = Paths.get(resumeUrl);

            Resume resume = new Resume();
            resume.setFileUrl(resumeUrl);
            applyResumeParsing(resume, resumePath);
            savedCandidate.setResume(resume);

            var persistedCandidate = candidateRepository.saveAndFlush(savedCandidate);
            return persistedCandidate.getId();
        } catch (RuntimeException runtimeException) {
            deleteResumeIfExists(resumePath);
            throw runtimeException;
        }
    }

    public PagedResponse<CandidateResponseDTO> getAll(
            String firstName,
            int pageNumber,
            int pageSize,
            String sortBy,
            boolean sortAsc
    ){
        Sort sort = sortAsc
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Candidate> page = (firstName != null && !firstName.isBlank())?
                candidateRepository.findByFirstNameContainingIgnoreCase(firstName, pageable)
                :candidateRepository.findAll(pageable);

        return PaginationUtils.toPagedResponse(page, this::mapToResponse);
    }

    public CandidateResponseDTO getById(Long id){


        return mapToResponse(candidateRepository.findById((id)).orElseThrow(
                ()-> new RuntimeException("Candidate with this id does not exist.")));
    }

    public CandidateResponseDTO update(Long id, UpdateCandidateRequestDTO updateRequestDTO){
        Candidate candidateEntity = candidateRepository.findById((id)).orElseThrow(
                ()-> new RuntimeException("Employer with this id does not exist."));

        candidateEntity.setFirstName(updateRequestDTO.firstName());
        userService.update(candidateEntity.getUser().getId(), updateRequestDTO.phoneNumbers());

        return mapToResponse(candidateRepository.save(candidateEntity));
    }


    public String saveResume(MultipartFile resumeFile, Long candidateId){
        if (resumeFile == null || resumeFile.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required.");
        }

        String fileName = candidateId+"_"+StringUtils.cleanPath(Objects.requireNonNull(resumeFile.getOriginalFilename()));
        String uploadDirectory = "./uploads/resume/";
        Path path = Paths.get(uploadDirectory, fileName);

        try {
            Files.createDirectories(path.getParent());
            resumeFile.transferTo(path);
        } catch (IOException ioException){
            throw new RuntimeException("Could not save the resume file.", ioException);
        }
        return path.toString();
    }

    private void deleteResumeIfExists(Path resumePath) {
        if (resumePath == null) {
            return;
        }

        try {
            Files.deleteIfExists(resumePath);
        } catch (IOException exception) {
           throw  new RuntimeException("Could not delete the resume file.", exception);
        }
    }

    private void applyResumeParsing(Resume resume, Path resumePath) {
        ResumeParsingResult parsingResult = resumeParsingService.parse(resumePath);
        resume.setParseStatus(parsingResult.status());
        resume.setParserProvider(parsingResult.provider());
        resume.setParsedFullName(parsingResult.fullName());
        resume.setParsedEmail(parsingResult.email());
        resume.setParsedPhone(parsingResult.phone());
        resume.setParsedSkills(parsingResult.skills());
        resume.setParseError(parsingResult.error());
    }

    public CandidateResponseDTO mapToResponse(Candidate candidate){
        return new CandidateResponseDTO(
                candidate.getId(),
                candidate.getFirstName(),
                candidate.getLastName(),
                candidate.getUser().getEmail(),
                candidate.getResume() != null ? candidate.getResume().getFileUrl() : null,
                candidate.getResume() != null && candidate.getResume().getParseStatus() != null
                        ? candidate.getResume().getParseStatus().name()
                        : null,
                candidate.getResume() != null ? candidate.getResume().getParsedFullName() : null,
                candidate.getResume() != null ? candidate.getResume().getParsedEmail() : null,
                candidate.getResume() != null ? candidate.getResume().getParsedPhone() : null,
                candidate.getResume() != null ? candidate.getResume().getParsedSkills() : null
            );
    }
}
