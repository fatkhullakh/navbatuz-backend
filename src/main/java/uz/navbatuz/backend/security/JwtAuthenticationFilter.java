package uz.navbatuz.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.navbatuz.backend.auth.service.JwtService;
import uz.navbatuz.backend.user.repository.UserRepository;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/forgot-password")
                || path.equals("/api/auth/reset-password")
                || path.startsWith("/api/auth/public/")
                || path.equals("/api/health")
                || path.equals("/health")
                || path.startsWith("/api/providers/public/")
                || path.startsWith("/api/services/public/")
                || path.startsWith("/api/workers/free-slots/")
                || path.startsWith("/uploads/")
                || path.startsWith("/public/")
                || path.startsWith("/p/")
                || path.startsWith("/s/")
                || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        System.out.println("=== JWT FILTER CALLED FOR: " + request.getRequestURI() + " ===");
        System.out.println("Authorization header: " + request.getHeader("Authorization"));

        final String path = request.getRequestURI();
        final String header = request.getHeader("Authorization");

        // Debug logging
        log.debug("Processing request to: {}", path);
        log.debug("Authorization header present: {}", header != null);

        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("No valid Authorization header found");
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        log.debug("Extracted JWT token (first 20 chars): {}",
                token.length() > 20 ? token.substring(0, 20) + "..." : token);

        uz.navbatuz.backend.user.model.User domainUser = null;

        try {
            // First check if token is expired before processing
            if (jwtService.isExpired(token)) {
                log.warn("JWT token is expired");
                chain.doFilter(request, response);
                return;
            }

            var sub = jwtService.extractSubject(token);
            log.debug("Extracted subject from JWT: {}", sub);

            if (sub == null || sub.isBlank()) {
                log.warn("JWT subject is null or empty");
                chain.doFilter(request, response);
                return;
            }

            try {
                var id = java.util.UUID.fromString(sub);
                log.debug("Subject is UUID, looking up user by ID: {}", id);
                domainUser = userRepository.findById(id).orElse(null);
            } catch (IllegalArgumentException e) {
                log.debug("Subject is not UUID, looking up user by email: {}", sub);
                domainUser = userRepository.findByEmail(sub).orElse(null);
            }

            if (domainUser == null) {
                log.warn("User not found for subject: {}", sub);
                chain.doFilter(request, response);
                return;
            }

            log.debug("Found user: {} ({})", domainUser.getEmail(), domainUser.getId());

        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage(), e);
            chain.doFilter(request, response);
            return;
        }

        // Check if already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("User already authenticated, skipping");
            chain.doFilter(request, response);
            return;
        }

        try {
            var authority = new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_" + domainUser.getRole().name());
            var auth = new UsernamePasswordAuthenticationToken(domainUser, null, java.util.List.of(authority));
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.debug("Successfully authenticated user: {} with role: {}",
                    domainUser.getEmail(), domainUser.getRole().name());

        } catch (Exception e) {
            log.error("Error setting authentication context: {}", e.getMessage(), e);
            chain.doFilter(request, response);
            return;
        }

        chain.doFilter(request, response);
    }
}