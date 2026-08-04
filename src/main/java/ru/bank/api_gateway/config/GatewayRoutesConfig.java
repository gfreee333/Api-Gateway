package ru.bank.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocators(RouteLocatorBuilder builder){
        return builder.routes()
                // todo: 1. Публичные endpoints для AuthService
                .route("auth-service-public", r -> r
                        .path("/auth/login", "/auth/refresh/tokens")
                        .filters(f -> f.
                                addResponseHeader(
                                "X-Service",
                                "auth-public"
                                )
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(ipKeyResolver())
                                )
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(
                                                HttpStatus.SERVICE_UNAVAILABLE,
                                                HttpStatus.INTERNAL_SERVER_ERROR)
                                )
                        )
                        .uri("lb://auth-service")
                )
                // todo: 2. Защищенный маршрут для logout в Auth-Service
                .route("auth-service-protected", r -> r
                        .path("/auth/logout")
                        .filters(f -> f
                                .addResponseHeader(
                                "X-Service",
                                "auth-protected"
                                )
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                        )
                        .uri("lb://auth-service")
                )
                // todo: 3. Защищенный маршрут для Auth-Service
                //  по взаимодействию с пользователем системы
                .route("user-management", r -> r
                        .path("/users/**")
                        .filters(f -> f
                                .addResponseHeader(
                                "X-Service",
                                "user-management"
                                )
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                        )
                        .uri("lb://auth-service")
                )
                .build();
    }

    /**
     * <p><b>Метод: redisRateLimiter</b></p>
     * <p><b>Настройки:</b></p>
     * <ul>
     *   <li><b>replenishRate</b> — сколько запросов в секунду разрешено</li>
     *   <li><b>burstCapacity</b> — максимальное количество запросов в burst</li>
     * </ul>
     */
    @Bean
    public RedisRateLimiter redisRateLimiter(){
        return new RedisRateLimiter(10,20,1);
    }

    /**<p><b>Метод: ipKeyResolver</b></p>
     * <p><b>Ключ: IP-адрес клиента</b></p>
     * <p><b>Используется для:</b> публичных маршрутов</p>
     */
    @Bean
    public KeyResolver ipKeyResolver(){
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(ip);
        };
    }

    /** <p><b>Метод: userKeyResolver</b></p>
     * <p><b>Ключ: userId из заголовка</b></p>
     * <p><b>Используется для:</b> защищенных маршрутов после аутентификации</p>
     */
    @Bean
    public KeyResolver userKeyResolver(){
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }

}
