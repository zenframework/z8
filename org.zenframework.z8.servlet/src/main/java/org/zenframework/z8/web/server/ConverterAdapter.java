package org.zenframework.z8.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeUtility;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.converter.FileConverter;
import org.zenframework.z8.server.engine.IServerInfo;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.web.servlet.Servlet;

public class ConverterAdapter extends Adapter {

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
		FileConverter.stopOfficeManager();
	}

	@Override
	protected void service(ISession session, Map<String, String> parameters, List<file> files, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// URLDecoder.decode заменяет '+' на ' '
		String encodedUrl = request.getRequestURI().replaceAll("\\+", "%2b");
		String requestUrl = URLDecoder.decode(encodedUrl, encoding.Default.toString());
		String contextPath = request.getContextPath() + '/';

		if(requestUrl.startsWith(contextPath))
			requestUrl = requestUrl.substring(contextPath.length());

		File relativePath = new File(requestUrl);
		File absolutePath = new File(super.getServlet().getServletPath(), requestUrl);

		boolean preview = parameters.containsKey(Json.preview);

		file file = null;

		if(!absolutePath.exists()) {
			file = new file();
			file.path = new string(relativePath.toString());
			file.name = new string(relativePath.getName());
			file.id = new guid(parameters.get(Json.id));
			file = downloadFile(session.getServerInfo(), file, absolutePath);
		}

		if(preview) {
			if(FileConverter.isConvertableToPdf(absolutePath)) {
				absolutePath = getConverter().getConvertedPdf(relativePath.getPath(), absolutePath, parameters);
				response.addHeader("Content-Type", "application/pdf");
			} else
				response.addHeader("Content-Type", getContentType(absolutePath));
		} else {
			response.addHeader("Content-Type", "application/*");
			String name = file != null ? file.name.get() : absolutePath.getName();
			response.addHeader("Content-Disposition", getContentDisposition(request, name));
		}

		response.addHeader("Content-Length", Long.toString(absolutePath.length()));
		response.addHeader("Accept-ranges", "bytes");

		IOUtils.copy(new FileInputStream(absolutePath), response.getOutputStream());
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

	private file downloadFile(IServerInfo serverInfo, file file, File path) throws IOException {
		file downloadedFile = serverInfo.getServer().download(file);

		/*
		 * The storage folder may be shared between servlet and application
		 * server, so the previuos call could already put a copy of the file
		 * there
		 */

		if(!path.exists()) {
			InputStream in = downloadedFile == null ? null : downloadedFile.getInputStream();
			if(in != null)
				IOUtils.copy(in, path);
			else
				throw new IOException("File '" + file.name.get() + "' does not exist");
		}

		return downloadedFile;
	}

	private String getContentDisposition(HttpServletRequest request, String fileName) throws UnsupportedEncodingException {
		String agent = request.getHeader("USER-AGENT").toLowerCase();

		if(agent == null)
			return "attachment; filename=\"" + MimeUtility.encodeText(fileName, "utf8", "B") + "\"";

		if(agent.contains("msie"))
			return "attachment; filename=\"" + toHexString(fileName) + "\"";

		if(agent.contains("webkit"))
			return "attachment; filename*=\"" + fileName + "\"";

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

		return "attachment; filename=\"" + MimeUtility.encodeText(fileName, "utf8", "B") + "\"";
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
			converter = new FileConverter(new File(super.getServlet().getServletPath(), Folders.Cache));

		return converter;
	}

}
