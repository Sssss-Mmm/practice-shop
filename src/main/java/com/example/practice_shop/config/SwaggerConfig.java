package com.example.practice_shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI custOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Practice Shop API")
                        .description("쇼핑몰 연습 프로젝트의 API 명세서")
                        .version("v1.0.0")
                        .contact(new Contact()
                                 .name("이승민")
                                 .email("elation4927@gmail.com")
                                 .url("https://github.com/Sssss-Mmm")));
    }
}
