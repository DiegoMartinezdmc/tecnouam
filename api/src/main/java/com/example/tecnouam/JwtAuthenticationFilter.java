package com.example.tecnouam;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserDetailsService users;

    public JwtAuthenticationFilter(JwtService jwt, UserDetailsService users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String username = jwt.verifyAndGetSubject(authorization.substring(7));
            if (username != null) {
                try {
                    UserDetails user = users.loadUserByUsername(username);
                    SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
                } catch (UsernameNotFoundException ignored) {
                    // Un usuario eliminado deja de poder usar tokens anteriores.
                }
            }
        }
        chain.doFilter(request, response);
    }
}
