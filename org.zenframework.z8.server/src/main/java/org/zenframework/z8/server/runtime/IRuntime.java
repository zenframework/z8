package org.zenframework.z8.server.runtime;

import java.util.Collection;

import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Property;

public interface IRuntime {
    public Collection<CLASS<? extends Table>> tables();

    public Collection<CLASS<? extends OBJECT>> entries();

    public Collection<CLASS<? extends Procedure>> jobs();
    
    public Collection<Property> properties();

    public CLASS<? extends Table> getTable(String className);

    public CLASS<? extends OBJECT> getEntry(String className);

    public CLASS<? extends Procedure> getJob(String className);
}
