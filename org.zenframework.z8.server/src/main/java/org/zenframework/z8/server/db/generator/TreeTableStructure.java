package org.zenframework.z8.server.db.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.table.TreeTable;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class TreeTableStructure {
    private TreeTable table = null;
    private Map<guid, guid> tree = new HashMap<guid, guid>();

    public TreeTableStructure(TreeTable table) {
        this.table = table;
    }

    public void run() {
        initTree();
        correctTree();
    }

    private void initTree() {
        Collection<Field> fields = new ArrayList<Field>();
        fields.add(table.primaryKey());
        fields.add(table.parentKey());
        
        table.read(fields);

        while(table.next()) {
            guid id = table.recordId.get().guid();

            if(!id.equals(guid.NULL)) {
                guid parentId = table.parentId.get().guid();

                if(!parentId.equals(id)) {
                    tree.put(id, parentId);
                }
                else {
                    System.out.println(table.displayName() + ": parentId equals recordId: " + id.toString());
                }
            }
        }
    }

    private void correctTree() {
        table.keepIntegrity.set(false);

        for(guid id : tree.keySet()) {
            String path = getPath(id);
            guid[] parents = parsePath(path);

            path += (path.isEmpty() ? "" : ".") + id;

            guid parent0 = parents.length == 0 ? guid.NULL : parents[0];
            guid root = parent0.equals(guid.NULL) ? id : parent0;

            table.path.get().set(new string(path));
            table.root.get().set(root);
            
            table.parent1.get().set(parents.length > 0 ? parents[0] : null);
            table.parent2.get().set(parents.length > 1 ? parents[1] : null);
            table.parent3.get().set(parents.length > 2 ? parents[2] : null);
            table.parent4.get().set(parents.length > 3 ? parents[3] : null);
            table.parent5.get().set(parents.length > 4 ? parents[4] : null);
            table.parent6.get().set(parents.length > 5 ? parents[5] : null);
            
            table.update(id);
        }
    }

    private String getPath(guid id) {
        String path = "";

        while(id != null) {
            guid parentId = tree.get(id);

            if(parentId != null && !parentId.equals(guid.NULL)) {
                path = parentId + (path.isEmpty() ? "" : ".") + path;
            }

            id = parentId;
        }

        return path;
    }

    private guid[] parsePath(String path) {
        List<guid> result = new ArrayList<guid>();

        if(!path.isEmpty()) {
            String[] levels = path.split("\\.");

            for(String level : levels) {
                result.add(new guid(level));
            }
        }

        return result.toArray(new guid[0]);
    }

}
