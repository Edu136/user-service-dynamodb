package br.unibh.userservice.service;

import br.unibh.userservice.dto.*;
import br.unibh.userservice.entity.User;
import br.unibh.userservice.entity.UserState;
import br.unibh.userservice.exception.UserExceptions;
import br.unibh.userservice.mapper.UserMapper;
import br.unibh.userservice.repository.UserRepository;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository , UserQueryService userQueryService, DynamoDbTable<User> userTable, UserMapper userMapper) {
        this.userMapper = userMapper;
        this.userTable = userTable;
        this.userQueryService = userQueryService;
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public UserResponseDTO createUser(CreateUserRequestDTO request ) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();

        ValidationResultDTO validado = this.validationResultDTO(email, username);
        if (!validado.valid()) {
            throw new UserExceptions.UserAlreadyExistsException(validado.message());
        }

        String encryptedPassword = passwordEncoder.encode(request.password());

        User novoUser = userMapper.toEntity(request, encryptedPassword);

        userRepository.save(novoUser);

        return userMapper.toResponseDto(novoUser);
    }

    public void deleteUser(String id) {
        log.info("Deletando usuário com id: {}", id);
        User user = userQueryService.findUserOrThrow(id);
        userRepository.deleteById(user.getId());
    }

    public UserResponseDTO updateUserField(String id, Consumer<User> updateAction) {
        User user = userQueryService.findUserOrThrow(id);
        updateAction.accept(user);
        user.setUpdatedAt(LocalDateTime.now());

        User usuarioAtualizado = userRepository.save(user);

        return userMapper.toResponseDto(usuarioAtualizado);
    }

    public UserResponseDTO updateUsername(String id, UpdateUsernameDTO request) {
        if(userQueryService.userJaCadastradoUsername(request.username().trim())) {
            throw new UserExceptions.UserAlreadyExistsException("Username já cadastrado: " + request.username());
        }

        log.info("Atualizando username do usuário com id: {}" , id);
        return updateUserField(id, user -> user.setUsername(request.username()));
    }

    public UserResponseDTO updateEmail(String id, UpdateEmailDTO request) {
        if(userQueryService.userJaCadastradoEmail(request.email().trim())) {
            throw new UserExceptions.UserAlreadyExistsException("Email já cadastrado: " + request.email());
        }

        log.info("Atualizando email do usuário com id: {}", id);
        return updateUserField(id, user -> user.setEmail(request.email()));
    }

    public UserResponseDTO updatePassword(String id, UpdatePasswordDTO request) {
        log.info("Atualizando senha do usuário com id: {}", id);

        if(!validaSenhaAntiga(id, request.oldPassword())){
            throw new UserExceptions.InvalidOldPasswordException("Senha antiga inválida para o usuário com id: " + id);
        }

        if(senhasIguais(id, request.newPassword())){
            throw new UserExceptions.InvalidNewPasswordException("A nova senha não pode ser igual a ultima senha.");
        }

        User userTrocandoSenha = userQueryService.findUserOrThrow(id);

        List<String> senhasAntigas = userTrocandoSenha.getPasswordHistory();

        for(String hashAntigo : senhasAntigas){
            if(passwordEncoder.matches(request.newPassword() , hashAntigo)){
                throw new UserExceptions.InvalidOldPasswordException("A nova senha não pode ser igual a nenhuma das últimas 3 senhas utilizadas.");
            }
        }

        if(senhasAntigas.size() >= 3){
            senhasAntigas.remove(0);
        }

        senhasAntigas.add(userTrocandoSenha.getPassword());

        String senhaEmHash = trataAlteracaoSenhaDTO(request.newPassword());

        log.info("Historico de todas as senhas antigas do usuário com id: {}: {}", id, senhasAntigas);

        return updateUserField(id, user -> {
                user.setPassword(senhaEmHash);
                user.setPasswordHistory(senhasAntigas);
                });
    }

    public UserResponseDTO updateRole(String id, UpdateRoleDTO request) {
        log.info("Atualizando role do usuário com id: {}", id);
        return updateUserField(id, user -> user.setRole(request.role()));
    }

    public UserResponseDTO updateUserStatus(String id, UpdateStatusDTO req) {
        log.info("Atualizando status do usuário com id: {} para {}", id, req.userState());
        return updateUserField(id, user -> user.setStatus(req.userState()));
    }

    public CreateUserRequestDTO trataDadosRegisterUserDTO(CreateUserRequestDTO request) {
        log.info("Tratando dados do usuário para registro...");
        String encryptedPassword = passwordEncoder.encode(request.password());
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();
        return new CreateUserRequestDTO(username, email, encryptedPassword);
    }

    private boolean validaSenhaAntiga(String id, String senhaAntiga){
        User user = userQueryService.findUserOrThrow(id);
        return passwordEncoder.matches(senhaAntiga, user.getPassword());
    }

    private String trataAlteracaoSenhaDTO(String senha){
        log.info("Tratando dados da alteração de senha...");
        return passwordEncoder.encode(senha);
    }

    private boolean senhasIguais(String id, String senha){
        User user = userQueryService.findUserOrThrow(id);
        String senhaAtualHash = user.getPassword();
        return passwordEncoder.matches(senha, senhaAtualHash);
    }

    public ValidationResultDTO validationResultDTO(String email, String username) {
        log.info("Validando dados do usuário para registro...");
        if(userRepository.existsByEmail(email) && userRepository.existsByUsername(username)) {
            log.warn("Email e Username já cadastrados: {} , {}", email, username);
            return new ValidationResultDTO(false, "Email e Username já cadastrados.");
        }
        if(userQueryService.userJaCadastradoEmail(email)) {
            return new ValidationResultDTO(false, "Email já cadastrado.");
        }
        if(userQueryService.userJaCadastradoUsername(username)) {
            return new ValidationResultDTO(false, "Username já cadastrado.");
        }
        return new ValidationResultDTO(true , "OK.");
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
            throw new UserExceptions.UserLoginNotFoundException("Usuário não encontrado com o login: " + login);
        }

        if (!userValidStatus(user)) {
            log.warn("Usuário com login {} está inativo ou bloqueado.", login);
            throw new UserExceptions.UserStateException("Usuário com login " + login + " está inativo ou bloqueado.");
        }

        return user;
    }

    public boolean userValidStatus(User user) {
        return user.getStatus() == UserState.ACTIVE;
    }

    public PaginatedResult<UserResponseDTO> listUsers(String lastKey, int limit) {
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

        List<UserResponseDTO> dtos = users.stream()
                .map(userMapper::toResponseDto)
                .toList();

        return new PaginatedResult<>(dtos, nextKey);
    }

    public UserResponseDTO getUserById(String username) {
        User user = userQueryService.findByUsername(username);
        return userMapper.toResponseDto(user);
    }
}
