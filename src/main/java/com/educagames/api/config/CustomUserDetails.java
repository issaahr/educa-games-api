package com.educagames.api.config;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.Role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Implementação de UserDetails do Spring Security que envolve a entidade User.
 * <p>
 * Usado para integrar a entidade User do domínio com o sistema de autenticação
 * do Spring Security.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority(
            "ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    /**
     * Retorna o role do usuário diretamente.
     *
     * @return role do usuário
     */
    public Role getRole() {
        return user.getRole();
    }

    /**
     * Retorna o ID do usuário.
     *
     * @return ID do usuário
     */
    public Long getId() {
        return user.getId();
    }
}

