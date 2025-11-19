package com.educagames.api.model.dto.student;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de resposta com dados de uma badge conquistada")
public class BadgeDTO {

    @Schema(description = "Tipo da badge (código)", example = "three_days_streak")
    private String type;

    @Schema(description = "Data e hora em que a badge foi conquistada", example = "2025-11-19T10:30:00")
    private LocalDateTime earnedAt;
}

