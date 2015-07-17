package org.zenframework.z8.web.server;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.file.FilesStorage;
import org.zenframework.z8.server.base.model.actions.Action;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.engine.ServerInfo;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.web.servlet.Servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

// Access restriction: import sun.misc.BASE64Decoder; instead: org.apache.commons.codec.binary.Base64

public class JsonAdapter extends Adapter {

    private static final String FilesStoragePath = "files";
    private static final Object AdapterPath = "/request.json";

    public JsonAdapter(Servlet servlet) {
        super(servlet);
    }

    @Override
    public boolean canHandleRequest(HttpServletRequest request) {
        return request.getServletPath().equals(AdapterPath);
    }

    @Override
    protected void service(ISession session, Map<String, String> parameters, List<FileInfo> files,
            HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String path = parameters.get(Json.path);

        if (path == null) {
            if (Action.attachAction.equalsIgnoreCase(parameters.get(Json.action)))
                processUploadRequest(parameters, files);
            super.service(session, parameters, files, request, response);
        } else
            downloadFile(session.getServerInfo(), path, parameters, response);
    }

    private void processUploadRequest(Map<String, String> parameters, List<FileInfo> files) throws IOException, ServletException {

        String recordId = parameters.get(Json.recordId);
        String requestId = parameters.get(Json.requestId);

        FilesStorage filesStorage = new FilesStorage(getServlet().getWebInfPath());

        for (FileInfo info : files) {
            String fileName = FileUtils.getFile(file.StorageFolder, requestId, recordId, info.name.get()).toString();
            filesStorage.save(info, fileName);
        }
    }

    //TODO:протестировать!
    private void downloadFile(ServerInfo serverInfo, String filePath, Map<String, String> parameters,
            HttpServletResponse response) throws IOException {

        FilesStorage storage = getFilesStorage();
        if (parameters.get(Json.serverId) != null) {
            storage.save(serverInfo.getAppServer().download(filePath).getInputStream(), filePath);
        } else {
            storage.save(Base64.decodeBase64(parameters.get(Json.image)), filePath);
        }

        JsonWriter writer = new JsonWriter();
        writer.startResponse(null, true);
        writer.writeProperty(new string(Json.source), filePath);
        writer.startArray(Json.data);
        writer.finishArray();
        writer.finishResponse();

        writeResponse(response, writer.toString().getBytes(encoding.Default.toString()));
    }

    private void writeError(HttpServletResponse response, String errorText, int status) throws IOException {
        JsonWriter writer = new JsonWriter();

        if (errorText == null || errorText.isEmpty()) {
            errorText = "Internal server error.";
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }

        writer.startResponse(null, false, status);
        writer.writeInfo(errorText);
        writer.startArray(Json.data);
        writer.finishArray();
        writer.finishResponse();

        writeResponse(response, writer.toString().getBytes(encoding.Default.toString()));
    }

    @Override
    protected void processAccessDenied(HttpServletResponse response) throws IOException {
        super.processAccessDenied(response);
        writeError(response, Resources.get("Exception.accessDenied"), HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    protected void processError(HttpServletResponse response, Throwable ex) throws IOException {
        writeError(response, ex.getMessage(), 0);
    }

    protected FilesStorage getFilesStorage() {
        return new FilesStorage(new File(getServlet().getWebInfPath(), FilesStoragePath));
    }

}
