package uz.navbatuz.backend.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uz.navbatuz.backend.user.model.User;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String SECRET = "12345678901234567890123456789012"; // >=32 chars
    private static final long EXPIRATION = 1000L * 60 * 60 * 24 * 30; // 30 days

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(User user) {
        try {
            String token = Jwts.builder()
                    .setSubject(user.getId().toString())
                    .claim("email", user.getEmail())
                    .claim("role", user.getRole().name())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                    .signWith(getKey(), SignatureAlgorithm.HS256)
                    .compact();

            log.debug("Generated JWT token for user: {} ({})", user.getEmail(), user.getId());
            return token;
        } catch (Exception e) {
            log.error("Error generating JWT token for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    public String extractSubject(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String subject = claims.getSubject();
            log.debug("Extracted subject from JWT: {}", subject);
            return subject;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw new RuntimeException("JWT token is expired", e);
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            throw new RuntimeException("JWT token is malformed", e);
        } catch (SignatureException e) {
            log.warn("JWT signature is invalid: {}", e.getMessage());
            throw new RuntimeException("JWT signature is invalid", e);
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            throw new RuntimeException("JWT token is unsupported", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            throw new RuntimeException("JWT claims string is empty", e);
        } catch (Exception e) {
            log.error("Unexpected error extracting subject from JWT: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract subject from JWT", e);
        }
    }

    public String extractEmail(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("email", String.class);
        } catch (Exception e) {
            log.error("Error extracting email from JWT: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract email from JWT", e);
        }
    }

    public String extractRole(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("role", String.class);
        } catch (Exception e) {
            log.error("Error extracting role from JWT: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract role from JWT", e);
        }
    }

    public boolean isExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            boolean expired = expiration.before(new Date());

            if (expired) {
                log.debug("JWT token is expired. Expiration: {}, Current: {}", expiration, new Date());
            }

            return expired;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired (caught ExpiredJwtException)");
            return true;
        } catch (Exception e) {
            log.warn("Error checking JWT expiration, treating as expired: {}", e.getMessage());
            return true; // If we can't validate, treat as expired for security
        }
    }

    // Additional method to validate token completely
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token validation failed - expired");
            return false;
        } catch (Exception e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }
}