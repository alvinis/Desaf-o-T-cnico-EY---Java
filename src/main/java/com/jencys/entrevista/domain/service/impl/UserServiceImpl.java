package com.leonel.entrevista.domain.service.impl;

import com.leonel.entrevista.data.dao.PhoneDao;
import com.leonel.entrevista.data.dao.UserDao;
import com.leonel.entrevista.data.entity.Phone;
import com.leonel.entrevista.data.entity.User;
import com.leonel.entrevista.domain.dto.*;
import com.leonel.entrevista.domain.service.UserService;
import com.leonel.entrevista.domain.utils.JwtTokenUtil;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final PhoneDao phoneDao;
    private final JwtTokenUtil jwtTokenUtil;
    @Override
    public ResponseEntity<Object> findAll() {
        List<User> users = userDao.findAll();
        if (users.isEmpty())
            return ResponseEntity.noContent().build();

        return ResponseEntity.ok(users);
    }

    @Override
    public ResponseEntity<Object> registerUser(UserDTO userDTO) {

        List<Phone> phones = Optional.ofNullable(userDTO.getPhones())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(phoneItem -> Phone.builder()
                        .number(phoneItem.getNumber())
                        .cityCode(phoneItem.getCitycode())
                        .countryCode(phoneItem.getContrycode())
                        .build())
                .collect(Collectors.toList());

        User user = User.builder()
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .password(userDTO.getPassword())
                .phones(phones)
                .isActive(Boolean.TRUE)
                .build();

        ResponseEntity<Object> userDB = persistUser(user, phones);
        HttpStatus statusCode = userDB.getStatusCode();
        if (statusCode.isError()){
            log.error("FAILED TO PERSIST USER");
            return userDB;
        }
        User body = (User) userDB.getBody();

        return ResponseEntity
                .created(URI.create("/api"))
                .body(UserApiResponse
                .builder()
                .id(body.getId())
                .created(body.getCreated())
                .modified(body.getModified())
                .lastLogin(body.getLastLogin())
                .isActive(body.getIsActive())
                .build());
    }

    @Override
    public ResponseEntity<Object> getToken(UserPassword userPassword) {
        String email = userPassword.getEmail();
        String password = userPassword.getPassword();

        if (email == null || password == null)
            return ResponseEntity.badRequest().body(new ErrorMessage(Collections.singletonList("Email o contrasena invalidos")));

        Optional<User> userOptional = userDao.findByEmail(email);
        if (userOptional.isEmpty())
            return ResponseEntity.badRequest().body(new ErrorMessage(Collections.singletonList("Email invalido")));

        String token = jwtTokenUtil.generateToken(userOptional.get());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @Override
    public ResponseEntity<Object> login(UserPassword userPassword, Map<String, Object> headers) {
        String password = userPassword.getPassword();
        String email = userPassword.getEmail();
        if (password == null || email == null)
            return ResponseEntity.badRequest().body(new ErrorMessage(Collections.singletonList("Email y contrasena son campos requeridos")));

        Optional<User> optionalUser = userDao.findByEmail(email);
        if (optionalUser.isEmpty())
            return ResponseEntity.badRequest().body(new ErrorMessage(Collections.singletonList("Email enviado no existe")));

        User user = optionalUser.get();
        if (!user.getPassword().equals(password)){
            return ResponseEntity.badRequest().body(new ErrorMessage(Collections.singletonList("Email o contrasena invalidos")));
        }

        String bearer = (String) headers.get("authorization");
        if (bearer == null)
            return ResponseEntity.badRequest().body(new ErrorMessage(Collections.singletonList("bearer token es un campo necesario")));

        try {
            Boolean tokenIsValid = jwtTokenUtil.validateToken(bearer, user);
            if (!tokenIsValid){
                return ResponseEntity.badRequest().body(new ErrorMessage(Collections.singletonList("token invalido")));
            }
        }catch (MalformedJwtException e){
            return ResponseEntity.badRequest().body(new ErrorMessage(Collections.singletonList("token invalido")));
        }


        return ResponseEntity.ok(new ErrorMessage(Collections.singletonList("Te has logueado ;D")));
    }

    @Transactional
    ResponseEntity<Object> persistUser(User user, List<Phone> phones){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<User>> validate = validator.validate(user);
        if (!validate.isEmpty()){
            List<String> errorMessage = new ArrayList<>();
            validate.forEach(error -> errorMessage.add(String.format("Error en el campo: %s, %s", error.getPropertyPath(), error.getMessage())));
            return ResponseEntity.badRequest().body(new ErrorMessage(errorMessage));
        }
        User userDB = userDao.save(user);
        phones.forEach(phone -> {
            phone.setUser(userDB);
            phoneDao.save(phone);
        });

        return ResponseEntity.created(URI.create("/api/user/register")).body(user);
    }

}
