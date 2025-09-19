package br.unibh.userservice.repository;

import java.time.Instant;
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

import br.unibh.userservice.model.User;
import br.unibh.userservice.model.UserStatus;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;

@SpringBootTest // Carrega o contexto completo do Spring Boot
@ActiveProfiles("test") // Ativa o perfil 'test', lendo application-test.properties
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Permite métodos não estáticos para @BeforeAll
class DynamoDbUserRepositoryTest {

    @Autowired // Spring injeta o repositório que já está configurado
    private UserRepository userRepository;

    @Autowired // Precisamos do client para criar/deletar a tabela de teste
    private DynamoDbClient dynamoDbClient;

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${aws.dynamodb.tableName}") // Pega o nome da tabela do application-test.properties
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
        newUser.setPasswordHash("hashed_password_example_123"); // Alterar para ByCrypt
        newUser.setStatus(UserStatus.ACTIVE);

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
        user.setPasswordHash("another_hash_456");
        user.setStatus(UserStatus.INACTIVE);

        userRepository.save(user);
        Instant firstUpdate = userRepository.findById("user-456").get().getUpdatedAt();

        Thread.sleep(10); // Pequena pausa para garantir que o timestamp mude

        // Act
        user.setUsername("janedoe_updated");
        userRepository.save(user);
        Instant secondUpdate = userRepository.findById("user-456").get().getUpdatedAt();

        // Assert
        assertNotNull(firstUpdate);
        assertNotNull(secondUpdate);
        assertTrue(secondUpdate.isAfter(firstUpdate), "A data de atualização deveria ser mais recente");
    }
}