package com.securenotes.security;

import com.securenotes.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JWT token provider for RS256 (asymmetric) token generation and validation. Generates signed JWT
 * tokens for successful authentication and validates tokens on each request. Uses RSA private key
 * for signing (server-only) and public key for verification.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

  private final JwtConfig jwtConfig;

  /**
   * Generates a signed JWT token for authenticated user. Token includes userId (subject) and email
   * claim. Current implementation: Single 24-hour access token (stateless). TODO: Implement refresh
   * token pattern (15min access + 7day refresh) + proper logout.
   *
   * @param userId user's unique identifier
   * @param email user's email address
   * @return signed JWT token
   */
  public String generateToken(UUID userId, String email) {
    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plusSeconds(jwtConfig.getExpiration());

    return Jwts.builder()
        .subject(userId.toString())
        .claim("email", email)
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(expiresAt))
        .signWith(jwtConfig.getPrivateKey(), SignatureAlgorithm.RS256)
        .compact();
  }

  public String getUserIdFromToken(String token) {
    return getClaims(token).getSubject();
  }

  public String getEmailFromToken(String token) {
    return (String) getClaims(token).get("email");
  }

  /**
   * Validates JWT token signature and expiration. Catches all JWT exceptions (expired, malformed,
   * invalid signature, etc.) and returns false. Does NOT throw exceptions - returns validation
   * result for graceful handling.
   *
   * @param token JWT token to validate
   * @return true if token is valid, false otherwise
   */
  public boolean validateToken(String token) {
    try {
      getClaims(token);
      return true;
    } catch (ExpiredJwtException e) {
      log.debug("JWT token expired", e);
      return false;
    } catch (UnsupportedJwtException e) {
      log.debug("JWT token unsupported", e);
      return false;
    } catch (MalformedJwtException e) {
      log.debug("Invalid JWT token", e);
      return false;
    } catch (SignatureException e) {
      log.debug("JWT signature validation failed", e);
      return false;
    } catch (IllegalArgumentException e) {
      log.debug("JWT claims string is empty", e);
      return false;
    }
  }

  /**
   * Parses and verifies JWT token signature using public key. Validates token structure, signature,
   * and expiration. Throws JwtException if token is invalid (expired, malformed, bad signature).
   *
   * @param token JWT token
   * @return verified claims payload
   */
  private Claims getClaims(String token) {
    return Jwts.parser()
        .verifyWith(jwtConfig.getPublicKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
