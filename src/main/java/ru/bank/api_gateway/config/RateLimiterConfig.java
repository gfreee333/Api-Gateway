package ru.bank.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

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
