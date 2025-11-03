package br.unibh.userservice.service;

import br.unibh.userservice.config.TokenService;
import br.unibh.userservice.dto.*;
import br.unibh.userservice.entity.User;
import br.unibh.userservice.entity.UserRole;
import br.unibh.userservice.entity.UserState;
import br.unibh.userservice.exception.UserExceptions;
import br.unibh.userservice.mapper.UserMapper;
import br.unibh.userservice.repository.UserRepository;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final TokenService tokenService;

    public UserService(UserRepository userRepository , UserQueryService userQueryService, DynamoDbTable<User> userTable, UserMapper userMapper, TokenService tokenService) {
        this.userMapper = userMapper;
        this.userTable = userTable;
        this.userQueryService = userQueryService;
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.tokenService = tokenService;
    }

    public UserResponseDTO createUser(CreateUserRequestDTO request ) {
        String username = request.username().trim().toLowerCase();
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
        checkAdminOrSelf(id);
        log.info("Deletando usuário com id: {}", id);
        User user = userQueryService.findUserOrThrow(id);
        userRepository.deleteById(user.getId());
    }

    public UserUpdateResponseDTO updateUserField(String id, Consumer<User> updateAction) {
        User user = userQueryService.findUserOrThrow(id);
        updateAction.accept(user);
        user.setUpdatedAt(LocalDateTime.now());

        User usuarioAtualizado = userRepository.save(user);

        var token = tokenService.generateToken(user);

        return userMapper.toUpdateResponseDto(usuarioAtualizado,token);
    }

    public UserUpdateResponseDTO updateUsername(String id, UpdateUsernameDTO request) {
        checkAdminOrSelf(id);
        String username = request.username().trim().toLowerCase();
        if(userQueryService.userJaCadastradoUsername(username)) {
            throw new UserExceptions.UserAlreadyExistsException("Username já cadastrado: " + request.username());
        }

        log.info("Atualizando username do usuário com id: {}" , id);
        return updateUserField(id, user -> user.setUsername(username));
    }

    public UserUpdateResponseDTO updateEmail(String id, UpdateEmailDTO request) {
        checkAdminOrSelf(id);
        String email = request.email().trim().toLowerCase();
        if(userQueryService.userJaCadastradoEmail(email)) {
            throw new UserExceptions.UserAlreadyExistsException("Email já cadastrado: " + request.email());
        }

        log.info("Atualizando email do usuário com id: {}", id);
        return updateUserField(id, user -> user.setEmail(email));
    }

    public UserUpdateResponseDTO updatePassword(String id, UpdatePasswordDTO request) {
        checkAdminOrSelf(id);
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

    public UserUpdateResponseDTO updateRole(String id, UpdateRoleDTO request) {
        log.info("Atualizando role do usuário com id: {}", id);
        return updateUserField(id, user -> user.setRole(request.role()));
    }

    public UserUpdateResponseDTO updateUserStatus(String id, UpdateStatusDTO req) {
        log.info("Atualizando status do usuário com id: {} para {}", id, req.userState());
        return updateUserField(id, user -> user.setStatus(req.userState()));
    }

    private void checkAdminOrSelf(String targetUserId) {
        User usuarioLogado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (usuarioLogado.getRole() == UserRole.ADMIN) {
            return;
        }

        if (usuarioLogado.getId().equals(targetUserId)) {
            return;
        }

        throw new AccessDeniedException("Acesso negado: você só pode alterar seus próprios dados.");
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
