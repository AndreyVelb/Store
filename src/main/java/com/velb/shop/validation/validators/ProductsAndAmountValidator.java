package com.velb.shop.validation.validators;

import com.velb.shop.validation.ProductsAndAmount;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProductsAndAmountValidator implements ConstraintValidator<ProductsAndAmount, Map<Long, Integer>> {

    @Override
    public boolean isValid(Map<Long, Integer> productsAndAmount, ConstraintValidatorContext context) {
        AtomicBoolean marker = new AtomicBoolean(true);
        productsAndAmount.forEach((key, value) -> {
            if (key <= 0 || value <= 0) {
                marker.set(false);
            }
        });
        return marker.get();
    }
}
