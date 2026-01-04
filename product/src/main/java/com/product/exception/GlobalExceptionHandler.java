//package com.product.exception;
//
//import feign.FeignException;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//    @ExceptionHandler(UserIdNotFoundException.class)
//    ResponseEntity<Object> handleUserIdNotFound(UserIdNotFoundException ex){
//        Map<String,Object> response=new HashMap<>();
//        response.put("error",ex.getMessage());
//        return ResponseEntity.status(404).body(response);
//    }
//
//    @ExceptionHandler(CategoryNotFoundException.class)
//    ResponseEntity<Object> handleCategoryNotFound(CategoryNotFoundException ex){
//        Map<String,Object> response=new HashMap<>();
//        response.put("error",ex.getMessage());
//        return ResponseEntity.status(404).body(response);
//    }
//
//    @ExceptionHandler(ProductNotFoundException.class)
//    ResponseEntity<Object> handleProductNotFound(ProductNotFoundException ex){
//        Map<String,Object> response=new HashMap<>();
//        response.put("error",ex.getMessage());
//        return ResponseEntity.status(404).body(response);
//    }
//
//    @ExceptionHandler(FeignException.class)
//    ResponseEntity<Object> handleFeignException(FeignException ex){
//        Map<String,Object> response=new HashMap<>();
//        response.put("error",ex.getMessage());
//        return ResponseEntity.ofNullable(response);
//    }
//
//    @ExceptionHandler(Exception.class)
//    ResponseEntity<Object> handleGenricException(Exception ex){
//        Map<String,Object> response=new HashMap<>();
//        response.put("error",ex.getMessage());
//        return ResponseEntity.status(500).body(response);
//    }
//
//
//
//}

package com.product.exception;

import feign.FeignException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserIdNotFoundException.class)
    public ResponseEntity<Object> handleUserIdNotFound(UserIdNotFoundException ex){
        return buildResponse(ex.getMessage(), 404);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Object> handleCategoryNotFound(CategoryNotFoundException ex){
        return buildResponse(ex.getMessage(), 404);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Object> handleProductNotFound(ProductNotFoundException ex){
        return buildResponse(ex.getMessage(), 404);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Object> handleFeignException(FeignException ex){
        // This will now show the actual status code from the gateway/inventory
        return buildResponse("External Service Error: " + ex.getMessage(), ex.status() != -1 ? ex.status() : 500);
    }

    @ExceptionHandler(InventoryServiceException.class)
    public ResponseEntity<Object> handleInventoryServiceException(InventoryServiceException ex){
        // Using your buildResponse method with 503 (Service Unavailable)
        return buildResponse(ex.getMessage(), 503);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex){
        return buildResponse(ex.getMessage(), 500);
    }

    private ResponseEntity<Object> buildResponse(String message, int status) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        return ResponseEntity.status(status).body(response);
    }
}