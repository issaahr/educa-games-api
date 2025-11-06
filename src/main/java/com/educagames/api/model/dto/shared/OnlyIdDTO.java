package com.educagames.api.model.dto.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class OnlyIdDTO {
    @Schema(description = "Id do objeto")
    private Long id;
}
