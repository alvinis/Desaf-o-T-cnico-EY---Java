package com.leonel.entrevista.domain.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leonel.entrevista.data.dao.PhoneDao;
import com.leonel.entrevista.data.dao.UserDao;
import com.leonel.entrevista.data.entity.User;
import com.leonel.entrevista.domain.dto.ErrorMessage;
import com.leonel.entrevista.domain.dto.UserDTO;
import com.leonel.entrevista.domain.dto.UserPassword;
import com.leonel.entrevista.domain.service.UserService;
import com.leonel.entrevista.domain.utils.JwtTokenUtil;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    private static final String REGISTER_USER_PATH = "src/test/java/com/leonel/entrevista/resources/register_user_body.json";

    private UserDao userDao;
    private PhoneDao phoneDao;
    private JwtTokenUtil jwtTokenUtil;
    private UserService userService;
    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        userDao = mock(UserDao.class);
        phoneDao = mock(PhoneDao.class);
        jwtTokenUtil = mock(JwtTokenUtil.class);
        userService = new UserServiceImpl(userDao, phoneDao, jwtTokenUtil);
    }

    @Test
    void FindAll_given_a_empty_list_will_return_204() {
        //arrange
        when(userDao.findAll()).thenReturn(Collections.emptyList());

        //act
        ResponseEntity<Object> response = userService.findAll();

        //asserts
        assertNotNull(response);
        assertNull(response.getBody());
        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode());
    }

    @Test
    void FindAll_given_a_valid_list_will_return_200() {
        //arrange
        User user = new User();
        List<User> users = new ArrayList<>();
        users.add(user);

        when(userDao.findAll()).thenReturn(users);

        //act
        ResponseEntity<Object> response = userService.findAll();

        //asserts
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, ((List<?>) response.getBody()).size());
    }

    @Test
    void registerUser_given_a_valid_userDto_will_return_CREATED() throws IOException {
        //arrange
        JsonNode jsonNode = objectMapper.readTree(new File(REGISTER_USER_PATH));
        UserDTO userDTO = objectMapper.convertValue(jsonNode, UserDTO.class);

        //act
        ResponseEntity<Object> response = userService.registerUser(userDTO);

        //asserts
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void registerUser_given_a_invalid_userDto_will_return_BAD_REQUEST() throws IOException {
        //arrange
        JsonNode jsonNode = objectMapper.readTree(new File(REGISTER_USER_PATH));
        UserDTO userDTO = objectMapper.convertValue(jsonNode, UserDTO.class);
        userDTO.setName("");

        //act
        ResponseEntity<Object> response = userService.registerUser(userDTO);

        //asserts
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(((ErrorMessage) response.getBody()).getMensaje());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getToken_given_a_valid_UserPassword_will_return_OK() {
        //arrange
        UserPassword userPassword = new UserPassword("any-email@email.com", "Any-Password123");
        when(jwtTokenUtil.generateToken(any(User.class))).thenReturn("any-token-123abc");
        when(userDao.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        //act
        ResponseEntity<Object> response = userService.getToken(userPassword);

        //asserts
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getoken_given_a_invalid_UserPasswrod_will_return_BAD_REQUEST() {
        //arrange
        UserPassword userPassword = new UserPassword("any-email@email.com", "Any-Password123");
        when(jwtTokenUtil.generateToken(any(User.class))).thenReturn("any-token-123abc");
        when(userDao.findByEmail(anyString())).thenReturn(Optional.empty());

        //act
        ResponseEntity<Object> response = userService.getToken(userPassword);

        //asserts
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(((ErrorMessage) response.getBody()).getMensaje());
    }

    @Test
    void getoken_given_a_UserPasswrod_with_null_fields_will_return_BAD_REQUEST() {
        //arrange
        UserPassword userPassword = new UserPassword(null, null);
        when(jwtTokenUtil.generateToken(any(User.class))).thenReturn("any-token-123abc");

        //act
        ResponseEntity<Object> response = userService.getToken(userPassword);

        //asserts
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(((ErrorMessage) response.getBody()).getMensaje());
    }

    @Test
    void login_given_a_valid_userpassword_and_headers_will_return_OK() {
        //arrange
        UserPassword userPassword = new UserPassword("any-user", "any-password");
        Map<String, Object> headers = new HashMap<>();
        headers.put("authorization", "Bearer any-bearer-token-123");
        User user = new User();
        user.setPassword("any-password");

        when(userDao.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtTokenUtil.validateToken(anyString(), any(User.class))).thenReturn(Boolean.TRUE);

        //act
        ResponseEntity<Object> response = userService.login(userPassword, headers);

        //asserts
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void login_given_a_invalid_token_will_return_BAD_REQUEST() {
        //arrange
        UserPassword userPassword = new UserPassword("any-user", "any-password");
        Map<String, Object> headers = new HashMap<>();
        headers.put("authorization", "Bearer any-bearer-token-123");
        User user = new User();
        user.setPassword("any-password");

        when(userDao.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtTokenUtil.validateToken(anyString(), any(User.class))).thenThrow(MalformedJwtException.class);

        //act
        ResponseEntity<Object> response = userService.login(userPassword, headers);

        //asserts
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void login_given_a_expired_token_will_return_BAD_REQUEST() {
        //arrange
        UserPassword userPassword = new UserPassword("any-user", "any-password");
        Map<String, Object> headers = new HashMap<>();
        headers.put("authorization", "Bearer any-bearer-token-123");
        User user = new User();
        user.setPassword("any-password");

        when(userDao.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtTokenUtil.validateToken(anyString(), any(User.class))).thenReturn(Boolean.FALSE);

        //act
        ResponseEntity<Object> response = userService.login(userPassword, headers);

        //asserts
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void login_given_a_invalid_userpassword_will_return_BAD_REQUEST() {
        //arrange
        UserPassword userPassword = new UserPassword("any-user", "any-passwor");
        Map<String, Object> headers = new HashMap<>();
        headers.put("authorization", "Bearer any-bearer-token-123");
        User user = new User();
        user.setPassword("any-password");

        when(userDao.findByEmail(anyString())).thenReturn(Optional.of(user));

        //act
        ResponseEntity<Object> response = userService.login(userPassword, headers);

        //asserts
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void login_given_a_invalid_password_will_return_BAD_REQUEST() {
        //arrange
        UserPassword userPassword = new UserPassword(null, null);
        Map<String, Object> headers = new HashMap<>();

        //act
        ResponseEntity<Object> response = userService.login(userPassword, headers);

        //asserts
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void login_given_a_request_without_headers_will_return_BAD_REQUEST() {
        //arrange
        UserPassword userPassword = new UserPassword("any-string", "any-password");
        Map<String, Object> headers = new HashMap<>();
        when(userDao.findByEmail(anyString())).thenReturn(Optional.empty());

        //act
        ResponseEntity<Object> response = userService.login(userPassword, headers);

        //asserts
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void login_given_a_request_without_bearer_will_return_BAD_REQUEST() {
        //arrange
        UserPassword userPassword = new UserPassword("any-string", "any-password");
        Map<String, Object> headers = new HashMap<>();
        User user = new User();
        user.setPassword("any-password");
        when(userDao.findByEmail(anyString())).thenReturn(Optional.of(user));

        //act
        ResponseEntity<Object> response = userService.login(userPassword, headers);

        //asserts
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}