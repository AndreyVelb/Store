package com.velb.shop.exception;

public class InsufficientProductQuantityException extends RuntimeException {

    public InsufficientProductQuantityException(String message) {
        super(message);
    }

}
