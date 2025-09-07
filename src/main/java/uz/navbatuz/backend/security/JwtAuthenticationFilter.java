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

    private static final org.springframework.util.AntPathMatcher ANT = new org.springframework.util.AntPathMatcher();
    private static final java.util.List<String> PUBLIC = java.util.List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/public/**",
            "/api/health", "/health",
            "/api/providers/public/**",
            "/api/services/public/**",
            "/api/workers/free-slots/**",
            "/uploads/**"
    );

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
                || path.equals("/error");   // important!
    }



    @Override
    protected void doFilterInternal(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            jakarta.servlet.FilterChain chain) throws jakarta.servlet.ServletException, java.io.IOException {

        final String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        uz.navbatuz.backend.user.model.User domainUser = null;
        try {
            var sub = jwtService.extractSubject(token);
            try {
                var id = java.util.UUID.fromString(sub);
                domainUser = userRepository.findById(id).orElse(null);
            } catch (IllegalArgumentException ignored) {
                domainUser = userRepository.findByEmail(sub).orElse(null);
            }
        } catch (Exception ignored) {
            chain.doFilter(request, response);
            return;
        }

        if (domainUser != null
                && SecurityContextHolder.getContext().getAuthentication() == null
                && !jwtService.isExpired(token)) {
            var authority = new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_" + domainUser.getRole().name());
            var auth = new UsernamePasswordAuthenticationToken(domainUser, null, java.util.List.of(authority));
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }
}

