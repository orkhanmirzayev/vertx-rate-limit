package com.vertx.ratelimit.model;

import lombok.Data;

@Data
public class UserLoginModel {

    private String username;
    private String password;
}
