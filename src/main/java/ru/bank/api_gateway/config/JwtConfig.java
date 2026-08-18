package ru.bank.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.bank.jwt.JwtValidationFactory;
import ru.bank.jwt.JwtValidator;

@Configuration
public class JwtConfig {

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    @Bean
    public JwtValidator jwtValidator(){
        return JwtValidationFactory.fromClasspath(publicKeyPath);
    }


}
