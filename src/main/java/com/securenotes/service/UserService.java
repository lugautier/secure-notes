package com.securenotes.service;

import com.securenotes.domain.Role;
import com.securenotes.domain.User;
import com.securenotes.domain.UserRole;
import com.securenotes.repository.UserRepository;
import com.securenotes.repository.UserRoleRepository;
import com.securenotes.security.JwtProvider;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User management service for registration, authentication, and profile management. Handles
 * password hashing with bcrypt + salt, JWT token generation, and user roles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private static final int SALT_LENGTH = 16;

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;

  /**
   * Registers a new user with email and password. Generates random salt, hashes password with
   * bcrypt + salt for additional security. Automatically assigns USER role to new users.
   * Transactional: Both User and UserRole saved together (atomicity).
   *
   * @param email user's email (must be unique)
   * @param password plaintext password (hashed before storage)
   * @return created User entity
   * @throws IllegalArgumentException if email already registered
   */
  @Transactional
  public User registerUser(String email, String password) {
    if (userRepository.findByEmail(email).isPresent()) {
      throw new IllegalArgumentException("Email already registered");
    }

    String salt = generateSalt();
    String passwordHash = passwordEncoder.encode(password + salt);

    User user = User.builder().email(email).passwordHash(passwordHash).salt(salt).build();

    user = userRepository.save(user);

    UserRole userRole = UserRole.builder().user(user).role(Role.USER).build();

    userRoleRepository.save(userRole);

    log.info("User registered successfully: {}", email);
    return user;
  }

  /**
   * Authenticates user by email/password and returns JWT token. Verifies password against bcrypt
   * hash (with salt) stored in database. Returns JWT token with 24-hour expiration for subsequent
   * requests. Generic error message prevents email enumeration attacks.
   *
   * @param email user's email address
   * @param password plaintext password (checked against hash)
   * @return JWT token for authenticated user
   * @throws IllegalArgumentException if email not found or password incorrect
   */
  public String generateLoginToken(String email, String password) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

    if (!passwordEncoder.matches(password + user.getSalt(), user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid email or password");
    }

    String token = jwtProvider.generateToken(user.getId(), user.getEmail());
    log.info("User authenticated successfully: {}", email);
    return token;
  }

  public User getUserProfile(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  public Set<String> getUserRoles(UUID userId) {
    return userRoleRepository.findByUserId(userId).stream()
        .map(userRole -> userRole.getRole().toString())
        .collect(Collectors.toSet());
  }

  /**
   * Generates cryptographically secure random salt for password hashing. Salt prevents rainbow
   * table attacks and ensures identical passwords produce different hashes. Salt stored with user
   * (not secret) - security comes from randomness + bcrypt.
   *
   * @return base64-encoded random salt (16 bytes)
   */
  private String generateSalt() {
    byte[] salt = new byte[SALT_LENGTH];
    new SecureRandom().nextBytes(salt);
    return Base64.getEncoder().encodeToString(salt);
  }
}
