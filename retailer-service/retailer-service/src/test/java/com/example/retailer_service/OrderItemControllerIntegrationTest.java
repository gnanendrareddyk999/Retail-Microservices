package com.example.retailer_service;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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
class OrderItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    // =====================================================
    // TEST 1
    // GET ALL ORDER ITEMS
    // RETAILER
    // =====================================================

    @Test
    void testGetOrderItemsAsRetailer() throws Exception {

        mockMvc.perform(
                get("/order-items")
                    .with(
                        user("rrr")
                            .roles("RETAILER")
                    )
        )
        .andExpect(status().isOk());
    }


    // =====================================================
    // TEST 2
    // GET ORDER ITEMS
    // WITH DATE FILTER
    // =====================================================

    @Test
    void testGetOrderItemsWithDateFilter()
            throws Exception {

        mockMvc.perform(
                get("/order-items")
                    .param(
                        "from",
                        "2020-01-01"
                    )
                    .param(
                        "to",
                        "2030-12-31"
                    )
                    .with(
                        user("rrr")
                            .roles("RETAILER")
                    )
        )
        .andExpect(status().isOk());
    }


    // =====================================================
    // TEST 3
    // INVALID DATE RANGE
    // =====================================================

    @Test
    void testGetOrderItemsWithInvalidDateRange()
            throws Exception {

        mockMvc.perform(
                get("/order-items")
                    .param(
                        "from",
                        "2030-01-01"
                    )
                    .param(
                        "to",
                        "2020-01-01"
                    )
                    .with(
                        user("rrr")
                            .roles("RETAILER")
                    )
        )
        .andExpect(status().isBadRequest());
    }


    // =====================================================
    // TEST 4
    // GET ORDER ITEM BY ID
    // OWNER RETAILER
    // =====================================================

    @Test
    void testGetOrderItemByIdAsOwner()
            throws Exception {

        mockMvc.perform(
                get("/order-items/6")
                    .with(
                        user("rrr")
                            .roles("RETAILER")
                    )
        )
        .andExpect(status().isOk());
    }


    // =====================================================
    // TEST 5
    // GET ORDER ITEM BY ID
    // DIFFERENT RETAILER
    // =====================================================

    @Test
    void testGetOrderItemByIdUnauthorizedRetailer()
            throws Exception {

        mockMvc.perform(
                get("/order-items/6")
                    .with(
                        user("pspk")
                            .roles("RETAILER")
                    )
        )
        .andExpect(status().isForbidden());
    }


    // =====================================================
    // TEST 6
    // ORDER ITEM NOT FOUND
    // =====================================================

    @Test
    void testGetOrderItemByIdNotFound()
            throws Exception {

        mockMvc.perform(
                get("/order-items/999999")
                    .with(
                        user("rrr")
                            .roles("RETAILER")
                    )
        )
        .andExpect(status().isNotFound());
    }


    // =====================================================
    // TEST 7
    // USER CANNOT ACCESS ORDER ITEMS
    // =====================================================

    @Test
    void testGetOrderItemsAsUser()
            throws Exception {

        mockMvc.perform(
                get("/order-items")
                    .with(
                        user("testuser")
                            .roles("USER")
                    )
        )
        .andExpect(status().isForbidden());
    }
}
