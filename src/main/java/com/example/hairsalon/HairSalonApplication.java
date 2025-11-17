package com.example.hairsalon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HairSalonApplication {

    public static void main(String[] args) {
        SpringApplication.run(HairSalonApplication.class, args);
    }

}
