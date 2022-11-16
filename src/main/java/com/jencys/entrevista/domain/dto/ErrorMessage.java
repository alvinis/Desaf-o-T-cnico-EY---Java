package com.leonel.entrevista.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ErrorMessage {
    private List<String> mensaje;
}
