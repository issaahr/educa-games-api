package com.educagames.api.service;

import java.time.LocalDateTime;

import com.educagames.api.model.entity.StudentClassroom;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.StudentClassroomRepository;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.educagames.api.config.CustomUserDetails;
import com.educagames.api.exception.BadRequestException;
import com.educagames.api.exception.ConflictException;
import com.educagames.api.exception.InviteExpiredException;
import com.educagames.api.exception.NotFoundException;
import com.educagames.api.exception.UnauthorizedException;
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
    private final StudentClassroomRepository studentClassroomRepository;

    /**
     * Valida um token de convite e retorna seus detalhes.
     * <p>
     * Verifica se o convite existe, não foi usado, não expirou e está com status válido.
     * </p>
     *
     * @param token token UUID do convite a ser validado
     * @return detalhes do convite (email e role)
     * @throws NotFoundException       se o convite não for encontrado
     * @throws ConflictException       se o convite já foi utilizado
     * @throws InviteExpiredException se o convite expirou
     */
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

    /**
     * Finaliza o cadastro de um novo usuário a partir de um convite.
     * <p>
     * Valida o convite, cria o usuário com os dados fornecidos e marca o convite como aceito.
     * Verifica se já existe um usuário com o email do convite antes de criar.
     * </p>
     *
     * @param request DTO contendo o token do convite, nome e senha do usuário
     * @throws BadRequestException  se o convite não for encontrado ou estiver inválido
     */
    @Transactional
    public void completeSignup(CompleteSignupRequest request) {
        Invite invite = inviteRepository.findByToken(request.getInvite())
            .orElseThrow(() -> new BadRequestException("Convite inválido ou expirado"));

        validateInvite(invite);

        // Busca usuário existente ou cria novo
        User user = findOrCreateUser(invite, request);

        // Se for estudante, vincula à turma
        if (invite.getRole() == Role.STUDENT && invite.getClassroom() != null) {
            createEnrollment(user, invite);
        }

        invite.setStatus(InviteStatus.ACCEPTED);
        invite.setAcceptedAt(LocalDateTime.now());
        inviteRepository.save(invite);
    }

    /**
     * Busca usuário existente ou cria novo conforme o role do convite.
     * <p>
     * - Para INSTRUCTOR: se usuário já existe, lança exceção
     * - Para STUDENT: se usuário já existe, retorna o existente (não cria novo)
     * - Se usuário não existe, cria
     * @return o usuário criado ou encontrado
     * @throws ConflictException   se o convite já foi utilizado ou já existe usuário com o email
     * </p>
     */
    private User findOrCreateUser(Invite invite, CompleteSignupRequest request) {
        User existingUser = userRepository.findByEmail(invite.getEmail()).orElse(null);

        // Se não é STUDENT e usuário já existe, erro
        if (invite.getRole() != Role.STUDENT && existingUser != null) {
            throw new ConflictException("Já existe um usuário cadastrado com este email");
        }

        // Se é STUDENT e usuário já existe, retorna o existente
        if (invite.getRole() == Role.STUDENT && existingUser != null) {
            return existingUser;
        }

        // Caso contrário, cria novo usuário
        User newUser = User.builder()
            .name(request.getName())
            .email(invite.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(invite.getRole())
            .active(true)
            .build();

        return userRepository.save(newUser);
    }

    /**
     * Vincula um estudante à uma turma.
     * @param user estudante a ser matriculado
     * @param invite convite recebido pelo estudante
     */
    private void createEnrollment(User user, Invite invite){
        if (studentClassroomRepository.existsByStudentIdAndClassroomId(
            user.getId(), invite.getClassroom().getId())) {
            throw new ConflictException("Aluno já está vinculado a esta turma");
        }

        Long classroomId = invite.getClassroom().getId();
        long count = studentClassroomRepository.countByClassroomId(classroomId);
        String enrollment = String.format("%01d", count + 1);

        StudentClassroom studentClassroom = StudentClassroom.builder()
            .student(user)
            .classroom(invite.getClassroom())
            .enrollment(enrollment)
            .active(true)
            .build();

        studentClassroomRepository.save(studentClassroom);
    }

    /**
     * Obtém o perfil de um usuário pelo ID.
     * <p>
     * Verifica se o usuário existe e está ativo antes de retornar seus dados.
     * </p>
     *
     * @param userId ID do usuário cujo perfil será obtido
     * @return perfil do usuário (ID, nome e role)
     * @throws UnauthorizedException se o usuário não for encontrado ou estiver inativo
     */
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

    /**
     * Obtém o CustomUserDetails autenticado do SecurityContext.
     * <p>
     * Útil quando você precisa tanto do User quanto do Role sem fazer múltiplas chamadas.
     *
     * @return CustomUserDetails autenticado
     */
    public CustomUserDetails getAuthenticatedUserDetails() {
        return (CustomUserDetails) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();
    }

    /**
     * Obtém o usuário autenticado do SecurityContext.
     * <p>
     * Método de conveniência para quando você só precisa do User.
     * Internamente usa {@link #getAuthenticatedUserDetails()} para evitar duplicação.
     *
     * @return User autenticado
     */
    public User getAuthenticatedUser() {
        return getAuthenticatedUserDetails().getUser(); // ← Reutiliza o método acima
    }

    /**
     * Valida um convite.
     * <p>
     * Verifica se o convite está com status válido e não expirou.
     * </p>
     *
     * @param invite Convite a ser validado
     * @throws ConflictException se o convite já foi utilizado ou inválido
     * @throws InviteExpiredException se o convite expirou
     */
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
