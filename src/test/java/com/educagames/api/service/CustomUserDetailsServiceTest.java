package com.educagames.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.educagames.api.config.CustomUserDetails;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Deve carregar usuário por email e retornar CustomUserDetails")
    void whenUserExists_shouldReturnCustomUserDetails() {
        User user = User.builder()
            .name("Test User")
            .email("test@email.com")
            .password("encoded")
            .role(Role.INSTRUCTOR)
            .active(true)
            .build();
        user.setId(1L);

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@email.com");
        assertInstanceOf(CustomUserDetails.class, userDetails);
        CustomUserDetails cud = (CustomUserDetails) userDetails;
        assertEquals(user.getId(), cud.getUser().getId());
        assertEquals(user.getEmail(), cud.getUsername());
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando email não existe")
    void whenUserNotFound_shouldThrowUsernameNotFoundException() {
        when(userRepository.findByEmail("missing@email.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
            customUserDetailsService.loadUserByUsername("missing@email.com")
        );
    }
}
