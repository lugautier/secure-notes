package com.securenotes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.securenotes.domain.Role;
import com.securenotes.domain.User;
import com.securenotes.domain.UserRole;
import com.securenotes.repository.UserRepository;
import com.securenotes.repository.UserRoleRepository;
import com.securenotes.security.JwtProvider;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class UserServiceTest {

  @Autowired private UserService userService;

  @Autowired private PasswordEncoder passwordEncoder;

  @MockitoBean private UserRepository userRepository;

  @MockitoBean private UserRoleRepository userRoleRepository;

  @MockitoBean private JwtProvider jwtProvider;

  @Test
  void testRegisterUser_ValidRequest_HashesPasswordWithSalt() {
    String email = "user@example.com";
    String password = "ValidPassword123!";
    UUID userId = UUID.randomUUID();

    User savedUser =
        User.builder().id(userId).email(email).passwordHash("hashed").salt("salt").build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(userRoleRepository.save(any())).thenReturn(null);

    User result = userService.registerUser(email, password);

    // Verify password is NOT plaintext (is hashed)
    verify(userRepository)
        .save(
            argThat(
                user ->
                    !user.getPasswordHash().equals(password)
                        && user.getPasswordHash().length() > 20
                        && user.getSalt() != null
                        && !user.getSalt().isEmpty()));

    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo(email);
  }

  @Test
  void testRegisterUser_DuplicateEmail_ThrowsException() {
    String email = "user@example.com";
    String password = "ValidPassword123!";

    User existingUser =
        User.builder().id(UUID.randomUUID()).email(email).passwordHash("hash").salt("salt").build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    assertThatThrownBy(() -> userService.registerUser(email, password))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email already registered");

    // Verify save was NEVER called (exception thrown before saving)
    verify(userRepository, never()).save(any());
  }

  @Test
  void testRegisterUser_AssignsUserRole() {
    String email = "user@example.com";
    String password = "ValidPassword123!";
    UUID userId = UUID.randomUUID();

    User savedUser =
        User.builder().id(userId).email(email).passwordHash("hashed").salt("salt").build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(userRoleRepository.save(any())).thenReturn(null);

    userService.registerUser(email, password);

    // Verify UserRole was created with Role.USER
    verify(userRoleRepository)
        .save(
            argThat(
                ur ->
                    ur.getRole() == Role.USER
                        && ur.getUser() != null
                        && ur.getUser().getId().equals(userId)));
  }

  @Test
  void testGenerateLoginToken_CorrectPassword_ReturnsToken() {
    String email = "user@example.com";
    String password = "ValidPassword123!";
    UUID userId = UUID.randomUUID();

    String salt = "randomSalt";
    String encodedPassword = passwordEncoder.encode(password + salt);

    User user =
        User.builder().id(userId).email(email).passwordHash(encodedPassword).salt(salt).build();

    String jwtToken = "valid.jwt.token";

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(jwtProvider.generateToken(userId, email)).thenReturn(jwtToken);

    String result = userService.generateLoginToken(email, password);

    // Verify token was generated with correct user ID and email
    verify(jwtProvider).generateToken(userId, email);
    assertThat(result).isEqualTo(jwtToken);
  }

  @Test
  void testGenerateLoginToken_WrongPassword_ThrowsException() {
    String email = "user@example.com";
    String correctPassword = "ValidPassword123!";
    String wrongPassword = "WrongPassword456!";
    UUID userId = UUID.randomUUID();

    String salt = "randomSalt";
    String encodedPassword = passwordEncoder.encode(correctPassword + salt);

    User user =
        User.builder().id(userId).email(email).passwordHash(encodedPassword).salt(salt).build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> userService.generateLoginToken(email, wrongPassword))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid email or password");

    // Verify token generation was NEVER called (exception thrown before generating token)
    verify(jwtProvider, never()).generateToken(any(), any());
  }

  @Test
  void testGenerateLoginToken_NonExistentEmail_ThrowsException() {
    String email = "nonexistent@example.com";
    String password = "ValidPassword123!";

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.generateLoginToken(email, password))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid email or password");

    // Verify token generation was NEVER called
    verify(jwtProvider, never()).generateToken(any(), any());
  }

  @Test
  void testGetUserProfile_ValidUserId_ReturnsUser() {
    UUID userId = UUID.randomUUID();
    User user =
        User.builder()
            .id(userId)
            .email("user@example.com")
            .passwordHash("hash")
            .salt("salt")
            .build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    User result = userService.getUserProfile(userId);

    assertThat(result).isEqualTo(user);
    assertThat(result.getEmail()).isEqualTo("user@example.com");
  }

  @Test
  void testGetUserProfile_NonExistentUserId_ThrowsException() {
    UUID userId = UUID.randomUUID();

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getUserProfile(userId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User not found");
  }

  @Test
  void testGetUserRoles_ReturnsUserRoles() {
    UUID userId = UUID.randomUUID();

    UserRole userRole =
        UserRole.builder()
            .id(UUID.randomUUID())
            .role(Role.USER)
            .user(
                User.builder()
                    .id(userId)
                    .email("user@example.com")
                    .passwordHash("hash")
                    .salt("salt")
                    .build())
            .build();

    when(userRoleRepository.findByUserId(userId)).thenReturn(Set.of(userRole));

    Set<String> result = userService.getUserRoles(userId);

    assertThat(result).isNotNull().hasSize(1).contains("USER");
  }

  @Test
  void testGetUserRoles_NoRoles_ReturnsEmptySet() {
    UUID userId = UUID.randomUUID();

    when(userRoleRepository.findByUserId(userId)).thenReturn(Set.of());

    Set<String> result = userService.getUserRoles(userId);

    assertThat(result).isNotNull().isEmpty();
  }
}
