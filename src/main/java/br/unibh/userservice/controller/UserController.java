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
import org.springdoc.core.converters.models.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

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

    @Operation(summary = "Lista todos os usuários", description = "Retorna uma lista paginada de usuários cadastrados no sistema.")
    @GetMapping
    public ResponseEntity<PaginatedResult<UserResponseDTO>> getAllUsers(@RequestParam(required = false) String lastKey,
                                                       @RequestParam(defaultValue = "10") int limit
                                                  ) {
        PaginatedResult<User> paginatedResult = userService.listUsers(lastKey, limit);

        List<UserResponseDTO> dtos = paginatedResult.getItems().stream()
                .map(userQueryService::toUserResponseDTO)
                .toList();

        PaginatedResult<UserResponseDTO> response = new PaginatedResult<>(dtos, paginatedResult.getNextKey());

        return ResponseEntity.ok(response);
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
    @PatchMapping("/{id}/state")
    public ResponseEntity<UserResponseDTO> updateUserStatus(@PathVariable String id, @RequestBody @Valid UpdateStatusDTO req) {
        User updateUser = userService.updateUserStatus(id, req);
        UserResponseDTO responseDTO = userQueryService.toUserResponseDTO(updateUser);
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
