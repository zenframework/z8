package org.zenframework.z8.justintime.table;

import java.util.Arrays;

import org.zenframework.z8.server.base.table.TreeTable;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.table.value.StringExpression;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.db.sql.expressions.NotEqu;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.string.IndexOf;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.sql.sql_integer;

public class Source extends TreeTable {

	public static final String TableName = "SystemSources";

	static public class fieldNames {
		public final static String TypeId = "Type";
		public final static String Source = "Source";
	}

	static public class strings {
		public final static String Title = "Source.title";
		public final static String Name = "Source.name";
		public final static String ShortName = "Source.shortName";
		public final static String Source = "Source.source";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);
		public final static String ShortName = Resources.get(strings.ShortName);
		public final static String Source = Resources.get(strings.Source);
	}

	public static class CLASS<T extends Source> extends TreeTable.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Source.class);
			setName(Source.TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Source(container);
		}
	}

	public static class Parent extends Source {
		public static class CLASS<T extends Source.Parent> extends Source.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(Source.Parent.class);
			}

			public Object newObject(IObject container) {
				return new Source.Parent(container);
			}
		}

		public Parent(IObject container) {
			super(container);
		}

		public void z8_beforeRead() {
			super.z8_beforeRead();
			//addWhere(new NotEqu(recordId.get(), ((Source.CLASS<Source>) getContainer().getCLASS()).get().recordId()));
			addWhere(new Equ(typeId.get(), SourceType.Package));
		}
	};

	public Source.CLASS<? extends Source> parent = new Parent.CLASS<Parent>(this);

	public SourceType.CLASS<SourceType> type = new SourceType.CLASS<SourceType>(this);
	public Link.CLASS<Link> typeId = new Link.CLASS<Link>(this);

	public TextField.CLASS<TextField> source = new TextField.CLASS<TextField>(this);
	public StringExpression.CLASS<StringExpression> icon = new StringExpression.CLASS<StringExpression>(this);

	protected boolean eventsDisabled = false;
	protected String prevName = null;
	protected String newName = null;

	public Source(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(parent);
		objects.add(type);
		objects.add(typeId);
		objects.add(source);
		objects.add(icon);
	}

	@Override
	public void constructor1() {
		super.constructor1();

		parentId.get(IClass.Constructor).operatorAssign(parent);
		typeId.get(IClass.Constructor).operatorAssign(type);
		icon.get(IClass.Constructor).operatorAssign(type.get(IClass.Constructor1).icon.get(IClass.Constructor1).sql_string());
	}

	@Override
	public void constructor2() {
		super.constructor2();

		parent.setIndex("parent");

		type.setIndex("type");

		typeId.setName(fieldNames.TypeId);
		typeId.setIndex("typeId");
		typeId.get().defaultValue = SourceType.Package;

		name.setDisplayName(displayNames.Name);
		name.get().length = new integer(1000L);

		shortName.setDisplayName(displayNames.ShortName);
		shortName.get().length = new integer(100L);

		source.setName(fieldNames.Source);
		source.setIndex("source");
		source.setDisplayName(displayNames.Source);

		icon.setIndex("icon");
	}

	public void z8_beforeUpdate(guid recordId) {
		super.z8_beforeUpdate(recordId);

		Source source = new Source.CLASS<Source>(this).get();

		if (eventsDisabled || !source.readRecord(recordId, Arrays.asList(source.name.get(), source.shortName.get(), source.parent.get().name.get())))
			return;
	
		String prevShortName = source.shortName.get().string().get();
		String prevParentName = source.parent.get().name.get().string().get();

		prevName = source.name.get().string().get();

		newName = parentId.get().changed()
				? (source.readRecord(parentId.get().guid(), Arrays.asList(source.name.get())) ? source.name.get().string().get() : "")
				: prevParentName;

		newName += (newName.isEmpty() ? "" : ".") + (shortName.get().changed() ? shortName.get().string().get() : prevShortName);

		name.get().set(newName);
	}

	public void z8_afterUpdate(guid recordId) {
		super.z8_afterUpdate(recordId);

		if (eventsDisabled || !name.get().changed())
			return;

		Source source = new Source.CLASS<Source>(this).get();
		source.eventsDisabled = true;
		source.read(Arrays.asList(source.name.get()), new And(
				new NotEqu(source.recordId.get(), recordId),
				new Rel(new IndexOf(recordId.toString(), source.path.get(), 0L), Operation.GE, new sql_integer(0L))
		));
		while (source.next()) {
			source.name.get().set(newName + source.name.get().string().get().substring(prevName.length()));
			source.update(source.recordId());
		}
	}

}
