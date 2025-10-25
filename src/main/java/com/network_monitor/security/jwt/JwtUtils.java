package com.network_monitor.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import com.network_monitor.security.services.UserDetailsImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtils {
  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

  @Value("${netmon.app.jwtSecret}")
  private String jwtSecret;

  @Value("${netmon.app.jwtExpirationMs}")
  private int jwtExpirationMs;

  @Value("${netmon.app.jwtCookieName}")
  private String jwtCookie;

  public String getJwtFromCookies(HttpServletRequest request) {
    Cookie cookie = WebUtils.getCookie(request, jwtCookie);
    if (cookie != null) {
      return cookie.getValue();
    } else {
      return null;
    }
  }

  public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
    String jwt = generateTokenFromUsername(userPrincipal);
    ResponseCookie cookie = ResponseCookie.from(jwtCookie, jwt)
        .path("/api")
        .maxAge(24 * 60 * 60)
        .httpOnly(true)
        .build();
    return cookie;
  }

  public ResponseCookie getCleanJwtCookie() {
    return ResponseCookie.from(jwtCookie, "")
        .path("/api")
        .maxAge(0) // cookie süresi 0 saniye = silinir
        .httpOnly(true) // JS erişimini engeller
        // .secure(true) // HTTPS ortamında önerilir
        // .sameSite("Strict") // CSRF koruması için önerilir
        .build();
  }

  /**
   * JWT 0.13.0 ile uyumlu getUserNameFromJwtToken metodu
   */
  public String getUserNameFromJwtToken(String token) {
    JwtParser parser = Jwts.parser()
        .verifyWith(getSigningKey())
        .build();

    Claims claims = parser.parseSignedClaims(token).getPayload();
    return claims.getSubject();
  }

  /**
   * SecretKey döndüren metod (JWT 0.13.0 için önerilen)
   */
  private SecretKey getSigningKey() {
    // byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
    byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * JWT 0.13.0 ile uyumlu validateJwtToken metodu
   */
  public boolean validateJwtToken(String authToken) {
    try {
      JwtParser parser = Jwts.parser()
          .verifyWith(getSigningKey())
          .build();

      parser.parseSignedClaims(authToken);
      return true;
    } catch (SignatureException e) {
      logger.error("Invalid JWT signature: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      logger.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      logger.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty: {}", e.getMessage());
    }
    return false;
  }

  /**
   * JWT 0.13.0 ile uyumlu token generation metodu
   */
  public String generateTokenFromUsername(UserDetailsImpl userPrincipal) {

    Map<String, Object> claims = new HashMap<>();
    claims.put("username", userPrincipal.getUsername());
    claims.put("email", userPrincipal.getEmail());
    claims.put("roles", userPrincipal.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList()));

    return Jwts.builder()
        .claims(claims)
        .subject(userPrincipal.getUsername())
        .issuedAt(new Date())
        .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
        .signWith(getSigningKey(), Jwts.SIG.HS256)
        .compact();
  }

  /**
   * Token'dan claims çıkarmak için yardımcı metod
   */
  public Claims getClaimsFromToken(String token) {
    JwtParser parser = Jwts.parser()
        .verifyWith(getSigningKey())
        .build();

    return parser.parseSignedClaims(token).getPayload();
  }

  /**
   * Token'ın expire olup olmadığını kontrol eden metod
   */
  public boolean isTokenExpired(String token) {
    try {
      Claims claims = getClaimsFromToken(token);
      return claims.getExpiration().before(new Date());
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Token'dan expiration date'i alan metod
   */
  public Date getExpirationDateFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.getExpiration();
  }

  /**
   * Token'dan issued date'i alan metod
   */
  public Date getIssuedDateFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.getIssuedAt();
  }

  public boolean checkAuthentication(HttpServletRequest request) {
    String token = getJwtFromCookies(request);
    if (token == null) {
      logger.warn("JWT cookie bulunamadı");
      return false;
    }

    boolean isValid = validateJwtToken(token);
    if (!isValid) {
      logger.warn("Geçersiz veya süresi dolmuş JWT token");
      return false;
    }

    // Token geçerliyse
    Claims claims = getClaimsFromToken(token);
    String username = claims.getSubject();

    if (username == null || username.isEmpty()) {
      return false;
    }

    return true;
  }
}