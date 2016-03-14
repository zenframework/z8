package org.zenframework.z8.server.types;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.types.sql.sql_binary;

public class binary extends primary {
    private static final long serialVersionUID = -5161828489385386903L;

    private InputStream stream;

    public binary() {
        this(new byte[0]);
    }

    public binary(byte[] data) {
        set(data);
    }

    public binary(InputStream stream) {
        set(stream);
    }

    public binary(binary binary) {
        set(binary);
    }

    public binary(File file) {
        set(file);
    }

    public binary(string string) {
        set(string != null ? string.getBytes(encoding.Default) : new byte[0]);
    }

    public binary(String s) {
        this(new string(s));
    }

    private void set(byte[] bytes) {
        set(new ByteArrayInputStream(bytes));
    }

    private void set(binary binary) {
        if(this.stream != binary.stream) {
            close();
            this.stream = binary.stream;
        }
    }

    private void set(File file) {
        try {
            set(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void set(InputStream stream) {
        if(this.stream != stream) {
            close();
            this.stream = stream;
        }
    }
    
    public InputStream get() {
        return stream;
    }

    @Override
    public binary defaultValue() {
        return new binary();
    }

    private void close() {
        try {
            if(stream != null) {
                stream.close();
                stream = null;
            }
        } catch(IOException e) {
            throw new exception(e);
        }
    }

    @Override
    public FieldType type() {
        return FieldType.Binary;
    }

    @Override
    public String toDbConstant(DatabaseVendor dbtype) {
        switch (dbtype) {
            case Postgres:
            case Oracle:
                return "null";
            default:
                return "''";
        }
    }

    @Override
    public String toString() {
        return "binary value";
    }

    public sql_binary sql_binary() {
        return new sql_binary(this);
    }
}