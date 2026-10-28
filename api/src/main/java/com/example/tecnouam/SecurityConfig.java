package com.example.tecnouam;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService users(PasswordEncoder encoder,
            @Value("${ADMIN_PASSWORD}") String adminPassword,
            @Value("${USER_PASSWORD}") String userPassword) {
        return new InMemoryUserDetailsManager(
            User.withUsername("admin").password(encoder.encode(adminPassword)).roles("ADMIN").build(),
            User.withUsername("user").password(encoder.encode(userPassword)).roles("USER").build());
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, JwtService jwt, UserDetailsService users) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, exception) -> response.setStatus(401))
                .accessDeniedHandler((request, response, exception) -> response.setStatus(403)))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/equipos", "/api/equipos/*", "/api/prestamos").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/**").hasRole("ADMIN")
                .anyRequest().denyAll())
            .addFilterBefore(new JwtAuthenticationFilter(jwt, users), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
