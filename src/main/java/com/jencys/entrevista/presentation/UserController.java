package com.leonel.entrevista.presentation;

import com.leonel.entrevista.domain.dto.UserDTO;
import com.leonel.entrevista.domain.dto.UserPassword;
import com.leonel.entrevista.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/all")
    public ResponseEntity<Object> findAll(){
        log.info("CALLING ENDPOINT: /api/user/all ");
        return userService.findAll();
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody UserDTO userDTO){
        log.info("CALLING ENDPOINT: /api/user/register ");
        return userService.registerUser(userDTO);
    }

    @GetMapping("/token")
    public ResponseEntity<Object> getToken(@RequestBody UserPassword userPassword){
        return userService.getToken(userPassword);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserPassword userPassword, @RequestHeader Map<String, Object> headers){
        return userService.login(userPassword, headers);
    }
}
