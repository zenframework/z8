package org.zenframework.z8.server.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.mozilla.universalchardet.UniversalDetector;
import org.zenframework.z8.server.logs.Trace;

public class IOUtils {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    final public static int DefaultBufferSize = 1024 * 1024;

    private IOUtils() {/* hide constructor */}

    public static void copy(InputStream input, OutputStream output) throws IOException {
    	copy(input, output, true);
    }

    public static void copy(InputStream input, OutputStream output, boolean autoClose) throws IOException {
    	try {
	        int count = 0;
	        byte[] buffer = new byte[DefaultBufferSize];
	        
	        
	        while ((count = input.read(buffer)) != -1) {
	            if (count > 0)
	            	output.write(buffer, 0, count);
	        }
	        
	        if(autoClose) {
    			input.close();
    			output.close();
    			autoClose = false;
	        }
    	} finally {        
    		if(autoClose) {
    			closeQuietly(input);
    			closeQuietly(output);
    		}
    	}
    }

    public static boolean moveFolder(File src, File dest, boolean trace) {
        try {
            dest.getParentFile().mkdirs();
            ProcessBuilder cmd;
            String charset;
            if (OS_NAME.startsWith("win")) {
                cmd = new ProcessBuilder("cmd", "/C", "move", "/Y", src.getAbsolutePath(), dest.getAbsolutePath());
                charset = "cp866";
            } else if (OS_NAME.startsWith("linux")) {
                cmd = new ProcessBuilder("mv", src.getAbsolutePath(), dest.getAbsolutePath());
                charset = "UTF-8";
            } else {
                if (trace) {
                    Trace.logError("Move '" + src + "' to '" + dest + "' failed", new UnsupportedOperationException(
                            "Unsupported OS '" + OS_NAME + "'"));
                }
                return false;
            }
            Process proc = cmd.start();
            if (trace) {
            	InputStream in = proc.getErrorStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
                copy(in, out);
                Trace.logEvent("Moving '" + src + "' to '" + dest + "':\r\n" + new String(out.toByteArray(), charset));
            } else
                proc.waitFor();

            return dest.exists();
        } catch (Exception e) {
            Trace.logError("Move '" + src + "' to '" + dest + "' failed", e);
            return false;
        }
    }
    
    public static void closeQuietly(Closeable closable) {
    	try {
    		if(closable != null)
    			closable.close();
    	} catch(Throwable e) {
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

    public static byte[] objectToBytes(Object object) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(object);
            objOut.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object bytesToObject(byte buf[]) {
        try {
            return new ObjectInputStream(new ByteArrayInputStream(buf)).readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String determineEncoding(File file, String defaultCharset) {
        UniversalDetector detector = new UniversalDetector(null);
        // Reset detector before using
        detector.reset();
        // Buffer
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] buf = new byte[1024];
            for (int n = in.read(buf); n >= 0 && !detector.isDone(); n = in.read(buf)) {
                detector.handleData(buf, 0, n);
            }
            detector.dataEnd();
            return detector.getDetectedCharset();
        } catch (Exception e) {
            Trace.logError("Can't detect encoding of '" + file.getAbsolutePath() + "'", e);
            return defaultCharset;
        } finally {
            detector.reset();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {}
            }
        }
    }

}
