package org.zenframework.z8.server.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.simple.Runnable;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Property;

public abstract class AbstractRuntime implements IRuntime {

    private List<CLASS<? extends Table>> tables = new ArrayList<CLASS<? extends Table>>();
    private List<CLASS<? extends OBJECT>> entries = new ArrayList<CLASS<? extends OBJECT>>();
    private List<CLASS<? extends Procedure>> jobs = new ArrayList<CLASS<? extends Procedure>>();
    private List<CLASS<? extends Runnable>> activators = new ArrayList<CLASS<? extends Runnable>>();
    private List<Property> properties = new ArrayList<Property>();

    @Override
    public Collection<CLASS<? extends Table>> tables() {
        return tables;
    }

    @Override
    public Collection<CLASS<? extends OBJECT>> entries() {
        return entries;
    }

    @Override
    public Collection<CLASS<? extends Procedure>> jobs() {
        return jobs;
    }

    @Override
    public Collection<CLASS<? extends Runnable>> activators() {
        return activators;
    }

    @Override
    public Collection<Property> properties() {
        return properties;
    }

    @Override
    public CLASS<? extends Table> getTable(String name) {
        return get(name, tables);
    }

    @Override
    public CLASS<? extends OBJECT> getEntry(String name) {
        return get(name, entries);
    }

    @Override
    public CLASS<? extends Procedure> getJob(String name) {
        return get(name, jobs);
    }

    private static <T extends IObject> CLASS<? extends T> get(String name, Collection<CLASS<? extends T>> list) {
        for (CLASS<? extends T> cls : list) {
            if (name.equals(cls.classId())) {
                return cls;
            }
        }
        return null;
    }
    
    protected void addTable(CLASS<? extends Table> cls) {
        for (CLASS<? extends Table> table : tables) {
            if (table.classId().equals(cls.classId()))
                return;
        }
        tables.add(cls);
    }
    
    protected void addEntry(CLASS<? extends OBJECT> cls) {
        for (CLASS<? extends OBJECT> entry : entries) {
            if (entry.classId().equals(cls.classId()))
                return;
        }
        entries.add(cls);
    }

    protected void addJob(CLASS<? extends Procedure> cls) {
        for (CLASS<? extends Procedure> job : jobs) {
            if (job.classId().equals(cls.classId()))
                return;
        }
        jobs.add(cls);
    }
    
    protected void addActivator(CLASS<? extends Runnable> cls) {
        for (CLASS<? extends Runnable> activator : activators) {
            if (activator.classId().equals(cls.classId()))
                return;
        }
        activators.add(cls);
    }
    
    protected void addProperty(Property property) {
        if (!properties.contains(property))
            properties.add(property);
    }

    protected void mergeWith(IRuntime runtime) {
        for (CLASS<? extends Table> table : runtime.tables()) {
            addTable(table);
        }
        for (CLASS<? extends Procedure> job : runtime.jobs()) {
            addJob(job);
        }
        for (CLASS<? extends OBJECT> entry : runtime.entries()) {
            addEntry(entry);
        }
        for (CLASS<? extends Runnable> activator : runtime.activators()) {
            addActivator(activator);
        }
        for (Property property : runtime.properties()) {
            addProperty(property);
        }
    }

}
