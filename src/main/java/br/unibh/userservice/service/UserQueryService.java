package br.unibh.userservice.service;

import br.unibh.userservice.dto.UserResponseDTO;
import br.unibh.userservice.entity.User;
import br.unibh.userservice.exception.UserExceptions;
import br.unibh.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserQueryService {

    private final UserRepository userRepository;

    public UserQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow();
    }

    public User findUserOrThrow(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException("Usuário não encontrado com o id: " + id));
    }

    public boolean userJaCadastradoUsername(String username){
        if(userRepository.existsByUsername(username)) {
            log.warn("Email já cadastrado: {}", username);
            return true;
        }
        return false;
    }

    public boolean userJaCadastradoEmail(String email){
        if(userRepository.existsByEmail(email)) {
            log.warn("Username já cadastrado: {}", email);
            return true;
        }
        return false;
    }
}
