package org.zenframework.z8.server.types;

public enum encoding {
    Default(""),
    Cp866(Names.Cp866),
    KOI8R(Names.KOI8R),
    USASCII(Names.USASCII),
    UTF8(Names.UTF8),
    UTF16(Names.UTF16),
    UTF16BE(Names.UTF16BE),
    UTF16LE(Names.UTF16LE),
    Windows1250(Names.Windows1250),
    Windows1251(Names.Windows1251),
    Windows1252(Names.Windows1252),
    Windows1253(Names.Windows1253),
    Windows1254(Names.Windows1254),
    Windows1257(Names.Windows1257);

    class Names {
        static protected final String Cp866 = "CP866";
        static protected final String KOI8R = "KOI8-R";
        static protected final String USASCII = "US-ASCII";
        static protected final String UTF8 = "UTF-8";
        static protected final String UTF16 = "UTF-16";
        static protected final String UTF16BE = "UTF-16BE";
        static protected final String UTF16LE = "UTF-16LE";

        static protected final String Windows1250 = "Windows-1250";
        static protected final String Windows1251 = "Windows-1251";
        static protected final String Windows1252 = "Windows-1252";
        static protected final String Windows1253 = "Windows-1253";
        static protected final String Windows1254 = "Windows-1254";
        static protected final String Windows1257 = "Windows-1257";
    }

    private String fName = null;

    encoding(String name) {
        fName = name;
    }

    @Override
    public String toString() {
        return fName.isEmpty() ? UTF8.toString() : fName;
    }

    static public encoding fromString(String string) {
        string = string.toLowerCase();

        if(Names.Cp866.toLowerCase().equals(string)) {
            return encoding.Cp866;
        }
        else if(Names.KOI8R.toLowerCase().equals(string)) {
            return encoding.KOI8R;
        }
        else if(Names.USASCII.toLowerCase().equals(string)) {
            return encoding.USASCII;
        }
        else if(Names.UTF8.toLowerCase().equals(string)) {
            return encoding.UTF8;
        }
        else if(Names.UTF16.toLowerCase().equals(string)) {
            return encoding.UTF16;
        }
        else if(Names.UTF16LE.toLowerCase().equals(string)) {
            return encoding.UTF16LE;
        }
        else if(Names.UTF16BE.toLowerCase().equals(string)) {
            return encoding.UTF16BE;
        }
        else if(Names.Windows1250.toLowerCase().equals(string)) {
            return encoding.Windows1250;
        }
        else if(Names.Windows1251.toLowerCase().equals(string)) {
            return encoding.Windows1251;
        }
        else if(Names.Windows1252.toLowerCase().equals(string)) {
            return encoding.Windows1252;
        }
        else if(Names.Windows1253.toLowerCase().equals(string)) {
            return encoding.Windows1253;
        }
        else if(Names.Windows1254.toLowerCase().equals(string)) {
            return encoding.Windows1254;
        }
        else if(Names.Windows1257.toLowerCase().equals(string)) {
            return encoding.Windows1257;
        }
        else {
            throw new RuntimeException("Unknown encoding: '" + string + "'");
        }
    }
}
