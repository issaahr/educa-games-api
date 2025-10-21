package com.educagames.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.educagames.api.model.entity.Invite;
import com.educagames.api.model.enums.InviteStatus;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {
    Optional<Invite> findByEmail(String email);

    Optional<Invite> findByToken(String token);

    List<Invite> findByStatus(InviteStatus status);
}
