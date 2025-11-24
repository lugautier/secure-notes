package com.securenotes.controller;

import com.securenotes.config.JwtConfig;
import com.securenotes.domain.User;
import com.securenotes.dto.LoginRequest;
import com.securenotes.dto.LoginResponse;
import com.securenotes.dto.ProfileResponse;
import com.securenotes.dto.RegisterRequest;
import com.securenotes.dto.RegisterResponse;
import com.securenotes.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final UserService userService;
  private final JwtConfig jwtConfig;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    User user = userService.registerUser(request.email(), request.password());

    RegisterResponse response =
        new RegisterResponse(user.getId(), user.getEmail(), "User registered successfully");

    log.info("Registration successful for email: {}", request.email());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    try {
      String token = userService.generateLoginToken(request.email(), request.password());

      LoginResponse response = new LoginResponse(token, jwtConfig.getExpiration());

      log.info("Login successful for email: {}", request.email());
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      log.warn("Login failed for email: {} - {}", request.email(), e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/profile")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ProfileResponse> getProfile() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userId = (String) authentication.getPrincipal();
    UUID userUuid = UUID.fromString(userId);

    User user = userService.getUserProfile(userUuid);
    var roles = userService.getUserRoles(userUuid);

    ProfileResponse response =
        new ProfileResponse(
            user.getId(), user.getEmail(), roles, user.getCreatedAt(), user.getUpdatedAt());

    log.info("Profile retrieved for user: {}", user.getEmail());
    return ResponseEntity.ok(response);
  }
}
