package org.zenframework.z8.server.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HexUtilsTest {

    @Test
    public void testHex() {
        String hex = HexUtils.hex("test-hex-value");
        System.out.println("test-hex-value hashed by MD5: " + hex);
        assertEquals("168f6540493857c991e5748b3c24113a", hex);
    }

    @Test
    public void testSha256Hex() {
        String hex = HexUtils.sha256hex("test-hex-value");
        System.out.println("test-hex-value hashed by SHA-256: " + hex);
        assertEquals("a3476cac4c8f229c17a32665b04aefb71bcf40db030c9c0552351f1f131f86dc", hex);
    }

    @Test
    public void testSha1Hex() {
        String hex = HexUtils.sha1hex("test-hex-value");
        System.out.println("test-hex-value hashed by SHA-1: " + hex);
        assertEquals("4c82dba2114f6481a15a51cd3ece61072c837c44", hex);
    }
}