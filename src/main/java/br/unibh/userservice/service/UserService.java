package br.unibh.userservice.service;

import br.unibh.userservice.dto.*;
import br.unibh.userservice.entity.User;
import br.unibh.userservice.entity.UserRole;
import br.unibh.userservice.entity.UserState;
import br.unibh.userservice.entity.builders.UserBuilder;
import br.unibh.userservice.repository.UserRepository;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.auth0.jwt.JWT.decode;

@Slf4j
@Service
public class UserService  {

    private final UserRepository userRepository;
    private final UserQueryService userQueryService;
    private final DynamoDbTable<User> userTable;

    public UserService(UserRepository userRepository , UserQueryService userQueryService, DynamoDbTable<User> userTable) {
        this.userTable = userTable;
        this.userQueryService = userQueryService;
        this.userRepository = userRepository;
    }

    public User createUser(CreateUserRequestDTO request ) {
        log.info("Criando novo usuário ...");
        User novoUsuario = new UserBuilder()
                .id(java.util.UUID.randomUUID().toString())
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .status(UserState.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .role(UserRole.USER)
                .build();
        return userRepository.save(novoUsuario);
    }

    public void deleteUser(String id) {
        log.info("Deletando usuário com id: {}", id);
        User user = findUserOrThrow(id);
        userRepository.deleteById(user.getId());
    }

    private User updateUserField(String id, Consumer<User> updateAction) {
        User user = findUserOrThrow(id);
        updateAction.accept(user);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    //Atulizações parciais dos campos do usuário

    public User updateUsername(String id, UpdateUsernameDTO request) {
        if(userJaCadastradoUsername(request.username().trim())) {
            throw new RuntimeException("Username já cadastrado: " + request.username());
        }

        log.info("Atualizando username do usuário com id: {}" , id);
        return updateUserField(id, user -> user.setUsername(request.username()));
    }

    public User updateEmail(String id, UpdateEmailDTO request) {
        if(userJaCadastradoEmail(request.email().trim().toLowerCase())) {
            throw new RuntimeException("Email já cadastrado: " + request.email());
        }

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

    public User updateUserStatus(String id, UpdateStatusDTO req) {
        log.info("Atualizando status do usuário com id: {} para {}", id, req.userState());
        return updateUserField(id, user -> user.setStatus(req.userState()));
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
        if(userJaCadastradoEmail(email)) {
            return new ValidationResultDTO(false, "Email já cadastrado.");
        }
        if(userJaCadastradoUsername(username)) {
            return new ValidationResultDTO(false, "Username já cadastrado.");
        }
        return new ValidationResultDTO(true , "OK.");
    }

    private boolean userJaCadastradoUsername(String username){
        if(userRepository.existsByUsername(username)) {
            log.warn("Email já cadastrado: {}", username);
            return true;
        }
        return false;
    }

    private boolean userJaCadastradoEmail(String email){
        if(userRepository.existsByEmail(email)) {
            log.warn("Username já cadastrado: {}", email);
            return true;
        }
        return false;
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

    public PaginatedResult<User> listUsers(String lastKey, int limit) {
        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .limit(limit)
                .exclusiveStartKey(lastKey != null
                        ? Map.of("id", AttributeValue.fromS(lastKey))
                        : null)
                .build();

        var scanResult = userTable.scan(request);

        var pageIterator = scanResult.iterator();
        if (!pageIterator.hasNext()) {
            return new PaginatedResult<>(List.of(), null);
        }

        var page = pageIterator.next();
        List<User> users = page.items();

        String nextKey = null;
        if (page.lastEvaluatedKey() != null && !page.lastEvaluatedKey().isEmpty()) {
            nextKey = page.lastEvaluatedKey().get("id").s();
        }

        return new PaginatedResult<>(users, nextKey);
    }


}
