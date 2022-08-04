package com.velb.shop.handler;

import com.velb.shop.exception.BasketElementNotFoundException;
import com.velb.shop.exception.BasketIsEmptyException;
import com.velb.shop.exception.InsufficientProductQuantityException;
import com.velb.shop.exception.OrderNotFoundException;
import com.velb.shop.exception.ProductChangingException;
import com.velb.shop.exception.ProductNotFoundException;
import com.velb.shop.exception.TotalRuntimeException;
import com.velb.shop.exception.UserAlreadyExistsException;
import com.velb.shop.exception.UserNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
@RequestMapping(consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatus status,
                                                                  @NonNull WebRequest request) {
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        StringBuilder stringBuilder = new StringBuilder();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(objectError -> stringBuilder.append(objectError.getDefaultMessage()));
        return new ResponseEntity<>(new ExceptionResponse(stringBuilder.toString()), headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseBody
    public final ResponseEntity<Object> handleConstraintViolationEx(ConstraintViolationException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        StringBuilder stringBuilder = new StringBuilder();
        ex.getConstraintViolations().forEach(constraintViolation -> stringBuilder.append(constraintViolation.getMessage()));
        return new ResponseEntity<>(new ExceptionResponse(stringBuilder.toString()), headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = ProductChangingException.class)
    @ResponseBody
    public final ResponseEntity<Object> handleProductChangingEx(ProductChangingException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(new ExceptionResponse(ex.getMessage()), headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = TotalRuntimeException.class)
    @ResponseBody
    public final ResponseEntity<Object> handleTotalRuntimeEx(TotalRuntimeException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(new ExceptionResponse(ex.getMessage()), headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = UserAlreadyExistsException.class)
    @ResponseBody
    public final ResponseEntity<Object> handleUserAlreadyExistsEx(UserAlreadyExistsException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(new ExceptionResponse(ex.getMessage()), headers, HttpStatus.OK);
    }

    @ExceptionHandler(value = BasketIsEmptyException.class)
    @ResponseBody
    public final ResponseEntity<Object> handleBasketIsEmptyEx(BasketIsEmptyException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(new ExceptionResponse(ex.getMessage()), headers, HttpStatus.OK);
    }

    @ExceptionHandler(value = OrderNotFoundException.class)
    @ResponseBody
    public final ResponseEntity<Object> handleOrderNotFoundEx(OrderNotFoundException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(new ExceptionResponse(ex.getMessage()), headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = UserNotFoundException.class)
    @ResponseBody
    public final ResponseEntity<Object> handleUserNotFoundEx(UserNotFoundException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
        return new ResponseEntity<>(new ExceptionResponse(ex.getMessage()), headers, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = ProductNotFoundException.class)
    @ResponseBody
    public final ResponseEntity<Object> handleProductNotFoundEx(ProductNotFoundException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(new ExceptionResponse(ex.getMessage()), headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = BasketElementNotFoundException.class)
    @ResponseBody
    public final ResponseEntity<Object> handleBasketElNotFoundEx(BasketElementNotFoundException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(new ExceptionResponse(ex.getMessage()), headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = InsufficientProductQuantityException.class)
    @ResponseBody
    public final ResponseEntity<Object> handleInsufficientProductQuantityEx(InsufficientProductQuantityException ex) {
        return new ResponseEntity<>(new ExceptionResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
