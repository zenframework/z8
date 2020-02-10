package org.zenframework.z8.app.runtime;

import org.zenframework.z8.server.db.generator.DBGenerator;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class RuntimeListener extends OBJECT implements DBGenerator.Listener{

	public static class CLASS<T extends RuntimeListener> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(RuntimeListener.class);
		}

		public Object newObject(IObject container) {
			return new RuntimeListener(container);
		}
	}

	public RuntimeListener(IObject container) {
		super(container);
	}

	@Override
	public void beforeStart() {
		z8_beforeDBGenerate();
	}

	@Override
	public void afterFinish() {
		z8_afterDBGenerate();
	}

	public void z8_beforeDBGenerate() {}

	public void z8_afterDBGenerate() {}

}
