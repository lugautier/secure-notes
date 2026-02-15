package com.securenotes.security;

import static org.junit.jupiter.api.Assertions.*;

import com.securenotes.config.JwtConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("JwtProvider Tests")
class JwtProviderTest {

  private JwtProvider jwtProvider;
  private JwtConfig jwtConfig;

  // Test RSA Keys (2048-bit)
  private static final String TEST_PRIVATE_KEY =
      """
            -----BEGIN PRIVATE KEY-----
            MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDJ3RWAYWdIC2rH
            g49q+EhJPOgI1e/nd6l+wYL9b90JWVndxCFj/6X42R//PeNgGO01jZuPwQg+cNsr
            mVo31z9ojujMq7i5wrTTFdO5q4isyrh83H9787L1zg0xBqf0qi3QRv0DBETdW5rH
            NYqjj8aipdwpQF4GDndQl1M1mUByQpTJ7pOIoUyXLnxbn/rn2ENOItn6vpTLAgd7
            8oPYMSy9SKdk12108Msxa8pU/ATzY4fZhIALwSnjs0o9PKfzrtQfzNycb0J1B2zH
            yvUF1tkSWzuMrM4M1CXJdrCSKGSoHU4SXWELGeLa+wnNgNPCcXp+yIbtoRKBNg0F
            ha8bMxllAgMBAAECggEAIXif59Nex36jJDVfirZj8AbiF5L3rkr0ZxDpqiHKBhKT
            eNbcTYM2j0JbUJx/ru+7J2HVXTr95bKbWMmbAL7XY/wsREGmBeEvz/9ixbrYVQRh
            Hk0Gc3RXZHQX0lz+7O3p3krjCYTD8WtOyQDK/e9pWY9EZ++lF35/ELUixjSkz70I
            lIE0LVwTDn2wH5i9Ws/eSaL9TCdkjBM6lkIIdCk6j+UKjygrOv8QhtndFakCvFCJ
            03TtuAb5Y7T0oFsoFWYCWjmYsCrHLujyErSOhOw2nNPqA5FPDBRiUS22tyT7+iK0
            8HHaSJpy7bph3tfKBXid0122IlithGD0ip1sznmnqQKBgQDrsarR3fZuUi1yi/sa
            fVy2R3RJ3tJh8yiRVO15gAk44j2ADNxZ1QI6yshY5pKkKlavaq+fcfoqrBW5NvBk
            Iq2Pao0VXvLlhu71r2fSWy72MbaeGXLAgeoAEZjGD8rAraE7pGWN5HLMhqiCx0t8
            rliNgu3hBh57ojzlM69MFwdGfQKBgQDbQUXLv3+opNUonk+LYDlEj67yggTb9KwU
            NDujIDgMnL9kVLfuzi/p5/IQWotE03TW4NK9v12ZltdLLeR1Ik7DQzMeK1RFSPrM
            0dPilUt/FZzfwTtYjGH746wj6XIIeXhjp4H7V5bTU3MEg1DXb0CPpaLu4s8OZ0l3
            jOhAZmdLCQKBgQDY1MaV9GG19Jwi+Wy1XgdhGjN9kiRyQEVeDoe6c3QIhPqXRz2g
            1zoJ5GyUfOsDZIADOV8AjNbdUxtZHZXiSZTqj9fjhUpops5H8GrPN1vo2qtqn3bW
            a65fCdFGxVh+Ej52pDNZaoXCa0+zoK1tsud8qKs3jW2VyBfFtNrcYYMr8QKBgHXG
            hjuATo7EnEwJXik8MwcFN7DE7t9IevcPZ8mkkPcVbCn06Ci7UTmQgpMOUClUfTq/
            4fRTS3ApetTDfij9mNmCy361P7tIDJDhVbQtBjTp4y6+maZjIm8wSVOxHrQ2q9i8
            LjJZRoeWF/6gm1heRovjKbaw1xChovE5G7kcSPghAoGAB2Gt/ORdl/gDXp3ek7yG
            RmRVB5Hd28gy2LmWKfnmBS8V+HsmgG8GbB8fYtmbRnv6P1AdDkrEU4J6R3QRjm54
            miROEv7l1Wlp0x+4EmAkKsmgNE1aG0sLJ5Jjw1oFzDh49EAyitZnahbTBxnul3Cx
            FUknKzvCc7B9/aKTsrdgqPM=
            -----END PRIVATE KEY-----
            """;

  private static final String TEST_PUBLIC_KEY =
      """
            -----BEGIN PUBLIC KEY-----
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyd0VgGFnSAtqx4OPavhI
            STzoCNXv53epfsGC/W/dCVlZ3cQhY/+l+Nkf/z3jYBjtNY2bj8EIPnDbK5laN9c/
            aI7ozKu4ucK00xXTuauIrMq4fNx/e/Oy9c4NMQan9Kot0Eb9AwRE3VuaxzWKo4/G
            oqXcKUBeBg53UJdTNZlAckKUye6TiKFMly58W5/659hDTiLZ+r6UywIHe/KD2DEs
            vUinZNdtdPDLMWvKVPwE82OH2YSAC8Ep47NKPTyn867UH8zcnG9CdQdsx8r1BdbZ
            Els7jKzODNQlyXawkihkqB1OEl1hCxni2vsJzYDTwnF6fsiG7aESgTYNBYWvGzMZ
            ZQIDAQAB
            -----END PUBLIC KEY-----
            """;

