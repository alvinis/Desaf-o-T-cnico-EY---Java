package com.leonel.entrevista.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserApiResponse {
    private String id;
    private Date created;
    private Date modified;
    private Date lastLogin;
    private Boolean isActive;
}
