package edu.yale.library.paperless;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableScheduling
public class PaperlessPagingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperlessPagingApplication.class, args);
    }
    @Bean
    public WebMvcConfigurer corsConfigurer(@Value("${cors.origins:}") String origins) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(origins.split(","))
                        .allowCredentials(true)
                        .allowedMethods("GET", "POST", "PUT", "OPTIONS");
            }
        };
    }
}