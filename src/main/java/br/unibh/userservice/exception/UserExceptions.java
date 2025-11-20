package br.unibh.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class UserExceptions {

    public static class InvalidOldPasswordException extends RuntimeException {
        public InvalidOldPasswordException(String message) {
            super(message);
        }
    }

    public static class InvalidNewPasswordException extends RuntimeException {
        public InvalidNewPasswordException(String message) {
            super(message);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class UserLoginNotFoundException extends RuntimeException {
        public UserLoginNotFoundException(String message) {
            super(message);
        }
    }

    public static class UserStateException extends RuntimeException {
        public UserStateException(String message) {
            super(message);
        }
    }

    public static class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class PasswordOrLoginInvalidException extends RuntimeException {
        public PasswordOrLoginInvalidException(String message) {
            super(message);
        }
    }
}
