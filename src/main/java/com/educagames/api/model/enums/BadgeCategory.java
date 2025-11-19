package com.educagames.api.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BadgeCategory {
    LOGIN_STREAK("login_streak"),
    COMPLETED_MODULES("completed_modules");

    private final String code;
}

