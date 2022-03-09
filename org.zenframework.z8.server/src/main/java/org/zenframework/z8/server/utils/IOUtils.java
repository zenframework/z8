package org.zenframework.z8.server.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.mozilla.universalchardet.UniversalDetector;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.encoding;

public class IOUtils {

	public static final String OS_NAME = System.getProperty("os.name").toLowerCase();
	public static final boolean IS_WINDOWS = OS_NAME.startsWith("win");
	public static final boolean IS_LINUX = OS_NAME.startsWith("linux");

	final static public int DefaultBufferSize = 1024 * 1024;
	final static public String DefaultCharset = encoding.UTF8.toString();

	private IOUtils() {/* hide constructor */ }

	public static byte[] read(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copy(in, out);
		return out.toByteArray();
	}

	public static int read(InputStream input, byte[] buffer) throws IOException {
		return read(input, buffer, 0, buffer.length);
	}

	public static int read(InputStream input, byte[] buffer, int offset, int length) throws IOException {
		if(length < 0)
			throw new IllegalArgumentException("Length must not be negative: " + length);

		int remaining = length;
	
		while(remaining > 0) {
			int location = length - remaining;
			int count = input.read(buffer, offset + location, remaining);
			if(count == -1) // EOF
				break;

			remaining -= count;
		}

		return length - remaining;
	}

	static public void copy(InputStream input, DataOutput output) throws IOException {
		copy(input, output, true);
	}

	static public void copy(InputStream input, DataOutput output, boolean autoClose) throws IOException {
		try {
			int count = 0;
			byte[] buffer = new byte[DefaultBufferSize];

			while((count = input.read(buffer)) != -1) {
				if(count > 0)
					output.write(buffer, 0, count);
			}

			if(autoClose) {
				input.close();
				if(output instanceof Closeable)
					((Closeable)output).close();
				autoClose = false;
			}
		} finally {
			if(autoClose) {
				closeQuietly(input);
				closeQuietly((Closeable)output);
			}
		}
	}

	static public void copy(InputStream input, File file) throws IOException {
		copy(input, file, true);
	}

	static public void copy(InputStream input, File file, boolean autoClose) throws IOException {
		try {
			file.getParentFile().mkdirs();
			copy(input, new FileOutputStream(file), autoClose);
		} finally {
			if(autoClose)
				closeQuietly(input);
		}
	}

	static public void copy(InputStream input, OutputStream output) throws IOException {
		copy(input, output, true);
	}

	static public void copy(InputStream input, OutputStream output, boolean autoClose) throws IOException {
		try {
			int count = 0;
			byte[] buffer = new byte[DefaultBufferSize];

			while((count = input.read(buffer)) != -1) {
				if(count > 0)
					output.write(buffer, 0, count);
			}

			output.flush();

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

	static public long copyLarge(InputStream input, OutputStream output) throws IOException {
		return copyLarge(input, output, true);
	}

	static public long copyLarge(InputStream input, OutputStream output, boolean autoClose) throws IOException {
		try {
			byte[] buffer = new byte[DefaultBufferSize];
			long count = 0;
			int read = 0;
			while((read = input.read(buffer)) != -1) {
				output.write(buffer, 0, read);
				count += read;
			}

			output.flush();

			if(autoClose) {
				input.close();
				output.close();
				autoClose = false;
			}

			return count;
		} finally {
			if(autoClose) {
				closeQuietly(input);
				closeQuietly(output);
			}
		}
	}

	static public long copyLarge(InputStream input, OutputStream output, long length) throws IOException {
		return copyLarge(input, output, length, true);
	}

	static public long copyLarge(InputStream input, OutputStream output, long length, boolean autoClose) throws IOException {
		try {
			byte[] buffer = new byte[DefaultBufferSize];

			if(length == 0)
				return 0;

			final int bufferLength = buffer.length;

			int bytesToRead = bufferLength;
			if(length > 0 && length < bufferLength)
				bytesToRead = (int)length;

			int read;
			long totalRead = 0;

			while(bytesToRead > 0 && (read = input.read(buffer, 0, bytesToRead)) != -1) {
				output.write(buffer, 0, read);
				totalRead += read;
				if(length > 0)
					bytesToRead = (int)Math.min(length - totalRead, bufferLength);
			}

			if(autoClose) {
				input.close();
				output.close();
				autoClose = false;
			}

			return totalRead;
		} finally {
			if(autoClose) {
				closeQuietly(input);
				closeQuietly(output);
			}
		}
	}

	static public String readText(InputStream in) throws IOException {
		return readText(in, Charset.defaultCharset());
	}

	static public String readText(InputStream in, Charset charset) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);
		return new String(out.toByteArray(), charset);
	}

	static public String readText(URL resource) throws IOException {
		return readText(resource.openStream());
	}

	static public void closeQuietly(Closeable closable) {
		try {
			if(closable != null)
				closable.close();
		} catch(Throwable e) {
		}
	}

	static public void closeQuietly(Collection<Closeable> closeables) {
		for(Closeable closeable : closeables)
			closeQuietly(closeable);
	}

