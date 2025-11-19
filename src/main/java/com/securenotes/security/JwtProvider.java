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

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

  private final JwtConfig jwtConfig;

  public String generateToken(UUID userId, String email) {
    // Current: Single 24h access token (defined in application.yml)
    // TODO: Replace with 15min access token + 7day refresh token pattern + proper logout
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

  private Claims getClaims(String token) {
    return Jwts.parser()
        .verifyWith(jwtConfig.getPublicKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
