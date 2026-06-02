package com.educagames.api.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BadgeType {
    THREE_DAYS_STREAK("three_days_streak", "3 Dias Consecutivos", BadgeCategory.LOGIN_STREAK, 1),
    TEN_DAYS_STREAK("ten_days_streak", "10 Dias Consecutivos", BadgeCategory.LOGIN_STREAK, 2),
    THIRTY_DAYS_STREAK("thirty_days_streak", "30 Dias Consecutivos", BadgeCategory.LOGIN_STREAK, 3),
    FIRST_MODULE("first_module", "Primeiro Módulo Concluído", BadgeCategory.COMPLETED_MODULES, 1);

    private final String code;
    private final String displayName;
    private final BadgeCategory category;
    private final Integer order;
}

