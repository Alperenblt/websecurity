package com.alperen.websecurity.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PasswordComplexValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordComplex {
    String message() default "password must be at least 8 chars and include upper, lower, digit, and symbol";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
