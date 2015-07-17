package org.zenframework.z8.server.security;

import java.security.MessageDigest;
import java.util.Properties;

public class Digest_utils {
    static public String PasswordDigest = null;

    static public final String paramPasswordDigest = "password_digest";

    static public void initialize(Properties properties) {
        if(properties.containsKey(paramPasswordDigest) && properties.getProperty(paramPasswordDigest).trim().length() > 0)
            PasswordDigest = properties.getProperty(paramPasswordDigest);
    }

    public static String get_pwd_Digest(String _pwd) {
        if(PasswordDigest != null)
            return get_s_Digest(PasswordDigest, _pwd);
        return _pwd;
    }

    public static String bytesToHex(byte[] bytes) {
        // fastest algorithm! (see: http://stackoverflow.com/a/9855338) 
        char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for(int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String get_s_Digest(String _provider, String _str) {
        byte[] digest = get_b_Digest(_provider, _str);
        String hexString = bytesToHex(digest);

        assert (hexString.length() == 32);

        return hexString;
    }

    public static byte[] get_b_Digest(String _provider, String _str) {
        try {
            MessageDigest md = MessageDigest.getInstance(_provider);
            md.reset();
            md.update(_str.getBytes());//"UTF-8"
            return md.digest();
        }
        catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMD5(String _str) {
        return get_s_Digest("md5", _str);
    }
}
