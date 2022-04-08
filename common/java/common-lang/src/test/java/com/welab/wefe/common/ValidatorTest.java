package com.welab.wefe.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author zane
 * @date 2022/4/8
 */
class ValidatorTest {

    @Test
    void isBoolean() {
        assertTrue(Validator.isBoolean("true"));
        assertTrue(Validator.isBoolean("false"));
        assertTrue(Validator.isBoolean("True"));
        assertTrue(Validator.isBoolean("False"));
        assertTrue(Validator.isBoolean("TRUE"));
        assertTrue(Validator.isBoolean("FALSE"));
        assertTrue(Validator.isBoolean("0"));
        assertTrue(Validator.isBoolean("1"));

        assertFalse(Validator.isBoolean("True1"));
        assertFalse(Validator.isBoolean("False1"));
        assertFalse(Validator.isBoolean("true1"));
        assertFalse(Validator.isBoolean("false1"));
        assertFalse(Validator.isBoolean("True "));
        assertFalse(Validator.isBoolean("False "));
        assertFalse(Validator.isBoolean("true "));
        assertFalse(Validator.isBoolean("false "));
    }

    @Test
    void isLong() {
        assertTrue(Validator.isLong("123"));
        assertTrue(Validator.isLong("-123"));
        assertTrue(Validator.isLong("0"));
        assertTrue(Validator.isLong("+123"));


        assertFalse(Validator.isLong("5.85E-05"));
        assertFalse(Validator.isLong("123L"));
        assertFalse(Validator.isLong("-123L"));
        assertFalse(Validator.isLong("0L"));
        assertFalse(Validator.isLong("+123L"));
        assertFalse(Validator.isLong("123.0"));
        assertFalse(Validator.isLong("123.123"));
        assertFalse(Validator.isLong("123.123.123"));
    }

    @Test
    void isDouble() {
        assertTrue(Validator.isDouble("123.0"));
        assertTrue(Validator.isDouble("123.123"));
        assertTrue(Validator.isDouble("5.85E-05"));

        assertFalse(Validator.isDouble("123.123.123"));

    }

    @Test
    void isInteger() {
        assertTrue(Validator.isInteger("123"));
        assertTrue(Validator.isInteger("-123"));
        assertTrue(Validator.isInteger("0"));
        assertTrue(Validator.isInteger("+123"));

        assertFalse(Validator.isInteger("5.85E-05"));
        assertFalse(Validator.isInteger("123.0"));
        assertFalse(Validator.isInteger("123.123"));
        assertFalse(Validator.isInteger("123.123.123"));
    }

    @Test
    void isUnsignedInteger() {
    }
}