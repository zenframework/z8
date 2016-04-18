package org.zenframework.z8.server.db.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class TableDescription {
    private String name;
    private boolean isView;
    private Collection<Column> columns = new ArrayList<Column>();
    private PrimaryKey primaryKey;
    private Collection<Index> indices = new ArrayList<Index>();
    private Collection<Index> uniqueIndices = new ArrayList<Index>();
    private Collection<ForeignKey> links = new HashSet<ForeignKey>();

    public TableDescription(String name, boolean isView) {
        this.name = name;
        this.isView = isView;
    }

    public String getName() {
        return name;
    }

    public boolean isView() {
        return isView;
    }

    public Collection<Column> getColumns() {
        return columns;
    }

    public PrimaryKey getPK() {
        return primaryKey;
    }

    public Collection<Index> getIndexes() {
        return indices;
    }

    public Collection<Index> getUniqueIndexes() {
        return uniqueIndices;
    }

    public Collection<ForeignKey> getRelations() {
        return links;
    }

    void addField(Column field) {
        columns.add(field);
    }

    void setPK(PrimaryKey pk) {
        primaryKey = pk;
    }

    void addIndex(Index idx) {
        indices.add(idx);
    }

    void addUniqueIndex(Index idx) {
        uniqueIndices.add(idx);
    }

    void addLink(ForeignKey relation) {
        links.add(relation);
    }
}
