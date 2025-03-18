package com.restaurant.reservation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.restaurant.reservation.dto.*;
import com.restaurant.reservation.exception.TokenRefreshException;
import com.restaurant.reservation.model.RefreshToken;
import com.restaurant.reservation.model.Role;
import com.restaurant.reservation.model.User;
import com.restaurant.reservation.repository.RoleRepository;
import com.restaurant.reservation.repository.UserRepository;
import com.restaurant.reservation.security.jwt.JwtUtils;
import com.restaurant.reservation.security.services.RefreshTokenService;
import com.restaurant.reservation.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceTest authService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private User user;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest("testUser", "test@example.com", "password", "Test User", "1234567890", new HashSet<>());
        loginRequest = new LoginRequest("testUser", "password");

        user = User.builder()
                .id(1L)
                .username("testUser")
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .phoneNumber("1234567890")
                .roles(new HashSet<>())
                .build();

        userDetails = new UserDetailsImpl(1L, "testUser", "test@example.com", "encodedPassword", "Test User", "1234567890", new HashSet<>());
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(any())).thenReturn(Optional.of(new Role(1L, Role.ERole.ROLE_USER)));
        when(userRepository.save(any(User.class))).thenReturn(user);

        MessageResponse response = authService.registerUser(signupRequest);

        assertEquals("User registered successfully!", response.getMessage());
    }

    @Test
    void authenticateUser_Success() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(any())).thenReturn("jwtToken");
        when(refreshTokenService.createRefreshToken(anyLong())).thenReturn(new RefreshToken(1L, user, "refreshToken", new Date()));

        JwtResponse response = authService.authenticateUser(loginRequest);

        assertEquals("jwtToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("testUser", response.getUsername());
    }

    @Test
    void refreshToken_Success() {
        RefreshToken refreshToken = new RefreshToken(1L, user, "refreshToken", new Date());
        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(any())).thenReturn(refreshToken);
        when(jwtUtils.generateJwtToken(any())).thenReturn("newJwtToken");

        TokenRefreshResponse response = authService.refreshToken(new RefreshTokenRequest("refreshToken"));

        assertEquals("newJwtToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
    }

    @Test
    void refreshToken_Failure() {
        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        assertThrows(TokenRefreshException.class, () -> authService.refreshToken(new RefreshTokenRequest("invalidToken")));
    }

    @Test
    void logoutUser_Success() {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        MessageResponse response = authService.logoutUser();

        verify(refreshTokenService, times(1)).deleteByUserId(userDetails.getId());
        assertEquals("Log out successful!", response.getMessage());
    }

    @Test
    void getCurrentUser_Success() {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        UserDetailsResponse response = authService.getCurrentUser();

        assertEquals(userDetails.getUsername(), response.getUsername());
        assertEquals(userDetails.getEmail(), response.getEmail());
    }
}
