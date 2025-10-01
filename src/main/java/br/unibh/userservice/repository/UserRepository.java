package br.unibh.userservice.repository;

import java.util.List;
import java.util.Optional;

import br.unibh.userservice.entity.User;

/**
 * Interface que define as operações de persistência para a entidade User.
 */
public interface UserRepository {

    /**
     * Salva um usuário novo ou atualiza um existente.
     * Atribui/atualiza os timestamps de criação e atualização.
     *
     * @param user O objeto User a ser salvo.
     */
    void save(User user);

    /**
     * Busca um usuário pelo seu ID (chave de partição).
     *
     * @param id O ID único do usuário.
     * @return um Optional contendo o usuário se encontrado, ou um Optional vazio caso contrário.
     */
    Optional<User> findById(String id);

    /**
     * Deleta um usuário da base de dados pelo seu ID.
     *
     * @param id O ID do usuário a ser deletado.
     * @return um Optional contendo o usuário que foi deletado, ou Optional.empty() se não foi encontrado.
     */
    Optional<User> deleteById(String id);

    List<User> findAll();
}