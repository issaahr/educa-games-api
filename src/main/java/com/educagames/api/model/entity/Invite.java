package com.educagames.api.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.educagames.api.model.enums.InviteStatus;
import com.educagames.api.model.enums.Role;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"email", "classroomId"})
)

public class Invite extends BaseEntity {
    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column()
    private LocalDateTime acceptedAt;

    @Column(length = 120)
    private String className;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int resendCount;

    @Column()
    private LocalDateTime lastResendAt;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroomId")
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senderId", nullable = false)
    private User sender;

}
