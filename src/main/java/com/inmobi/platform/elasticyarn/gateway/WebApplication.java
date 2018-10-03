package com.inmobi.platform.elasticyarn.gateway;

import com.inmobi.platform.elasticyarn.gateway.service.FileStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.Arrays;

@SpringBootApplication
@EnableScheduling
@Slf4j
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            ArrayList l = new ArrayList();
            log.debug("Let's inspect the beans provided by Spring Boot: {}", Arrays.asList(beanNames));
        };
    }

}
