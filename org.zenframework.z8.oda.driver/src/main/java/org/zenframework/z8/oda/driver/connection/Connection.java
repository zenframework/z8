package org.zenframework.z8.oda.driver.connection;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.datatools.connectivity.oda.OdaException;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.config.AppServerConfig;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.config.SystemProperty;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Database;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.IRuntime;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.User;

public class Connection {
    public static String RuntimeClass = "org.zenframework.z8.server.engine.Runtime";
    public static String UserCodeJarPath = "WEB-INF/lib/usercode.jar";
    public static String UserCodeClassesPath = "WEB-INF/classes/";

    private Database database = null;
    private String url = null;
    private Map<String, UserProfile> users = new HashMap<String, UserProfile>();

    private static boolean isInRuntimeMode = false;

    static {
        isInRuntimeMode = checkRuntimeMode();
    }

    public static synchronized Connection connect(String url, String user, String password) throws OdaException {
        Connection connection = new Connection(url);
        if(!isInRuntimeMode()) {
            connection.open();
            connection.login(user, password);
        }
        return connection;
    }

    public static synchronized void disconnect(Connection connection) {
        connection.close();
    }

    private static boolean checkRuntimeMode() {
        try {
            Class<?> persistentListClass = Connection.class.getClassLoader().loadClass(RuntimeClass);

            if(persistentListClass != null) {
                return true;
            }
        }
        catch(ClassNotFoundException e) {}

        return false;
    }

    public static boolean isInRuntimeMode() {
        return isInRuntimeMode;
    }

    public String getUrl() {
        return url;
    }

    public UserProfile getUserProfile(String login) {
        assert (users != null);
        return users.get(login);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Query getDataSet(String className) throws OdaException {
        ClassLoader classLoader = Runtime.instance().getClass().getClassLoader();

        try {
            Class<Query> queryClass = (Class<Query>)classLoader.loadClass(className);

            for(Class innerClass : queryClass.getClasses()) {
                if(innerClass.getSimpleName().equals("CLASS")) {
                    OBJECT container = new OBJECT();
                    CLASS cls = (CLASS)innerClass.getDeclaredConstructor(IObject.class).newInstance(container);
                    return (Query)cls.get();
                }
            }
        }
        catch(Throwable e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private Connection(String url) throws OdaException {
        this.url = url;
    }

    private void open() throws OdaException {
        if(!isInRuntimeMode()) {
            System.setProperty(SystemProperty.ConfigFilePath, new File(getUrl(), "WEB-INF").toString());

            URLClassLoader classLoader = null;

            try {
                Class<?> runtimeClass = null;

                URL userCodeJarUrl = new File(getUrl(), UserCodeJarPath).toURI().toURL();
                URL userCodeClassesUrl = new File(getUrl(), UserCodeClassesPath).toURI().toURL();
                classLoader = new URLClassLoader(new URL[] { userCodeClassesUrl, userCodeJarUrl }, getClass()
                        .getClassLoader());

                runtimeClass = classLoader.loadClass(RuntimeClass);
                Runtime.set((IRuntime)runtimeClass.newInstance());
            }
            catch(Throwable e) {
                close();
                throw new OdaException(e);
            }
            finally {}
        }
    }

    private void login(String login, String password) throws OdaException {
        if(!isInRuntimeMode()) {
            assert (users != null);

            UserProfile profile = users.get(login);
            if(profile == null) {
                ServerConfig config = new AppServerConfig();

                database = new Database(config);

                IUser user = User.load(login, password, false, database);

                profile = new UserProfile(user);
                users.put(login, profile);
            }
        }
    }

    private void close() {
        if(!isInRuntimeMode()) {
            Runtime.set(null);
            ApplicationServer.setRequest(null);
        }
    }
}
