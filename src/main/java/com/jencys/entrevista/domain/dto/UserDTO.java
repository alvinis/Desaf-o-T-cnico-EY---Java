package com.leonel.entrevista.domain.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO{
	private String password;
	private String name;
	private List<PhonesItem> phones;
	private String email;
}