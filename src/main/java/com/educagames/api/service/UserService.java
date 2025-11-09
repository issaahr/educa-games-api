package com.educagames.api.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.educagames.api.exception.BadRequestException;
import com.educagames.api.exception.ForbiddenException;
import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.dto.user.EditProfileRequestDTO;
import com.educagames.api.model.dto.user.ListInstructorDTO;
import com.educagames.api.model.dto.user.ProfileResponseDTO;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthService authService;
    private final UploadService uploadService;
    private final UserRepository userRepository;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/png",
        "image/jpeg",
        "image/jpg"
    );

    private static final long MAX_AVATAR_SIZE = 3 * 1024 * 1024;

    /**
     * Lista instrutores com paginação, ordenação e busca.
     * <p>
     * Apenas ADMIN pode acessar este método.
     * Permite filtrar por status (ativo/inativo) e buscar por nome ou email.
     * </p>
     *
     * @param active   true para listar apenas instrutores ativos, false para inativos
     * @param search   termo de busca para filtrar por nome ou email (opcional)
     * @param pageable configuração de paginação e ordenação
     * @return página de instrutores convertidos para DTO
     * @throws ForbiddenException se o usuário autenticado não for ADMIN
     */
    public PageResponseDTO<ListInstructorDTO> listInstructors(boolean active, String search, Pageable pageable) {
        User user = authService.getAuthenticatedUser();
        Role userRole = user.getRole();

        if (userRole != Role.ADMIN){
            throw new ForbiddenException("Você não tem permissão para acessar este recurso");
        }

        String searchPattern = search != null && !search.trim().isEmpty()
            ? "%" + search.toLowerCase() + "%"
            : null;

        Page<User> instructors = userRepository.findByRoleAndActive(Role.INSTRUCTOR, active, searchPattern, pageable);

        Page<ListInstructorDTO> instructorsDTOs = instructors.map( instructor ->{
            ListInstructorDTO dto = new ListInstructorDTO();
            dto.setId(instructor.getId());
            dto.setName(instructor.getName());
            dto.setEmail(instructor.getEmail());
            dto.setActive(instructor.isActive());
            return dto;
        });

        return PageResponseDTO.<ListInstructorDTO>builder()
            .content(instructorsDTOs.getContent())
            .totalElements(instructorsDTOs.getTotalElements())
            .totalPages(instructorsDTOs.getTotalPages())
            .size(instructorsDTOs.getSize())
            .number(instructorsDTOs.getNumber())
            .first(instructorsDTOs.isFirst())
            .last(instructorsDTOs.isLast())
            .build();
    }

    /**
     * Exclui permanentemente um instrutor do sistema.
     * <p>
     * Apenas ADMIN pode excluir instrutores.
     * Verifica se o usuário existe e se é do tipo INSTRUCTOR antes de excluir.
     * </p>
     *
     * @param instructorId ID do instrutor a ser excluído
     * @throws NotFoundException   se o instrutor não for encontrado
     * @throws BadRequestException se o usuário informado não for um instrutor
     */
    public void deleteInstructor(Long instructorId){
        User instructor = userRepository.findById(instructorId)
            .orElseThrow(() -> new NotFoundException("Instrutor não encontrado"));

        if (instructor.getRole() != Role.INSTRUCTOR){
            throw new BadRequestException("O usuário informado não é um instrutor");
        }

        userRepository.deleteById(instructorId);
    }

    /**
     * Altera o status (ativo/inativo) de um usuário/instrutor.
     * <p>
     * ADMIN pode alterar status apenas de INSTRUCTOR.
     * INSTRUCTOR poderá alterar status de STUDENT nas suas turmas (implementação futura).
     * </p>
     *
     * @param id     ID do usuário/instrutor a ter o status alterado
     * @param active true para ativar, false para desativar
     * @throws NotFoundException   se o usuário não for encontrado
     * @throws BadRequestException se ADMIN tentar alterar status de um usuário que não é INSTRUCTOR
     */
    public void changeUserStatus(Long id, boolean active){
        User authenticatedUser = authService.getAuthenticatedUser();
        Role userRole = authenticatedUser.getRole();

        User targetUser = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (userRole == Role.ADMIN) {
            targetUser.setActive(active);
            userRepository.save(targetUser);
        }
    }

    /**
     * Atualiza o perfil do usuário autenticado.
     *<p>
     * Funcionalidades suportadas:
     * - Atualizar nome, data de nascimento e descrição;
     * - Remover descrição (clearDescription = true);
     * - Remover avatar (removeAvatar = true);
     * - Upload de novo avatar via multipart;
     *
     * @param avatar arquivo de imagem enviado (opcional)
     * @param dto    dados do perfil em JSON (nome, bio, nascimento, flags)
     */
    @Transactional
    public void updateAuthenticatedProfileUser(MultipartFile avatar, EditProfileRequestDTO dto) {
        User user = authService.getAuthenticatedUser();

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName());
        }

        if (dto.getBirthDate() != null) {
            validateBirthDate(dto.getBirthDate());
            user.setBirthDate(dto.getBirthDate());
        }

        if (Boolean.TRUE.equals(dto.getClearDescription())) {
            user.setDescription(null);
        } else if (dto.getDescription() != null && !dto.getDescription().trim().isEmpty()) {
            user.setDescription(dto.getDescription());
        }

        String oldAvatar = user.getAvatarUrl();

        if (Boolean.TRUE.equals(dto.getRemoveAvatar())) {
            user.setAvatarUrl(null);
        } else if (avatar != null && !avatar.isEmpty()) {
            validateAvatar(avatar);
            String newAvatarUrl = uploadService.uploadAvatar(user.getId(), avatar);
            user.setAvatarUrl(newAvatarUrl);
        }

        userRepository.save(user);

        if (oldAvatar != null && !oldAvatar.isBlank() &&
            (Boolean.TRUE.equals(dto.getRemoveAvatar()) ||
                (avatar != null && !avatar.isEmpty()))) {
            uploadService.deleteFile(oldAvatar);
        }
    }

    /**
     * Verifica se o avatar enviado é válido.
     *
     * @param file arquivo enviado
     * @throws BadRequestException se ultrapassar 3MB ou tipo não permitido
     */
    private void validateAvatar(MultipartFile file) {
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BadRequestException("Arquivo muito grande. Máximo: 3MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Formato inválido. Use: PNG, JPG ou JPEG");
        }

        // Valida conteúdo da imagem
        try {
            if (javax.imageio.ImageIO.read(file.getInputStream()) == null) {
                throw new BadRequestException("Formato inválido. Use: PNG, JPG ou JPEG");
            }
        } catch (java.io.IOException e) {
            throw new BadRequestException("Não foi possível processar a imagem enviada");
        }
    }

    /**
     * Valida se a idade não ultrapassa 120 anos.
     *
     * @param birthDate data de nascimento do usuário
     * @throws BadRequestException se idade > 120
     */
    public void validateBirthDate(LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age > 120) {
            throw new BadRequestException("Data de nascimento inválida");
        }
    }

    public ProfileResponseDTO getAuthenticatedUserProfile(){
        User authenticatedUser = authService.getAuthenticatedUser();

        User user = userRepository.findById(authenticatedUser.getId())
            .orElseThrow(()->new NotFoundException("Usuário não encontrado"));

        ProfileResponseDTO.ProfileResponseDTOBuilder builder = ProfileResponseDTO.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .description(user.getDescription())
            .birthDate(user.getBirthDate())
            .avatarUrl(user.getAvatarUrl());

        return builder.build();
    }
}
