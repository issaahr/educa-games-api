package com.educagames.api.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.ColumnDefault;

import com.educagames.api.model.enums.InviteStatus;
import com.educagames.api.model.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "invites",
    uniqueConstraints = @UniqueConstraint(columnNames = {"email", "classroom_id"})
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
    @JoinColumn
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User sender;

}
