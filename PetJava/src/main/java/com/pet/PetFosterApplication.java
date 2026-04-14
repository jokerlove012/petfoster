package com.pet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 宠物寄养平台启动类
 * 这是Spring Boot应用的入口点，负责启动整个后端服务
 * 
 * @SpringBootApplication - Spring Boot核心注解，组合了@Configuration、@EnableAutoConfiguration和@ComponentScan
 * @MapperScan - 指定MyBatis Mapper接口的扫描路径，自动注册Mapper为Spring Bean
 */
@SpringBootApplication
@MapperScan("com.pet.mapper")
public class PetFosterApplication {
    
    /**
     * 主方法 - 应用程序入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(PetFosterApplication.class, args);
    }
}
