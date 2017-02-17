package org.zenframework.z8.server.base.table.system.view;

import org.zenframework.z8.server.base.table.system.Sequences;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class SequencesView extends Sequences {
	public static class CLASS<T extends SequencesView> extends Sequences.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(SequencesView.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new SequencesView(container);
		}
	}

	public SequencesView(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		readOnly = new bool(!ApplicationServer.getUser().isAdministrator());

		description.get().colspan = new integer(4);

		registerControl(key);
		registerControl(value);
		registerControl(description);

		sortFields.add(description);
	}
}