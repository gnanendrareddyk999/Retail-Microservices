package com.example.user_service.service;


import com.example.user_service.dto.LoginRequest;
import com.example.user_service.dto.LoginResponse;
import com.example.user_service.dto.RegisterRequest;
import com.example.user_service.dto.UserResponse;
import com.example.user_service.entity.User;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {


    @Mock
    private UserRepository userRepository;


    @Mock
    private PasswordEncoder passwordEncoder;


    @Mock
    private JwtService jwtService;


    @InjectMocks
    private UserService userService;


    @BeforeEach
    void setUp() {

    }


    // =====================================================
    // TEST 1
    // Valid USER registration
    // =====================================================

    @Test
    void register_shouldRegisterUserSuccessfully() {

        // Arrange

        RegisterRequest request = new RegisterRequest();

        request.setUsername("gnan");
        request.setPassword("password123");
        request.setRole("USER");


        when(
                userRepository.existsByUsernameAndRole(
                        "gnan",
                        "USER"
                )
        ).thenReturn(false);


        when(
                passwordEncoder.encode("password123")
        ).thenReturn("encodedPassword");


        User savedUser = new User();

        savedUser.setId(1L);
        savedUser.setUsername("gnan");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole("USER");


        when(
                userRepository.save(any(User.class))
        ).thenReturn(savedUser);


        // Act

        UserResponse response =
                userService.register(request);


        // Assert

        assertNotNull(response);

        assertEquals(1L, response.getId());

        assertEquals(
                "gnan",
                response.getUsername()
        );

        assertEquals(
                "USER",
                response.getRole()
        );


        // Verify

        verify(
                userRepository
        ).existsByUsernameAndRole(
                "gnan",
                "USER"
        );


        verify(
                passwordEncoder
        ).encode("password123");


        verify(
                userRepository
        ).save(any(User.class)
        );
    }


    // =====================================================
    // TEST 2
    // Empty role -> USER
    // =====================================================

    @Test
    void register_shouldUseUSERWhenRoleIsEmpty() {

        // Arrange

        RegisterRequest request = new RegisterRequest();

        request.setUsername("ravi");
        request.setPassword("ravi123");
        request.setRole("");


        when(
                userRepository.existsByUsernameAndRole(
                        "ravi",
                        "USER"
                )
        ).thenReturn(false);


        when(
                passwordEncoder.encode("ravi123")
        ).thenReturn("encodedPassword");


        User savedUser = new User();

        savedUser.setId(2L);
        savedUser.setUsername("ravi");
        savedUser.setRole("USER");


        when(
                userRepository.save(any(User.class))
        ).thenReturn(savedUser);


        // Act

        UserResponse response =
                userService.register(request);


        // Assert

        assertEquals(
                "USER",
                response.getRole()
        );


        // Verify

        verify(
                userRepository
        ).existsByUsernameAndRole(
                "ravi",
                "USER"
        );
    }


    // =====================================================
    // TEST 3
    // Invalid role
    // =====================================================

    @Test
    void register_shouldThrowExceptionForInvalidRole() {

        // Arrange

        RegisterRequest request = new RegisterRequest();

        request.setUsername("testuser");
        request.setPassword("test123");
        request.setRole("ADMIN");


        // Act + Assert

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> userService.register(request)
                );


        assertEquals(
                "Invalid role. Use USER or RETAILER",
                exception.getMessage()
        );
    }


    // =====================================================
    // TEST 4
    // Duplicate username + role
    // =====================================================

    @Test
    void register_shouldThrowExceptionWhenUserAlreadyExists() {

        // Arrange

        RegisterRequest request = new RegisterRequest();

        request.setUsername("gnan");
        request.setPassword("password123");
        request.setRole("USER");


        when(
                userRepository.existsByUsernameAndRole(
                        "gnan",
                        "USER"
                )
        ).thenReturn(true);


        // Act + Assert

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> userService.register(request)
                );


        assertEquals(
                "Username already exists for this role",
                exception.getMessage()
        );
    }


    // =====================================================
    // TEST 5
    // RETAILER registration
    // =====================================================

    @Test
    void register_shouldRegisterRetailerSuccessfully() {

        // Arrange

        RegisterRequest request = new RegisterRequest();

        request.setUsername("retailer1");
        request.setPassword("retailer123");
        request.setRole("RETAILER");


        when(
                userRepository.existsByUsernameAndRole(
                        "retailer1",
                        "RETAILER"
                )
        ).thenReturn(false);


        when(
                passwordEncoder.encode("retailer123")
        ).thenReturn("encodedPassword");


        User savedUser = new User();

        savedUser.setId(10L);
        savedUser.setUsername("retailer1");
        savedUser.setRole("RETAILER");


        when(
                userRepository.save(any(User.class))
        ).thenReturn(savedUser);


        // Act

        UserResponse response =
                userService.register(request);


        // Assert

        assertEquals(
                10L,
                response.getId()
        );

        assertEquals(
                "retailer1",
                response.getUsername()
        );

        assertEquals(
                "RETAILER",
                response.getRole()
        );


        // Verify

        verify(
                userRepository
        ).save(any(User.class));
    }

    // =====================================================
    // LOGIN TEST 1
    // Valid username + password
    // =====================================================

    @Test
    void login_shouldLoginSuccessfully() {

        // Arrange

        LoginRequest request = new LoginRequest();

        request.setUsername("gnan");
        request.setPassword("password123");


        User user = new User();

        user.setId(1L);
        user.setUsername("gnan");
        user.setPassword("encodedPassword");
        user.setRole("USER");


        when(
                userRepository.findByUsername("gnan")
        ).thenReturn(
                java.util.List.of(user)
        );


        when(
                passwordEncoder.matches(
                        "password123",
                        "encodedPassword"
                )
        ).thenReturn(true);


        when(
                jwtService.generateToken(
                        "gnan",
                        "USER"
                )
        ).thenReturn("jwt-token-123");


        // Act

        LoginResponse response =
                userService.login(request);


        // Assert

        assertNotNull(response);

        assertEquals(
                "jwt-token-123",
                response.getToken()
        );

        assertEquals(
                "gnan",
                response.getUsername()
        );

        assertEquals(
                "USER",
                response.getRole()
        );


        // Verify

        verify(
                userRepository
        ).findByUsername("gnan");


        verify(
                passwordEncoder
        ).matches(
                "password123",
                "encodedPassword"
        );


        verify(
                jwtService
        ).generateToken(
                "gnan",
                "USER"
        );
    }


    // =====================================================
    // LOGIN TEST 2
    // Username not found
    // =====================================================

    @Test
    void login_shouldThrowExceptionWhenUsernameNotFound() {

        // Arrange

        LoginRequest request = new LoginRequest();

        request.setUsername("unknown");
        request.setPassword("password123");


        when(
                userRepository.findByUsername("unknown")
        ).thenReturn(
                java.util.Collections.emptyList()
        );


        // Act + Assert

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> userService.login(request)
                );


        assertEquals(
                "Invalid username or password",
                exception.getMessage()
        );


        // Verify

        verify(
                userRepository
        ).findByUsername("unknown");
    }


    // =====================================================
    // LOGIN TEST 3
    // Wrong password
    // =====================================================

    @Test
    void login_shouldThrowExceptionWhenPasswordIsWrong() {

        // Arrange

        LoginRequest request = new LoginRequest();

        request.setUsername("gnan");
        request.setPassword("wrongPassword");


        User user = new User();

        user.setId(1L);
        user.setUsername("gnan");
        user.setPassword("encodedPassword");
        user.setRole("USER");


        when(
                userRepository.findByUsername("gnan")
        ).thenReturn(
                java.util.List.of(user)
        );


        when(
                passwordEncoder.matches(
                        "wrongPassword",
                        "encodedPassword"
                )
        ).thenReturn(false);


        // Act + Assert

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> userService.login(request)
                );


        assertEquals(
                "Invalid username or password",
                exception.getMessage()
        );


        // Verify

        verify(
                passwordEncoder
        ).matches(
                "wrongPassword",
                "encodedPassword"
        );
    }


    // =====================================================
    // LOGIN TEST 4
    // Multiple matching accounts
    // =====================================================

    @Test
    void login_shouldThrowExceptionForMultipleMatchingAccounts() {

        // Arrange

        LoginRequest request = new LoginRequest();

        request.setUsername("gnan");
        request.setPassword("samePassword");


        User user1 = new User();

        user1.setId(1L);
        user1.setUsername("gnan");
        user1.setPassword("encodedPassword1");
        user1.setRole("USER");


        User user2 = new User();

        user2.setId(2L);
        user2.setUsername("gnan");
        user2.setPassword("encodedPassword2");
        user2.setRole("RETAILER");


        when(
                userRepository.findByUsername("gnan")
        ).thenReturn(
                java.util.List.of(user1, user2)
        );


        when(
                passwordEncoder.matches(
                        "samePassword",
                        "encodedPassword1"
                )
        ).thenReturn(true);


        when(
                passwordEncoder.matches(
                        "samePassword",
                        "encodedPassword2"
                )
        ).thenReturn(true);


        // Act + Assert

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> userService.login(request)
                );


        assertEquals(
                "Multiple accounts found. " +
                "Use different passwords for different roles.",
                exception.getMessage()
        );
    }

    // =====================================================
    // GET CURRENT USER TEST 1
    // User exists
    // =====================================================

    @Test
    void getCurrentUser_shouldReturnUserSuccessfully() {

        // Arrange

        User user = new User();

        user.setId(1L);
        user.setUsername("gnan");
        user.setRole("USER");


        when(
                userRepository.findByUsernameAndRole(
                        "gnan",
                        "USER"
                )
        ).thenReturn(
                java.util.Optional.of(user)
        );


        // Act

        UserResponse response =
                userService.getCurrentUser(
                        "gnan",
                        "USER"
                );


        // Assert

        assertNotNull(response);

        assertEquals(
                1L,
                response.getId()
        );

        assertEquals(
                "gnan",
                response.getUsername()
        );

        assertEquals(
                "USER",
                response.getRole()
        );


        // Verify

        verify(
                userRepository
        ).findByUsernameAndRole(
                "gnan",
                "USER"
        );
    }


    // =====================================================
    // GET CURRENT USER TEST 2
    // User not found
    // =====================================================

    @Test
    void getCurrentUser_shouldThrowExceptionWhenUserNotFound() {

        // Arrange

        when(
                userRepository.findByUsernameAndRole(
                        "unknown",
                        "USER"
                )
        ).thenReturn(
                java.util.Optional.empty()
        );


        // Act + Assert

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> userService.getCurrentUser(
                                "unknown",
                                "USER"
                        )
                );


        assertEquals(
                "User not found",
                exception.getMessage()
        );


        // Verify

        verify(
                userRepository
        ).findByUsernameAndRole(
                "unknown",
                "USER"
        );
    }
}