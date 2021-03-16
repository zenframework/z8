package org.zenframework.z8.justintime.table;

import org.zenframework.z8.server.base.form.Desktop;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class JustInTimeTools extends Desktop {

	static public class strings {
		public final static String Title = "JustInTimeTools.title";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
	}

	public static class CLASS<T extends JustInTimeTools> extends Desktop.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(JustInTimeTools.class);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new JustInTimeTools(container);
		}
	}

	public OBJECT.CLASS<? extends OBJECT> sources = new SourcesView.CLASS<SourcesView>(this);

	public JustInTimeTools(IObject container) {
		super(container);
	}

	public void initMembers() {
		super.initMembers();

		objects.add(sources);
	}

	public void constructor2() {
		super.constructor2();

		sources.setIndex("sources");
	}

}
