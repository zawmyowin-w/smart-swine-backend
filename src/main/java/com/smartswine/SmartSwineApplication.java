package com.smartswine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SmartSwineApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartSwineApplication.class, args);
    }
}
