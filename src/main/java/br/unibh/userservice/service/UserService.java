package br.unibh.userservice.service;

import br.unibh.userservice.dto.CreateUserRequestDTO;
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

        userRepository.save(novoUsuario);

        return novoUsuario;
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o id : " + id));
    }

}
