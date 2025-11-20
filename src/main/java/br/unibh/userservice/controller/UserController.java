package br.unibh.userservice.controller;

import br.unibh.userservice.dto.*;
import br.unibh.userservice.entity.User;
import br.unibh.userservice.service.PaginatedResult;
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
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name ="Usuários", description = "Endpoints para gerenciamento de usuários")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Lista todos os usuários", description = "Retorna uma lista paginada de usuários cadastrados no sistema.")
    @GetMapping
    public ResponseEntity<PaginatedResult<UserResponseDTO>> getAllUsers(@RequestParam(required = false) String lastKey,
                                                       @RequestParam(defaultValue = "10") int limit
                                                  ) {
        PaginatedResult<UserResponseDTO> paginatedResult = userService.listUsers(lastKey, limit);

        return ResponseEntity.ok(paginatedResult);
    }

//    @Operation(summary = "Obtém detalhes do usuário por token", description = "Retorna os detalhes do usuário correspondente ao token JWT fornecido.")
//    @GetMapping("/{token}")
//    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String token) {
//        String username = userService.decodeJwtToken(token);
//        UserResponseDTO responseDTO = userService.getUserById(username);
//        return ResponseEntity.ok(responseDTO);
//    }

    @Operation(summary = "Exclui um usuário por ID", description = "Exclui o usuário correspondente ao ID fornecido.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsersById(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualiza o status do usuário", description = "Atualiza o status (ACTIVE/INACTIVE/BLOCKED) do usuário correspondente ao ID fornecido.")
    @PatchMapping("/{id}/state")
    public ResponseEntity<UserUpdateResponseDTO> updateUserStatus(@PathVariable String id, @RequestBody @Valid UpdateStatusDTO req) {
        UserUpdateResponseDTO responseDTO = userService.updateUserStatus(id, req);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Atualiza o nome de usuário", description = "Atualiza o nome de usuário do usuário correspondente ao ID fornecido.")
    @PatchMapping("/{id}/username")
    public ResponseEntity<UserUpdateResponseDTO> updateUsername(@PathVariable String id, @Valid @RequestBody UpdateUsernameDTO request) {
        UserUpdateResponseDTO responseDTO = userService.updateUsername(id, request);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Atualiza o email do usuário", description = "Atualiza o email do usuário correspondente ao ID fornecido.")
    @PatchMapping("/{id}/email")
    public ResponseEntity<UserUpdateResponseDTO> updateUserEmail(@PathVariable String id,@Valid @RequestBody UpdateEmailDTO request) {
        UserUpdateResponseDTO responseDTO = userService.updateEmail(id, request);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Atualiza a senha do usuário", description = "Atualiza a senha do usuário correspondente ao ID fornecido.")
    @PatchMapping("/{id}/password")
    public ResponseEntity<UserUpdateResponseDTO> updateUserPassword(@PathVariable String id,@Valid @RequestBody UpdatePasswordDTO request) {
        UserUpdateResponseDTO responseDTO = userService.updatePassword(id,request);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Atualiza o papel do usuário", description = "Atualiza o papel (role) do usuário correspondente ao ID fornecido.")
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserUpdateResponseDTO> updateUserRole(@PathVariable String id,@Valid @RequestBody UpdateRoleDTO request) {
        UserUpdateResponseDTO responseDTO = userService.updateRole(id, request);
        return ResponseEntity.ok(responseDTO);
    }
}
