package com.joao2dev.payment_system.dto;

import com.joao2dev.payment_system.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotNull(message = "o nome nao pode ser vazio")
        @NotBlank(message = "campo nao pode ser nulo")
        String name,
        @NotNull(message = "o email nao pode ser vazio")
        @NotBlank(message = "campo nao pode ser nulo")
        @Email
        String email,
        @NotNull(message = "a senha nao pode ser vazia")
        @NotBlank(message = "campo nao pode ser nulo")
        @Size(min = 8, message = "a senha deve conter pelo menos 8 caracteres.")
        String senha)
{
    public User toModel(){
        return new User(name,email, senha);

    }
}
