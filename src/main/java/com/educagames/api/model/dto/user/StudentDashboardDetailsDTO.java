package com.educagames.api.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDashboardDetailsDTO {
    private Long id;
    private String name;
    private String profilePictureUrl;

}
