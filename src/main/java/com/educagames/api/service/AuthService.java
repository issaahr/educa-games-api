package com.educagames.api.service;

import java.time.LocalDateTime;
import java.util.List;

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
import com.educagames.api.model.dto.classroom.ClassroomInfoDTO;
import com.educagames.api.model.entity.Invite;
import com.educagames.api.model.entity.StudentClassroom;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.InviteStatus;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.InviteRepository;
import com.educagames.api.repository.StudentClassroomRepository;
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
     * Para convites de STUDENT, verifica se o usuário já existe para indicar se é necessário
     * criar novo usuário ou apenas vincular à turma.
     * </p>
     *
     * @param token token UUID do convite a ser validado
     * @return detalhes do convite (email, role, className e requiresSignup)
     * @throws NotFoundException       se o convite não for encontrado
     * @throws ConflictException       se o convite já foi utilizado
     * @throws InviteExpiredException se o convite expirou
     */
    @Transactional(readOnly = true)
    public InviteDetailsResponseDTO validateInvite(String token) {
        Invite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Convite inválido"));

        validateInvite(invite);

        InviteDetailsResponseDTO.InviteDetailsResponseDTOBuilder builder = InviteDetailsResponseDTO.builder()
            .type(invite.getRole())
            .email(invite.getEmail());

        if (invite.getClassName() != null) {
            builder.className(invite.getClassName());
        }

        // Para STUDENT, verifica se o usuário já existe
        // Se existir, não precisa criar usuário, apenas vincular à turma
        boolean requiresSignup = true;
        if (invite.getRole() == Role.STUDENT) {
            User existingUser = userRepository.findByEmail(invite.getEmail()).orElse(null);
            requiresSignup = existingUser == null;
        }

        builder.requiresSignup(requiresSignup);

        return builder.build();
    }

    /**
     * Realiza o login do usuário com email e senha.
     * <p>
     * Valida as credenciais e se o usuário está ativo. Se bem-sucedido, gera um token JWT
     * e o adiciona a um cookie HttpOnly na resposta HTTP.
     *
     * @param request DTO contendo email e senha do usuário.
     * @param response A resposta HTTP onde o cookie de autenticação será adicionado.
     * @throws UnauthorizedException se as credenciais forem inválidas ou o usuário estiver inativo.
     */
    public void login(LoginRequestDTO request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email ou senha incorretos"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Usuário inativo");
        }

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
     * Para STUDENT, se o usuário já existe, apenas vincula à turma sem criar novo usuário.
     * </p>
     *
     * @param request DTO contendo o token do convite, nome e senha do usuário (nome e senha são opcionais quando o usuário já existe)
     * @throws BadRequestException  se o convite não for encontrado, estiver inválido ou se nome/senha forem obrigatórios mas não fornecidos
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
     * - Se usuário não existe, cria novo usuário
     * </p>
     * <p>
     * A verificação de existência do usuário é feita através de findByEmail.
     * Se o usuário existe, retorna ele (não precisa de nome/senha).
     * Se não existe, cria novo (nome/senha já foram validados pelo @Valid quando presentes).
     * </p>
     *
     * @param invite convite sendo utilizado
     * @param request dados do cadastro
     * @return o usuário criado ou encontrado
     * @throws ConflictException se o convite já foi utilizado ou já existe usuário com o email
     */
    private User findOrCreateUser(Invite invite, CompleteSignupRequest request) {
        User existingUser = userRepository.findByEmail(invite.getEmail()).orElse(null);

        // Se não é STUDENT e usuário já existe, erro
        if (invite.getRole() != Role.STUDENT && existingUser != null) {
            throw new ConflictException("Já existe um usuário cadastrado com este email");
        }

        // Se é STUDENT e usuário já existe, retorna o existente (apenas vincula à turma)
        if (invite.getRole() == Role.STUDENT && existingUser != null) {
            return existingUser;
        }

        // Cria novo usuário
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
     * Para STUDENT, retorna os IDs das turmas onde está matriculado.
     * Para INSTRUCTOR, retorna os IDs das turmas que leciona.
     * </p>
     *
     * @param userId ID do usuário cujo perfil será obtido
     * @return perfil do usuário (ID, nome, role e lista de IDs das turmas)
     * @throws UnauthorizedException se o usuário não for encontrado ou estiver inativo
     */
    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Usuário inativo");
        }

        UserProfileDTO.UserProfileDTOBuilder builder = UserProfileDTO.builder()
            .userId(user.getId())
            .role(user.getRole());

        // Para STUDENT: busca turmas ativas onde está matriculado (id e className)
        if (user.getRole() == Role.STUDENT) {
            List<ClassroomInfoDTO> classrooms = studentClassroomRepository.findActiveClassroomIdAndNameByStudentId(userId)
                .stream()
                .map(result -> ClassroomInfoDTO.builder()
                    .id((Long) result[0])
                    .className((String) result[1])
                    .build())
                .toList();
            builder.classes(classrooms);
        }

        return builder.build();
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
