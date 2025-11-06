package com.educagames.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.Role;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("""
    SELECT u FROM User u
    WHERE u.role = :role
    AND u.active = :active
    AND (:searchPattern IS NULL OR
         LOWER(u.email) LIKE :searchPattern OR
         LOWER(u.name) LIKE :searchPattern)
    """)
    Page<User> findByRoleAndActive(
        @Param("role") Role role,
        @Param("active") boolean active,
        @Param("searchPattern") String searchPattern,
        Pageable pageable
    );
}
