package br.unibh.userservice.controller;

import br.unibh.userservice.dto.*;
import br.unibh.userservice.entity.User;
import br.unibh.userservice.service.UserQueryService;
import br.unibh.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserQueryService userQueryService;
    private final UserService userService;
    public UserController(UserQueryService userQueryService , UserService userService) {
        this.userService = userService;
        this.userQueryService = userQueryService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userQueryService.GetAllUsersResponse());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsersById(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable String id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable String id) {
        userService.activateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/username")
    public ResponseEntity<UserResponseDTO> updateUsername(@PathVariable String id, @Valid @RequestBody UpdateUsernameDTO request) {
        User updatedUser = userService.updateUsername(id, request);
        UserResponseDTO responseDTO = userQueryService.toUserResponseDTO(updatedUser);
        return ResponseEntity.ok(responseDTO);
    }

    @PatchMapping("/{id}/email")
    public ResponseEntity<UserResponseDTO> updateUserEmail(@PathVariable String id,@Valid @RequestBody UpdateEmailDTO request) {
        User updatedUser = userService.updateEmail(id, request);
        UserResponseDTO responseDTO = userQueryService.toUserResponseDTO(updatedUser);
        return ResponseEntity.ok(responseDTO);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<UserResponseDTO> updateUserPassword(@PathVariable String id,@Valid @RequestBody UpdatePasswordDTO request) {
        User updatedUser = userService.updatePassword(id, request);
        UserResponseDTO responseDTO = userQueryService.toUserResponseDTO(updatedUser);
        return ResponseEntity.ok(responseDTO);
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponseDTO> updateUserRole(@PathVariable String id,@Valid @RequestBody UpdateRoleDTO request) {
        User updatedUser = userService.updateRole(id, request);
        UserResponseDTO responseDTO = userQueryService.toUserResponseDTO(updatedUser);
        return ResponseEntity.ok(responseDTO);
    }
}
