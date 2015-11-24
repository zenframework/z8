package org.zenframework.z8.server.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;

import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.FieldType;

public class Column {
    public static final int DEFAULT_COLUMN_WIDTH = 100;

    private float width;
    private String displayName;

    private Field field;
    private boolean isIndentationColumn = false;

    private Column parent;
    private List<Column> subcolumns = new ArrayList<Column>();

    public Column() {
        this(null, 0);
    }

    public Column(String caption) {
        this(caption, DEFAULT_COLUMN_WIDTH);
    }

    public Column(String caption, int width) {
        this(caption, Math.max(DEFAULT_COLUMN_WIDTH, width), false);
    }

    private Column(String caption, int width, boolean isIndentationColumn) {
        this.displayName = caption;
        this.width = width;
        this.isIndentationColumn = isIndentationColumn;
    }

    public static Column createIndentationColumn(int width) {
        assert (width > 0);
        return new Column(null, width, true);
    }

    public String getName() {
        return field != null ? field.id() : null;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : getName();
    }

    public Column getParent() {
        return parent;
    }

    public void setParent(Column parent) {
        this.parent = parent;
    }

    public boolean isIndentation() {
        return isIndentationColumn;
    }

    public void addColumn(Column column, int index) {
        column.parent = this;
        subcolumns.add(index, column);
    }

    public void addColumn(Column[] columns) {
        for(Column column : columns)
            addColumn(column);
    }

    public void addColumn(Column column) {
        column.parent = this;
        subcolumns.add(column);
    }

    public void removeColumn(Column column) {
        column.parent = null;
        subcolumns.remove(column);
    }

    public boolean hasSubcolumns() {
        return subcolumns.size() > 0;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public int getDepth() {
        int depth = 0;

        for(Column subColumn : subcolumns) {
            depth = Math.max(subColumn.getDepth(), depth);
        }

        return depth + 1;
    }

    public int getHeight() {
        int height = 0;

        Column column = this;

        while(column.getParent() != null) {
            height++;
            column = column.getParent();
        }

        return height;
    }

    // calculates width of the table in cells

    public int getColspan() {
        if(subcolumns.size() == 0) {
            return 1;
        }

        int width = 0;

        for(Column column : subcolumns) {
            width += column.getColspan();
        }

        return width;
    }

    public LinkedList<Column> getLayer(int depth) {
        LinkedList<Column> layer = new LinkedList<Column>();
        LinkedList<Column> stack = new LinkedList<Column>();

        stack.addFirst(this);

        while(stack.size() > 0) {
            Column column = stack.poll();

            if(column.getHeight() < depth) {
                if(column.hasSubcolumns()) {
                    LinkedList<Column> lst = new LinkedList<Column>();

                    for(Column col : column.subcolumns) {
                        lst.add(col);
                    }

                    //(LinkedList<Column>)column.subcolumns.clone();

                    Collections.reverse(lst);

                    for(Column col : lst) {
                        stack.addFirst(col);
                    }
                }
                else {
                    layer.add(column);
                }
            }

            else if(!column.hasSubcolumns() || column.getHeight() == depth) {

                layer.add(column);
            }
        }
        return layer;
    }

    public float getTotalWidth() {
        float width = 0;

        if(hasSubcolumns()) {

            for(Column subcolumn : subcolumns) {
                width += subcolumn.getTotalWidth();
            }
        }
        else {
            width = this.width;
        }

        return width;
    }

    public float getWidth() {
        return width;
    }

    public Column[] getColumns() {
        List<Column> columns = new ArrayList<Column>();

        for(Column column : subcolumns) {
            if(column.hasSubcolumns()) {
                Column[] subcolumns = column.getColumns();

                for(Column subcolumn : subcolumns) {
                    columns.add(subcolumn);
                }
            }
            else {
                columns.add(column);
            }
        }

        return columns.toArray(new Column[0]);
    }

    public Column[] getSubcolumns() {
        return subcolumns.toArray(new Column[0]);
    }

    public String getStringValue() {
        if(!isIndentation()) {
            return field.toString();
        }

        return "";
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getHorizontalAlignment() {
        String ret = DesignChoiceConstants.TEXT_ALIGN_LEFT;

        if(field.type() == FieldType.Decimal || field.type() == FieldType.Integer || field.type() == FieldType.Datespan) {
            ret = DesignChoiceConstants.TEXT_ALIGN_RIGHT;
        }
        if(field.type() == FieldType.Boolean) {
            ret = DesignChoiceConstants.TEXT_ALIGN_CENTER;
        }
        return ret;
    }

    public Aggregation getAggregation() {
        if(field == null)
            return Aggregation.None;
        
        FieldType type = field.type();
        return type != FieldType.String && type != FieldType.Text && type != FieldType.Boolean ? field.aggregation : Aggregation.None;
    }

    public boolean hasAggregation() {
        return getAggregation() != Aggregation.None;
    }
}
