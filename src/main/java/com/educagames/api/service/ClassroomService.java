package com.educagames.api.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.educagames.api.config.CustomUserDetails;
import com.educagames.api.model.dto.classroom.ClassroomDTO;
import com.educagames.api.model.dto.classroom.CreateClassRequestDTO;
import com.educagames.api.model.entity.Classroom;
import com.educagames.api.model.entity.User;
import com.educagames.api.repository.ClassroomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;

    public void createClass(CreateClassRequestDTO dto) {
        // @PreAuthorize no controller garante que o usuário está autenticado e é INSTRUCTOR
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();
        User instructor = userDetails.getUser();

        Classroom classroom = new Classroom();
        classroom.setName(dto.getName());
        classroom.setActive(true);
        classroom.setInstructor(instructor);
        classroomRepository.save(classroom);
    }

    public List<ClassroomDTO> listClasses (){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();
        User instructor = userDetails.getUser();

        List<Classroom> classes = classroomRepository.findByInstructorId(instructor.getId());

        return classes.stream()
            .map( classroom -> {
                ClassroomDTO dto = new ClassroomDTO();
                dto.setId(classroom.getId());
                dto.setName(classroom.getName());
                dto.setActive(classroom.isActive());
                return dto;
            })
            .toList();
    }
}
