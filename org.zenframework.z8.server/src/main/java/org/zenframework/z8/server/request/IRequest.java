package org.zenframework.z8.server.request;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.server.types.string;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

public abstract class IRequest {
    abstract public Map<String, String> getParameters();
    abstract public List<FileInfo> getFiles();

    private int events = 0;
    
    public String getParameter(string key) {
        return getParameter(key.get());
    }
    
    public String getParameter(String key) {
        Map<String, String> parameters = getParameters();
        return parameters != null ? parameters.get(key) : null;
    }

    public String id() {
        try {
            String id = getParameter(Json.requestId);
            return id != null ? URLDecoder.decode(id, encoding.UTF8.toString()) : null;
        }
        catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void disableEvents() {
        events++;
    }
    
    public void enableEvents() {
        events--;
    }

    public boolean events() {
        return events == 0;
    }

    abstract public IResponse getResponse();

    abstract public ISession getSession();

    abstract public IMonitor getMonitor();

    abstract public void setMonitor(IMonitor monitor);
}
