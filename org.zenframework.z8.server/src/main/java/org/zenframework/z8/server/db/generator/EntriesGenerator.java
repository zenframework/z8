package org.zenframework.z8.server.db.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.form.Desktop;
import org.zenframework.z8.server.base.table.system.Entries;
import org.zenframework.z8.server.base.table.system.UserEntries;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.security.Component;
import org.zenframework.z8.server.types.guid;

public class EntriesGenerator {
    public EntriesGenerator() {
    }

    public Component[] readComponents() {
        Entries entries = new Entries.CLASS<Entries>().get();

        Collection<Field> fields = new ArrayList<Field>();
        fields.add(entries.id.get());
        fields.add(entries.name.get());

        List<Component> components = new ArrayList<Component>();

        entries.read(fields);

        while(entries.next()) {
            String recordId = entries.recordId().toString();
            String className = entries.id.get().string().get();
            String title = entries.name.get().string().get();
            components.add(new Component(recordId, className, title));
        }

        return components.toArray(new Component[0]);
    }

    public void run(Collection<Desktop.CLASS<? extends Desktop>> entryClasses, ILogger logger) {
        logger.progress(0);

        Component[] existingComponents = readComponents();

        List<Component> toDelete = new ArrayList<Component>();
        List<Component> toUpdate = new ArrayList<Component>();
        List<Component> toCreate = new ArrayList<Component>();

        for(Component component : existingComponents) {
            boolean found = false;

            for(Desktop.CLASS<? extends Desktop> cls : entryClasses) {
                if(component.className().equals(cls.classId())) {
                    toUpdate.add(new Component(component.id(), cls.classId(), cls.displayName()));
                    found = true;
                    break;
                }
            }

            if(!found) {
                toDelete.add(component);
            }
        }

        for(Desktop.CLASS<? extends Desktop> cls : entryClasses) {
            boolean found = false;

            for(Component component : existingComponents) {
                if(component.className().equals(cls.classId())) {
                    found = true;
                    break;
                }
            }

            if(!found) {
                toCreate.add(new Component(null, cls.classId(), cls.displayName()));
            }
        }

        UserEntries userEntries = new UserEntries.CLASS<UserEntries>().get();
        Entries entries = new Entries.CLASS<Entries>().get();

        for(Component component : toDelete) {
            SqlToken where = new Equ(userEntries.entry.get(), new guid(component.id()));

            userEntries.destroy(where);
            entries.destroy(new guid(component.id()));
        }

        for(Component component : toUpdate) {
            entries.id.get().set(component.className());
            entries.name.get().set(component.title());
            entries.update(new guid(component.id()));
        }

        for(Component component : toCreate) {
            entries.id.get().set(component.className());
            entries.name.get().set(component.title());
            entries.create();
        }

        logger.progress(100);
    }
}
