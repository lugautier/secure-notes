package com.securenotes.service;

import com.securenotes.domain.Role;
import com.securenotes.domain.User;
import com.securenotes.domain.UserRole;
import com.securenotes.repository.UserRepository;
import com.securenotes.repository.UserRoleRepository;
import com.securenotes.security.JwtProvider;
import java.security.SecureRandom;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private static final int SALT_LENGTH = 16;

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;

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

  private String generateSalt() {
    byte[] salt = new byte[SALT_LENGTH];
    new SecureRandom().nextBytes(salt);
    return Base64.getEncoder().encodeToString(salt);
  }
}
