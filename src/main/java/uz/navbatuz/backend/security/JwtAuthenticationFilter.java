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

    private final JwtService jwtService;
    private final UserRepository userRepository; // inject repo directly

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        // Try UUID subject first
        uz.navbatuz.backend.user.model.User domainUser = null;
        try {
            var sub = jwtService.extractSubject(token); // UUID or legacy email
            try {
                var id = java.util.UUID.fromString(sub);
                domainUser = userRepository.findById(id).orElse(null);
            } catch (IllegalArgumentException ignored) {
                // legacy token with email in sub
                domainUser = userRepository.findByEmail(sub).orElse(null);
            }
        } catch (Exception e) {
            chain.doFilter(request, response);
            return;
        }

        if (domainUser != null && SecurityContextHolder.getContext().getAuthentication() == null && !jwtService.isExpired(token)) {
            var authority = new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + domainUser.getRole().name());
            var auth = new UsernamePasswordAuthenticationToken(domainUser, null, java.util.List.of(authority));
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }
}

