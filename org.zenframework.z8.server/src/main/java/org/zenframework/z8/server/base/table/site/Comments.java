package org.zenframework.z8.server.base.table.site;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.TreeTable;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.guid;

public class Comments extends TreeTable {
	final static public String TableName = "SiteComments";

	static public class names {
		public final static String Topic = "Topic";
		public final static String Author = "Author";
		public final static String Text = "Text";
		public final static String Time = "Time";
	}

	static public class strings {
		public final static String Title = "SiteComments.title";
		public final static String Text = "SiteComments.text";
		public final static String Time = "SiteComments.time";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Text = Resources.get(strings.Text);
		public final static String Time = Resources.get(strings.Time);
	}

	public static class CLASS<T extends Comments> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Comments.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new Comments(container);
		}
	}

	public Comments.CLASS<Comments> parentComment = new Comments.CLASS<Comments>(this);
	public Topics.CLASS<Topics> topics = new Topics.CLASS<Topics>(this);
	public Accounts.CLASS<Accounts> authors = new Accounts.CLASS<Accounts>(this);

	public TextField.CLASS<TextField> text = new TextField.CLASS<TextField>(this);
	public DatetimeField.CLASS<DatetimeField> time = new DatetimeField.CLASS<DatetimeField>(this);

	public Link.CLASS<Link> topic = new Link.CLASS<Link>(this);
	public Link.CLASS<Link> author = new Link.CLASS<Link>(this);

	public Comments() {
		this(null);
	}

	public Comments(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		topic.get(IClass.Constructor1).operatorAssign(topics);
		author.get(IClass.Constructor1).operatorAssign(authors);
		parentId.get(IClass.Constructor1).operatorAssign(parentComment);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		topics.setIndex("topics");
		authors.setIndex("authors");
		parentComment.setIndex("parentComment");

		topic.setName(names.Topic);
		topic.setIndex("topic");

		author.setName(names.Author);
		author.setIndex("author");

		text.setName(names.Text);
		text.setIndex("text");
		text.setDisplayName(displayNames.Text);

		time.setName(names.Time);
		time.setIndex("time");
		time.setDisplayName(displayNames.Time);

		registerDataField(topic);
		registerDataField(author);
		registerDataField(text);
		registerDataField(time);

		objects.add(parentComment);
		objects.add(topics);
		objects.add(authors);
	}

	@Override
	public void onNew(guid recordId, guid parentId) {
		super.onNew(recordId, parentId);
		time.get().set(new date());
	}
}
