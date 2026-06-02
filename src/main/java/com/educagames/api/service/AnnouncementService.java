package com.educagames.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.announcement.AnnouncementRequestDTO;
import com.educagames.api.model.dto.announcement.AnnouncementResponseDTO;
import com.educagames.api.model.entity.Announcement;
import com.educagames.api.model.entity.Classroom;
import com.educagames.api.model.entity.User;
import com.educagames.api.repository.AnnouncementRepository;
import com.educagames.api.repository.ClassroomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ClassroomRepository classroomRepository;
    private final AuthService authService;

    /**
     * Lista todos os avisos do instrutor autenticado.
     * <p>
     * Retorna os avisos ordenados por data de criação (mais recentes primeiro).
     * </p>
     *
     * @return lista de avisos convertidos para DTO
     */
    @Transactional(readOnly = true)
    public List<AnnouncementResponseDTO> listAnnouncements() {
        User instructor = authService.getAuthenticatedUser();
        List<Announcement> announcements = announcementRepository.findByInstructorOrderByCreatedAtDesc(instructor);
        return announcements.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Obtém um aviso específico do instrutor autenticado.
     *
     * @param id ID do aviso
     * @return DTO do aviso encontrado
     * @throws NotFoundException se o aviso não for encontrado ou não pertencer ao instrutor
     */
    @Transactional(readOnly = true)
    public AnnouncementResponseDTO getAnnouncement(Long id) {
        User instructor = authService.getAuthenticatedUser();
        Announcement announcement = announcementRepository.findByIdAndInstructor(id, instructor)
            .orElseThrow(() -> new NotFoundException("Aviso não encontrado"));
        return mapToResponse(announcement);
    }

    /**
     * Cria um novo aviso associado ao instrutor autenticado.
     * <p>
     * Valida que as turmas especificadas pertencem ao instrutor antes de associá-las ao aviso.
     * </p>
     *
     * @param dto DTO contendo os dados do aviso (título, conteúdo e turmas)
     * @return DTO do aviso criado
     * @throws NotFoundException se alguma turma não for encontrada ou não pertencer ao instrutor
     */
    @Transactional
    public AnnouncementResponseDTO createAnnouncement(AnnouncementRequestDTO dto) {
        User instructor = authService.getAuthenticatedUser();

        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setInstructor(instructor);

        if (dto.getAssignedClasses() != null && !dto.getAssignedClasses().isEmpty()) {
            List<Classroom> classrooms = classroomRepository.findAllById(dto.getAssignedClasses());
            for (Classroom classroom : classrooms) {
                if (!classroom.getInstructor().getId().equals(instructor.getId())) {
                    throw new NotFoundException("Turma não encontrada ou não pertence ao instrutor");
                }
            }
            announcement.setClassrooms(classrooms);
        }

        announcement = announcementRepository.save(announcement);
        return mapToResponse(announcement);
    }

    /**
     * Atualiza um aviso existente do instrutor autenticado.
     * <p>
     * Valida que o aviso pertence ao instrutor e que as turmas especificadas também pertencem a ele.
     * Se a lista de turmas for vazia, remove todas as associações.
     * </p>
     *
     * @param id  ID do aviso a ser atualizado
     * @param dto DTO contendo os novos dados do aviso
     * @return DTO do aviso atualizado
     * @throws NotFoundException se o aviso não for encontrado ou alguma turma não pertencer ao instrutor
     */
    @Transactional
    public AnnouncementResponseDTO updateAnnouncement(Long id, AnnouncementRequestDTO dto) {
        User instructor = authService.getAuthenticatedUser();
        Announcement announcement = announcementRepository.findByIdAndInstructor(id, instructor)
            .orElseThrow(() -> new NotFoundException("Aviso não encontrado"));

        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());

        if (dto.getAssignedClasses() != null) {
            if (dto.getAssignedClasses().isEmpty()) {
                announcement.setClassrooms(List.of());
            } else {
                List<Classroom> classrooms = classroomRepository.findAllById(dto.getAssignedClasses());
                for (Classroom classroom : classrooms) {
                    if (!classroom.getInstructor().getId().equals(instructor.getId())) {
                        throw new NotFoundException("Turma não encontrada ou não pertence ao instrutor");
                    }
                }
                announcement.setClassrooms(classrooms);
            }
        }

        announcement = announcementRepository.save(announcement);
        return mapToResponse(announcement);
    }

    /**
     * Remove um aviso do instrutor autenticado.
     *
     * @param id ID do aviso a ser removido
     * @throws NotFoundException se o aviso não for encontrado ou não pertencer ao instrutor
     */
    @Transactional
    public void deleteAnnouncement(Long id) {
        User instructor = authService.getAuthenticatedUser();
        Announcement announcement = announcementRepository.findByIdAndInstructor(id, instructor)
            .orElseThrow(() -> new NotFoundException("Aviso não encontrado"));
        announcementRepository.delete(announcement);
    }

    /**
     * Obtém todos os avisos direcionados para uma turma específica.
     * <p>
     * Retorna os avisos ordenados por data de criação (mais recentes primeiro).
     * </p>
     *
     * @param classroom turma para a qual buscar os avisos
     * @return lista de avisos convertidos para DTO
     */
    @Transactional(readOnly = true)
    public List<AnnouncementResponseDTO> getAnnouncementsByClassroom(Classroom classroom) {
        List<Announcement> announcements = announcementRepository.findByClassroomOrderByCreatedAtDesc(classroom);
        return announcements.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Converte uma entidade Announcement para seu DTO de resposta.
     *
     * @param announcement entidade a ser convertida
     * @return DTO de resposta com os dados do aviso
     */
    private AnnouncementResponseDTO mapToResponse(Announcement announcement) {
        List<Long> assignedClasses = announcement.getClassrooms().stream()
            .map(Classroom::getId)
            .collect(Collectors.toList());

        return AnnouncementResponseDTO.builder()
            .id(announcement.getId())
            .title(announcement.getTitle())
            .content(announcement.getContent())
            .date(announcement.getCreatedAt())
            .assignedClasses(assignedClasses)
            .build();
    }
}
