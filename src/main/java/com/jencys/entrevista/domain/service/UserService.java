package com.leonel.entrevista.domain.service;

import com.leonel.entrevista.data.entity.User;
import com.leonel.entrevista.domain.dto.UserDTO;
import com.leonel.entrevista.domain.dto.UserPassword;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface UserService {
    ResponseEntity<Object> findAll();
    ResponseEntity<Object> registerUser(UserDTO userDTO);
    ResponseEntity<Object> getToken(UserPassword userPassword);
    ResponseEntity<Object> login(UserPassword userPassword, Map<String, Object> headers);
}
