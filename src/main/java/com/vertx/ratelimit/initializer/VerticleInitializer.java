package com.vertx.ratelimit.initializer;


import com.vertx.ratelimit.verticle.LoginVerticle;
import io.vertx.core.Vertx;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class VerticleInitializer {

    private final LoginVerticle loginVerticle;

    public VerticleInitializer(LoginVerticle loginVerticle) {
        this.loginVerticle = loginVerticle;
    }


    @PostConstruct
    public void inti() {
        Vertx.vertx().deployVerticle(loginVerticle);
    }
}
