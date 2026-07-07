package com.comidapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ComidAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComidAppApplication.class, args);
    }
}
