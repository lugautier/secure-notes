package com.securenotes.config;

import com.securenotes.security.JwtAuthenticationFilter;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security configuration for stateless JWT authentication. Disables sessions (stateless with
 * JWT), enables CORS, and defines public/protected endpoints. JWT tokens validated per-request via
 * JwtAuthenticationFilter.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  /**
   * Configures BCrypt password encoder with default strength (10 rounds). Passwords are salted
   * during registration for additional security.
   *
   * @return BCrypt password encoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Configures Spring Security filter chain for JWT-based stateless authentication. - CORS: Allows
   * requests from localhost:3000/4200 - CSRF: Disabled (stateless JWT, no session cookies) -
   * Sessions: STATELESS (no server-side session storage) - Public endpoints: /auth/register,
   * /auth/login, /actuator/health, /swagger-ui.html, /v3/api-docs - Protected endpoints: All other
   * endpoints require JWT Bearer token - JWT Filter: Validates token before
   * UsernamePasswordAuthenticationFilter - Security headers: X-Frame-Options: DENY (prevents
   * clickjacking)
   *
   * @param http HttpSecurity builder
   * @return configured SecurityFilterChain
   * @throws Exception if configuration fails
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exception ->
                exception.authenticationEntryPoint(
                    (request, response, e) -> {
                      response.setStatus(401);
                      response.setContentType("application/json");
                      response
                          .getWriter()
                          .write(
                              "{\"error\": \"Unauthorized\", \"message\": \"Authentication required\"}");
                    }))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/auth/register", "/auth/login", "/error")
                    .permitAll()
                    .requestMatchers(
                        "/actuator/health", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny));

    return http.build();
  }

  /**
   * Configures CORS (Cross-Origin Resource Sharing) for frontend integration. Allows requests from
   * localhost development servers with credentials (JWT tokens).
   *
   * @return CORS configuration source
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        Arrays.asList("http://localhost:3000", "http://localhost:4200"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
