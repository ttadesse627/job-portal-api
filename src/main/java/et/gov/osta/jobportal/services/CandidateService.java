package et.gov.osta.jobportal.services;

import et.gov.osta.jobportal.domain.entities.Candidate;
import et.gov.osta.jobportal.domain.entities.Resume;
import et.gov.osta.jobportal.domain.enums.Role;
import et.gov.osta.jobportal.domain.repositories.CandidateRepository;
import et.gov.osta.jobportal.dtos.requests.CreateCandidateRequestDTO;
import et.gov.osta.jobportal.dtos.responses.CandidateResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    public CandidateResponseDTO create(CreateCandidateRequestDTO candidateRequestDTO, MultipartFile resumeFile){
        Candidate candidate = new Candidate();

        candidate.setFirstName(candidateRequestDTO.firstName());
        candidate.setLastName(candidateRequestDTO.lastName());

        var savedUser = userService.save(candidateRequestDTO.user(), Role.CANDIDATE);

        candidate.setUser(savedUser);

        var savedCandidate = candidateRepository.saveAndFlush(candidate);
        Path resumePath = null;

        try {
            String resumeUrl = saveResume(resumeFile, savedCandidate.getId());
            resumePath = Paths.get(resumeUrl);

            Resume resume = new Resume();
            resume.setFileUrl(resumeUrl);
            savedCandidate.setResume(resume);

            var persistedCandidate = candidateRepository.saveAndFlush(savedCandidate);
            return mapToResponse(persistedCandidate);
        } catch (RuntimeException runtimeException) {
            deleteResumeIfExists(resumePath);
            throw runtimeException;
        }
    }

    public String saveResume(MultipartFile resumeFile, Long candidateId){
        if (resumeFile == null || resumeFile.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required.");
        }

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(resumeFile.getOriginalFilename()))+"_"+candidateId;
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


    public CandidateResponseDTO mapToResponse(Candidate candidate){
        return new CandidateResponseDTO(
                candidate.getId(),
                candidate.getFirstName(),
                candidate.getLastName(),
                candidate.getUser().getEmail(),
                candidate.getResume() != null ? candidate.getResume().getFileUrl() : null
            );
    }

}
