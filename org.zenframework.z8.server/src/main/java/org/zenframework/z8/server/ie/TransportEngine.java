package org.zenframework.z8.server.ie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.runtime.ServerRuntime;

public class TransportEngine implements Properties.Listener {

    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(TransportEngine.class);

    private static TransportEngine INSTANCE;

    private final List<String> enabledProtocols = getEnabledProtocols(Properties
            .getProperty(ServerRuntime.EnableProtocolsProperty));
    private final Map<String, Transport> transports = new HashMap<String, Transport>();

    private TransportEngine() {
        Properties.addListener(this);
    }

    public synchronized Transport getTransport(TransportContext context, String protocol) {
        Transport transport= null;
        if (enabledProtocols.contains(protocol)) {
            String transportId = transportId(context, protocol);
            transport = transports.get(transportId);
            if (transport == null) {
                transport = createTransport(context, protocol);
                transports.put(transportId, transport);
            }
        }
        return transport;
    }

    public synchronized List<String> getEnabledProtocols() {
        return new ArrayList<String>(enabledProtocols);
    }
    
    public List<Transport> getEnabledTransports(TransportContext context) {
        List<String> protocols = getEnabledProtocols();
        List<Transport> transports = new ArrayList<Transport>(protocols.size());
        for (String protocol : protocols) {
            transports.add(getTransport(context, protocol));
        }
        return transports;
    }

    @Override
    public void onPropertyChange(String key, String value) {
        if (ServerRuntime.EnableProtocolsProperty.equalsKey(key)) {
            stop();
            synchronized (enabledProtocols) {
                enabledProtocols.clear();
                enabledProtocols.addAll(getEnabledProtocols(value));
            }
            start();
        }
    }

    private synchronized void start() {
        for (Transport t : transports.values()) {
            t.init();
        }
    }

    public synchronized void stop() {
        for (Transport t : transports.values()) {
            t.shutdown();
        }
    }

    public static TransportEngine getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TransportEngine();
            INSTANCE.start();
        }
        return INSTANCE;
    }

    private static List<String> getEnabledProtocols(String list) {
        String strs[] = list.split("\\,");
        List<String> protocols = new ArrayList<String>(strs.length);
        for (String s : strs) {
            s = s.trim().toLowerCase();
            if (s.length() > 0) {
                protocols.add(s);
            }
        }
        return protocols;
    }

    private static String transportId(TransportContext context, String protocol) {
        return context.getProperty(TransportContext.SelfAddressProperty) + '/' + protocol;
    }
    
    private static Transport createTransport(TransportContext context, String protocol) {
        if (JmsTransport.PROTOCOL.equals(protocol)) {
            return new JmsTransport(context);
        } else if (FileTransport.PROTOCOL.equals(protocol)) {
            return new FileTransport(context);
        } else {
            return null;
        }
    }

}
