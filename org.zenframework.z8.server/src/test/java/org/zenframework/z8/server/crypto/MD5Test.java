package org.zenframework.z8.server.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MD5Test {

    @Test
    public void testHex() {
        String hex = MD5.hex("test-hex-value");
        System.out.println(hex);
        assertEquals("168f6540493857c991e5748b3c24113a", hex);
    }

    @Test
    public void testSha256Hex() {
        String hex = MD5.sha256hex("test-hex-value");
        System.out.println(hex);
        assertEquals("a3476cac4c8f229c17a32665b04aefb71bcf40db030c9c0552351f1f131f86dc", hex);
    }
}