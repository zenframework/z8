package org.zenframework.z8.server.ie;


public abstract class AbstractTransport implements Transport {

    @Override
    public void init() {}

    @Override
    public void shutdown() {}

    @Override
    public String getUrl(String address) {
        return new StringBuilder(20).append(getProtocol()).append("://").append(address).toString();
    }

}
