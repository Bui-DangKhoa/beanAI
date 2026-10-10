package com.example.beanai.exception;

/**
 * Ngoại lệ ném ra khi email đăng ký đã tồn tại trong hệ thống
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
