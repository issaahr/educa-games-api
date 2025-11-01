package com.educagames.api.repository;

import com.educagames.api.model.entity.Classroom ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {}
