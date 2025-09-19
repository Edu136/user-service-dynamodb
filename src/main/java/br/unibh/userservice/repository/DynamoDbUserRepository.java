package br.unibh.userservice.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import br.unibh.userservice.model.User;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository 
public class DynamoDbUserRepository implements UserRepository {

    private final DynamoDbTable<User> userTable;

    // Injeção de dependências via construtor
    public DynamoDbUserRepository(DynamoDbEnhancedClient enhancedClient,
                                @Value("${aws.dynamodb.tableName}") String tableName) {
        this.userTable = enhancedClient.table(tableName, TableSchema.fromBean(User.class));
    }

    @Override
    public void save(User user) {
        // Lógica para definir data de criação ou atualização
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(Instant.now());
        }
        user.setUpdatedAt(Instant.now());
        userTable.putItem(user);
    }

    @Override
    public Optional<User> findById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        return Optional.ofNullable(userTable.getItem(key));
    }

    @Override
    public Optional<User> deleteById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        return Optional.ofNullable(userTable.deleteItem(key));
    }
}