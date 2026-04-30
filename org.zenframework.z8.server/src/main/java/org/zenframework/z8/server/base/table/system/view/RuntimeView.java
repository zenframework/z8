package org.zenframework.z8.server.base.table.system.view;

import java.util.Collection;

import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.engine.Version;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.JsonUtils;

public class RuntimeView extends Query {
	static public class strings {
		static public String Title = "RuntimeView.title";

		static public String Id = "RuntimeView.id";
		static public String ClassName = "RuntimeView.className";
		static public String Name = "RuntimeView.name";
		static public String DisplayName = "RuntimeView.displayName";
		static public String ControlSum = "RuntimeView.controlSum";
		static public String NewName = "RuntimeView.newName";
		static public String NewDisplayName = "RuntimeView.newDisplayName";
		static public String NewControlSum = "RuntimeView.newControlSum";
		static public String Changed = "RuntimeView.changed";

		static public String Export = "RuntimeView.export";
	}

	static public class displayNames {
		static public String Title = Resources.get(strings.Title);

		static public String Id = Resources.get(strings.Id);
		static public String ClassName = Resources.get(strings.ClassName);
		static public String Name = Resources.get(strings.Name);
		static public String DisplayName = Resources.get(strings.DisplayName);
		static public String ControlSum = Resources.get(strings.ControlSum);
		static public String NewName = Resources.get(strings.NewName);
		static public String NewDisplayName = Resources.get(strings.NewDisplayName);
		static public String NewControlSum = Resources.get(strings.NewControlSum);
		static public String Changed = Resources.get(strings.Changed);

		static public String Export = Resources.get(strings.Export);
	}

	public static class CLASS<T extends RuntimeView> extends Query.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(RuntimeView.class);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new RuntimeView(container);
		}
	}

	public static class Export extends Action {

		public static class CLASS<T extends Export> extends Action.CLASS<T> {
			public CLASS() {
				this(null);
			}

			public CLASS(IObject container) {
				super(container);
				setJavaClass(Export.class);
			}

			@Override
			public Object newObject(IObject container) {
				return new Export(container);
			}
		}

		public Export(IObject container) {
			super(container);
		}

		@Override
		public void execute(Collection<guid> records, Query context, Collection<guid> selected, Query query) {
			RuntimeView view = (RuntimeView) query;
			file csv = file.createTempFile("runtime ", "csv");
			csv.name = new string("runtime.csv");
			csv.write(JsonUtils.toCsv(view.getData(),
					view.recordId.id(), view.className.id(), view.name.id(), view.newName.id(),
					view.displayName.id(), view.newDisplayName.id(), view.controlSum.id(), view.newControlSum.id(),
					view.changed.id()));
			ApplicationServer.getMonitor().print(csv);
		}

	}

	private GuidField.CLASS<GuidField> recordId = new GuidField.CLASS<GuidField>(this);
	private StringField.CLASS<StringField> className = new StringField.CLASS<StringField>(this);
	private StringField.CLASS<StringField> name = new StringField.CLASS<StringField>(this);
	private StringField.CLASS<StringField> displayName = new StringField.CLASS<StringField>(this);
	private StringField.CLASS<StringField> controlSum = new StringField.CLASS<StringField>(this);
	private StringField.CLASS<StringField> newName = new StringField.CLASS<StringField>(this);
	private StringField.CLASS<StringField> newDisplayName = new StringField.CLASS<StringField>(this);
	private StringField.CLASS<StringField> newControlSum = new StringField.CLASS<StringField>(this);
	private BoolField.CLASS<BoolField> changed = new BoolField.CLASS<BoolField>(this);

	private Export.CLASS<Export> export = new Export.CLASS<Export>(this);

	public RuntimeView(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(recordId);
		objects.add(className);
		objects.add(name);
		objects.add(displayName);
		objects.add(controlSum);
		objects.add(newName);
		objects.add(newDisplayName);
		objects.add(newControlSum);
		objects.add(changed);
		objects.add(export);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		readOnly = bool.True;

		recordId.setIndex("recordId");
		recordId.setDisplayName(displayNames.Id);

		className.setIndex("className");
		className.setDisplayName(displayNames.ClassName);
		className.get().colSpan = new integer(2);

		name.setIndex("name");
		name.setDisplayName(displayNames.Name);

		displayName.setIndex("displayName");
		displayName.setDisplayName(displayNames.DisplayName);

		controlSum.setIndex("controlSum");
		controlSum.setDisplayName(displayNames.ControlSum);

		newName.setIndex("newName");
		newName.setDisplayName(displayNames.NewName);

		newDisplayName.setIndex("newDisplayName");
		newDisplayName.setDisplayName(displayNames.NewDisplayName);

		newControlSum.setIndex("newControlSum");
		newControlSum.setDisplayName(displayNames.NewControlSum);

		changed.setIndex("changed");
		changed.setDisplayName(displayNames.Changed);

		colCount = new integer(2);

		registerControl(recordId);
		registerControl(changed);
		registerControl(className);
		registerControl(name);
		registerControl(newName);
		registerControl(displayName);
		registerControl(newDisplayName);
		registerControl(controlSum);
		registerControl(newControlSum);

		export.setIndex("export");
		export.setDisplayName(displayNames.Export);

		actions.add(export);

		names.add(className);
		names.add(changed);

		columns.add(recordId);
		columns.add(className);
		columns.add(name);
		columns.add(newName);
		columns.add(displayName);
		columns.add(newDisplayName);
		columns.add(controlSum);
		columns.add(newControlSum);
		columns.add(changed);
	}

	@Override
	public JsonArray getData() {
		return Version.getVersionDiff(Version.readVersion(ApplicationServer.getSchema()),
				Version.getVersion(Runtime.instance()));
	}

}
