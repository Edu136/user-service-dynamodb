package br.unibh.userservice.exception;

public class TokenExceptions {

    public static class FailedGenerationTokenException extends RuntimeException {
        public FailedGenerationTokenException(String message) {
            super(message);
        }
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }
}
