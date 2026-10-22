package com.joao2dev.payment_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity(name = "users")
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    private String email;

    private String senha;

    private String verificationCode;

    private boolean enabled;

    public User(Long id, String name, String email, String senha, String verificationCode, boolean enabled) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.senha = senha;
        this.verificationCode = verificationCode;
        this.enabled = enabled;
    }

    public User(String name, String email, String senha) {
        this.name = name;
        this.email = email;
        this.senha = senha;
    }

    public User() {

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled(){
        return this.enabled;
    }
}
