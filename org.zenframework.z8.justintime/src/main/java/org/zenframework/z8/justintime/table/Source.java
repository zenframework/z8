package org.zenframework.z8.justintime.table;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

import org.zenframework.z8.justintime.runtime.ISource;
import org.zenframework.z8.justintime.runtime.Workspace;
import org.zenframework.z8.server.base.table.TreeTable;
import org.zenframework.z8.server.base.table.value.BoolExpression;
import org.zenframework.z8.server.base.table.value.StringExpression;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.Add;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.db.sql.expressions.IsNot;
import org.zenframework.z8.server.db.sql.expressions.NotEqu;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Or;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.If;
import org.zenframework.z8.server.db.sql.functions.string.IndexOf;
import org.zenframework.z8.server.db.sql.functions.string.Length;
import org.zenframework.z8.server.db.sql.functions.string.Reverse;
import org.zenframework.z8.server.db.sql.functions.string.Substr;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.sql.sql_bool;
import org.zenframework.z8.server.types.sql.sql_integer;
import org.zenframework.z8.server.types.sql.sql_string;
import org.zenframework.z8.server.utils.IOUtils;

public class Source extends TreeTable implements ISource {

	public static final String TableName = "SystemSources";

	static public class fieldNames {
		public final static String TypeId = "Type";
		public final static String Source = "Source";
	}

	static public class strings {
		public final static String Title = "Source.title";
		public final static String Name = "Source.name";
		public final static String ShortName = "Source.shortName";
		public final static String ParentName = "Source.parentName";
		public final static String Source = "Source.source";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Name = Resources.get(strings.Name);
		public final static String ShortName = Resources.get(strings.ShortName);
		public final static String ParentName = Resources.get(strings.ParentName);
		public final static String Source = Resources.get(strings.Source);
	}

	static public class extensions {
		public static final sql_string Bl = new sql_string("bl");
		public static final sql_string Nls = new sql_string("nls");
		public static final sql_string Java = new sql_string("java");
	}

	static public class icons {
		public static final sql_string Bl = new sql_string("fa-file-code-o");
		public static final sql_string Nls = new sql_string("fa-file-text-o");
		public static final sql_string Java = new sql_string("fa-file-code-o");
		public static final sql_string Package = new sql_string("fa-th-large");
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
			addWhere(new SqlField(isPackage.get()));
		}
	};

	public Source.CLASS<? extends Source> parent = new Parent.CLASS<Parent>(this);

	public TextField.CLASS<TextField> source = new TextField.CLASS<TextField>(this);

	public StringExpression.CLASS<StringExpression> ext = new StringExpression.CLASS<StringExpression>(this);
	public BoolExpression.CLASS<BoolExpression> isBl = new BoolExpression.CLASS<BoolExpression>(this);
	public BoolExpression.CLASS<BoolExpression> isNls = new BoolExpression.CLASS<BoolExpression>(this);
	public BoolExpression.CLASS<BoolExpression> isJava = new BoolExpression.CLASS<BoolExpression>(this);
	public BoolExpression.CLASS<BoolExpression> isPackage = new BoolExpression.CLASS<BoolExpression>(this);
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
		objects.add(source);
		objects.add(ext);
		objects.add(isBl);
		objects.add(isNls);
		objects.add(isJava);
		objects.add(isPackage);
		objects.add(icon);
	}

	@Override
	public void constructor1() {
		super.constructor1();

		parentId.get(IClass.Constructor).operatorAssign(parent);

		SqlToken shortName = this.shortName.get(IClass.Constructor1).sql_string();
		SqlToken extPos = new IndexOf(new sql_string("."), new Reverse(shortName), null);
		ext.get(IClass.Constructor).setExpression(new If(
				new Rel(extPos, Operation.GT, new sql_integer(0L)),
						new Substr(shortName, new Add(new Length(shortName), Operation.Sub, extPos)),
						new sql_string()));

		isBl.get(IClass.Constructor).setExpression(new Equ(ext.get(IClass.Constructor1), extensions.Bl));
		isNls.get(IClass.Constructor).setExpression(new Equ(ext.get(IClass.Constructor1), extensions.Nls));
		isJava.get(IClass.Constructor).setExpression(new Equ(ext.get(IClass.Constructor1), extensions.Java));
		isPackage.get(IClass.Constructor).setExpression(new IsNot(Or.fromArray(
				new SqlField(isBl.get(IClass.Constructor1)), new SqlField(isNls.get(IClass.Constructor1)), new SqlField(isJava.get(IClass.Constructor1)))));

		icon.get(IClass.Constructor).setExpression(
				new If(new sql_bool(new SqlField(isBl.get(IClass.Constructor1))), icons.Bl,
						new If(new sql_bool(new SqlField(isNls.get(IClass.Constructor1))), icons.Nls,
								new If(new sql_bool(new SqlField(isJava.get(IClass.Constructor1))), icons.Java, icons.Package))));
	}

	@Override
	public void constructor2() {
		super.constructor2();

		parent.setIndex("parent");

		name.setDisplayName(displayNames.Name);
		name.get().length = new integer(1000L);

		shortName.setDisplayName(displayNames.ShortName);
		shortName.get().length = new integer(100L);

		source.setName(fieldNames.Source);
		source.setIndex("source");
		source.setDisplayName(displayNames.Source);

		ext.setIndex("ext");

		isBl.setIndex("isBl");
		isNls.setIndex("isNls");
		isJava.setIndex("isJava");
		isPackage.setIndex("isPackage");

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

	@Override
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

	@Override
	public void exportSources(Workspace workspace) {
		read(Arrays.asList(parent.get().name.get(), shortName.get(), source.get()), new IsNot(isPackage.get()));
		while (next()) {
			File file = new File(workspace.getBlSources(), parent.get().name.get().string().get().replace('.', '/') + '/' + shortName.get().string().get());
			file.getParentFile().mkdirs();
			Writer writer = null;
			try {
				writer = new OutputStreamWriter(new FileOutputStream(file));
				writer.write(source.get().string().get());
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(writer);
			}
		}
	}

}
