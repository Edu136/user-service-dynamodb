package br.unibh.userservice.controller;

import br.unibh.userservice.dto.*;
import br.unibh.userservice.entity.User;
import br.unibh.userservice.service.UserQueryService;
import br.unibh.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth")
@Tag(name ="Usuários", description = "Endpoints para gerenciamento de usuários")
public class UserController {
    private final UserQueryService userQueryService;
    private final UserService userService;
    public UserController(UserQueryService userQueryService , UserService userService) {
        this.userService = userService;
        this.userQueryService = userQueryService;
    }

    @Operation(summary = "Lista todos os usuários", description = "Retorna uma lista de todos os usuários cadastrados no sistema.")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userQueryService.GetAllUsersResponse());
    }

    @Operation(summary = "Obtém detalhes do usuário por token", description = "Retorna os detalhes do usuário correspondente ao token JWT fornecido.")
    @GetMapping("/{token}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String token) {
        String idUser = userService.decodeJwtToken(token);
        User user = userQueryService.findByUsername(idUser);
        UserResponseDTO responseDTO = userQueryService.toUserResponseDTO(user);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Exclui um usuário por ID", description = "Exclui o usuário correspondente ao ID fornecido.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsersById(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualiza o status do usuário", description = "Atualiza o status (ACTIVE/INACTIVE/BLOCKED) do usuário correspondente ao ID fornecido.")
    @PatchMapping("/{id}/{status}")
    public ResponseEntity<UserResponseDTO> updateUserStatus(@PathVariable String id, @PathVariable String status) {
        UserResponseDTO responseDTO= userService.updateUserStatus(id, status);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Atualiza o nome de usuário", description = "Atualiza o nome de usuário do usuário correspondente ao ID fornecido.")
    @PatchMapping("/{id}/username")
    public ResponseEntity<UserResponseDTO> updateUsername(@PathVariable String id, @Valid @RequestBody UpdateUsernameDTO request) {
        User updatedUser = userService.updateUsername(id, request);
        UserResponseDTO responseDTO = userQueryService.toUserResponseDTO(updatedUser);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Atualiza o email do usuário", description = "Atualiza o email do usuário correspondente ao ID fornecido.")
    @PatchMapping("/{id}/email")
    public ResponseEntity<UserResponseDTO> updateUserEmail(@PathVariable String id,@Valid @RequestBody UpdateEmailDTO request) {
        User updatedUser = userService.updateEmail(id, request);
        UserResponseDTO responseDTO = userQueryService.toUserResponseDTO(updatedUser);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Atualiza a senha do usuário", description = "Atualiza a senha do usuário correspondente ao ID fornecido.")
    @PatchMapping("/{id}/password")
    public ResponseEntity<UserResponseDTO> updateUserPassword(@PathVariable String id,@Valid @RequestBody UpdatePasswordDTO request) {
        String senhaCriptografada = userService.TrataAlteracaoSenhaDTO(request.password());
        User updatedUser = userService.updatePassword(id, senhaCriptografada);
        UserResponseDTO responseDTO = userQueryService.toUserResponseDTO(updatedUser);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Atualiza o papel do usuário", description = "Atualiza o papel (role) do usuário correspondente ao ID fornecido.")
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponseDTO> updateUserRole(@PathVariable String id,@Valid @RequestBody UpdateRoleDTO request) {
        User updatedUser = userService.updateRole(id, request);
        UserResponseDTO responseDTO = userQueryService.toUserResponseDTO(updatedUser);
        return ResponseEntity.ok(responseDTO);
    }
}
