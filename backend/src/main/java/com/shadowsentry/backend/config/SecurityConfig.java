package com.shadowsentry.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.shadowsentry.backend.repository.UserRepository;
import com.shadowsentry.backend.security.JwtAuthenticationFilter;
import com.shadowsentry.backend.security.JwtService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return usernameOrEmail -> {
            var userOpt = usernameOrEmail.contains("@")
                    ? userRepository.findByEmail(usernameOrEmail)
                    : userRepository.findByUsername(usernameOrEmail);

            var user = userOpt.orElseThrow(() ->
                    new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found: " + usernameOrEmail));

            String[] roles = (user.getRoles() != null && !user.getRoles().isEmpty())
                    ? user.getRoles().toArray(String[]::new)
                    : new String[]{"ROLE_USER"};

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPasswordHash())
                    .authorities(roles)
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, UserDetailsService uds) {
        return new JwtAuthenticationFilter(jwtService, uds);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {}) // enable CORS (CorsConfigurationSource bean will be used)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // allow auth endpoints and incidents (risk) to be called without JWT
                .requestMatchers("/api/v1/auth/**", "/api/v1/incidents/**", "/hello", "/ping").permitAll()
                // protect user endpoints
                .requestMatchers("/api/v1/user/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
