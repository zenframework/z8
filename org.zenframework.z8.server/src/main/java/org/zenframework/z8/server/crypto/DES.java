package org.zenframework.z8.server.crypto;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

public class DES {
	static String Algorithm = "DESede/ECB/NoPadding";
/*
	static public void main(String[] args) {
		try {
			SecretKey key = generateKey();

			String uuid = UUID.randomUUID().toString();
			Trace.logEvent("UUID: " + uuid);
			uuid = uuid.replace("-", "");
			byte[] data = uuid.getBytes();
			Trace.logEvent("S: " + Arrays.toString(data));

			ByteArrayInputStream in = new ByteArrayInputStream(data);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			encrypt(key, in, out);

			byte[] encripted = out.toByteArray();
			Trace.logEvent("E: " + Arrays.toString(encripted));

			Trace.logEvent("D/E :" + data.length + "/" + encripted.length);

			ByteArrayInputStream in1 = new ByteArrayInputStream(encripted);
			ByteArrayOutputStream out1 = new ByteArrayOutputStream();
			decrypt(key, in1, out1);

			byte[] decripted = out1.toByteArray();
			Trace.logEvent("D: " + Arrays.toString(decripted));
			Trace.logEvent("S: " + Arrays.toString(data));
		} catch(Exception e) {
			Trace.logError(e);
		}
	}
*/

	/** Generate a secret TripleDES encryption/decryption key */
	static public SecretKey generateKey() throws NoSuchAlgorithmException {
		return KeyGenerator.getInstance("DESede").generateKey();
	}

	static public byte[] getKeyBytes(SecretKey key) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");
		DESedeKeySpec keyspec = (DESedeKeySpec)keyfactory.getKeySpec(key, DESedeKeySpec.class);
		return keyspec.getKey();
	}

	static public SecretKey getKeyFromBytes(byte[] bytes) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		DESedeKeySpec keyspec = new DESedeKeySpec(bytes);
		SecretKeyFactory keyfactory = SecretKeyFactory.getInstance(Algorithm);
		return keyfactory.generateSecret(keyspec);
	}

	/**
	 * Use the specified TripleDES key to encrypt bytes from the input stream
	 * and write them to the output stream. This method uses CipherOutputStream
	 * to perform the encryption and write bytes at the same time.
	 */
	static public void encrypt(SecretKey key, InputStream in, OutputStream out) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
		Cipher cipher = Cipher.getInstance(Algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, key);

		CipherOutputStream cipherOutput = new CipherOutputStream(out, cipher);

		int bytesRead;
		byte[] buffer = new byte[2048];

		while((bytesRead = in.read(buffer)) != -1)
			cipherOutput.write(buffer, 0, bytesRead);

		cipherOutput.close();
	}

	/**
	 * Use the specified TripleDES key to decrypt bytes ready from the input
	 * stream and write them to the output stream. This method uses uses Cipher
	 * directly to show how it can be done without CipherInputStream and
	 * CipherOutputStream.
	 */
	static public void decrypt(SecretKey key, InputStream in, OutputStream out) throws NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(Algorithm);
		cipher.init(Cipher.DECRYPT_MODE, key);

		int bytesRead;
		byte[] buffer = new byte[2048];
	
		while((bytesRead = in.read(buffer)) != -1)
			out.write(cipher.update(buffer, 0, bytesRead));

		out.write(cipher.doFinal());
		out.flush();
	}
}
