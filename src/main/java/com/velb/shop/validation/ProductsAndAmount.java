package com.velb.shop.validation;

import com.velb.shop.validation.validators.ProductsAndAmountValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ProductsAndAmountValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProductsAndAmount {

    String message() default " Вы ввели некорректные данные - либо значение уникального идентификатора товара " +
            "либо его количество меньше 0; ";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
