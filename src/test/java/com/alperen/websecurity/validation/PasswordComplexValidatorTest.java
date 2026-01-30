package com.alperen.websecurity.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordComplexValidatorTest {

    private final PasswordComplexValidator validator = new PasswordComplexValidator();

    @Test
    void acceptsStrongPassword() {
        assertTrue(validator.isValid("Abcdef1!", null));
    }

    @Test
    void rejectsTooShort() {
        assertFalse(validator.isValid("Ab1!", null));
    }

    @Test
    void rejectsMissingUpper() {
        assertFalse(validator.isValid("abcdef1!", null));
    }

    @Test
    void rejectsMissingLower() {
        assertFalse(validator.isValid("ABCDEF1!", null));
    }

    @Test
    void rejectsMissingDigit() {
        assertFalse(validator.isValid("Abcdefg!", null));
    }

    @Test
    void rejectsMissingSymbol() {
        assertFalse(validator.isValid("Abcdefg1", null));
    }
}

