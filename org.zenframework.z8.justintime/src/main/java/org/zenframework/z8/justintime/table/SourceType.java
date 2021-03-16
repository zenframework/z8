package org.zenframework.z8.justintime.table;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.TreeTable;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class SourceType extends TreeTable {

	public static final String TableName = "SystemSourceTypes";

	static public class fieldNames {
		public final static String Source = "SourceType";
	}

	static public class strings {
		public final static String Title = "SourceType.title";
		public final static String Name = "SourceType.name";
		public final static String ShortName = "SourceType.shortName";

		public final static String PackageIcon = "fa-th-large";
		public final static String PackageName = "SourceType.package.name";
		public final static String PackageShortName = "SourceType.package.shortName";

		public final static String BlIcon = "fa-file-code-o";
		public final static String BlName = "SourceType.bl.name";
		public final static String BlShortName = "SourceType.bl.shortName";

		public final static String NlsIcon = "fa-file-text-o";
		public final static String NlsName = "SourceType.nls.name";
		public final static String NlsShortName = "SourceType.nls.shortName";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);
		public final static String ShortName = Resources.get(strings.ShortName);

		public final static String PackageName = Resources.get(strings.PackageName);
		public final static String PackageShortName = Resources.get(strings.PackageShortName);
		public final static String BlName = Resources.get(strings.BlName);
		public final static String BlShortName = Resources.get(strings.BlShortName);
		public final static String NlsName = Resources.get(strings.NlsName);
		public final static String NlsShortName = Resources.get(strings.NlsShortName);
	}

	public static class CLASS<T extends SourceType> extends TreeTable.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(SourceType.class);
			setName(SourceType.TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new SourceType(container);
		}
	}

	public static guid Package = new guid(/* 0922c809-9cc7-44a8-8ec9-78b96b495846 */658308439160079528L, -8157856512240691130L);
	public static guid BL = new guid(/* 31a902c4-f30d-43c3-ab0b-dd2f5b595105 */3578394423837148099L, -6121556073014275835L);
	public static guid NLS = new guid(/* d1be388c-89cc-4549-9b98-2760616343c5 */-3333164497949145783L, -7234989506466462779L);

	public StringField.CLASS<StringField> icon = new StringField.CLASS<StringField>(this);

	public SourceType(IObject container) {
		super(container);
	}

	@Override
	public void initStaticRecords() {
		super.initStaticRecords();
		initStaticRecord(Package, displayNames.PackageName, displayNames.PackageShortName, strings.PackageIcon);
		initStaticRecord(BL, displayNames.BlName, displayNames.BlShortName, strings.BlIcon);
		initStaticRecord(NLS, displayNames.NlsName, displayNames.NlsShortName, strings.NlsIcon);
	}

	@Override
	public void initMembers() {
		super.initMembers();
		objects.add(icon);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		name.setDisplayName(displayNames.Name);
		shortName.setDisplayName(displayNames.ShortName);
		shortName.get().length = new integer(30);

		icon.setIndex("icon");
	}

	private void initStaticRecord(guid recordId, String name, String shortName, String icon) {
		Map<IField, primary> map = new HashMap<IField, primary>();
		map.put(this.name.get(), new string(name));
		map.put(this.shortName.get(), new string(shortName));
		map.put(this.icon.get(), new string(icon));
		internalAddRecord(recordId, map);
	}

}
