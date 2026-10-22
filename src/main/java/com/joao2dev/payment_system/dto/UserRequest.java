package com.joao2dev.payment_system.dto;

import com.joao2dev.payment_system.entity.User;

public record UserRequest(String name, String email, String password) {
    public User toModel(){
        return new User(name,email,password);

    }
}
