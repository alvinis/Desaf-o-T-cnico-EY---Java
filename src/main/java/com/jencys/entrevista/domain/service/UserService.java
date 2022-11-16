package com.jencys.entrevista.domain.service;

import com.jencys.entrevista.data.entity.User;
import com.jencys.entrevista.domain.dto.UserDTO;
import com.jencys.entrevista.domain.dto.UserPassword;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface UserService {
    ResponseEntity<Object> findAll();
    ResponseEntity<Object> registerUser(UserDTO userDTO);
    ResponseEntity<Object> getToken(UserPassword userPassword);
    ResponseEntity<Object> login(UserPassword userPassword, Map<String, Object> headers);
}
