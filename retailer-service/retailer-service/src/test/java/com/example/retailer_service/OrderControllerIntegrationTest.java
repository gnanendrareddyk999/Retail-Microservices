package com.example.retailer_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.example.retailer_service.client.UserClient;
import com.example.retailer_service.dto.UserResponse;

import org.springframework.http.MediaType;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(
    scripts = "/integration-test-data.sql",
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
    scripts = "/integration-test-data.sql",
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD
)
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserClient userClient;

    @Test
    void testGetOrders() throws Exception {

        mockMvc.perform(
                get("/orders")
                    .with(user("testuser").roles("USER"))
        )
        .andExpect(status().isOk());
    }

    @Test
    void testGetOrdersWithoutAuthentication() throws Exception {

        mockMvc.perform(
                get("/orders")
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void testGetOrderByIdAsUser() throws Exception {

        mockMvc.perform(
                get("/orders/4")
                    .with(user("rr").roles("USER"))
        )
        .andExpect(status().isOk());
    }

    @Test
    void testGetOrderByIdAsRetailer() throws Exception {

        mockMvc.perform(
                get("/orders/4")
                    .with(user("rrr").roles("RETAILER"))
        )
        .andExpect(status().isOk());
    }

    @Test
    void testCreateOrderAsUser() throws Exception {

        UserResponse userResponse =
                new UserResponse(
                        1L,
                        "testuser",
                        "USER"
                );

        when(
                userClient.getCurrentUser()
        ).thenReturn(userResponse);

        mockMvc.perform(
                post("/orders")
                    .with(user("testuser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "items": [
                                    {
                                        "inventoryId": 6,
                                        "quantity": 1
                                    }
                                ]
                            }
                            """)
        )
        .andExpect(status().isOk());
    }

    @Test
    void testCreateOrderWithInvalidQuantity() throws Exception {

        mockMvc.perform(
                post("/orders")
                    .with(user("testuser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "items": [
                                    {
                                        "inventoryId": 6,
                                        "quantity": 0
                                    }
                                ]
                            }
                            """)
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateOrderAsRetailer() throws Exception {

        mockMvc.perform(
                post("/orders")
                    .with(user("rrr").roles("RETAILER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "items": [
                                    {
                                        "inventoryId": 6,
                                        "quantity": 1
                                    }
                                ]
                            }
                            """)
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void testGetOtherUserOrderAsUser() throws Exception {

        mockMvc.perform(
                get("/orders/1")
                    .with(user("rr").roles("USER"))
        )
        .andExpect(status().isForbidden());
    }    @Test
    void testUpdateOrderStatusAsRetailer() throws Exception {

        mockMvc.perform(
                put("/orders/4/status")
                    .with(user("rrr").roles("RETAILER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "status": "CONFIRMED"
                            }
                            """)
        )
        .andExpect(status().isOk());
    }    @Test
    void testUpdateOrderStatusAsUser() throws Exception {

        mockMvc.perform(
                put("/orders/4/status")
                    .with(user("rr").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "status": "DELIVERED"
                            }
                            """)
        )
        .andExpect(status().isForbidden());
    }
}
