package com.securenotes.controller;

import com.securenotes.config.JwtConfig;
import com.securenotes.domain.User;
import com.securenotes.dto.LoginRequest;
import com.securenotes.dto.LoginResponse;
import com.securenotes.dto.RegisterRequest;
import com.securenotes.dto.RegisterResponse;
import com.securenotes.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
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
    String token = userService.generateLoginToken(request.email(), request.password());

    LoginResponse response = new LoginResponse(token, jwtConfig.getExpiration());

    log.info("Login successful for email: {}", request.email());
    return ResponseEntity.ok(response);
  }
}
