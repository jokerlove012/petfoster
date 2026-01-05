package com.pet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pet.mapper")
public class PetFosterApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetFosterApplication.class, args);
    }
}
