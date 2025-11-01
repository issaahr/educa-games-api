package com.educagames.api.service;

import com.educagames.api.config.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.educagames.api.exception.NotFoundException;
import com.educagames.api.exception.UnauthorizedException;
import com.educagames.api.model.entity.User;
import com.educagames.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Serviço que implementa UserDetailsService para carregar usuários do banco de dados.
 * <p>
 * Usado pelo Spring Security para autenticação baseada em usuário/senha.
 * Também fornece método auxiliar para carregar usuário por ID, usado no JwtFilter.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carrega um usuário pelo email (username).
     * <p>
     * Implementação do método da interface UserDetailsService.
     * Usado principalmente para autenticação via formulário.
     *
     * @param email email do usuário (username)
     * @return UserDetails contendo os dados do usuário
     * @throws UsernameNotFoundException se o usuário não for encontrado
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        return new CustomUserDetails(user);
    }

    /**
     * Carrega um usuário pelo ID.
     * <p>
     * Método auxiliar usado principalmente pelo JwtFilter para carregar
     * o usuário completo a partir do ID extraído do token JWT.
     *
     * @param userId ID do usuário
     * @return CustomUserDetails contendo os dados do usuário
     * @throws NotFoundException se o usuário não for encontrado
     */
    public CustomUserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado com ID: " + userId));

        if (!user.isActive()) {
            throw new UnauthorizedException("Usuário inativo");
        }

        return new CustomUserDetails(user);
    }
}

