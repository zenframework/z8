package org.zenframework.z8.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.z8.server.base.file.FileConverter;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.xml.GNode;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.web.servlet.Servlet;

public class ConverterAdapter extends Adapter {

	private final Servlet servlet;

	public ConverterAdapter(Servlet servlet) {
		this.servlet = servlet;
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
	protected void service(HttpServletRequest request, HttpServletResponse response, Map<String, String> parameters, List<file> files, ISession session) throws IOException {
		// URLDecoder.decode заменяет '+' на ' '
		String encodedUrl = request.getRequestURI().replaceAll("\\+", "%2b");
		String requestUrl = URLDecoder.decode(encodedUrl, encoding.Default.toString()).replaceAll("%23", "#");
		String contextPath = request.getContextPath() + '/';

		if(requestUrl.startsWith(contextPath))
			requestUrl = requestUrl.substring(contextPath.length());

		if (requestUrl.contains(".."))
			throw new IOException();

		File relativePath = new File(requestUrl);
		File absolutePath = null;
		if (requestUrl.startsWith(Files.Storage))
			absolutePath = new File(ServerConfig.storagePath(), requestUrl.substring(Files.Storage.length()));
		else
			absolutePath = new File(Folders.Base, requestUrl);

		boolean preview = parameters.containsKey(Json.preview.get());
		boolean noCache = parameters.containsKey(Json.noCache.get());
		boolean stamps = parameters.containsKey(FileConverter.Stamps.get());

		file file = null;

		if(noCache || !absolutePath.exists()) {
			file = new file();
			file.path = new string(relativePath.toString());
			file.name = new string(relativePath.getName());
			file.id = new guid(parameters.get(Json.id.get()));

			file = session.getServerInfo().getServer().download(session, new GNode(parameters, files), file);

			/*
			 * The storage folder may be shared between servlet and application
			 * server, so the previuos call could already put a copy of the file
			 * there
			 */

			if(!absolutePath.exists()) {
				InputStream in = file == null ? null : file.getInputStream();
				if(in == null)
					throw new IOException("File '" + relativePath + "' does not exist");
				IOUtils.copy(in, absolutePath);
			}
		}

		if (preview) {
			String ext = FileConverter.getExtension(absolutePath);
			if (FileConverter.isConvertableToPdf(ext)) {
				response.addHeader("Content-Type", "application/pdf");
				if (!FileConverter.isPdfExtension(ext) || stamps) {
					File convertedFile = new File(ServerConfig.storagePreviewPath(), relativePath.toString() + '.' + FileConverter.PDF);
					absolutePath = FileConverter.convert(absolutePath, convertedFile, parameters);
				}
			} else {
				response.addHeader("Content-Type", getContentType(absolutePath));
			}
		} else {
			response.addHeader("Content-Type", "application/*");
			String name = file != null ? file.name.get() : absolutePath.getName();
			response.addHeader("Content-Disposition", getContentDisposition(request, name));
		}

		response.addHeader("Content-Length", Long.toString(absolutePath.length()));
		response.addHeader("Accept-ranges", "bytes");

		IOUtils.copy(new FileInputStream(absolutePath), response.getOutputStream());
	}

	private String getContentType(File file) throws IOException {
		String contentType = servlet.getServletContext().getMimeType(file.getName().toLowerCase());

		if(contentType == null)
			return "text/plain";

		if(contentType.startsWith("text/")) {
			String encoding = IOUtils.determineEncoding(file, "UTF-8");
			contentType += "; charset=" + encoding;
		}

		return contentType;
	}

	private String getContentDisposition(HttpServletRequest request, String fileName) throws UnsupportedEncodingException {
		String agent = request.getHeader("USER-AGENT").toLowerCase();

		if(agent == null)
			return "attachment; filename=\"" + MimeUtility.encodeText(fileName, "utf8", "B") + "\"";

		if(agent.contains("msie"))
			return "attachment; filename=\"" + toHexString(fileName) + "\"";

		if(agent.contains("webkit"))
			return "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + URLEncoder.encode(fileName, encoding.Default.toString()).replace("+", "%20");

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

}
