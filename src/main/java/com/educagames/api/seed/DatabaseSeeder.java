package com.educagames.api.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.UserRepository;

@Component
public class DatabaseSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String email = "professor@educagames.com";
        // TODO: mudar o a forma de cadastrar a senha quando o smtp for implementado
        if (userRepository.findByEmail(email).isEmpty()) {
            User professor = User.builder()
                .name("Professor Inicial")
                .email(email)
                .password(passwordEncoder.encode("SenhaTeste123"))
                .role(Role.INSTRUCTOR)
                .active(true)
                .build();

            userRepository.save(professor);
            System.out.println("Professor seed criado: " + email);
        }
    }
}
