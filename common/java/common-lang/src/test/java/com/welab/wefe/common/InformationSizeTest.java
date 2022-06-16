package com.welab.wefe.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author zane
 * @date 2022/4/21
 */
class InformationSizeTest {

    @Test
    void toBit() {
        assertEquals(InformationSize.fromBit(10).toBit(), 10);
        assertEquals(InformationSize.fromByte(0.5).toBit(), 4);
        assertEquals(InformationSize.fromByte(2).toBit(), 16);
        assertEquals(InformationSize.fromKiB(0.5).toBit(), 4096);
    }

    @Test
    void toByte() {
        assertEquals(InformationSize.fromBit(8).toByte(), 1);
        assertEquals(InformationSize.fromByte(0.5).toByte(), 0.5);
        assertEquals(InformationSize.fromByte(2).toByte(), 2);
        assertEquals(InformationSize.fromKiB(0.5).toByte(), 512);
    }

    @Test
    void toKiB() {
        assertEquals(InformationSize.fromByte(1024).toKiB(), 1);
    }

    @Test
    void toMiB() {
        assertEquals(InformationSize.fromByte(1024 * 1024).toMiB(), 1);
    }

    @Test
    void toGiB() {
        assertEquals(InformationSize.fromByte(1024 * 1024 * 1024).toGiB(), 1);
    }

    @Test
    void testToString() {
        System.out.println(InformationSize.fromKiB(1024 * 1024 * 0.32));
    }
}