package org.zenframework.z8.server.base.table;

import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.generator.IForeignKey;
import org.zenframework.z8.server.types.primary;

public interface ITable {
	public String name();

	public String displayName();

	public void initStaticRecords();

	public Collection<Map<IField, primary>> getStaticRecords();

	public Collection<IForeignKey> getForeignKeys();

	public Collection<IField> getIndices();

	public Collection<IField> getUniqueIndices();
}
