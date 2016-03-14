package org.zenframework.z8.server.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeUtility;

public class EmlUtils {

    public static String QUOTED_PRINTABLE_BEGINNING = "=?";
    public static String QUOTED_PRINTABLE_BEGINNING_EXPRESSION = "=\\?";
    public static String QUOTED_PRINTABLE_MIDDLE_EXPRESSION = "\\?[BQ]\\?";
    public static String QUOTED_PRINTABLE_ENDING = "?=";
    public static String BASE64_ENDING = "=?=";
    public static String QUOTED_PRINTABLE_ENDING_EXPRESSION = "\\?=";
    public static String QUOTED_PRINTABLE_SPACE = "=20";

    private EmlUtils() {}

    public static String parsePartDocText(Part p) throws MessagingException, IOException {
        if (p.isMimeType("text/html")) {
            String text = decode((String) p.getContent());
            text = text.replaceAll("<.*?>", "").trim();
            text = text.replaceAll("((\r\n)+ +(\r\n)+)+", "\r\n");
            text = text.replaceAll("( )+", " ");
            text = text.replaceAll("(\r\n)+", "\r\n");
            return text;
        }
        if (p.isMimeType("text/*")) {
            return decode((String) p.getContent());
        }
        if (p.isMimeType("multipart/alternative")) {
            // prefer plain text over html text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/html")) {
                    if (text == null) {
                        text = parsePartDocText(bp);
                    }
                } else if (bp.isMimeType("text/plain")) {
                    String s = parsePartDocText(bp);
                    if (s != null) {
                        return s;
                    }
                } else {
                    return parsePartDocText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = parsePartDocText(mp.getBodyPart(i));
                if (s != null) {
                    return s;
                }
            }
        }
        return null;
    }

    private static String decode(String src) {
        if (src == null)
            return null;

        List<String> forDecode = new LinkedList<String>();
        int position = 0;

        //Эта замена нужна, т.к. в корректных quoted-printable строках такие пробелы должны игнорироваться
        src = src.replaceAll(QUOTED_PRINTABLE_ENDING_EXPRESSION + "\\s*" + QUOTED_PRINTABLE_BEGINNING_EXPRESSION,
                QUOTED_PRINTABLE_ENDING + QUOTED_PRINTABLE_BEGINNING);

        while (position < src.length()) {
            Pair<Integer, String> tmp = getStringPart(src.substring(position));
            position += tmp.first;
            forDecode.add(tmp.second);
        }

        StringBuilder forReturn = new StringBuilder();

        for (String part : forDecode) {
            try {
                forReturn.append(MimeUtility.decodeText(part));
            } catch (UnsupportedEncodingException e) {}
        }

        return forReturn.toString();
    }

    private static Pair<Integer, String> getStringPart(String src) {
        Pair<Integer, String> forReturn = new Pair<Integer, String>();

        if (src.startsWith(QUOTED_PRINTABLE_BEGINNING)) {
            int beginIndex = getQPStartIndex(src.substring(QUOTED_PRINTABLE_BEGINNING.length()))
                    + QUOTED_PRINTABLE_BEGINNING.length();
            int endIndex = getQPEndIndex(src);

            forReturn.first = beginIndex < endIndex ? beginIndex : endIndex;
            forReturn.second = src.substring(0, forReturn.first);
            forReturn.second = beginIndex <= endIndex ? forReturn.second : forReturn.second.replaceAll("\\s",
                    QUOTED_PRINTABLE_SPACE).concat(QUOTED_PRINTABLE_ENDING);
            forReturn.first += beginIndex <= endIndex ? 0 : QUOTED_PRINTABLE_ENDING.length();
        } else {
            forReturn.first = getQPStartIndex(src);
            forReturn.second = src.substring(0, forReturn.first);
        }

        return forReturn;
    }

    private static class Pair<T1, T2> {
        T1 first;
        T2 second;
    }

    private static int getQPEndIndex(String src) {
        src = src.replaceFirst(QUOTED_PRINTABLE_MIDDLE_EXPRESSION, "___");
        int forReturn = src.indexOf(QUOTED_PRINTABLE_ENDING);
        return forReturn == -1 ? src.length() : forReturn;
    }

    private static int getQPStartIndex(String src) {
        int base64Index = src.indexOf(BASE64_ENDING);
        int forReturn = src.indexOf(QUOTED_PRINTABLE_BEGINNING);
        if (forReturn == base64Index)
            forReturn = src.indexOf(QUOTED_PRINTABLE_BEGINNING, forReturn + 1);
        return forReturn == -1 ? src.length() : forReturn;
    }

}
