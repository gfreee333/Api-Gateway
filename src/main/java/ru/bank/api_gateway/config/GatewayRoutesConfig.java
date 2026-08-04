package ru.bank.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocators(RouteLocatorBuilder builder){
        return builder.routes()
                // todo: 1. Публичные endpoints для AuthService
                .route("auth-service-public", r -> r
                        .path("/auth/login", "/auth/refresh/tokens")
                        .filters(f -> f.addResponseHeader(
                                "X-Service",
                                "auth-public")
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(
                                                HttpStatus.SERVICE_UNAVAILABLE,
                                                HttpStatus.INTERNAL_SERVER_ERROR)
                                )
                        ).uri("lb://auth-service")
                )
                // todo: 2. Защищенный маршрут для logout в Auth-Service
                .route("auth-service-protected", r -> r
                        .path("/auth/logout")
                        .filters(f -> f.addResponseHeader(
                                "X-Service",
                                "auth-protected"
                        ))
                        .uri("lb://auth-service")
                )
                // todo: 3. Защищенный маршрут для Auth-Service
                //  по взаимодействию с пользователем системы
                .route("user-management", r -> r
                        .path("/users/**")
                        .filters(f -> f.addResponseHeader(
                                "X-Service",
                                "user-management"
                        )).uri("lb://auth-service")
                )
                .build();
    }

}
