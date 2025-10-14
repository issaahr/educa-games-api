package com.educagames.api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.educagames.api.exceptions.UnauthorizedException;
import com.educagames.api.model.dto.auth.AuthResult;
import com.educagames.api.model.dto.auth.LoginRequestDTO;
import com.educagames.api.model.dto.auth.UserProfileDTO;
import com.educagames.api.model.entity.User;
import com.educagames.api.repository.UserRepository;
import com.educagames.api.util.JwtUtil;

/**
 * Serviço responsável pela autenticação de usuários.
 *
 * Gerencia operações de login, validação de credenciais e geração de tokens JWT.
 * Utiliza BCrypt para verificação segura de senhas e JWT para autenticação stateless.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Construtor do AuthService.
     *
     * @param userRepository repositório para operações com usuários
     * @param passwordEncoder encoder para verificação de senhas
     * @param jwtUtil utilitário para geração e validação de tokens JWT
     */
    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Realiza o login do usuário com email e senha.
     *
     * Valida as credenciais fornecidas contra o banco de dados e retorna
     * um token JWT válido junto com o papel do usuário.
     *
     * @param request DTO contendo email e senha do usuário
     * @return AuthResult contendo o token JWT e o papel do usuário
     * @throws UnauthorizedException se as credenciais forem inválidas
     */
    public AuthResult login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Email ou senha incorretos"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Email ou senha incorretos");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole().toString());
        return new AuthResult(token, user.getRole().toString());
    }

    /**
     * Obtém o perfil completo do usuário autenticado.
     *
     * Busca os dados atualizados do usuário no banco de dados,
     * garantindo que as informações retornadas estejam sempre atualizadas.
     *
     * @param userId ID do usuário autenticado
     * @return UserProfileDTO com dados completos do usuário
     * @throws UnauthorizedException se o usuário não for encontrado ou estiver inativo
     */
    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));

        if (!user.getActive()) {
            throw new UnauthorizedException("Usuário inativo");
        }

        return UserProfileDTO.builder()
            .userId(user.getId())
            .name(user.getName())
            .build();
    }

}
