package et.gov.osta.jobportal.services;

import et.gov.osta.jobportal.domain.entities.AppUser;
import et.gov.osta.jobportal.domain.entities.Candidate;
import et.gov.osta.jobportal.domain.enums.Role;
import et.gov.osta.jobportal.dtos.requests.CreateCandidateRequestDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

    @Mock
    private et.gov.osta.jobportal.domain.repositories.CandidateRepository candidateRepository;

    @Mock
    private UserService userService;

    @Mock
    private ResumeParsingService resumeParsingService;

    @InjectMocks
    private CandidateService candidateService;

    @AfterEach
    void cleanUpUploadedResume() throws Exception {
        Files.deleteIfExists(Path.of("./uploads/resume/resume.pdf_5"));
    }

    @Test
    void createStoresParsedResumeFieldsWhenParserReturnsData() {
        MockMultipartFile resumeFile = new MockMultipartFile(
                "resume",
                "resume.pdf",
                "application/pdf",
                "fake pdf bytes".getBytes()
        );
        CreateCandidateRequestDTO request = new CreateCandidateRequestDTO(
                "Abel",
                "Kassa",
                Set.of("+251911223344"),
                "abel.kassa@example.com",
                "Abel@123",
                resumeFile
        );

        when(userService.save(any(), eq(Role.CANDIDATE))).thenReturn(user(15L));
        when(candidateRepository.saveAndFlush(any(Candidate.class))).thenAnswer(invocation -> {
            Candidate candidate = invocation.getArgument(0);
            if (candidate.getId() == null) {
                candidate.setId(5L);
            }
            return candidate;
        });
        when(resumeParsingService.parse(any(Path.class))).thenReturn(
                ResumeParsingResult.success(
                        "Affinda",
                        "Abel Kassa",
                        "abel.kassa@example.com",
                        "+251911223344",
                        "Java, Spring Boot"
                )
        );

        Long candidateId = candidateService.create(request, resumeFile);

        assertEquals(5L, candidateId);

        ArgumentCaptor<Candidate> captor = ArgumentCaptor.forClass(Candidate.class);
        verify(candidateRepository, times(2)).saveAndFlush(captor.capture());
        Candidate savedCandidate = captor.getAllValues().get(1);

        assertNotNull(savedCandidate.getResume());
        assertEquals("SUCCESS", savedCandidate.getResume().getParseStatus().name());
        assertEquals("Abel Kassa", savedCandidate.getResume().getParsedFullName());
        assertEquals("abel.kassa@example.com", savedCandidate.getResume().getParsedEmail());
        assertEquals("+251911223344", savedCandidate.getResume().getParsedPhone());
        assertEquals("Java, Spring Boot", savedCandidate.getResume().getParsedSkills());
    }

    private AppUser user(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail("abel.kassa@example.com");
        return user;
    }
}
