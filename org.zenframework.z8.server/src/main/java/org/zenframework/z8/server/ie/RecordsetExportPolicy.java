package org.zenframework.z8.server.ie;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;

public class RecordsetExportPolicy extends OBJECT {

	public static class CLASS<T extends RecordsetExportPolicy> extends OBJECT.CLASS<T> {

		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(RecordsetExportPolicy.class);
			setName(RecordsetExportPolicy.class.getName());
			setDisplayName(RecordsetExportPolicy.class.getName());
		}

		@Override
		public Object newObject(IObject container) {
			return new RecordsetExportPolicy(container);
		}

	}

	private final Map<guid, RecordExportPolicy> exportPolicies = new HashMap<guid, RecordExportPolicy>();
	private RecordExportPolicy defaultExportPolicy = new RecordExportPolicy();

	public RecordsetExportPolicy(IObject container) {
		super(container);
	}

	public void setImportPolicy(ImportPolicy importPolicy) {
		defaultExportPolicy.setDefaultImportPolicy(importPolicy);
	}

	public void setImportPolicy(Field field, ImportPolicy importPolicy) {
		defaultExportPolicy.setImportPolicy(field, importPolicy);
	}

	public void setImportPolicy(guid recordId, ImportPolicy importPolicy) {
		getOrCreateRecordExportPolicy(recordId).setDefaultImportPolicy(importPolicy);
	}

	public void setImportPolicy(guid recordId, Field field, ImportPolicy importPolicy) {
		getOrCreateRecordExportPolicy(recordId).setImportPolicy(field, importPolicy);
	}

	public ImportPolicy getDefaultImportPolicy(guid recordId) {
		return exportPolicies.containsKey(recordId) ? exportPolicies.get(recordId).getDefaultImportPolicy()
				: defaultExportPolicy.getDefaultImportPolicy();
	}

	public ImportPolicy getImportPolicy(guid recordId, Field field) {
		return exportPolicies.containsKey(recordId) ? exportPolicies.get(recordId).getImportPolicy(field)
				: defaultExportPolicy.getImportPolicy(field);
	}

	public void setExportAttachments(boolean exportAttachments) {
		defaultExportPolicy.setDefaultExportAttachments(exportAttachments);
	}

	public void setExportAttachments(Field field, boolean exportAttachments) {
		defaultExportPolicy.setExportAttachments(field, exportAttachments);
	}

	public void setExportAttachments(guid recordId, boolean exportAttachments) {
		getOrCreateRecordExportPolicy(recordId).setDefaultExportAttachments(exportAttachments);
	}

	public void setExportAttachments(guid recordId, Field field, boolean exportAttachments) {
		getOrCreateRecordExportPolicy(recordId).setExportAttachments(field, exportAttachments);
	}

	public boolean isExportAttachments(guid recordId, Field field) {
		return exportPolicies.containsKey(recordId) ? exportPolicies.get(recordId).isExportAttachments(field)
				: defaultExportPolicy.isExportAttachments(field);
	}

	public void z8_setImportPolicy(ImportPolicy importPolicy) {
		setImportPolicy(importPolicy);
	}

	public void z8_setImportPolicy(Field.CLASS<? extends Field> field, ImportPolicy importPolicy) {
		setImportPolicy(field.get(), importPolicy);
	}

	public void z8_setImportPolicy(guid recordId, ImportPolicy importPolicy) {
		setImportPolicy(recordId, importPolicy);
	}

	public void z8_setImportPolicy(guid recordId, Field.CLASS<? extends Field> field, ImportPolicy importPolicy) {
		setImportPolicy(recordId, field.get(), importPolicy);
	}

	public ImportPolicy z8_getImportPolicy(guid recordId, Field.CLASS<? extends Field> field) {
		return exportPolicies.containsKey(recordId) ? exportPolicies.get(recordId).getImportPolicy(field.get())
				: defaultExportPolicy.getImportPolicy(field.get());
	}

	public void z8_setExportAttachments(bool exportAttachments) {
		setExportAttachments(exportAttachments.get());
	}

	public void z8_setExportAttachments(Field.CLASS<? extends Field> field, bool exportAttachments) {
		setExportAttachments(field.get(), exportAttachments.get());
	}

	public void z8_setExportAttachments(guid recordId, bool exportAttachments) {
		setExportAttachments(recordId, exportAttachments.get());
	}

	public void z8_setExportAttachments(guid recordId, Field.CLASS<? extends Field> field, bool exportAttachments) {
		setExportAttachments(recordId, field.get(), exportAttachments.get());
	}

	public bool z8_isExportAttachments(guid recordId, Field.CLASS<? extends Field> field) {
		return new bool(isExportAttachments(recordId, field.get()));
	}

	private RecordExportPolicy getOrCreateRecordExportPolicy(guid recordId) {
		RecordExportPolicy policy = exportPolicies.get(recordId);
		if (policy == null) {
			policy = new RecordExportPolicy();
			exportPolicies.put(recordId, policy);
		}
		return policy;
	}

}
