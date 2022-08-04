package com.velb.shop.exception;

public class BasketIsEmptyException extends RuntimeException {

    public BasketIsEmptyException(String message) {
        super(message);
    }

}
