package org.zenframework.z8.server.ie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private static final List<Transport> TRANSPORTS = Arrays.<Transport> asList(new FileTransport(), new JmsTransport());
    private static final Map<String, Transport> PROTOCOL_TRANSPORTS;
    private static TransportEngine INSTANCE;

    private final List<String> enabledProtocols = getEnabledProtocols(Properties
            .getProperty(ServerRuntime.EnableProtocolsProperty));

    static {
        Map<String, Transport> map = new HashMap<String, Transport>();
        for (Transport t : TRANSPORTS) {
            map.put(t.getProtocol(), t);
        }
        PROTOCOL_TRANSPORTS = Collections.unmodifiableMap(map);
    }

    private TransportEngine() {
        Properties.addListener(this);
    }

    public Transport getTransport(String protocol) {
        synchronized (enabledProtocols) {
            return enabledProtocols.contains(protocol) ? PROTOCOL_TRANSPORTS.get(protocol) : null;
        }
    }

    public List<Transport> getEnabledTransports() {
        synchronized (enabledProtocols) {
            List<Transport> transports = new ArrayList<Transport>(enabledProtocols.size());
            for (String protocol : enabledProtocols) {
                if (PROTOCOL_TRANSPORTS.containsKey(protocol)) {
                    transports.add(PROTOCOL_TRANSPORTS.get(protocol));
                }
            }
            return transports;
        }
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

    private void start() {
        for (Transport t : getEnabledTransports()) {
            t.init();
        }
    }

    public void stop() {
        for (Transport t : getEnabledTransports()) {
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

}
