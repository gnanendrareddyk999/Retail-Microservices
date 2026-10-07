package com.example.user_service;

import org.junit.jupiter.api.Test;

import com.example.user_service.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import com.example.user_service.entity.User;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;


import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testRegisterUser() throws Exception {

        String requestBody = """
                {
                    "username": "integration_register_001",
                    "password": "Test@123",
                    "role": "USER"
                }
                """;

        mockMvc.perform(
                post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isOk());
    }



@Test
void testRegisterUserWithEmptyUsername() throws Exception {

    String requestBody = """
            {
                "username": "",
                "password": "Test@123",
                "role": "USER"
            }
            """;

    mockMvc.perform(
            post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
    )
    .andExpect(status().isBadRequest());
}


@BeforeEach
void cleanupBeforeTest() {

    userRepository
            .findByUsernameAndRole("integration_user_001", "USER")
            .ifPresent(userRepository::delete);

    User user = new User();
    user.setUsername("integration_user_001");
    user.setPassword(passwordEncoder.encode("Test@123"));
    user.setRole("USER");

    userRepository.save(user);
}


@org.junit.jupiter.api.AfterEach
void cleanup() {

    userRepository
            .findByUsernameAndRole("integration_user_001", "USER")
            .ifPresent(userRepository::delete);

    userRepository
            .findByUsernameAndRole("integration_register_001", "USER")
            .ifPresent(userRepository::delete);
}

@Test
void testLoginUser() throws Exception {

    String requestBody = """
            {
                "username": "integration_user_001",
                "password": "Test@123"
            }
            """;

    mockMvc.perform(
            post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
    )
    .andExpect(status().isOk());
}


@Test
void testLoginWithWrongPassword() throws Exception {

    String requestBody = """
            {
                "username": "integration_user_001",
                "password": "WrongPassword123"
            }
            """;

    Exception exception = org.junit.jupiter.api.Assertions.assertThrows(
            jakarta.servlet.ServletException.class,
            () -> mockMvc.perform(
                    post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
            )
    );

    assertNotNull(exception.getCause());
    assertEquals(
            "Request processing failed: java.lang.RuntimeException: Invalid username or password",
            exception.getMessage()
    );
}


}