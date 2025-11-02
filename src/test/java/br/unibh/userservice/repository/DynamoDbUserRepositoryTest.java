package br.unibh.userservice.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.unibh.userservice.entity.User;
import br.unibh.userservice.entity.UserState;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;

@SpringBootTest
@ActiveProfiles("prod")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamoDbUserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${aws.dynamodb.tableName}")
    private String testTableName;

    @BeforeAll
    void setupTable() {
        DynamoDbTable<User> userTestTable = enhancedClient.table(testTableName, TableSchema.fromBean(User.class));
        try {
            userTestTable.createTable(builder -> builder
                    .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(1L).writeCapacityUnits(1L).build())
            );
            dynamoDbClient.waiter().waitUntilTableExists(b -> b.tableName(testTableName));
        } catch (ResourceInUseException e) {
            // Tabela já existe, não faz nada
        }
    }

    @AfterAll
    void tearDownTable() {
        dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName(testTableName).build());
    }

    @Test
    @DisplayName("Deve salvar um novo usuário com todos os campos e encontrá-lo")
    void shouldSaveAndFindUser() {
        // Arrange
        User newUser = new User();
        newUser.setId("user-123");
        newUser.setUsername("johndoe");
        newUser.setEmail("john.doe@test.com");
        newUser.setPassword("hashed_password_example_123");
        newUser.setStatus(UserState.ACTIVE);

        // Act
        userRepository.save(newUser);
        Optional<User> foundUserOpt = userRepository.findById("user-123");

        // Assert
        assertTrue(foundUserOpt.isPresent());
        User foundUser = foundUserOpt.get();

        assertEquals(newUser.getId(), foundUser.getId());
        assertEquals(newUser.getUsername(), foundUser.getUsername());
        assertEquals(newUser.getStatus(), foundUser.getStatus());
        assertNotNull(foundUser.getCreatedAt()); // O método save deve ter preenchido
        assertNotNull(foundUser.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve atualizar data 'updatedAt' ao salvar um usuário existente")
    void shouldUpdateTimestampOnSave() throws InterruptedException {
        // Arrange
        User user = new User();
        user.setId("user-456");
        user.setUsername("janedoe");
        user.setEmail("jane.doe@test.com");
        user.setPassword("another_hash_456");
        user.setStatus(UserState.INACTIVE);

        userRepository.save(user);
        LocalDateTime firstUpdate = userRepository.findById("user-456").get().getUpdatedAt();

        Thread.sleep(10);

        // Act
        user.setUsername("janedoe_updated");
        userRepository.save(user);
        LocalDateTime secondUpdate = userRepository.findById("user-456").get().getUpdatedAt();

        // Assert
        assertNotNull(firstUpdate);
        assertNotNull(secondUpdate);
        assertTrue(secondUpdate.isAfter(firstUpdate), "A data de atualização deveria ser mais recente");
    }


    @Test
    @DisplayName("Deve mostrar todos os usuários salvos")
    void shouldFindAllUsers() {
        // Arrange
        User user1 = new User();
        user1.setId("user-789");
        user1.setUsername("alice");
        user1.setEmail("alice.doe@test.com");
        user1.setPassword("hash_alice_789");
        user1.setStatus(UserState.ACTIVE);
        userRepository.save(user1);
        User user2 = new User();
        user2.setId("user-101");
        user2.setUsername("bob");
        user2.setEmail("bob.doe@test.com");
        user2.setPassword("hash_bob_101");
        user2.setStatus(UserState.INACTIVE);
        userRepository.save(user2);

        // Act
        var allUsers = userRepository.findAll();

        // Assert
        assertNotNull(allUsers);
        assertTrue(allUsers.size() >= 2, "Deveria haver pelo menos dois usuários");
    }

    @Test
    @DisplayName("Deve alterar o status do usuário")
    void shouldChangeUserStatus() {
        // Arrange
        User user = new User();
        user.setId("user-202");
        user.setUsername("charlie");
        user.setEmail("charlie@teste.com");
        user.setPassword("hash_charlie_202");
        user.setStatus(UserState.ACTIVE);
        userRepository.save(user);

        // Act
        user.setStatus(UserState.INACTIVE);
        userRepository.save(user);
        User updatedUser = userRepository.findById("user-202").get();

        // Assert
        assertEquals(UserState.INACTIVE, updatedUser.getStatus());
    }
}