package com.rxdonation.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RxDonationBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RxDonationBackendApplication.class, args);
    }

}
