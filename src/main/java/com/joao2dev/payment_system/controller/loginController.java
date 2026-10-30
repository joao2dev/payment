package com.joao2dev.payment_system.controller;

import com.joao2dev.payment_system.dto.AuthRequest;
import com.joao2dev.payment_system.dto.AuthResponse;
import com.joao2dev.payment_system.entity.User;
import com.joao2dev.payment_system.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class loginController {
    @Autowired
    private TokenService tokenService;

    @Autowired
    AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody AuthRequest authRequest){
        var usernamePassword = new UsernamePasswordAuthenticationToken(
                authRequest.email()
                ,authRequest.senha());

        var auth = authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken( (User) auth.getPrincipal());

        return ResponseEntity.ok(new AuthResponse(token));
    }
}
