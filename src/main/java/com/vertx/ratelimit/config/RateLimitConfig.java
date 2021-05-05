package com.vertx.ratelimit.config;

import net.jodah.expiringmap.ExpiringMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class RateLimitConfig {

    @Bean
    public ExpiringMap<String, Integer> cacheMap() {
        // minutes, attempt count will be fetched from Vault.
        final ExpiringMap<String, Integer> map = ExpiringMap.builder().expiration(1, TimeUnit.MINUTES).build();
        return map;
    }

}
