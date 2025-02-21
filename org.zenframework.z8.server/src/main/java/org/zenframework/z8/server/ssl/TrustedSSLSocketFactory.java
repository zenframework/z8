package org.zenframework.z8.server.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

public class TrustedSSLSocketFactory extends SSLSocketFactory {

	private static class TrustedX509TrustManager extends X509ExtendedTrustManager {

		private List<X509Certificate> trusted = new LinkedList<X509Certificate>();
	
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			trusted.addAll(Arrays.asList(chain));
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
				throws CertificateException {
			trusted.addAll(Arrays.asList(chain));
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
				throws CertificateException {
			trusted.addAll(Arrays.asList(chain));
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			trusted.addAll(Arrays.asList(chain));
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
				throws CertificateException {
			trusted.addAll(Arrays.asList(chain));
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
				throws CertificateException {
			trusted.addAll(Arrays.asList(chain));
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return trusted.toArray(new X509Certificate[trusted.size()]);
		}

	}

	public static synchronized SocketFactory getDefault() {
		return new TrustedSSLSocketFactory();
	}

	private SSLSocketFactory socketFactory;

	public TrustedSSLSocketFactory() {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init((KeyManager[]) null, new TrustManager[] { new TrustedX509TrustManager() }, new SecureRandom());
			socketFactory = ctx.getSocketFactory();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return socketFactory.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return socketFactory.getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket() throws IOException {
		return socketFactory.createSocket();
	}

	@Override
	public Socket createSocket(Socket s, InputStream consumed, boolean autoClose) throws IOException {
		return socketFactory.createSocket(s, consumed, autoClose);
	}

	@Override
	public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
		return socketFactory.createSocket(s, host, port, autoClose);
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		return socketFactory.createSocket(host, port);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
			throws IOException, UnknownHostException {
		return socketFactory.createSocket(host, port, localHost, localPort);
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		return socketFactory.createSocket(host, port);
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
			throws IOException {
		return socketFactory.createSocket(address, port, localAddress, localPort);
	}

}
