package br.unibh.userservice.controller;

import br.unibh.userservice.config.TokenService;
import br.unibh.userservice.dto.*;
import br.unibh.userservice.entity.User;
import br.unibh.userservice.service.UserQueryService;
import br.unibh.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@Tag(name ="Authentication", description = "Endpoints para usuário autenticar e registar")
public class AuthenticationController {
    private final UserService userService;

    public AuthenticationController( UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/login")
    @Operation(summary = "Autentica um usuário",
            description = "Recebe as credenciais do usuário e retorna um token JWT se a autenticação for bem-sucedida.")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AutheticationDTO request) {
        LoginResponseDTO response = userService.autenticar(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/register")
    @Operation(summary = "Registra um novo usuário" , description = "Recebe os dados do novo usuário, valida e cria uma nova conta de usuário.")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        UserResponseDTO responseDTO = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