  @BeforeEach
  void setUp() {
    jwtConfig = new JwtConfig();
    ReflectionTestUtils.setField(jwtConfig, "privateKeyString", TEST_PRIVATE_KEY);
    ReflectionTestUtils.setField(jwtConfig, "publicKeyString", TEST_PUBLIC_KEY);
    ReflectionTestUtils.setField(jwtConfig, "expiration", 3600L); // 1 hour

    jwtProvider = new JwtProvider(jwtConfig);
  }

  @Test
  @DisplayName("Should generate valid JWT token with userId and email")
  void testGenerateToken_Success() {
    UUID userId = UUID.randomUUID();
    String email = "john@example.com";

    String token = jwtProvider.generateToken(userId, email);

    assertNotNull(token);
    assertFalse(token.isEmpty());
    // Token should have 3 parts separated by dots: header.payload.signature
    assertEquals(3, token.split("\\.").length);
  }

  @Test
  @DisplayName("Should validate a valid JWT token")
  void testValidateToken_ValidToken_Success() {
    UUID userId = UUID.randomUUID();
    String email = "john@example.com";
    String token = jwtProvider.generateToken(userId, email);

    boolean isValid = jwtProvider.validateToken(token);

    assertTrue(isValid);
  }

  @Test
  @DisplayName("Should reject invalid/malformed JWT token")
  void testValidateToken_MalformedToken_Failure() {
    String malformedToken = "invalid.malformed.token";

    boolean isValid = jwtProvider.validateToken(malformedToken);

    assertFalse(isValid);
  }

  @Test
  @DisplayName("Should reject token with invalid signature")
  void testValidateToken_InvalidSignature_Failure() {
    UUID userId = UUID.randomUUID();
    String email = "john@example.com";
    String validToken = jwtProvider.generateToken(userId, email);

    // Tamper with the signature part
    String[] parts = validToken.split("\\.");
    String tamperedToken = parts[0] + "." + parts[1] + ".INVALID_SIGNATURE";

    boolean isValid = jwtProvider.validateToken(tamperedToken);

    assertFalse(isValid);
  }

  @Test
  @DisplayName("Should reject expired JWT token")
  void testValidateToken_ExpiredToken_Failure() {
    UUID userId = UUID.randomUUID();
    String email = "john@example.com";

    // Create an expired token (expiration in the past)
    String expiredToken =
        Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
            .expiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
            .signWith(jwtConfig.getPrivateKey(), SignatureAlgorithm.RS256)
            .compact();

    boolean isValid = jwtProvider.validateToken(expiredToken);

    assertFalse(isValid);
  }

  @Test
  @DisplayName("Should extract userId from valid token")
  void testGetUserIdFromToken_Success() {
    UUID userId = UUID.randomUUID();
    String email = "john@example.com";
    String token = jwtProvider.generateToken(userId, email);

    String extractedUserId = jwtProvider.getUserIdFromToken(token);

    assertEquals(userId.toString(), extractedUserId);
  }

  @Test
  @DisplayName("Should extract email from valid token")
  void testGetEmailFromToken_Success() {
    UUID userId = UUID.randomUUID();
    String email = "john@example.com";
    String token = jwtProvider.generateToken(userId, email);

    String extractedEmail = jwtProvider.getEmailFromToken(token);

    assertEquals(email, extractedEmail);
  }

  @Test
  @DisplayName("Should throw exception when extracting userId from invalid token")
  void testGetUserIdFromToken_InvalidToken_Failure() {
    String invalidToken = "invalid.token.signature";

    assertThrows(Exception.class, () -> jwtProvider.getUserIdFromToken(invalidToken));
  }

  @Test
  @DisplayName("Should throw exception when extracting email from invalid token")
  void testGetEmailFromToken_InvalidToken_Failure() {
    String invalidToken = "invalid.token.signature";

    assertThrows(Exception.class, () -> jwtProvider.getEmailFromToken(invalidToken));
  }

  @Test
  @DisplayName("Should create different tokens for different users")
  void testGenerateToken_DifferentUsers_DifferentTokens() {
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();
    String email1 = "user1@example.com";
    String email2 = "user2@example.com";

    String token1 = jwtProvider.generateToken(userId1, email1);
    String token2 = jwtProvider.generateToken(userId2, email2);

    assertNotEquals(token1, token2);
    assertEquals(userId1.toString(), jwtProvider.getUserIdFromToken(token1));
    assertEquals(userId2.toString(), jwtProvider.getUserIdFromToken(token2));
  }

  @Test
  @DisplayName("Should reject token with empty string")
  void testValidateToken_EmptyToken_Failure() {
    boolean isValid = jwtProvider.validateToken("");

    assertFalse(isValid);
  }

  @Test
  @DisplayName("Should reject token with null value")
  void testValidateToken_NullToken_Failure() {
    boolean isValid = jwtProvider.validateToken(null);

    assertFalse(isValid);
  }
}
