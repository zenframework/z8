package org.zenframework.z8.server.ie;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;

public class RecordsetExportRules extends OBJECT {

	public static class CLASS<T extends RecordsetExportRules> extends OBJECT.CLASS<T> {

		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(RecordsetExportRules.class);
			setName(RecordsetExportRules.class.getName());
			setDisplayName(RecordsetExportRules.class.getName());
		}

		@Override
		public Object newObject(IObject container) {
			return new RecordsetExportRules(container);
		}

	}

	private final Map<guid, RecordExportRules> exportRules = new HashMap<guid, RecordExportRules>();
	private RecordExportRules defaultExportRules = new RecordExportRules();

	public RecordsetExportRules(IObject container) {
		super(container);
	}

	public void setImportPolicy(ImportPolicy importPolicy) {
		defaultExportRules.setDefaultImportPolicy(importPolicy);
	}

	public void setImportPolicy(Field field, ImportPolicy importPolicy) {
		defaultExportRules.setImportPolicy(field, importPolicy);
	}

	public void setImportPolicy(guid recordId, ImportPolicy importPolicy) {
		getOrCreateRecordExportPolicy(recordId).setDefaultImportPolicy(importPolicy);
	}

	public void setImportPolicy(guid recordId, Field field, ImportPolicy importPolicy) {
		getOrCreateRecordExportPolicy(recordId).setImportPolicy(field, importPolicy);
	}

	public ImportPolicy getDefaultImportPolicy(guid recordId) {
		return exportRules.containsKey(recordId) ? exportRules.get(recordId).getDefaultImportPolicy()
				: defaultExportRules.getDefaultImportPolicy();
	}

	public ImportPolicy getImportPolicy(guid recordId, Field field) {
		return exportRules.containsKey(recordId) ? exportRules.get(recordId).getImportPolicy(field)
				: defaultExportRules.getImportPolicy(field);
	}

	public void setExportAttachments(boolean exportAttachments) {
		defaultExportRules.setDefaultExportAttachments(exportAttachments);
	}

	public void setExportAttachments(Field field, boolean exportAttachments) {
		defaultExportRules.setExportAttachments(field, exportAttachments);
	}

	public void setExportAttachments(guid recordId, boolean exportAttachments) {
		getOrCreateRecordExportPolicy(recordId).setDefaultExportAttachments(exportAttachments);
	}

	public void setExportAttachments(guid recordId, Field field, boolean exportAttachments) {
		getOrCreateRecordExportPolicy(recordId).setExportAttachments(field, exportAttachments);
	}

	public boolean isExportAttachments(guid recordId, Field field) {
		return exportRules.containsKey(recordId) ? exportRules.get(recordId).isExportAttachments(field)
				: defaultExportRules.isExportAttachments(field);
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
		return exportRules.containsKey(recordId) ? exportRules.get(recordId).getImportPolicy(field.get())
				: defaultExportRules.getImportPolicy(field.get());
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

	private RecordExportRules getOrCreateRecordExportPolicy(guid recordId) {
		RecordExportRules policy = exportRules.get(recordId);
		if (policy == null) {
			policy = new RecordExportRules();
			exportRules.put(recordId, policy);
		}
		return policy;
	}

	// 1) defaultImportPolicy, defaultExportAttachments
	public static RecordsetExportRules.CLASS<RecordsetExportRules> z8_newExportRules(ImportPolicy defaultImportPolicy,
			bool defaultExportAttachments) {
		RecordsetExportRules.CLASS<RecordsetExportRules> policy = new RecordsetExportRules.CLASS<RecordsetExportRules>();
		policy.get().setImportPolicy(defaultImportPolicy);
		policy.get().setExportAttachments(defaultExportAttachments.get());
		return policy;
	}

	// 2a) recordImportPolicy, recordExportAttachments
	public static RecordsetExportRules.CLASS<RecordsetExportRules> z8_newExportRules(guid recordId,
			ImportPolicy recordImportPolicy, bool recordExportAttachments) {
		RecordsetExportRules.CLASS<RecordsetExportRules> policy = new RecordsetExportRules.CLASS<RecordsetExportRules>();
		policy.get().setImportPolicy(recordId, recordImportPolicy);
		policy.get().setExportAttachments(recordId, recordExportAttachments.get());
		return policy;
	}

	// 2b) defaultImportPolicy, defaultExportAttachments, recordImportPolicy, recordExportAttachments
	public static RecordsetExportRules.CLASS<RecordsetExportRules> z8_newExportRules(ImportPolicy defaultImportPolicy,
			bool defaultExportAttachments, guid recordId, ImportPolicy recordImportPolicy, bool recordExportAttachments) {
		RecordsetExportRules.CLASS<RecordsetExportRules> policy = z8_newExportRules(defaultImportPolicy,
				defaultExportAttachments);
		policy.get().setImportPolicy(recordId, recordImportPolicy);
		policy.get().setExportAttachments(recordId, recordExportAttachments.get());
		return policy;
	}

	// 3a) fieldsImportPolicy
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static RecordsetExportRules.CLASS<RecordsetExportRules> z8_newExportRules(RCollection fields,
			ImportPolicy fieldsImportPolicy) {
		RecordsetExportRules.CLASS<RecordsetExportRules> policy = new RecordsetExportRules.CLASS<RecordsetExportRules>();
		for (Field field : CLASS.asList((RCollection<? extends Field.CLASS<? extends Field>>) fields))
			policy.get().setImportPolicy(field, fieldsImportPolicy);
		return policy;
	}

	// 3b) defaultImportPolicy, defaultExportAttachments, fieldsImportPolicy
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static RecordsetExportRules.CLASS<RecordsetExportRules> z8_newExportRules(ImportPolicy defaultImportPolicy,
			bool defaultExportAttachments, RCollection fields, ImportPolicy fieldsImportPolicy) {
		RecordsetExportRules.CLASS<RecordsetExportRules> policy = z8_newExportRules(defaultImportPolicy,
				defaultExportAttachments);
		for (Field field : CLASS.asList((RCollection<? extends Field.CLASS<? extends Field>>) fields))
			policy.get().setImportPolicy(field, fieldsImportPolicy);
		return policy;
	}

	// 4a) recordExportAttachments, recordFieldsImportPolicy
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static RecordsetExportRules.CLASS<RecordsetExportRules> z8_newExportRules(guid recordId,
			bool recordExportAttachments, RCollection fields, ImportPolicy recordFieldsImportPolicy) {
		RecordsetExportRules.CLASS<RecordsetExportRules> policy = new RecordsetExportRules.CLASS<RecordsetExportRules>();
		for (Field field : CLASS.asList((RCollection<? extends Field.CLASS<? extends Field>>) fields))
			policy.get().setImportPolicy(recordId, field, recordFieldsImportPolicy);
		policy.get().setExportAttachments(recordId, recordExportAttachments.get());
		return policy;
	}

	// 4b) defaultImportPolicy, defaultExportAttachments, recordExportAttachments, recordFieldsImportPolicy
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static RecordsetExportRules.CLASS<RecordsetExportRules> z8_newExportRules(ImportPolicy defaultImportPolicy,
			bool defaultExportAttachments, guid recordId, bool recordExportAttachments, RCollection fields,
			ImportPolicy recordFieldsImportPolicy) {
		RecordsetExportRules.CLASS<RecordsetExportRules> policy = z8_newExportRules(defaultImportPolicy,
				defaultExportAttachments);
		for (Field field : CLASS.asList((RCollection<? extends Field.CLASS<? extends Field>>) fields))
			policy.get().setImportPolicy(recordId, field, recordFieldsImportPolicy);
		policy.get().setExportAttachments(recordId, recordExportAttachments.get());
		return policy;
	}

}
