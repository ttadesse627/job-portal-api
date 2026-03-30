package et.gov.osta.jobportal.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain documentationSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher(
                        "/error",
                        "/scalar",
                        "/scalar/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/webjars/**" )
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());

        return httpSecurity.build(); }
    @Order(2)
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .authenticationProvider(authenticationProvider)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/employers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/employers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/employers/*").hasAnyRole("ADMIN", "EMPLOYER")
                        .requestMatchers(HttpMethod.POST, "/api/candidates").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/candidates").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/jobs").hasRole("EMPLOYER")
                        .requestMatchers(HttpMethod.GET, "/api/jobs").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/jobs/*").permitAll()
                        .requestMatchers("/api/users").hasRole("ADMIN")
                        .requestMatchers("/api/users/me").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    @Order(2)
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean()
    @Order(2)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
