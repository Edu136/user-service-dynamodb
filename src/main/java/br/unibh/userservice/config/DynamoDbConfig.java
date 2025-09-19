package br.unibh.userservice.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfig {

    @Value("${aws.region}")
    private String awsRegion;

    // Bean para o ambiente de produção
    @Bean
    @Profile("prod")
    public DynamoDbClient dynamoDbClientProd() {
        return DynamoDbClient.builder()
                .region(Region.of(awsRegion))
                // O SDK busca credenciais automaticamente do ambiente (IAM Role, etc)
                .build();
    }

    // Bean para o ambiente local e de teste
    @Bean
    @Profile({"local", "test"})
    public DynamoDbClient dynamoDbClientLocal(@Value("${aws.dynamodb.endpoint}") String endpoint) {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(awsRegion))
                .credentialsProvider(
                    StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy"))
                )
                .build();
    }
    
    // Bean do Enhanced Client que será injetado no repositório
    // Ele depende de um DynamoDbClient (local ou prod) que o Spring irá fornecer.
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}