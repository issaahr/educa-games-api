package com.educagames.api.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.educagames.api.model.enums.InviteStatus;
import com.educagames.api.model.enums.Role;

import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="invites")
public class Invite extends BaseEntity {
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
