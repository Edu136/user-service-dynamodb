package br.unibh.userservice.service;

import br.unibh.userservice.dto.*;
import br.unibh.userservice.entity.User;
import br.unibh.userservice.entity.UserRole;
import br.unibh.userservice.entity.UserState;
import br.unibh.userservice.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class UserService  {

    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(CreateUserRequestDTO request ) {
        User novoUsuario = new User();
        novoUsuario.setId(java.util.UUID.randomUUID().toString());
        novoUsuario.setUsername(request.username());
        novoUsuario.setEmail(request.email());
        novoUsuario.setPassword(request.password());
        novoUsuario.setStatus(UserState.ACTIVE);
        novoUsuario.setCreatedAt(LocalDateTime.now());
        novoUsuario.setUpdatedAt(LocalDateTime.now());
        novoUsuario.setRole(UserRole.USER);
        return userRepository.save(novoUsuario);
    }

    public void deleteUser(String id) {
        User user = findUserOrThrow(id);
        userRepository.deleteById(user.getId());
    }

    public void deactivateUser(String id) {
        User user = findUserOrThrow(id);
        user.setStatus(UserState.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void activateUser(String id) {
        User user = findUserOrThrow(id);
        user.setStatus(UserState.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public User updateUser(String id, CreateUserRequestDTO request) {
        User user = findUserOrThrow(id);
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    public User updateUsername(String id, UpdateUsernameDTO request) {
        User user = findUserOrThrow(id);
        user.setUsername(request.username());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    public User updateEmail(String id, UpdateEmailDTO request) {
        User user = findUserOrThrow(id);
        user.setEmail(request.email());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    public User updatePassword(String id, UpdatePasswordDTO request) {
        User user = findUserOrThrow(id);
        user.setPassword(request.password());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    public User updateRole(String id, UpdateRoleDTO request) {
        User user = findUserOrThrow(id);
        user.setRole(request.role());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    public CreateUserRequestDTO TrataDadosRegisterUserDTO(CreateUserRequestDTO request) {
        String encryptedPassword = new BCryptPasswordEncoder().encode(request.password());
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();
        return new CreateUserRequestDTO(username, email, encryptedPassword);
    }


    public ValidationResultDTO ValidationResultDTO(String email, String username) {
        if(userRepository.existsByEmail(email) && userRepository.existsByUsername(username)) {
            return new ValidationResultDTO(false, "Email e Username já cadastrados.");
        }
        if(userRepository.existsByEmail(email)) {
            return new ValidationResultDTO(false, "Email já cadastrado.");
        }
        if(userRepository.existsByUsername(username)) {
            return new ValidationResultDTO(false, "Username já cadastrado.");
        }
        return new ValidationResultDTO(true , "OK.");
    }

    private User findUserOrThrow(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o id: " + id));
    }
}
