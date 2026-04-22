package org.zenframework.z8.server.base.entity;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;

public class Visitor extends OBJECT {
	public static class CLASS<T extends Visitor> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Visitor.class);
		}

		public Object newObject(IObject container) {
			return new Visitor(container);
		}
	}

	public Visitor(IObject container) {
		super(container);
	}

	@SuppressWarnings("unchecked")
	public void visit(Entity entity) {
		z8_visit((Entity.CLASS<? extends Entity>) entity.getCLASS()); /* virtual */
	}

	/* BL */

	/* virtual */ public void z8_visit(Entity.CLASS<? extends Entity> entity) {}
}