	static public byte[] zip(byte[] bytes) {
		return zip(bytes, 0, bytes.length);
	}

	static public byte[] zip(byte[] bytes, int offset, int length) {
		if(bytes == null)
			return null;

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Deflater deflater = new Deflater();
		deflater.setInput(bytes, offset, length);
		deflater.finish();

		byte[] buffer = new byte[32768];
		while(!deflater.finished()) {
			int count = deflater.deflate(buffer);
			outputStream.write(buffer, 0, count);
		}

		return outputStream.toByteArray();
	}

	static public void unzip(InputStream input, OutputStream output) throws IOException {
		copy(new InflaterInputStream(input), output);
	}

	static public void unzip(InputStream input, RandomAccessFile output) throws IOException {
		copy(new InflaterInputStream(input), output);
	}

	static public byte[] unzip(byte[] bytes) {
		return unzip(bytes, 0, bytes.length);
	}
	
	static public byte[] unzip(byte[] bytes, int offset, int length) {
		if(bytes == null)
			return null;

		Inflater inflater = new Inflater();
		inflater.setInput(bytes, offset, length);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		byte[] buffer = new byte[32768];

		while(!inflater.finished()) {
			try {
				int count = inflater.inflate(buffer);
				outputStream.write(buffer, 0, count);
			} catch(DataFormatException e) {
				throw new RuntimeException(e);
			}
		}

		return outputStream.toByteArray();
	}

	static public void zip(InputStream input, OutputStream output) throws IOException {
		copy(new DeflaterInputStream(input), output);
	}

	static public void zip(InputStream input, RandomAccessFile output) throws IOException {
		copy(new DeflaterInputStream(input), output);
	}

	static public boolean moveFolder(File src, File dest, boolean trace) {
		dest.getParentFile().mkdirs();
		String message = "";
		try {
			if(IS_WINDOWS)
				message = cmd(trace, "cmd", "/C", "move", "/Y", src.getCanonicalPath(), dest.getCanonicalPath());
			else if(IS_LINUX)
				message = cmd(trace, "mv", src.getCanonicalPath(), dest.getCanonicalPath());
			if(trace) {
				Trace.logEvent("Move '" + src + "' to '" + dest + "':\r\n" + message);
			}
		} catch(Exception e) {
			Trace.logError("Can't move '" + src + "' to '" + dest + "'", e);
		}
		return dest.exists();
	}

	static public boolean copyFolder(File src, File dest, boolean trace) {
		dest.getParentFile().mkdirs();
		String message = "";
		try {
			if(IS_WINDOWS)
				message = cmd(trace, "cmd", "/C", "xcopy", "/E", "/Y", src.getCanonicalPath(), dest.getCanonicalPath());
			else if(IS_LINUX)
				message = cmd(trace, "cp", "-r", src.getCanonicalPath(), dest.getCanonicalPath());
			if(trace) {
				Trace.logEvent("Copy '" + src + "' to '" + dest + "':\r\n" + message);
			}
		} catch(Exception e) {
			Trace.logError("Can't copy '" + src + "' to '" + dest + "'", e);
		}
		return dest.exists();
	}

	static public boolean deleteFolder(File folder, boolean trace) {
		String message = "";
		try {
			if(IS_WINDOWS)
				message = cmd(trace, "cmd", "/C", "rmdir", "/S", folder.getCanonicalPath());
			else if(IS_LINUX)
				message = cmd(trace, "rm", "-r", folder.getCanonicalPath());
			if(trace) {
				Trace.logEvent("Delete '" + folder + "':\r\n" + message);
			}
		} catch(Exception e) {
			Trace.logError("Can't delete '" + folder + "'", e);
		}
		return folder.exists();
	}

	static public String cmd(boolean trace, String... c) throws IOException, InterruptedException {
		ProcessBuilder cmd = new ProcessBuilder(c);
		String charset;
		if(IS_WINDOWS)
			charset = "cp866";
		else if(IS_LINUX)
			charset = "UTF-8";
		else
			throw new UnsupportedOperationException("Unsupported OS '" + OS_NAME + "'");
		Process proc = cmd.start();
		if(trace) {
			InputStream in = proc.getErrorStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
			copy(in, out);
			return new String(out.toByteArray(), charset);
		} else {
			proc.waitFor();
			return null;
		}
	}

	static public String determineEncoding(File file, String defaultCharset) throws IOException {
		InputStream in = new FileInputStream(file);
		String encoding = determineEncoding(in, defaultCharset);
		in.close();
		return encoding;
	}

	static public String determineEncoding(InputStream in, String defaultCharset) throws IOException {
		UniversalDetector detector = new UniversalDetector(null);

		detector.reset();

		byte[] buf = new byte[1024];
		for(int n = in.read(buf); n >= 0 && !detector.isDone(); n = in.read(buf))
			detector.handleData(buf, 0, n);
		detector.dataEnd();
		return detector.getDetectedCharset() != null ? detector.getDetectedCharset() : defaultCharset;
	}
}
