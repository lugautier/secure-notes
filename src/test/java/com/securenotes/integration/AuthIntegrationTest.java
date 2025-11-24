package com.securenotes.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.securenotes.domain.User;
import com.securenotes.dto.LoginRequest;
import com.securenotes.dto.LoginResponse;
import com.securenotes.dto.ProfileResponse;
import com.securenotes.dto.RegisterRequest;
import com.securenotes.dto.RegisterResponse;
import com.securenotes.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class AuthIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("securenotes_test")
          .withUsername("testuser")
          .withPassword("testpass");

  @DynamicPropertySource
  static void configureDatabase(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);

    // Test JWT keys (from JwtProviderTest)
    String testPrivateKey =
        "-----BEGIN PRIVATE KEY-----\n"
            + "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDJ3RWAYWdIC2rH\n"
            + "g49q+EhJPOgI1e/nd6l+wYL9b90JWVndxCFj/6X42R//PeNgGO01jZuPwQg+cNsr\n"
            + "mVo31z9ojujMq7i5wrTTFdO5q4isyrh83H9787L1zg0xBqf0qi3QRv0DBETdW5rH\n"
            + "NYqjj8aipdwpQF4GDndQl1M1mUByQpTJ7pOIoUyXLnxbn/rn2ENOItn6vpTLAgd7\n"
            + "8oPYMSy9SKdk12108Msxa8pU/ATzY4fZhIALwSnjs0o9PKfzrtQfzNycb0J1B2zH\n"
            + "yvUF1tkSWzuMrM4M1CXJdrCSKGSoHU4SXWELGeLa+wnNgNPCcXp+yIbtoRKBNg0F\n"
            + "ha8bMxllAgMBAAECggEAIXif59Nex36jJDVfirZj8AbiF5L3rkr0ZxDpqiHKBhKT\n"
            + "eNbcTYM2j0JbUJx/ru+7J2HVXTr95bKbWMmbAL7XY/wsREGmBeEvz/9ixbrYVQRh\n"
            + "Hk0Gc3RXZHQX0lz+7O3p3krjCYTD8WtOyQDK/e9pWY9EZ++lF35/ELUixjSkz70I\n"
            + "lIE0LVwTDn2wH5i9Ws/eSaL9TCdkjBM6lkIIdCk6j+UKjygrOv8QhtndFakCvFCJ\n"
            + "03TtuAb5Y7T0oFsoFWYCWjmYsCrHLujyErSOhOw2nNPqA5FPDBRiUS22tyT7+iK0\n"
            + "8HHaSJpy7bph3tfKBXid0122IlithGD0ip1sznmnqQKBgQDrsarR3fZuUi1yi/sa\n"
            + "fVy2R3RJ3tJh8yiRVO15gAk44j2ADNxZ1QI6yshY5pKkKlavaq+fcfoqrBW5NvBk\n"
            + "Iq2Pao0VXvLlhu71r2fSWy72MbaeGXLAgeoAEZjGD8rAraE7pGWN5HLMhqiCx0t8\n"
            + "rliNgu3hBh57ojzlM69MFwdGfQKBgQDbQUXLv3+opNUonk+LYDlEj67yggTb9KwU\n"
            + "NDujIDgMnL9kVLfuzi/p5/IQWotE03TW4NK9v12ZltdLLeR1Ik7DQzMeK1RFSPrM\n"
            + "0dPilUt/FZzfwTtYjGH746wj6XIIeXhjp4H7V5bTU3MEg1DXb0CPpaLu4s8OZ0l3\n"
            + "jOhAZmdLCQKBgQDY1MaV9GG19Jwi+Wy1XgdhGjN9kiRyQEVeDoe6c3QIhPqXRz2g\n"
            + "1zoJ5GyUfOsDZIADOV8AjNbdUxtZHZXiSZTqj9fjhUpops5H8GrPN1vo2qtqn3bW\n"
            + "a65fCdFGxVh+Ej52pDNZaoXCa0+zoK1tsud8qKs3jW2VyBfFtNrcYYMr8QKBgHXG\n"
            + "hjuATo7EnEwJXik8MwcFN7DE7t9IevcPZ8mkkPcVbCn06Ci7UTmQgpMOUClUfTq/\n"
            + "4fRTS3ApetTDfij9mNmCy361P7tIDJDhVbQtBjTp4y6+maZjIm8wSVOxHrQ2q9i8\n"
            + "LjJZRoeWF/6gm1heRovjKbaw1xChovE5G7kcSPghAoGAB2Gt/ORdl/gDXp3ek7yG\n"
            + "RmRVB5Hd28gy2LmWKfnmBS8V+HsmgG8GbB8fYtmbRnv6P1AdDkrEU4J6R3QRjm54\n"
            + "miROEv7l1Wlp0x+4EmAkKsmgNE1aG0sLJ5Jjw1oFzDh49EAyitZnahbTBxnul3Cx\n"
            + "FUknKzvCc7B9/aKTsrdgqPM=\n"
            + "-----END PRIVATE KEY-----";

    String testPublicKey =
        "-----BEGIN PUBLIC KEY-----\n"
            + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyd0VgGFnSAtqx4OPavhI\n"
            + "STzoCNXv53epfsGC/W/dCVlZ3cQhY/+l+Nkf/z3jYBjtNY2bj8EIPnDbK5laN9c/\n"
            + "aI7ozKu4ucK00xXTuauIrMq4fNx/e/Oy9c4NMQan9Kot0Eb9AwRE3VuaxzWKo4/G\n"
            + "oqXcKUBeBg53UJdTNZlAckKUye6TiKFMly58W5/659hDTiLZ+r6UywIHe/KD2DEs\n"
            + "vUinZNdtdPDLMWvKVPwE82OH2YSAC8Ep47NKPTyn867UH8zcnG9CdQdsx8r1BdbZ\n"
            + "Els7jKzODNQlyXawkihkqB1OEl1hCxni2vsJzYDTwnF6fsiG7aESgTYNBYWvGzMZ\n"
            + "ZQIDAQAB\n"
            + "-----END PUBLIC KEY-----";

    registry.add("jwt.private-key", () -> testPrivateKey);
    registry.add("jwt.public-key", () -> testPublicKey);
    registry.add("jwt.expiration", () -> "86400");
  }

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  void testCompleteAuthFlow_RegisterLoginGetProfile() {
    String email = "alice@example.com";
    String password = "AlicePassword123!";

    // Step 1: Register user
    RegisterRequest registerRequest = new RegisterRequest(email, password);
    ResponseEntity<RegisterResponse> registerResponse =
        restTemplate.postForEntity("/auth/register", registerRequest, RegisterResponse.class);

    // Verify registration succeeded
    assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(registerResponse.getBody()).isNotNull();
    assertThat(registerResponse.getBody().email()).isEqualTo(email);
    String userId = registerResponse.getBody().userId().toString();

    // Verify user saved to database with hashed password
    Optional<User> savedUser = userRepository.findByEmail(email);
    assertThat(savedUser).isPresent();
    assertThat(savedUser.get().getEmail()).isEqualTo(email);
    assertThat(savedUser.get().getPasswordHash()).isNotEqualTo(password); // Hashed, not plaintext
    assertThat(
            passwordEncoder.matches(
                password + savedUser.get().getSalt(), savedUser.get().getPasswordHash()))
        .isTrue(); // Can verify password

    // Step 2: Login with correct password
    LoginRequest loginRequest = new LoginRequest(email, password);
    ResponseEntity<LoginResponse> loginResponse =
        restTemplate.postForEntity("/auth/login", loginRequest, LoginResponse.class);

    // Verify login succeeded
    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(loginResponse.getBody()).isNotNull();
    String jwtToken = loginResponse.getBody().token();
    assertThat(jwtToken).isNotNull().isNotEmpty();

    // Step 3: Use token to access protected endpoint
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(jwtToken);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<ProfileResponse> profileResponse =
        restTemplate.exchange(
            "/auth/profile", HttpMethod.GET, requestEntity, ProfileResponse.class);

    // Verify profile access succeeded
    assertThat(profileResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(profileResponse.getBody()).isNotNull();
    assertThat(profileResponse.getBody().email()).isEqualTo(email);
    assertThat(profileResponse.getBody().roles()).contains("USER");
  }

  @Test
  void testLoginWithWrongPassword_Fails() {
    String email = "bob@example.com";
    String correctPassword = "BobPassword123!";
    String wrongPassword = "WrongPassword456!";

    // Register user
    RegisterRequest registerRequest = new RegisterRequest(email, correctPassword);
    restTemplate.postForEntity("/auth/register", registerRequest, RegisterResponse.class);

    // Try to login with wrong password
    LoginRequest loginRequest = new LoginRequest(email, wrongPassword);
    ResponseEntity<LoginResponse> loginResponse =
        restTemplate.postForEntity("/auth/login", loginRequest, LoginResponse.class);

    // Verify login failed
    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void testAccessProtectedEndpointWithoutToken_Unauthorized() {
    // Try to access protected endpoint without token
    ResponseEntity<ProfileResponse> response =
        restTemplate.getForEntity("/auth/profile", ProfileResponse.class);

    // Verify access denied
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void testAccessProtectedEndpointWithInvalidToken_Unauthorized() {
    String invalidToken = "invalid.jwt.token";

    // Try to access protected endpoint with invalid token
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(invalidToken);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<ProfileResponse> response =
        restTemplate.exchange(
            "/auth/profile", HttpMethod.GET, requestEntity, ProfileResponse.class);

    // Verify access denied
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
