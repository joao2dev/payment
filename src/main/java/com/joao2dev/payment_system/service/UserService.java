package com.joao2dev.payment_system.service;

import com.joao2dev.payment_system.entity.User;
import com.joao2dev.payment_system.repository.UserRepository;
import com.joao2dev.payment_system.utils.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user){
        if (userRepository.findByEmail(user.getEmail())!= null) {
            throw new RuntimeException("This email already exists");

        }else {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setSenha(encodedPassword);

            String randomCode = RandomString.generatedRandomString(64);
            user.setVerificationCode(randomCode);
            user.setEnabled(false);

            User savedUser = userRepository.save(user);

            return savedUser;
        }
    }
}
