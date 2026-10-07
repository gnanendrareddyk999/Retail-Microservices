package com.example.retailer_service;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.MediaType;


import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Sql(
    scripts = "/integration-test-data.sql",
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
    scripts = "/integration-test-data.sql",
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD
)
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    // ==========================================
    // TEST 1
    // GET ALL INVENTORY
    // ==========================================

    @Test
    void testGetAllInventory() throws Exception {

        mockMvc.perform(
                get("/inventory")
                    .with(user("testuser").roles("USER"))
        )
        .andExpect(status().isOk());
    }


    // ==========================================
    // TEST 2
    // GET INVENTORY BY ID
    // ==========================================

    @Test
    void testGetInventoryById() throws Exception {

        mockMvc.perform(
                get("/inventory/1")
                    .with(user("testuser").roles("USER"))
        )
        .andExpect(status().isOk());
    }


    // ==========================================
    // TEST 3
    // GET INVENTORY WITH DATE FILTER
    // ==========================================

    @Test
    void testGetInventoryWithDateFilter() throws Exception {

        mockMvc.perform(
                get("/inventory")
                    .param("from", "2020-01-01")
                    .param("to", "2030-12-31")
                    .with(user("testuser").roles("USER"))
        )
        .andExpect(status().isOk());
    }


    // ==========================================
    // TEST 4
    // INVALID DATE RANGE
    // ==========================================

    @Test
    void testGetInventoryWithInvalidDateRange() throws Exception {

        mockMvc.perform(
                get("/inventory")
                    .param("from", "2030-01-01")
                    .param("to", "2020-01-01")
                    .with(user("testuser").roles("USER"))
        )
        .andExpect(status().isBadRequest());
    }


    // ==========================================
    // TEST 5
    // ADD INVENTORY AS RETAILER
    // ==========================================    @Sql(scripts = "/integration-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Test
    void testAddInventoryAsRetailer() throws Exception {

        mockMvc.perform(
                post("/inventory")
                    .with(user("testretailer").roles("RETAILER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "productName": "Integration Test Product",
                            "description": "Integration test description",
                            "quantity": 10,
                            "price": 9999.99
                        }
                        """)
        )
        .andExpect(status().isOk());
    }


    // ==========================================
    // TEST 6
    // ADD INVENTORY AS USER
    // ==========================================

    @Test
    void testAddInventoryAsUser() throws Exception {

        mockMvc.perform(
                post("/inventory")
                    .with(user("testuser").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "productName": "User Product",
                            "description": "User cannot add inventory",
                            "quantity": 10,
                            "price": 1000.0
                        }
                        """)
        )
        .andExpect(status().isForbidden());
    }


    // ==========================================
    // TEST 7
    // UPDATE INVENTORY AS OWNER
    // ==========================================    @Sql(scripts = "/integration-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Test
    void testUpdateInventoryAsOwner() throws Exception {

        mockMvc.perform(
                put("/inventory/1")
                    .with(user("pspk").roles("RETAILER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "productName": "Updated Samsung Fridge",
                            "description": "Updated description",
                            "quantity": 100,
                            "price": 26000.0
                        }
                        """)
        )
        .andExpect(status().isOk());
    }


    // ==========================================
    // TEST 8
    // UPDATE INVENTORY AS OTHER RETAILER
    // ==========================================

    @Test
    void testUpdateInventoryAsOtherRetailer() throws Exception {

        mockMvc.perform(
                put("/inventory/1")
                    .with(user("rrr").roles("RETAILER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "productName": "Unauthorized Update",
                            "description": "Should not update",
                            "quantity": 50,
                            "price": 20000.0
                        }
                        """)
        )
        .andExpect(status().isBadRequest());
    }


    // ==========================================
    // TEST 9
    // DELETE INVENTORY AS OWNER
    // ==========================================

    @Test
    void testDeleteInventoryAsOwner() throws Exception {

        String response =
            mockMvc.perform(
                    post("/inventory")
                        .with(user("testdelete").roles("RETAILER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "productName": "Delete Test Product",
                                "description": "Temporary product for delete test",
                                "quantity": 5,
                                "price": 1000.0
                            }
                            """)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    long inventoryId =
            new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(response)
                    .get("id")
                    .asLong();

    mockMvc.perform(
            delete("/inventory/" + inventoryId)
                .with(user("testdelete").roles("RETAILER"))
    )
    .andExpect(status().isOk());

    }


    // ==========================================
    // TEST 10
    // DELETE INVENTORY AS OTHER RETAILER
    // ==========================================

    @Test
    void testDeleteInventoryAsOtherRetailer() throws Exception {

        mockMvc.perform(
                delete("/inventory/1")
                    .with(user("rrr").roles("RETAILER"))
        )
        .andExpect(status().isBadRequest());
    }
}
