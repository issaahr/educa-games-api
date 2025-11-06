package com.educagames.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.educagames.api.exception.BadRequestException;
import com.educagames.api.exception.ForbiddenException;
import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.dto.user.ListInstructorDTO;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthService authService;
    private final UserRepository userRepository;

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
}
