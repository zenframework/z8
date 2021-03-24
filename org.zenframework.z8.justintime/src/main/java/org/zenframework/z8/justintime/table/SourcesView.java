package org.zenframework.z8.justintime.table;

import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.integer;

public class SourcesView extends Source {

	public static class CLASS<T extends SourcesView> extends Source.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(SourcesView.class);
			setAttribute(SystemTool, Integer.toString(900));
		}

		public Object newObject(IObject container) {
			return new SourcesView(container);
		}
	}

	public CompileAction.CLASS<CompileAction> compile = new CompileAction.CLASS<CompileAction>(this);

	public SourcesView(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(compile);
	}

	public void constructor2() {
		super.constructor2();

		parent.get().name.setDisplayName(displayNames.ParentName);

		source.get().colSpan = new integer(2);
		source.get().flex = new integer(1);

		colCount = new integer(2);

		names.add(name);

		controls.add(parent.get(IClass.Constructor1).name);
		controls.add(shortName);
		controls.add(source);

		columns.add(parent.get(IClass.Constructor1).name);
		columns.add(shortName);

		extraFields.add(icon);

		compile.setIndex("compile");

		actions.add(compile);
	}
}
