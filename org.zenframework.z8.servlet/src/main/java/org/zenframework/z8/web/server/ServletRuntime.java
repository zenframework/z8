package org.zenframework.z8.web.server;

import org.zenframework.z8.server.base.table.system.Property;
import org.zenframework.z8.server.runtime.AbstractRuntime;

public class ServletRuntime extends AbstractRuntime {

    public static final Property LibreOfficeDirectoryProperty = new Property("AA1B9A42-DB3B-45C0-BD0D-CC13EC1BA6BE",
            "z8.servlet.libreOfficeDirectory", "C:/Program Files (x86)/LibreOffice 4.0", "Путь к каталогу установки LibreOffice");

    public static final Property TrustLocalOnlyProperty = new Property("D07FA08E-8E16-4994-B042-4F4A30DC9950",
            "z8.servlet.trustLocalOnly", "true", "Доверенная аутентификация только через локальное соединение (через 'wall')");

    public ServletRuntime() {
        addProperty(LibreOfficeDirectoryProperty);
        addProperty(TrustLocalOnlyProperty);
    }

}
