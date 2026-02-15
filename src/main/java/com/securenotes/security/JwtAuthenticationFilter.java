package com.securenotes.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT authentication filter that validates Bearer tokens on every request. Extracts JWT from
 * Authorization header, validates signature/expiration, and sets Spring Security context for
 * authenticated requests. Runs once per request (OncePerRequestFilter) before other authentication
 * mechanisms.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtProvider jwtProvider;

  /**
   * Intercepts each request, validates JWT token if present, and authenticates user. If valid token
   * found: Sets Spring Security authentication context with userId and email. If invalid or
   * missing: Request proceeds unauthenticated (SecurityConfig defines which endpoints require
   * auth).
   *
   * @param request HTTP request
   * @param response HTTP response
   * @param filterChain filter chain
   * @throws ServletException if servlet processing fails
   * @throws IOException if I/O error occurs
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = extractTokenFromRequest(request);

    if (StringUtils.hasText(token)) {
      if (jwtProvider.validateToken(token)) {
        String userId = jwtProvider.getUserIdFromToken(token);
        String email = jwtProvider.getEmailFromToken(token);

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("JWT Token validated for user: {}", email);
      } else {
        log.debug("Invalid JWT token");
      }
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Extracts JWT token from "Authorization: Bearer <token>" header. Expected format: "Bearer
   * <token>" (case-sensitive).
   *
   * @param request HTTP request
   * @return JWT token or null if not present or malformed
   */
  private String extractTokenFromRequest(HttpServletRequest request) {
    String authHeader = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
      return authHeader.substring(BEARER_PREFIX.length());
    }
    return null;
  }
}
