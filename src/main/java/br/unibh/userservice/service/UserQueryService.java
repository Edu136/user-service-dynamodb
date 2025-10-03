package br.unibh.userservice.service;

import br.unibh.userservice.dto.UserResponseDTO;
import br.unibh.userservice.entity.User;
import br.unibh.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserQueryService {

    private final UserRepository userRepository;
    public UserQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public List<User> GetAllUsersResponse () {
        return userRepository.findAll();
    }

    public UserResponseDTO toUserResponseDTO(User user) {
       return new UserResponseDTO(
               user.getId(),
               user.getUsername(),
               user.getEmail(),
               user.getStatus() ,
               user.getCreatedAt(),
               user.getUpdatedAt());
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
