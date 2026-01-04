package com.cart.exception;

public class ProductServiceDownException extends RuntimeException {
    public ProductServiceDownException(String message){
        super(message);
    }
}
