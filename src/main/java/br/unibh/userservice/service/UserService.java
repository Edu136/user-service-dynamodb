package br.unibh.userservice.service;

import br.unibh.userservice.dto.CreateUserRequestDTO;
import br.unibh.userservice.dto.UpdateEmailDTO;
import br.unibh.userservice.dto.UpdatePasswordDTO;
import br.unibh.userservice.dto.UpdateUsernameDTO;
import br.unibh.userservice.entity.User;
import br.unibh.userservice.entity.UserState;
import br.unibh.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class UserService {

    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(CreateUserRequestDTO request) {
        User novoUsuario = new User();
        novoUsuario.setId(java.util.UUID.randomUUID().toString());
        novoUsuario.setUsername(request.username());
        novoUsuario.setEmail(request.email());
        novoUsuario.setPasswordHash(request.password());
        novoUsuario.setStatus(UserState.ACTIVE);
        novoUsuario.setCreatedAt(LocalDateTime.now());
        novoUsuario.setUpdatedAt(LocalDateTime.now());
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
        user.setPasswordHash(request.password());
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
        user.setPasswordHash(request.password());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }

    private User findUserOrThrow(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o id: " + id));
    }
}
