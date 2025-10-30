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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name ="Authentication", description = "Endpoints para usuário autenticar e registar")
public class AuthenticationController {
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
    @Operation(summary = "Autentica um usuário" , description = "Recebe as credenciais do usuário e retorna um token JWT se a autenticação for bem-sucedida.")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AutheticationDTO request) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(request.login(), request.password());
        var auth = authenticationManager.authenticate(usernamePassword);
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        User user = userService.loginComUsernameOuEmail(userDetails.getUsername());
        var token = tokenService.generateToken(user);
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }


    @PostMapping("/register")
    @Operation(summary = "Registra um novo usuário" , description = "Recebe os dados do novo usuário, valida e cria uma nova conta de usuário.")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        CreateUserRequestDTO dadosRegisterUserDTO =  userService.trataDadosRegisterUserDTO(request);
        ValidationResultDTO validado = userService.validationResultDTO(dadosRegisterUserDTO.email(), dadosRegisterUserDTO.username());
        if(!validado.valid()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponseDTO(validado.message()));
        }
        User novoUser = userService.createUser(dadosRegisterUserDTO);
        UserResponseDTO responseDTO = userQueryService.toUserResponseDTO(novoUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
