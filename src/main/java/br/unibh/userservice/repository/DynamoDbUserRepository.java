package br.unibh.userservice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import br.unibh.userservice.entity.User;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository 
public class DynamoDbUserRepository implements UserRepository {

    private final DynamoDbTable<User> userTable;

    public DynamoDbUserRepository(DynamoDbEnhancedClient enhancedClient,
                                @Value("${aws.dynamodb.tableName}") String tableName) {
        this.userTable = enhancedClient.table(tableName, TableSchema.fromBean(User.class));
    }

    @Override
    public void save(User user) {
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        user.setUpdatedAt(LocalDateTime.now());
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

    @Override
    public List<User> findAll() {
        return userTable.scan().items().stream().toList();
    }
}