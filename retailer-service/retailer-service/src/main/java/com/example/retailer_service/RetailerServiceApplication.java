
package com.example.retailer_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RetailerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetailerServiceApplication.class, args);
    }

}

