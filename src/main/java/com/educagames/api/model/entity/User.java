package com.educagames.api.model.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import com.educagames.api.model.enums.Role;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="users")
public class User extends BaseEntity {

    @Column(nullable=false, length=100)
    private String name;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(nullable=false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Role role;

    @Column(nullable=false)
    private Boolean active;

    private LocalDate birthDate;
}
