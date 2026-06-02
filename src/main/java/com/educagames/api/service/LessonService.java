package com.educagames.api.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.educagames.api.exception.BadRequestException;
import com.educagames.api.exception.NotFoundException;
import com.educagames.api.model.dto.lesson.LessonRequestDTO;
import com.educagames.api.model.dto.lesson.LessonResponseDTO;
import com.educagames.api.model.dto.lesson.ResourceDTO;
import com.educagames.api.model.entity.Lesson;
import com.educagames.api.model.entity.LessonMaterial;
import com.educagames.api.model.entity.Module;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.MaterialType;
import com.educagames.api.repository.LessonMaterialRepository;
import com.educagames.api.repository.LessonRepository;
import com.educagames.api.repository.ModuleRepository;
import com.educagames.api.repository.StudentLessonProgressRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonService {

    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final LessonMaterialRepository lessonMaterialRepository;
    private final StudentLessonProgressRepository studentLessonProgressRepository;
    private final AuthService authService;
    private final UploadService uploadService;

    /**
     * Salva aulas a partir de DTOs para um módulo.
     *
     * @param module Módulo ao qual as aulas serão associadas
     * @param lessonDtos Lista de DTOs das aulas a serem criadas
     */
    @Transactional
    public void saveLessonsFromDto(Module module, List<LessonRequestDTO> lessonDtos) {
        if (lessonDtos == null || lessonDtos.isEmpty()) return;
        for (int i = 0; i < lessonDtos.size(); i++) {
            LessonRequestDTO dto = lessonDtos.get(i);
            Lesson lesson = new Lesson();
            lesson.setModule(module);
            updateLessonFromDto(lesson, dto, i);
            lessonRepository.save(lesson);
            saveLessonMaterials(lesson, dto.getResources());
        }
    }

    /**
     * Salva os materiais de uma aula a partir de uma lista de recursos.
     * <p>
     * Filtra recursos que não são do YouTube e cria entidades LessonMaterial para cada um.
     * </p>
     *
     * @param lesson   aula à qual os materiais serão associados
     * @param resources lista de recursos DTO contendo os materiais
     */
    private void saveLessonMaterials(Lesson lesson, List<ResourceDTO> resources) {
        List<ResourceDTO> materials = filterNonYoutubeResources(resources);
        for (ResourceDTO r : materials) {
            LessonMaterial material = new LessonMaterial();
            material.setLesson(lesson);
            material.setName(Optional.ofNullable(r.getLabel()).orElse(""));
            material.setType(mapMaterialType(r.getType()));
            material.setUrl(Optional.ofNullable(r.getContent()).orElse(""));
            lessonMaterialRepository.save(material);
        }
    }

    /**
     * Atualiza aulas de um módulo a partir de DTOs.
     * Aulas com ID são atualizadas, sem ID são criadas, e aulas existentes não presentes na lista são removidas.
     *
     * @param module Módulo ao qual as aulas pertencem
     * @param lessonDtos Lista completa de DTOs das aulas (substitui todas as aulas existentes)
     */
    @Transactional
    public void updateLessonsFromDto(Module module, List<LessonRequestDTO> lessonDtos, List<MultipartFile> files) {
        List<Lesson> existingLessons = lessonRepository.findByModuleOrderByOrderIndexAsc(module);
        Map<Long, Lesson> byId = existingLessons.stream()
            .filter(l -> l.getId() != null)
            .collect(Collectors.toMap(Lesson::getId, l -> l));

        Set<Long> keepLessonIds = new HashSet<>();
        List<MultipartFile> availableFiles = files != null ? new ArrayList<>(files) : new ArrayList<>();
        int[] fileIndex = {0};

        if (lessonDtos != null) {
            for (int i = 0; i < lessonDtos.size(); i++) {
                LessonRequestDTO dto = lessonDtos.get(i);
                Lesson target = dto.getId() != null ? byId.get(dto.getId()) : null;

                if (target == null) {
                    target = new Lesson();
                    target.setModule(module);
                }

                updateLessonFromDto(target, dto, i);
                lessonRepository.save(target);
                if (target.getId() != null) keepLessonIds.add(target.getId());

                updateLessonMaterials(module.getId(), target, dto.getResources(), availableFiles, fileIndex);
            }
        }

        List<Lesson> lessonsToDelete = existingLessons.stream()
            .filter(l -> l.getId() == null || !keepLessonIds.contains(l.getId()))
            .toList();

        for (Lesson lesson : lessonsToDelete) {
            studentLessonProgressRepository.deleteAll(
                studentLessonProgressRepository.findByLesson(lesson)
            );
            lessonRepository.delete(lesson);
        }
    }

    /**
     * Atualiza os campos de uma aula a partir de um DTO.
     *
     * @param lesson     aula a ser atualizada
     * @param dto        DTO com os novos dados da aula
     * @param orderIndex índice de ordenação da aula no módulo
     */
    private void updateLessonFromDto(Lesson lesson, LessonRequestDTO dto, int orderIndex) {
        lesson.setOrderIndex(orderIndex);
        lesson.setTitle(Optional.ofNullable(dto.getTitle()).orElse(""));
        lesson.setContent(Optional.ofNullable(dto.getDescription()).orElse(""));
        lesson.setPoints(Optional.ofNullable(dto.getPoints()).orElse(0));
        lesson.setVideoLink(Optional.ofNullable(extractYoutubeUrl(dto.getResources())).orElse(""));
    }

    /**
     * Atualiza os materiais de uma aula a partir de uma lista de recursos.
     * <p>
     * Materiais com ID são atualizados, sem ID são criados, e materiais existentes
     * não presentes na lista são removidos.
     * </p>
     *
     * @param moduleId ID do módulo (para upload de arquivos)
     * @param lesson   aula cujos materiais serão atualizados
     * @param resources lista de recursos DTO contendo os materiais
     * @param availableFiles lista de arquivos disponíveis para upload
     * @param fileIndex array com índice atual dos arquivos (usado para manter ordem sequencial)
     */
    private void updateLessonMaterials(Long moduleId, Lesson lesson, List<ResourceDTO> resources, List<MultipartFile> availableFiles, int[] fileIndex) {
        List<ResourceDTO> materials = filterNonYoutubeResources(resources);
        List<LessonMaterial> existingMaterials = lessonMaterialRepository.findByLesson(lesson);
        Map<Long, LessonMaterial> matsById = existingMaterials.stream()
            .filter(m -> m.getId() != null)
            .collect(Collectors.toMap(LessonMaterial::getId, m -> m));

        Set<Long> keepMaterialIds = new HashSet<>();
        for (ResourceDTO r : materials) {
            LessonMaterial mat = r.getId() != null ? matsById.get(r.getId()) : null;
            if (mat == null) {
                mat = new LessonMaterial();
                mat.setLesson(lesson);
            }
            mat.setName(Optional.ofNullable(r.getLabel()).orElse(""));
            if (r.getType() != null && !r.getType().trim().isEmpty()) {
                mat.setType(mapMaterialType(r.getType()));
            } else if (mat.getId() == null) {
                mat.setType(MaterialType.LINK);
            }

            String materialTypeStr = materialTypeToString(mat.getType());
            String content = r.getContent();

            if ("link".equalsIgnoreCase(materialTypeStr)) {
                mat.setUrl(Optional.ofNullable(content).orElse(""));
            } else {
                boolean hasExistingUrl = mat.getId() != null && mat.getUrl() != null && !mat.getUrl().isEmpty();
                boolean needsNewFile = (content == null || content.isBlank()) && !hasExistingUrl;

                if (needsNewFile) {
                    MultipartFile fileToUse = null;
                    int fileIndexToRemove = -1;

                    for (int i = 0; i < availableFiles.size(); i++) {
                        try {
                            MultipartFile f = availableFiles.get(i);
                            String fileType = mapContentTypeToMaterialType(f.getContentType());
                            if (fileType.equals(materialTypeStr)) {
                                fileToUse = f;
                                fileIndexToRemove = i;
                                break;
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    }

                    if (fileToUse != null) {
                        try {
                            String publicUrl = uploadService.uploadLessonContent(moduleId, lesson.getId(), fileToUse);
                            mat.setUrl(publicUrl);
                            availableFiles.remove(fileIndexToRemove);
                        } catch (Exception e) {
                            log.error("Erro ao fazer upload de arquivo {} para material '{}' da aula {}: {}",
                                fileToUse.getOriginalFilename(), mat.getName(), lesson.getId(), e.getMessage(), e);
                            mat.setUrl("");
                            availableFiles.remove(fileIndexToRemove);
                        }
                    } else {
                        if (mat.getId() == null) {
                            log.error("Nenhum arquivo do tipo '{}' encontrado para novo material '{}' na aula {}",
                                materialTypeStr, mat.getName(), lesson.getId());
                        }
                        mat.setUrl("");
                    }
                } else if (content != null && !content.isBlank()) {
                    mat.setUrl(content);
                } else if (!hasExistingUrl) {
                    mat.setUrl("");
                }
            }

            lessonMaterialRepository.save(mat);
            if (mat.getId() != null) keepMaterialIds.add(mat.getId());
        }

        // Deletar materiais que não estão mais na lista
        existingMaterials.stream()
            .filter(m -> m.getId() == null || !keepMaterialIds.contains(m.getId()))
            .forEach(material -> {
                // Se o material tem um arquivo (não é LINK), deletar do bucket também
                if (material.getType() != MaterialType.LINK && material.getUrl() != null && !material.getUrl().isEmpty()) {
                    uploadService.deleteLessonFile(material.getUrl());
                }
                lessonMaterialRepository.delete(material);
            });
    }

    /**
     * Faz upload de um arquivo como material de uma aula.
     *
     * @param moduleId ID do módulo
     * @param lessonId ID da aula
     * @param file Arquivo a ser enviado
     * @return DTO do material criado
     * @throws NotFoundException se o módulo ou aula não forem encontrados ou não pertencerem ao instrutor
     * @throws BadRequestException se o tipo de arquivo não for permitido
     */
    @Transactional
    public ResourceDTO uploadLessonMaterial(Long moduleId, Long lessonId, MultipartFile file) {
        User instructor = authService.getAuthenticatedUser();
        Module module = moduleRepository.findOneByIdAndInstructor(moduleId, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Módulo não encontrado"));

        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new NotFoundException("Aula não encontrada"));
        if (!lesson.getModule().getId().equals(module.getId())) {
            throw new NotFoundException("Aula não pertence ao módulo informado");
        }

        String ct = file != null ? file.getContentType() : null;
        if (ct == null || !isAllowedLessonContentType(ct)) {
            throw new BadRequestException("Tipo de arquivo não permitido. Tipos aceitos: PNG, JPEG, GIF, WebP, PDF ou ZIP.");
        }

        String publicUrl = uploadService.uploadLessonContent(moduleId, lessonId, file);

        LessonMaterial material = new LessonMaterial();
        material.setLesson(lesson);
        material.setName(Optional.ofNullable(file.getOriginalFilename()).orElse("arquivo"));
        material.setType(mapMaterialType(mapContentTypeToMaterialType(ct)));
        material.setUrl(publicUrl);
        lessonMaterialRepository.save(material);

        return ResourceDTO.builder()
            .id(material.getId())
            .type(materialTypeToString(material.getType()))
            .content(publicUrl)
            .label(material.getName())
            .build();
    }

    /**
     * Atualiza o arquivo de um material de aula existente.
     *
     * @param moduleId ID do módulo
     * @param lessonId ID da aula
     * @param materialId ID do material a ser atualizado
     * @param file Novo arquivo para o material
     * @return DTO do material atualizado
     * @throws NotFoundException se o módulo, aula ou material não forem encontrados ou não pertencerem ao instrutor
     * @throws BadRequestException se o tipo de arquivo não for permitido
     */
    @Transactional
    public ResourceDTO updateLessonMaterial(Long moduleId, Long lessonId, Long materialId, MultipartFile file) {
        User instructor = authService.getAuthenticatedUser();
        Module module = moduleRepository.findOneByIdAndInstructor(moduleId, instructor.getId())
            .orElseThrow(() -> new NotFoundException("Módulo não encontrado"));

        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new NotFoundException("Aula não encontrada"));
        if (!lesson.getModule().getId().equals(module.getId())) {
            throw new NotFoundException("Aula não pertence ao módulo informado");
        }

        LessonMaterial material = lessonMaterialRepository.findById(materialId)
            .orElseThrow(() -> new NotFoundException("Material não encontrado"));
        if (!material.getLesson().getId().equals(lesson.getId())) {
            throw new NotFoundException("Material não pertence à aula informada");
        }

        String ct = file != null ? file.getContentType() : null;
        if (ct == null || !isAllowedLessonContentType(ct)) {
            throw new BadRequestException("Tipo de arquivo não permitido. Tipos aceitos: PNG, JPEG, GIF, WebP, PDF ou ZIP.");
        }

        String publicUrl = uploadService.uploadLessonContent(moduleId, lessonId, file);
        material.setUrl(publicUrl);
        material.setType(mapMaterialType(mapContentTypeToMaterialType(ct)));
        material.setName(Optional.ofNullable(file.getOriginalFilename()).orElse(material.getName()));
        lessonMaterialRepository.save(material);
        return ResourceDTO.builder()
            .id(material.getId())
            .type(materialTypeToString(material.getType()))
            .content(publicUrl)
            .label(material.getName())
            .build();
    }

    /**
     * Extrai a URL do YouTube de uma lista de recursos.
     *
     * @param resources Lista de recursos
     * @return URL do YouTube encontrada ou null se não houver
     */
    public String extractYoutubeUrl(List<ResourceDTO> resources) {
        if (resources == null) return null;
        return resources.stream()
            .filter(r -> r != null && "youtube".equalsIgnoreCase(r.getType()))
            .map(ResourceDTO::getContent)
            .findFirst()
            .orElse(null);
    }

    /**
     * Filtra recursos que não são do YouTube (PDF, ZIP, imagem ou link).
     *
     * @param resources Lista de recursos
     * @return Lista de recursos não-YouTube
     */
    public List<ResourceDTO> filterNonYoutubeResources(List<ResourceDTO> resources) {
        if (resources == null) return new ArrayList<>();
        return resources.stream()
            .filter(r -> r != null && ("pdf".equalsIgnoreCase(r.getType()) ||
                    "zip".equalsIgnoreCase(r.getType()) ||
                    "image".equalsIgnoreCase(r.getType()) ||
                    "link".equalsIgnoreCase(r.getType())))
            .toList();
    }

    /**
     * Verifica se um tipo de conteúdo é permitido para materiais de aulas.
     * Tipos permitidos: image/png, image/jpeg, image/gif, image/webp, application/pdf, application/zip.
     *
     * @param ct Tipo de conteúdo (MIME type)
     * @return true se o tipo for permitido, false caso contrário
     */
    public boolean isAllowedLessonContentType(String ct) {
        if (ct == null) return false;
        return ct.equals("image/png") ||
            ct.equals("image/jpeg") ||
            ct.equals("image/gif") ||
            ct.equals("image/webp") ||
            ct.equals("application/pdf") ||
            ct.equals("application/zip") ||
            ct.equals("application/x-zip-compressed");
    }

    /**
     * Converte uma string de tipo de material para o enum MaterialType.
     *
     * @param type String do tipo (pdf, zip, image, link)
     * @return MaterialType correspondente ou LINK como padrão
     */
    public MaterialType mapMaterialType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return MaterialType.LINK;
        }
        String t = type.trim().toLowerCase();
        return switch (t) {
            case "pdf" -> MaterialType.PDF;
            case "zip" -> MaterialType.ZIP;
            case "image" -> MaterialType.IMAGE;
            case "link" -> MaterialType.LINK;
            default -> MaterialType.LINK;
        };
    }

    /**
     * Mapeia um tipo de conteúdo (MIME type) para um tipo de material.
     *
     * @param ct Tipo de conteúdo (MIME type)
     * @return String do tipo de material (pdf, zip, image, link)
     */
    public String mapContentTypeToMaterialType(String ct) {
        if (ct == null) return "link";
        String c = ct.toLowerCase();
        if (c.startsWith("image/")) return "image";
        if ("application/pdf".equals(c)) return "pdf";
        if ("application/zip".equals(c) || "application/x-zip-compressed".equals(c)) return "zip";
        return "link";
    }

    /**
     * Converte um MaterialType para string.
     *
     * @param t MaterialType
     * @return String do tipo (pdf, zip, image, link)
     */
    public String materialTypeToString(MaterialType t) {
        if (t == null) return "link";
        return switch (t) {
            case PDF -> "pdf";
            case ZIP -> "zip";
            case IMAGE -> "image";
            case LINK -> "link";
        };
    }

    /**
     * Obtém todas as aulas de um módulo ordenadas por orderIndex.
     *
     * @param module Módulo
     * @return Lista de aulas do módulo
     */
    @Transactional(readOnly = true)
    public List<Lesson> getLessonsByModule(Module module) {
        return lessonRepository.findByModuleOrderByOrderIndexAsc(module);
    }

    /**
     * Converte uma lista de entidades Lesson para LessonResponseDTO.
     *
     * @param lessons Lista de aulas
     * @return Lista de DTOs de resposta das aulas
     */
    @Transactional(readOnly = true)
    public List<LessonResponseDTO> mapLessonsToResponse(List<Lesson> lessons) {
        return lessons.stream()
            .map(l -> {
                List<ResourceDTO> resources = new ArrayList<>();
                if (l.getVideoLink() != null && !l.getVideoLink().isBlank()) {
                    resources.add(ResourceDTO.builder()
                        .id(null)
                        .type("youtube")
                        .content(l.getVideoLink())
                        .label("Vídeo")
                        .build());
                }
                List<LessonMaterial> mats = lessonMaterialRepository.findByLesson(l);
                mats.forEach(mat -> resources.add(ResourceDTO.builder()
                    .id(mat.getId())
                    .type(materialTypeToString(mat.getType()))
                    .content(mat.getUrl())
                    .label(mat.getName())
                    .build()));

                return LessonResponseDTO.builder()
                    .id(l.getId())
                    .title(l.getTitle())
                    .points(l.getPoints())
                    .description(l.getContent())
                    .resources(resources)
                    .build();
            })
            .toList();
    }

    /**
     * Adiciona novas aulas a um módulo.
     * As aulas são adicionadas ao final da lista existente.
     *
     * @param module Módulo ao qual as aulas serão adicionadas
     * @param lessonDtos Lista de DTOs das aulas a serem adicionadas
     * @param files Lista de arquivos para upload (opcional)
     */
    @Transactional
    public void addLessonsToModule(Module module, List<LessonRequestDTO> lessonDtos, List<MultipartFile> files) {
        if (lessonDtos == null || lessonDtos.isEmpty()) return;
        List<Lesson> existing = lessonRepository.findByModuleOrderByOrderIndexAsc(module);
        int startIndex = existing.size();
        List<MultipartFile> availableFiles = files != null ? new ArrayList<>(files) : new ArrayList<>();
        int[] fileIndex = {0};

        for (int i = 0; i < lessonDtos.size(); i++) {
            LessonRequestDTO dto = lessonDtos.get(i);
            Lesson lesson = new Lesson();
            lesson.setModule(module);
            updateLessonFromDto(lesson, dto, startIndex + i);
            lessonRepository.save(lesson);
            updateLessonMaterials(module.getId(), lesson, dto.getResources(), availableFiles, fileIndex);
        }
    }

    /**
     * Obtém aulas de um módulo com orderIndex maior ou igual ao valor mínimo.
     *
     * @param module Módulo
     * @param minOrderIndex Valor mínimo do orderIndex
     * @return Lista de aulas filtradas
     */
    @Transactional(readOnly = true)
    public List<Lesson> getLessonsByModuleWithMinOrderIndex(Module module, int minOrderIndex) {
        return lessonRepository.findByModuleOrderByOrderIndexAsc(module).stream()
            .filter(l -> l.getOrderIndex() >= minOrderIndex)
            .toList();
    }

    /**
     * Obtém todos os materiais de uma aula.
     *
     * @param lesson Aula
     * @return Lista de materiais da aula
     */
    @Transactional(readOnly = true)
    public List<LessonMaterial> getMaterialsByLesson(Lesson lesson) {
        return lessonMaterialRepository.findByLesson(lesson);
    }
}
