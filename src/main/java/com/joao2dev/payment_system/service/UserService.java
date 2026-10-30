package com.joao2dev.payment_system.service;

import com.joao2dev.payment_system.dto.UserResponse;
import com.joao2dev.payment_system.entity.User;
import com.joao2dev.payment_system.repository.UserRepository;
import com.joao2dev.payment_system.utils.RandomString;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    public UserResponse registerUser(User user) throws MessagingException, UnsupportedEncodingException {
        if (userRepository.findByEmail(user.getEmail())!= null) {
            throw new RuntimeException("This email already exists");}

            String password = user.getSenha();
            user.setSenha(passwordEncoder.encode(user.getSenha()));
            user.setVerificationCode(RandomString.generatedRandomString(64));
            user.setEnabled(false);

            User savedUser = userRepository.save(user);
            mailService.sendVerificationEmail(savedUser);

            return new UserResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail());

    }
    public boolean verify(String verificationCode){
        User user = userRepository.findByVerificationCode(verificationCode);
        if(user == null || user.isEnabled()){
            return false;
        } else {
            user.setVerificationCode(null);
            user.setEnabled(true);
            userRepository.save(user);

            return true;
        }
    }
}
