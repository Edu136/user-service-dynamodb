package br.unibh.userservice.config;

import br.unibh.userservice.entity.User;
import br.unibh.userservice.exception.TokenExceptions;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;


@Service
public class TokenService {

    @Value("${jwt.token.secret}")
    private String secret;

    public String generateToken(User user) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getUsername())
                    .withExpiresAt(generateExpirationDate())
                    .withClaim("roles", user.getRole().toString())
                    .sign(algorithm);
            return token;
        }catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String validateToken(String token) {

        Algorithm algorithm = Algorithm.HMAC256(secret);
        var verifier = JWT.require(algorithm)
                .withIssuer("auth-api")
                .build();
        var decodedJWT = verifier.verify(token);
        return decodedJWT.getSubject();

    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(java.time.ZoneOffset.of("-03:00"));
    }
}
