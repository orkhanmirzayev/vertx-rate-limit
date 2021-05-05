package com.vertx.ratelimit.verticle;

import com.vertx.ratelimit.model.UserLoginModel;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class LoginVerticle extends AbstractVerticle {

    private int MAX_REQUESTS = 5; //or whatever you want it to be

    private final ExpiringMap<String, Integer> cacheMap;

    public LoginVerticle(ExpiringMap<String, Integer> cacheMap) {
        this.cacheMap = cacheMap;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.post("/login").handler(this::checkLoginRequest);
        router.post("/login").handler((ctx) -> ctx.response().end("Logined"));
        vertx.createHttpServer().requestHandler(router).listen(8090);
    }

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"};

    public static String getClientIpAddress(RoutingContext routingContext) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = routingContext.request().getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return routingContext.request().remoteAddress().hostAddress();
    }

    private boolean isMaximumRequestsPerSecondExceeded(String clientIpAddress, String username) {
        int requests = 0;
        String ipAddressWithUsername = clientIpAddress + "-" + username;
        try {
            //replace with a request_attempt_count table.
            final Integer value = cacheMap.get(ipAddressWithUsername);
            requests = value == null ? 0 : value;
            if (requests > MAX_REQUESTS) {
                cacheMap.put(ipAddressWithUsername, requests);
                return true;
            }
        } catch (Exception e) {
            requests = 0;
        }
        requests++;
        cacheMap.put(ipAddressWithUsername, requests);
        return false;
    }

    private void checkLoginRequest(RoutingContext ctx) {
        final UserLoginModel userLoginModel = ctx.getBodyAsJson().mapTo(UserLoginModel.class);
        String username = userLoginModel.getUsername();
        if (isMaximumRequestsPerSecondExceeded(getClientIpAddress(ctx), username)) {
            ctx.response().setStatusCode(HttpStatus.TOO_MANY_REQUESTS.value()).end("Failed!");
        } else {
            ctx.next();
        }
    }
}
