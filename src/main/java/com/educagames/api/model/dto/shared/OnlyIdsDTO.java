package com.educagames.api.model.dto.shared;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class OnlyIdsDTO {

    @Schema(description = "Lista de ids")
    private List<Long> ids;
}
