package org.zenframework.z8.server.base.xml;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.types.encoding;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GNode implements Serializable {
    private static final long serialVersionUID = 6229467644994428114L;

    private Map<String, String> attributes = new HashMap<String, String>();

    private byte[] bytes = null;
    private List<FileInfo> files = new ArrayList<FileInfo>();

    public GNode() {
    }

    public GNode(Map<String, String> attributes, List<FileInfo> files) {
        this.attributes = attributes;
        this.files = files;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getString() {
        try {
            return new String(bytes, encoding.Default.toString());
        }
        catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(byte[] bytes) {
        this.bytes = bytes;
    }

    public void set(String value) {
        try {
            this.bytes = value.getBytes(encoding.Default.toString());
        }
        catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<FileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<FileInfo> files) {
        this.files = files;
    }


}
