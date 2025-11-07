package com.educagames.api.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.educagames.api.config.CustomUserDetails;
import com.educagames.api.exception.BadRequestException;
import com.educagames.api.exception.ConflictException;
import com.educagames.api.exception.ForbiddenException;
import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.invite.CreateInviteRequestDTO;
import com.educagames.api.model.dto.invite.InviteDTO;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.entity.Classroom;
import com.educagames.api.model.entity.Invite;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.InviteStatus;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.ClassroomRepository;
import com.educagames.api.repository.InviteRepository;
import com.educagames.api.service.email.EmailTemplate;
import com.educagames.api.service.email.InviteEmailTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final InviteRepository inviteRepository;
    private final EmailService emailService;
    private final InviteEmailTemplate inviteEmailTemplate;
    private final ClassroomRepository classroomRepository;
    private final AuthService authService;

    @Value("${app.invite.signup-token-expiration-hours}")
    private int expirationHours;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.logo.public-url}")
    private String logoUrl;

    /**
     * Cria um novo convite e envia email para o destinatário.
     * <p>
     * O convite é criado com status NOT_SENT e um token único.
     * O status do convite é atualizado para AWAITING_ACCEPTANCE se o envio for bem-sucedido.
     * Convites com falha no envio permanecerão como NOT_SENT para reprocessamento.
     * </p>
     * <p>
     * ADMIN pode criar convites de INSTRUCTOR (classroomId deve ser null).
     * INSTRUCTOR pode criar convites de STUDENT (classroomId é obrigatório).
     * </p>
     * @param inviteToCreate DTO contendo email do destinatário
     * @param classroomId ID da turma (obrigatório para INSTRUCTOR, null para ADMIN)
     * @throws ConflictException se já existe um convite pendente para o email
     * @throws BadRequestException se INSTRUCTOR não informar classroomId
     * @throws NotFoundException se a turma não for encontrada ou não pertencer ao instrutor
     */
    @Transactional
    public void createInvite(CreateInviteRequestDTO inviteToCreate, Long classroomId) {
        CustomUserDetails userDetails = authService.getAuthenticatedUserDetails();
        User sender = userDetails.getUser();
        Role senderRole = sender.getRole();
        String token = UUID.randomUUID().toString();

        if(checkEmailInviteExists(inviteToCreate.getEmail(), senderRole, classroomId)){
            throw new ConflictException("Já existe um convite para este email");
        }

        if(senderRole == Role.INSTRUCTOR && classroomId == null){
            throw new BadRequestException("Não foi possível encontrar a turma informada");
        }

        Invite.InviteBuilder builder = Invite.builder()
            .email(inviteToCreate.getEmail())
            .token(token)
            .status(InviteStatus.NOT_SENT)
            .expiresAt(LocalDateTime.now().plusHours(expirationHours))
            .resendCount(0)
            .sender(sender);

        Role inviteRole = classroomId != null ? Role.STUDENT : Role.INSTRUCTOR;
        builder.role(inviteRole);

        if(inviteRole == Role.STUDENT){
            Classroom classroom = getClassDetails(classroomId, sender.getId());
            builder.classroom(classroom)
                   .className(classroom.getName());
        }

        Invite invite = builder.build();

        inviteRepository.save(invite);
        sendInviteEmail(invite);
    }

    /**
     * Lista convites com paginação, ordenação e busca.
     * <p>
     * ADMIN lista convites de INSTRUCTOR (classroomId é ignorado se fornecido).
     * INSTRUCTOR lista convites de STUDENT das suas turmas (requer classroomId).
     * Convites com status ACCEPTED são excluídos da listagem.
     * </p>
     *
     * @param classroomId ID da turma (obrigatório para INSTRUCTOR, ignorado para ADMIN)
     * @param search      termo de busca para filtrar por email (opcional)
     * @param pageable    configuração de paginação e ordenação
     * @return página de convites convertidos para DTO
     * @throws BadRequestException se INSTRUCTOR não informar classroomId
     */
    public PageResponseDTO<InviteDTO> listInvites(Long classroomId, String search, Pageable pageable) {
        User user = authService.getAuthenticatedUser();
        Role userRole = user.getRole();

        String searchPattern = search != null && !search.trim().isEmpty()
            ? "%" + search.toLowerCase() + "%"
            : null;

        if (userRole == Role.INSTRUCTOR && classroomId == null) {
            throw new BadRequestException("classroomId é obrigatório para listar convites de alunos");
        }

        Page<Invite> invites;
        if (userRole == Role.ADMIN) {
            invites = inviteRepository.findInstructorInvites(Role.INSTRUCTOR, searchPattern, pageable);
        } else {
            invites = inviteRepository.findStudentInvites(Role.STUDENT, classroomId, user.getId(), searchPattern, pageable);
        }

        // Converte para DTO
        Page<InviteDTO> inviteDTOs = invites.map(invite -> {
            InviteDTO dto = new InviteDTO();
            dto.setId(invite.getId());
            dto.setEmail(invite.getEmail());
            dto.setStatus(invite.getStatus());
            dto.setExpiresAt(invite.getExpiresAt());
            return dto;
        });

        return PageResponseDTO.<InviteDTO>builder()
            .content(inviteDTOs.getContent())
            .totalElements(inviteDTOs.getTotalElements())
            .totalPages(inviteDTOs.getTotalPages())
            .size(inviteDTOs.getSize())
            .number(inviteDTOs.getNumber())
            .first(inviteDTOs.isFirst())
            .last(inviteDTOs.isLast())
            .build();
    }


    /**
     * Exclui um convite.
     * <p>
     * Verifica se o convite pertence ao usuário autenticado antes de excluir.
     * ADMIN pode excluir convites de INSTRUCTOR.
     * INSTRUCTOR pode excluir apenas convites de STUDENT das suas próprias turmas.
     * </p>
     *
     * @param inviteId ID do convite a ser excluído
     * @throws ForbiddenException se o convite não pertencer ao usuário autenticado
     */
    @Transactional
    public void deleteInvite (Long inviteId){
        User user = authService.getAuthenticatedUser();
        Long senderId = user.getId();

        this.checkInviteProperty(inviteId, senderId);
        inviteRepository.deleteById(inviteId);
    }

    /**
     * Reenvia um convite existente.
     * <p>
     * Verifica se o convite pertence ao usuário autenticado e se não foi reenviado
     * há menos de 2 horas. ADMIN pode reenviar convites de INSTRUCTOR.
     * INSTRUCTOR pode reenviar apenas convites de STUDENT das suas próprias turmas.
     * </p>
     *
     * @param inviteId ID do convite a ser reenviado
     * @throws BadRequestException se o convite tiver sido reenviado há menos de 2 horas
     * @throws ForbiddenException se o convite não pertencer ao usuário autenticado
     * @throws NotFoundException se o convite não for encontrado
     */
    @Transactional
    public void resendInvite(Long inviteId) {
        User user = authService.getAuthenticatedUser();
        this.checkInviteProperty(inviteId, user.getId());

        Invite invite = inviteRepository.findById(inviteId)
            .orElseThrow(() -> new NotFoundException("Convite não encontrado"));

        if (invite.getLastResendAt() != null &&
            invite.getLastResendAt().isAfter(LocalDateTime.now().minusHours(2))){
            throw new BadRequestException("Não foi possível reenviar, convite já reenviado recentemente");
        }

        invite.setLastResendAt(LocalDateTime.now());
        invite.setResendCount(invite.getResendCount() + 1);
        inviteRepository.save(invite);

        this.sendInviteEmail(invite);
    }

    /**
     * Envia o email do convite e atualiza o status para AWAITING_ACCEPTANCE se bem-sucedido.
     * <p>
     * Método privado que centraliza a lógica comum de envio de emails de convite.
     * </p>
     *
     * @param invite convite cujo email será enviado
     */
    private void sendInviteEmail(Invite invite) {
        String inviteLink = String.format("%s/signup?invite=%s", frontendUrl, invite.getToken());
        EmailTemplate configuredTemplate = inviteEmailTemplate.withData(
            inviteLink,
            logoUrl,
            expirationHours,
            invite.getRole(),
            invite.getClassName() // null para INSTRUCTOR, nome da turma para STUDENT
        );

        boolean sent = emailService.send(invite.getEmail(), configuredTemplate);

        if (sent) {
            invite.setStatus(InviteStatus.AWAITING_ACCEPTANCE);
            inviteRepository.save(invite);
        }
    }

    /**
     * Verifica se já existe um convite pendente para o email informado.
     * <p>
     * Para ADMIN, verifica convites de INSTRUCTOR.
     * Para INSTRUCTOR, verifica apenas convites de STUDENT nas suas próprias turmas.
     * </p>
     *
     * @param email email a ser verificado
     * @param role  role do remetente (ADMIN ou INSTRUCTOR)
     * @param classroomId ID da turma (obrigatório para INSTRUCTOR, null para ADMIN)
     * @return true se já existe um convite pendente para o email, false caso contrário
     */
    private boolean checkEmailInviteExists(String email, Role role, Long classroomId){
        if(role == Role.ADMIN){
            return inviteRepository.findByEmail(email).isPresent();
        } else {
            return inviteRepository.findByClassroomIdAndEmail(classroomId, email)
                .isPresent();
        }
    }

    /**
     * Verifica se o convite pertence ao remetente informado.
     * <p>
     * Usado para garantir que apenas o criador do convite possa excluí-lo ou reenviá-lo.
     * </p>
     *
     * @param inviteId ID do convite a ser verificado
     * @param senderId ID do remetente que deve possuir o convite
     * @throws ForbiddenException se o convite não pertencer ao remetente
     */
    private void checkInviteProperty(Long inviteId, Long senderId){
        boolean hasProperty = inviteRepository.existsByIdAndSenderId(inviteId, senderId);

        if(!hasProperty){
            throw new ForbiddenException("Vocë não tem permissão para acessar esse convite");
        }
    }

    /**
     * Busca detalhes da turma quando o convite for direcionado a um aluno.
     * <p>
     * Verifica se a turma existe e pertence ao instrutor informado.
     * </p>
     * <p>
     * TODO: modificar validação quando admin puder convidar alunos
     * </p>
     *
     * @param classroomId ID da turma a ser buscada
     * @param instructorId ID do instrutor que deve possuir a turma
     * @return entidade Classroom encontrada
     * @throws NotFoundException se a turma não for encontrada ou não pertencer ao instrutor
     */
    private Classroom getClassDetails(Long classroomId, Long instructorId){
        return classroomRepository.findOneByIdAndInstructorId(classroomId, instructorId)
            .orElseThrow(() -> new NotFoundException("Turma não encontrada"));
    }

}
