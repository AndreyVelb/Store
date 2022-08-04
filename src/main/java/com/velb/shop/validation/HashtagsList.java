package com.velb.shop.validation;

import com.velb.shop.validation.validators.HashtagsListValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = HashtagsListValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HashtagsList {

    String message() default " ХЭШТЕГ не может быть пустым и больше 30 символов; ";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
