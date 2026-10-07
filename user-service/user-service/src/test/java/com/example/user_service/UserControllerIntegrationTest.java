package com.example.user_service;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.example.user_service.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.junit.jupiter.api.BeforeEach;

import com.example.user_service.entity.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.springframework.http.MediaType;



import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;





    @Test
    void testGetCurrentUserWithoutToken() throws Exception {

        mockMvc.perform(
                get("/users/me")
        )
        .andExpect(status().isForbidden());
    }

@BeforeEach
void setupUser() {

    userRepository
            .findByUsernameAndRole("integration_me_001", "USER")
            .ifPresent(userRepository::delete);

    User user = new User();

    user.setUsername("integration_me_001");
    user.setPassword(passwordEncoder.encode("Test@123"));
    user.setRole("USER");

    userRepository.save(user);
}

@AfterEach
void cleanupUser() {

    userRepository
            .findByUsernameAndRole("integration_me_001", "USER")
            .ifPresent(userRepository::delete);
}


@Test
void testGetCurrentUserWithValidToken() throws Exception {

    String loginRequest = """
            {
                "username": "integration_me_001",
                "password": "Test@123"
            }
            """;

    String response = mockMvc.perform(
            post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginRequest)
    )
    .andExpect(status().isOk())
    .andReturn()
    .getResponse()
    .getContentAsString();

    String token = new com.fasterxml.jackson.databind.ObjectMapper()
        .readTree(response)
        .get("token")
        .asText();


    mockMvc.perform(
            get("/users/me")
                    .header("Authorization", "Bearer " + token)
    )
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.username").value("integration_me_001"))
    .andExpect(jsonPath("$.role").value("USER"));
}

}
