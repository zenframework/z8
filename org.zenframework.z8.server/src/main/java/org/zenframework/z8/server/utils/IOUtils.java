package org.zenframework.z8.server.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.zenframework.z8.server.logs.Trace;

public class IOUtils {

    final public static int DefaultBufferSize = 1024 * 1024;

    private IOUtils() {/* hide constructor */}

    public static long streamSize(InputStream stream) {
        if (stream instanceof FileInputStream) {
            try {
                FileChannel channel = ((FileInputStream) stream).getChannel();
                return channel.size();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return -1;
    }

    public static void copy(InputStream inp, OutputStream out) throws IOException {
        byte[] buff = new byte[DefaultBufferSize];
        int count;
        while ((count = inp.read(buff)) != -1) {
            if (count > 0) {
                out.write(buff, 0, count);
            }
        }
    }

    public static boolean moveFolder(File src, File dest, boolean trace) {
        InputStream in = null;
        try {
            StringBuilder cmd = new StringBuilder();
            cmd.append("cmd /C move /Y \"").append(src.getAbsolutePath()).append("\" \"").append(dest.getAbsolutePath())
                    .append("\"");
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            if (trace) {
                in = proc.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
                copy(in, out);
                Trace.logEvent("Moving '" + src + "' to '" + dest + "':\r\n" + new String(out.toByteArray(), "cp866"));
            } else {
                proc.waitFor();
            }
            return true;
        } catch (Exception e) {
            Trace.logError("Move '" + src + "' to '" + dest + "' failed", e);
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Trace.logError("Can't close 'cmd' process InputStream", e);
                }
            }
        }
    }

    public static String readText(InputStream in, Charset charset) throws IOException {
        StringBuilder builder = new StringBuilder();
        Reader reader = new InputStreamReader(in, charset);
        char buf[] = new char[8192];
        for (int n = reader.read(buf); n >= 0; n = reader.read(buf)) {
            builder.append(new String(buf, 0, n));
        }
        return builder.toString();
    }
    
    public static String readText(InputStream in) throws IOException {
        return readText(in, Charset.defaultCharset());
    }
    
    public static String readText(URL resource) throws IOException {
        InputStream in = resource.openStream();
        try {
            return readText(in);
        } finally {
            in.close();
        }
    }

}
