package br.unibh.userservice.exception;

public class UserExceptions {

    public static class FaleidChangePasswordException extends RuntimeException {
        public FaleidChangePasswordException(String message) {
            super(message);
        }
    }
}
