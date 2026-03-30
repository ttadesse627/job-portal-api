package et.gov.osta.jobportal.data;

import et.gov.osta.jobportal.domain.enums.Role;
import et.gov.osta.jobportal.dtos.requests.CreateUserRequestDTO;
import et.gov.osta.jobportal.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner commandLineRunner(UserService userService){
        return (args -> {

            CreateUserRequestDTO request = new CreateUserRequestDTO(
                    null,
                    "tola@gmail.com",
                    "tola@123"
            );

            if (!userService.userExists(request.email())){
                userService.create(request, Role.ADMIN);
            }
        });
    }
}
