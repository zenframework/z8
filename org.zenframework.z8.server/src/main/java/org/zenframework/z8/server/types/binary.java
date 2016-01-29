package org.zenframework.z8.server.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.types.sql.sql_binary;
import org.zenframework.z8.server.utils.IOUtils;

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

    public byte[] getBytes() {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                IOUtils.copy(stream, output);
                return output.toByteArray();
            } finally {
                stream.reset();
                output.close();
            }
        } catch (IOException e) {
            throw new exception(e);
        }
    }

    @Override
    public FieldType type() {
        return FieldType.Binary;
    }

    @Override
    public String toDbConstant(DatabaseVendor dbtype) {
        byte[] bytes = getBytes();

        switch (dbtype) {
            case Postgres:
            case Oracle:
                return bytes.length != 0 ? "'" + new BigInteger(bytes).toString(16) + "'" : "null";

            case SqlServer: {
                String hex = new BigInteger(bytes).toString(16);

                if (hex.length() % 2 == 1)
                    hex = "0" + hex;

                return "0x" + hex;
            }

            default:
                return "'" + getString() + "'";
        }
    }

    @Override
    public String toDbString(DatabaseVendor dbtype) {
        return getString();
    }

    public String getString() {
        try {
            return new String(getBytes(), encoding.Default.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
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