package org.zenframework.z8.server.config;

import org.zenframework.z8.server.engine.Rmi;

public class AuthCenterConfig extends ServerConfig {
    private static final long serialVersionUID = -6555889512753930499L;

    public AuthCenterConfig() {
        super();
    }

    @Override
    protected void init() {
        super.init();
        authorityCenterHost = Rmi.localhost;
    }
}
