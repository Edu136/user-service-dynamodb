package br.unibh.userservice.controller;

import br.unibh.userservice.config.TokenService;
import br.unibh.userservice.dto.*;
import br.unibh.userservice.entity.User;
import br.unibh.userservice.service.UserQueryService;
import br.unibh.userservice.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserQueryService userQueryService;
    private final TokenService tokenService;

    public AuthenticationController(AuthenticationManager authenticationManager, UserService userService, UserQueryService userQueryService, TokenService tokenService) {
        this.tokenService = tokenService;
        this.userService = userService;
        this.userQueryService = userQueryService;
        this.authenticationManager = authenticationManager;
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AutheticationDTO request) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(request.login(), request.password());
        var auth = authenticationManager.authenticate(usernamePassword);

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        User user = userQueryService.findByEmail(userDetails.getUsername())
                .or(() -> userQueryService.findByUsername(userDetails.getUsername()))
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        var token = tokenService.generateToken(user);
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }


    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        CreateUserRequestDTO trataDados =  userService.TrataDadosRegisterUserDTO(request);
        ValidationResultDTO validado = userService.ValidationResultDTO(trataDados.email(), trataDados.username());
        if(!validado.valid()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponseDTO(validado.message()));
        }
        User novoUser = userService.createUser(trataDados);
        UserResponseDTO responseDTO = userQueryService.toUserResponseDTO(novoUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
