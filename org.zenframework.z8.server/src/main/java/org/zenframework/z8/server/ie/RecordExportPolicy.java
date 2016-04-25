package org.zenframework.z8.server.ie;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.value.Field;

public class RecordExportPolicy {

	private final Map<String, ImportPolicy> fieldsPolicies = new HashMap<String, ImportPolicy>();
	private final Map<String, Boolean> exportAttachments = new HashMap<String, Boolean>();
	private ImportPolicy defaultImportPolicy = ImportPolicy.DEFAULT;
	private boolean defaultExportAttachments = false;

	public void setDefaultImportPolicy(ImportPolicy importPolicy) {
		defaultImportPolicy = importPolicy;
	}

	public ImportPolicy getDefaultImportPolicy() {
		return defaultImportPolicy;
	}

	public void setImportPolicy(Field field, ImportPolicy importPolicy) {
		fieldsPolicies.put(field.id(), importPolicy);
	}

	public ImportPolicy getImportPolicy(Field field) {
		return fieldsPolicies.containsKey(field.id()) ? fieldsPolicies.get(field.id()) : defaultImportPolicy;
	}

	public void setDefaultExportAttachments(boolean exportAttachments) {
		defaultExportAttachments = exportAttachments;
	}

	public boolean isDefaultExportAttachments() {
		return defaultExportAttachments;
	}

	public void setExportAttachments(Field field, boolean exportAttachments) {
		this.exportAttachments.put(field.id(), exportAttachments);
	}

	public boolean isExportAttachments(Field field) {
		return exportAttachments.containsKey(field.id()) ? exportAttachments.get(field.id()) : defaultExportAttachments;
	}

}
