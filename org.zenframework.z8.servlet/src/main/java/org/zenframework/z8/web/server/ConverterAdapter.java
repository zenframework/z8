package org.zenframework.z8.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeUtility;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.base.file.FileConverter;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.engine.ServerInfo;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.web.servlet.Servlet;

public class ConverterAdapter extends Adapter {
	private static final Collection<String> IgnoredExceptions = Arrays.asList("org.apache.catalina.connector.ClientAbortException");

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(ConverterAdapter.class);

	private FileConverter converter = null;

	public ConverterAdapter(Servlet servlet) {
		super(servlet);
	}

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return "get".equalsIgnoreCase(request.getMethod());
	}

	@Override
	public void stop() {
		if(converter != null) {
			converter.close();
			converter = null;
		}
	}

	@Override
	protected void service(ISession session, Map<String, String> parameters, List<FileInfo> files, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// URLDecoder.decode заменяет '+' на ' '
		String encodedUrl = request.getRequestURI().replaceAll("\\+", "%2b"); 
		String requestUrl = URLDecoder.decode(encodedUrl, encoding.Default.toString());
		String contextPath = request.getContextPath() + '/';

		if(requestUrl.startsWith(contextPath)) {
			requestUrl = requestUrl.substring(contextPath.length());
		}

		File relativePath = new File(requestUrl);
		File absolutePath = new File(super.getServlet().getServletPath(), requestUrl);

		boolean preview = request.getParameter("preview") != null;

		if(!absolutePath.exists()) {
			FileInfo fileInfo = new FileInfo();
			fileInfo.path = new string(relativePath.toString());
			fileInfo.name = new string(absolutePath.getName());
			fileInfo.id = new guid(parameters.get(Json.recordId));
			downloadFile(session.getServerInfo(), fileInfo, absolutePath);
		}

		if(preview) {
			if(FileConverter.isConvertableToPdf(absolutePath)) {
				absolutePath = getConvertedPdf(relativePath, absolutePath);
				response.addHeader("Content-Type", "application/pdf");
			} else if(FileConverter.isConvertableToTxt(absolutePath)) {
				absolutePath = getConvertedTxt(relativePath, absolutePath);
				response.addHeader("Content-Type", "text/plain; charset=UTF-8");
			} else
				response.addHeader("Content-Type", getContentType(absolutePath));
		} else {
			response.addHeader("Content-Type", "application/*");
			response.addHeader("Content-Disposition", getContentDisposition(request, absolutePath.getName()));
		}

		sendFileToResponse(response, absolutePath);
	}

	private String getContentType(File file) {
		String contentType = getServlet().getServletContext().getMimeType(file.getName().toLowerCase());

		if(contentType == null)
			return "text/plain";

		if(contentType.startsWith("text/")) {
			String encoding = IOUtils.determineEncoding(file, "UTF-8");
			contentType += "; charset=" + encoding;
		}

		return contentType;
	}

	private void downloadFile(ServerInfo serverInfo, FileInfo fileInfo, File path) throws IOException {
		FileInfo downloadedFileInfo = serverInfo.getApplicationServer().download(fileInfo);

		if(downloadedFileInfo != null)
			FileUtils.copyInputStreamToFile(downloadedFileInfo.getInputStream(), path);
		else
			throw new IOException("File '" + fileInfo.path.get() + "' does not exist");
	}

	public File getConvertedPdf(File relativePath, File srcFile) throws IOException {
		return getConverter().getConvertedPdf(relativePath.getPath(), srcFile);
	}

	public File getConvertedTxt(File relativePath, File srcFile) throws IOException {
		return getConverter().getConvertedTxt(relativePath.getPath(), srcFile);
	}

	private void sendFileToResponse(HttpServletResponse response, File file) throws IOException {
		try {
			IOUtils.copy(new FileInputStream(file), response.getOutputStream());
		} catch(IOException e) {
			String className = e.getClass().getCanonicalName();
			if(IgnoredExceptions.contains(className))
				Trace.logEvent(className);
			else
				throw e;
		}
	}

	private String getContentDisposition(HttpServletRequest request, String fileName) throws UnsupportedEncodingException {
		String agent = request.getHeader("USER-AGENT").toLowerCase();

		if(agent == null)
			return "attachment;filename=\"" + MimeUtility.encodeText(fileName, "utf8", "B") + "\"";

		if(agent.contains("msie"))
			return "attachment;filename=\"" + toHexString(fileName) + "\"";

		if(agent.contains("opera")) {
			int version = -1;

			try {
				int prefixIndex = agent.indexOf("opera ");

				if(prefixIndex == -1)
					prefixIndex = agent.indexOf("opera/");

				int startIndex = prefixIndex + "opera/".length();
				int stopIndex = agent.indexOf(".", startIndex);

				if(stopIndex == -1) {
					stopIndex = agent.indexOf(" ", startIndex);
				}

				version = new Integer(agent.substring(startIndex, stopIndex)).intValue();
			} catch(Exception ex) {
			}

			if(version < 9 && version > -1)
				// Opera 8.x and before
				return "attachment;filename=\"" + fileName + "\"";
			else
				// Opera 9 or later (or unkown) (encoding according to RFC2231)
				return "attachment;filename*=utf8''" + toHexString(fileName);
		}

		return "attachment;filename=\"" + MimeUtility.encodeText(fileName, "utf8", "B") + "\"";
	}

	private String toHexString(String s) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer();

		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if(0 <= c && c <= 255 && !Character.isWhitespace(c)) {
				sb.append(c);
			} else {
				byte[] bytes = Character.toString(c).getBytes("utf8");
				for(int j = 0; j < bytes.length; j++) {
					int k = bytes[j];
					if(k < 0)
						k += 256;
					sb.append("%" + Integer.toHexString(k).toUpperCase());
				}
			}
		}
		return sb.toString();
	}

	private FileConverter getConverter() {
		if(converter == null)
			converter = new FileConverter(new File(super.getServlet().getServletPath(), file.CacheFolderName));

		return converter;
	}

}
