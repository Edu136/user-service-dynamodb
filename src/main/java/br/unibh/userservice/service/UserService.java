package br.unibh.userservice.service;

import br.unibh.userservice.dto.*;
import br.unibh.userservice.entity.User;
import br.unibh.userservice.entity.UserRole;
import br.unibh.userservice.entity.UserState;
import br.unibh.userservice.repository.UserRepository;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import static com.auth0.jwt.JWT.decode;

@Slf4j
@Service
public class UserService  {

    private final UserRepository userRepository;
    private final UserQueryService userQueryService;
    public UserService(UserRepository userRepository , UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
        this.userRepository = userRepository;
    }

    public User createUser(CreateUserRequestDTO request ) {
        log.info("Criando novo usuário ...");
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
        log.info("Deletando usuário com id: {}", id);
        User user = findUserOrThrow(id);
        userRepository.deleteById(user.getId());
    }

    public UserResponseDTO updateUserStatus(String id, String status) {
        log.info("Atualizando status do usuário com id: {} para {}", id, status);
        User user = findUserOrThrow(id);
        if(status.equalsIgnoreCase("ACTIVE")) {
            user.setStatus(UserState.ACTIVE);
        } else if(status.equalsIgnoreCase("INACTIVE")) {
            user.setStatus(UserState.INACTIVE);
        } else if(status.equalsIgnoreCase("BLOCKED")) {
            user.setStatus(UserState.BLOCKED);
        } else {
            throw new RuntimeException("Status inválido: " + status);
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return userQueryService.toUserResponseDTO(user);
    }

    private User updateUserField(String id, Consumer<User> updateAction) {
        User user = findUserOrThrow(id);
        updateAction.accept(user);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User updateUsername(String id, UpdateUsernameDTO request) {
        log.info("Atualizando username do usuário com id: {}" , id);
        return updateUserField(id, user -> user.setUsername(request.username()));
    }

    public User updateEmail(String id, UpdateEmailDTO request) {
        log.info("Atualizando email do usuário com id: {}", id);
        return updateUserField(id, user -> user.setEmail(request.email()));
    }

    public User updatePassword(String id, String senhaCriptografada) {
        log.info("Atualizando senha do usuário com id: {}", id);
        return updateUserField(id, user -> user.setPassword(senhaCriptografada));
    }

    public User updateRole(String id, UpdateRoleDTO request) {
        log.info("Atualizando role do usuário com id: {}", id);
        return updateUserField(id, user -> user.setRole(request.role()));
    }

    public CreateUserRequestDTO TrataDadosRegisterUserDTO(CreateUserRequestDTO request) {
        log.info("Tratando dados do usuário para registro...");
        String encryptedPassword = new BCryptPasswordEncoder().encode(request.password());
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();
        return new CreateUserRequestDTO(username, email, encryptedPassword);
    }

    public String TrataAlteracaoSenhaDTO(String senha){
        log.info("Tratando dados da alteração de senha...");
        return new BCryptPasswordEncoder().encode(senha);
    }


    public ValidationResultDTO ValidationResultDTO(String email, String username) {
        log.info("Validando dados do usuário para registro...");
        if(userRepository.existsByEmail(email) && userRepository.existsByUsername(username)) {
            log.warn("Email e Username já cadastrados: {} , {}", email, username);
            return new ValidationResultDTO(false, "Email e Username já cadastrados.");
        }
        if(userRepository.existsByEmail(email)) {
            log.warn("Email já cadastrado: {}", email);
            return new ValidationResultDTO(false, "Email já cadastrado.");
        }
        if(userRepository.existsByUsername(username)) {
            log.warn("Username já cadastrado: {}", username);
            return new ValidationResultDTO(false, "Username já cadastrado.");
        }
        return new ValidationResultDTO(true , "OK.");
    }

    private User findUserOrThrow(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o id: " + id));
    }

    public String decodeJwtToken(String token) {
        DecodedJWT decodedJWT = decode(token);
        return decodedJWT.getSubject();
    }

    public User loginComUsernameOuEmail(String login) {
        User user = null;

        if (userRepository.existsByEmail(login)) {
            user = userQueryService.findByEmail(login);
        } else if (userRepository.existsByUsername(login)) {
            user = userQueryService.findByUsername(login);
        }

        if (user == null) {
            log.warn("Usuário não encontrado com o login: {}", login);
            throw new RuntimeException("Usuário não encontrado com o login: " + login);
        }

        if (!userValidStatus(user)) {
            log.warn("Usuário com login {} está inativo ou bloqueado.", login);
            throw new RuntimeException("Usuário com login " + login + " está inativo ou bloqueado.");
        }

        return user;
    }

    public boolean userValidStatus(User user) {
        return user.getStatus() == UserState.ACTIVE;
    }
}
