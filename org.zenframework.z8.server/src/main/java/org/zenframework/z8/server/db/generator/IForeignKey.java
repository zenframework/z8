package org.zenframework.z8.server.db.generator;

import org.zenframework.z8.server.base.table.ITable;
import org.zenframework.z8.server.base.table.value.IField;

public interface IForeignKey {
    public ITable getReferencedTable();

    public IField getFieldDescriptor();

    public IField getReferer();
}
