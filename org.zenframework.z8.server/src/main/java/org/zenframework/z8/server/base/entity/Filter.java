package org.zenframework.z8.server.base.entity;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.bool;

public class Filter extends OBJECT {
	public static class CLASS<T extends Filter> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Filter.class);
		}

		public Object newObject(IObject container) {
			return new Filter(container);
		}
	}

	private interface InnerFilter {
		boolean accept(Entity entity);
	}

	private InnerFilter inner = null;

	public Filter(IObject container) {
		super(container);
	}

	private Filter setInner(InnerFilter inner) {
		this.inner = inner;
		return this;
	}

	public Filter not() {
		return new Filter.CLASS<Filter>(getContainer()).get().setInner(new InnerFilter() {
			@Override
			public boolean accept(Entity entity) {
				return !Filter.this.accept(entity);
			}
		});
	}

	public Filter and(Filter x) {
		return x != null ? new Filter.CLASS<Filter>(getContainer()).get().setInner(new InnerFilter() {
			public boolean accept(Entity entity) {
				return Filter.this.accept(entity) && x.accept(entity);
			}
		}) : this;
	}

	public Filter or(Filter x) {
		return x != null ? new Filter.CLASS<Filter>(getContainer()).get().setInner(new InnerFilter() {
			public boolean accept(Entity entity) {
				return Filter.this.accept(entity) || x.accept(entity);
			}
		}) : this;
	}

	@SuppressWarnings("unchecked")
	public boolean accept(Entity entity) {
		return inner != null ? inner.accept(entity)
				: z8_accept(entity != null ? (Entity.CLASS<? extends Entity>) entity.getCLASS() : null).get(); /* virtual */
	}

	/* BL */

	@SuppressWarnings("unchecked")
	public Filter.CLASS<? extends Filter> operatorNot() {
		return (Filter.CLASS<? extends Filter>) not().getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Filter.CLASS<? extends Filter> operatorAnd(Filter.CLASS<? extends Filter> x) {
		return (Filter.CLASS<? extends Filter>) and(x != null ? x.get() : null).getCLASS();
	}

	@SuppressWarnings("unchecked")
	public Filter.CLASS<? extends Filter> operatorOr(Filter.CLASS<? extends Filter> x) {
		return (Filter.CLASS<? extends Filter>) or(x != null ? x.get() : null).getCLASS();
	}

	/* virtual */ public bool z8_accept(Entity.CLASS<? extends Entity> entity) {
		return bool.True;
	}
}
