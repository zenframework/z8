package org.zenframework.z8.server.base.table.site;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;

public class Topics extends Table {
	final static public String TableName = "SiteTopics";

	static public class names {
		public final static String Headline = "Headline";
		public final static String Content = "Content";
		public final static String Author = "Author";
		public final static String Time = "Time";
	}

	static public class strings {
		public final static String Title = "SiteTopics.title";
		public final static String Headline = "SiteTopics.headline";
		public final static String Text = "SiteTopics.text";
		public final static String Time = "SiteTopics.time";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Headline = Resources.get(strings.Headline);
		public final static String Text = Resources.get(strings.Text);
		public final static String Time = Resources.get(strings.Time);
	}

	public static class CLASS<T extends Topics> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Topics.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Topics(container);
		}
	}

	public Accounts.CLASS<Accounts> authors = new Accounts.CLASS<Accounts>(this);

	public StringField.CLASS<StringField> headline = new StringField.CLASS<StringField>(this);
	public TextField.CLASS<TextField> text = new TextField.CLASS<TextField>(this);
	public DatetimeField.CLASS<DatetimeField> time = new DatetimeField.CLASS<DatetimeField>(this);

	public Link.CLASS<Link> author = new Link.CLASS<Link>(this);

	public Topics() {
		this(null);
	}

	public Topics(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		author.get(IClass.Constructor1).operatorAssign(authors);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		authors.setIndex("authors");

		author.setName(names.Author);
		author.setIndex("author");

		headline.setName(names.Headline);
		headline.setIndex("headline");
		headline.setDisplayName(displayNames.Headline);
		headline.get().length = new integer(256);

		text.setName(names.Content);
		text.setIndex("text");
		text.setDisplayName(displayNames.Text);

		time.setName(names.Time);
		time.setIndex("time");
		time.setDisplayName(displayNames.Time);

		registerDataField(headline);
		registerDataField(text);
		registerDataField(author);
		registerDataField(time);

		objects.add(authors);
	}

	@Override
	public void onNew(guid recordId, guid parentId) {
		super.onNew(recordId, parentId);
		time.get().set(new date());
	}
}
