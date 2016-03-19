package org.zenframework.z8.server.base.xml;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.types.encoding;

public class GNode implements Serializable {
    private static final long serialVersionUID = 6229467644994428114L;

    private Map<String, String> attributes;
    private List<FileInfo> files;
    private byte[] content = null;

    public GNode(String content) {
        try {
        	this.content = content.getBytes(encoding.Default.toString());
        }
        catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public GNode(Map<String, String> attributes, List<FileInfo> files) {
        this.attributes = attributes;
        this.files = files;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public byte[] getContent() {
        return content;
    }

    public List<FileInfo> getFiles() {
        return files;
    }
}
