package com.shadowsentry.backend.controller;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shadowsentry.backend.auth.dto.LoginRequest;
import com.shadowsentry.backend.auth.dto.RegisterRequest;
import com.shadowsentry.backend.auth.dto.TokenRefreshRequest;
import com.shadowsentry.backend.model.User;
import com.shadowsentry.backend.repository.UserRepository;
import com.shadowsentry.backend.security.JwtService;
import com.shadowsentry.backend.service.TokenService;
import com.shadowsentry.backend.service.TokenService.RotateResult;
import com.shadowsentry.backend.service.TokenService.TokenException;
import com.shadowsentry.backend.watchlist.WatchlistService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * ✅ AuthController — handles registration, login, token refresh, logout, and /me
 * Works with TokenService and JwtService for full JWT + refresh flow.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WatchlistService watchlistService;
    private final TokenService tokenService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          WatchlistService watchlistService,
                          TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.watchlistService = watchlistService;
        this.tokenService = tokenService;
    }

    // ---------------------------------------------------------------
    // ✅ REGISTER
    // ---------------------------------------------------------------
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            if (req.getPassword() == null || req.getPassword().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
            }

            if (req.getConfirmPassword() == null || req.getConfirmPassword().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Confirm password is required"));
            }

            if (!req.getPassword().equals(req.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Passwords do not match"));
            }

            if (userRepository.existsByUsername(req.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "username_taken"));
            }

            if (userRepository.existsByEmail(req.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "email_taken"));
            }

            User user = new User();
            user.setUsername(req.getUsername());
            user.setEmail(req.getEmail());
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
            user.setRoles(Set.of("ROLE_USER"));
            user.setCreatedAt(new Date());

            User saved = userRepository.save(user);

            try {
                watchlistService.addItem(saved.getId(), "email", saved.getEmail());
            } catch (Exception e) {
                log.warn("⚠️ User created but watchlist add failed: {}", e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id", saved.getId(),
                    "username", saved.getUsername(),
                    "email", saved.getEmail(),
                    "roles", saved.getRoles(),
                    "message", "Registration successful"
            ));
        } catch (Exception e) {
            log.error("❌ Error during registration: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Registration failed"));
        }
    }

    // ---------------------------------------------------------------
    // ✅ LOGIN
    // ---------------------------------------------------------------
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req,
                                   HttpServletRequest servletRequest,
                                   HttpServletResponse servletResponse) {
        try {
            final String identifier;
            if (req.getUsernameOrEmail() != null && !req.getUsernameOrEmail().isBlank()) {
                identifier = req.getUsernameOrEmail();
            } else if (req.getEmail() != null && !req.getEmail().isBlank()) {
                identifier = req.getEmail();
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "username_or_email_required"));
            }

            // Manual authentication (since AuthenticationManager sometimes fails in stateless mode)
            var user = userRepository.findByUsername(identifier)
                    .or(() -> userRepository.findByEmail(identifier))
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
                throw new BadCredentialsException("Invalid credentials");
            }

            user.setLastLogin(new Date());
            userRepository.save(user);

            var pair = tokenService.generateTokenPair(
                    user,
                    servletRequest.getRemoteAddr(),
                    servletRequest.getHeader("User-Agent")
            );

            ResponseCookie refreshCookie = ResponseCookie.from(jwtService.getRefreshCookieName(), pair.refreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(jwtService.getRefreshTokenExpiryDays() * 24L * 60 * 60)
                    .sameSite("Lax")
                    .build();

            servletResponse.addHeader("Set-Cookie", refreshCookie.toString());
            servletResponse.setHeader("Access-Control-Expose-Headers", "Set-Cookie");

            return ResponseEntity.ok(Map.of(
                    "accessToken", pair.accessToken,
                    "refreshToken", pair.refreshToken,
                    "expiresIn", jwtService.getAccessTokenExpiryMs(),
                    "user", Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "email", user.getEmail(),
                            "roles", user.getRoles()
                    )
            ));

        } catch (BadCredentialsException ex) {
            log.warn("⚠️ Invalid login attempt: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid_credentials"));
        } catch (Exception e) {
            log.error("❌ Error during login: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Login failed"));
        }
    }

    // ---------------------------------------------------------------
    // ✅ REFRESH TOKEN
    // ---------------------------------------------------------------
    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> refresh(@Valid @RequestBody(required = false) TokenRefreshRequest request,
                                     @CookieValue(value = "refresh_token", required = false) String cookieRefresh,
                                     HttpServletResponse servletResponse) {
        try {
            String incoming = null;
            if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
                incoming = request.getRefreshToken();
            } else if (cookieRefresh != null && !cookieRefresh.isBlank()) {
                incoming = cookieRefresh;
            }

            if (incoming == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "refresh_token_required"));
            }

            RotateResult result = tokenService.rotateRefreshToken(incoming);

            ResponseCookie refreshCookie = ResponseCookie.from(jwtService.getRefreshCookieName(), result.newRefreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(jwtService.getRefreshTokenExpiryDays() * 24L * 60 * 60)
                    .sameSite("Lax")
                    .build();

            servletResponse.addHeader("Set-Cookie", refreshCookie.toString());
            servletResponse.setHeader("Access-Control-Expose-Headers", "Set-Cookie");

            return ResponseEntity.ok(Map.of(
                    "accessToken", result.accessToken,
                    "refreshToken", result.newRefreshToken,
                    "expiresIn", jwtService.getAccessTokenExpiryMs()
            ));

        } catch (TokenException te) {
            log.warn("⚠️ Token rotation failed: {}", te.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", te.getMessage()));
        } catch (Exception ex) {
            log.error("❌ Error during refresh: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid_refresh_token"));
        }
    }

    // ---------------------------------------------------------------
    // ✅ LOGOUT
    // ---------------------------------------------------------------
    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> logout(@RequestBody(required = false) TokenRefreshRequest request,
                                    @CookieValue(value = "refresh_token", required = false) String cookieRefresh,
                                    HttpServletResponse servletResponse) {

        String tokenToRevoke = null;
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            tokenToRevoke = request.getRefreshToken();
        } else if (cookieRefresh != null && !cookieRefresh.isBlank()) {
            tokenToRevoke = cookieRefresh;
        }

        if (tokenToRevoke != null) {
            tokenService.revokeRefreshToken(tokenToRevoke);
        }

        ResponseCookie deleteCookie = ResponseCookie.from(jwtService.getRefreshCookieName(), "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        servletResponse.addHeader("Set-Cookie", deleteCookie.toString());
        return ResponseEntity.ok(Map.of("ok", true, "message", "Logged out successfully"));
    }

    // ---------------------------------------------------------------
    // ✅ ME
    // ---------------------------------------------------------------
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> me(@RequestHeader(name = "Authorization", required = false) String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "missing_token"));
            }

            String token = authorization.substring(7);
            String userId = jwtService.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "roles", user.getRoles()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid_token"));
        }
    }
}
