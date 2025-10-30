package br.unibh.userservice.config;

import br.unibh.userservice.exception.TokenExceptions;
import br.unibh.userservice.exception.UserExceptions;
import br.unibh.userservice.repository.UserRepository;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final UserRepository userRepository;

    public SecurityFilter(TokenService tokenService, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        var token = this.recoverToken(request);
        try {
            if (token != null) {
                var subject = tokenService.validateToken(token);
                UserDetails user = userRepository.findByEmail(subject)
                        .or(() -> userRepository.findByUsername(subject))
                        .orElseThrow(() -> new UserExceptions.UserNotFoundException("Usuário associado ao token não foi encontrado."));

                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        }catch (TokenExpiredException e){
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expirado.");
        } catch (TokenExceptions.InvalidTokenException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido.");
        }catch (JWTVerificationException e){
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Falha na verificação do token.");
        }catch (UserExceptions.UserNotFoundException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Usuário associado ao token não foi encontrado.");
        }
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            return authHeader.replace("Bearer ", "");
        }
        return null;
    }
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonError = String.format(
                "{\"status\": %d, \"erro\": \"Falha na Autenticação\", \"mensagem\": \"%s\"}",
                status,
                message
        );

        response.getWriter().write(jsonError);
    }
}
