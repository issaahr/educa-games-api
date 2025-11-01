package com.educagames.api.service;

import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.educagames.api.exception.*;
import com.educagames.api.model.dto.auth.CompleteSignupRequest;
import com.educagames.api.model.dto.auth.InviteDetailsResponseDTO;
import com.educagames.api.model.dto.auth.LoginRequestDTO;
import com.educagames.api.model.dto.auth.UserProfileDTO;
import com.educagames.api.model.entity.Invite;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.InviteStatus;
import com.educagames.api.repository.InviteRepository;
import com.educagames.api.repository.UserRepository;
import com.educagames.api.util.CookieUtil;
import com.educagames.api.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final InviteRepository inviteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    @Transactional(readOnly = true)
    public InviteDetailsResponseDTO validateInvite(String token) {
        Invite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Convite inválido"));

        validateInvite(invite);

        return InviteDetailsResponseDTO.builder()
                .type(invite.getRole())
                .email(invite.getEmail())
                .build();
    }

    /**
     * Realiza o login do usuário com email e senha.
     * <p>
     * Valida as credenciais e, se bem-sucedido, gera um token JWT
     * e o adiciona a um cookie HttpOnly na resposta HTTP.
     *
     * @param request DTO contendo email e senha do usuário.
     * @param response A resposta HTTP onde o cookie de autenticação será adicionado.
     * @throws UnauthorizedException se as credenciais forem inválidas.
     */
    public void login(LoginRequestDTO request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email ou senha incorretos"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Email ou senha incorretos");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole().toString());
        cookieUtil.addAuthCookie(response, token);
    }

    @Transactional
    public void completeSignup(CompleteSignupRequest request) {
        Invite invite = inviteRepository.findByToken(request.getInvite())
                .orElseThrow(() -> new BadRequestException("Convite inválido ou expirado"));

        validateInvite(invite);

        if (userRepository.findByEmail(invite.getEmail()).isPresent()) {
            throw new ConflictException("Já existe um usuário cadastrado com este email");
        }

        User newUser = User.builder()
                .name(request.getName())
                .email(invite.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(invite.getRole())
                .active(true)
                .build();

        userRepository.save(newUser);

        invite.setStatus(InviteStatus.ACCEPTED);
        invite.setAcceptedAt(LocalDateTime.now());

        inviteRepository.save(invite);
    }

    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Usuário inativo");
        }

        return UserProfileDTO.builder()
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }

    private void validateInvite(Invite invite) {
        if (invite.getStatus() != InviteStatus.AWAITING_ACCEPTANCE) {
            throw new ConflictException("Convite já utilizado ou inválido");
        }

        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setStatus(InviteStatus.EXPIRED);
            inviteRepository.save(invite);
            throw new InviteExpiredException("Link expirado, solicite um novo");
        }
    }
}
