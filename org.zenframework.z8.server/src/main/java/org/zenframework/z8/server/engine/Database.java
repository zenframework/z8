package org.zenframework.z8.server.engine;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Properties;

import org.zenframework.z8.server.base.table.system.Users;
import org.zenframework.z8.server.db.BasicSelect;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.Cursor;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.encoding;

public class Database implements Serializable {

    private static final long serialVersionUID = 6331151694463344646L;

    private String schema = null;
    private String user = null;
    private String password = null;
    private String connection = null;
    private String driver = null;
    private encoding charset = encoding.UTF8;
    private boolean systemInstalled = false;

    private DatabaseVendor vendor = DatabaseVendor.SqlServer;

    public Database() {}

    public Database(Properties prop) {
        setSchema(prop.getProperty("SCHEMA"));
        setUser(prop.getProperty("USER"));
        setPassword(prop.getProperty("PASSWORD"));
        setConnection(prop.getProperty("CONNECTION"));
        setDriver(prop.getProperty("DRIVER"));
        setCharset(encoding.fromString(prop.getProperty("CHARSET")));
    }

    public DatabaseVendor vendor() {
        return vendor;
    }

    public String schema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String password() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String user() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String connection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String driver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
        this.vendor = DatabaseVendor.fromString(driver);
    }

    public encoding charset() {
        return charset;
    }

    public void setCharset(encoding charset) {
        this.charset = charset;
    }

    public String tableName(String name) {
        return vendor.quote(schema()) + "." + vendor.quote(name);
    }

    public boolean tableExists(String name) {
        String sql = null;

        DatabaseVendor vendor = vendor();

        if (vendor == DatabaseVendor.SqlServer) {
            sql = "SELECT COUNT(TABLE_NAME) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_NAME = '" + name + "' AND " + 
                    "TABLE_CATALOG = '" + schema() + "'";
        } else if (vendor == DatabaseVendor.Postgres) {
            sql = "SELECT COUNT(table_name) FROM information_schema.tables " +
                    "WHERE table_name = '" + name + "' AND " +
                    "table_schema = '" + schema() + "'";
        } else if (vendor == DatabaseVendor.Oracle) {
            sql = "SELECT COUNT(TABLE_NAME) FROM ALL_TAB_COLUMNS " +
                    "WHERE TABLE_NAME = '" + name + "' AND " +
                    "OWNER = '" + schema() + "'";
        }

        Cursor cursor = null;
        
        try {
            cursor = BasicSelect.cursor(ConnectionManager.get(this), sql);
            return cursor.next() && cursor.getInteger(1).get() != 0;
        } catch (SQLException e) {
            Trace.logError(e);
            throw new RuntimeException(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }
    
    public boolean isSystemInstalled() {
        if (systemInstalled)
            return systemInstalled;

        try {
        	systemInstalled = tableExists(Users.TableName);
        } catch(Throwable e) {
        	Trace.logError(e);
        } finally {
        	ConnectionManager.release();
        }

    	return systemInstalled;
    }
    
/*    public String version() {
        return ServerRuntime.DbSchemeControlSumProperty.get.
    }*/
}
