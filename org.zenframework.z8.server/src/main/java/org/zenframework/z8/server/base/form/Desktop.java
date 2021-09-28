package org.zenframework.z8.server.base.form;

import java.util.Collection;

import org.zenframework.z8.server.base.Executable;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.security.Access;
import org.zenframework.z8.server.security.Privileges;

public class Desktop extends OBJECT {
	public static class CLASS<T extends Desktop> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Desktop.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Desktop(container);
		}

		@Override
		public void writeObject(JsonWriter writer) {
			Desktop desktop = this.newInstance();
			Collection<OBJECT.CLASS<OBJECT>> runnables = desktop.getRunnables();

			if(runnables.isEmpty())
				return;

			writer.startObject();
			writer.writeProperty(Json.text, displayName());
			writer.writeProperty(Json.icon, icon());

			writer.startArray(Json.items);
			for(OBJECT.CLASS<OBJECT> cls : runnables) {
				writer.startObject();
				cls.get().write(writer);
				writer.finishObject();
			}
			writer.finishArray();

			writer.finishObject();
		}
	}

	protected RCollection<OBJECT.CLASS<? extends OBJECT>> runnables = new RCollection<OBJECT.CLASS<? extends OBJECT>>();
	protected RCollection<Desktop.CLASS<? extends Desktop>> subDesktops = new RCollection<Desktop.CLASS<? extends Desktop>>();
	protected RCollection<Query.CLASS<? extends Query>> dataSets = new RCollection<Query.CLASS<? extends Query>>();

	public Desktop(IObject container) {
		super(container);
	}

	@SuppressWarnings({ "unchecked" })
	public Collection<OBJECT.CLASS<OBJECT>> getRunnables() {
		RCollection<OBJECT.CLASS<OBJECT>> result = new RCollection<OBJECT.CLASS<OBJECT>>();
		Privileges privileges = ApplicationServer.getUser().getPrivileges();

		for(IClass<? extends IObject> cls : members()) {
			if((cls instanceof Executable.CLASS || cls instanceof Query.CLASS) && privileges.getRequestAccess(cls.classIdKey()).get(Access.RequestExecute))
				result.add((OBJECT.CLASS<OBJECT>)cls);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked" })
	public Collection<Desktop.CLASS<Desktop>> getSubDesktops() {
		RCollection<Desktop.CLASS<Desktop>> result = new RCollection<Desktop.CLASS<Desktop>>();

		for(IClass<? extends IObject> cls : members()) {
			if(cls instanceof Desktop.CLASS)
				result.add((Desktop.CLASS<Desktop>)cls);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked" })
	public Collection<Query.CLASS<Query>> getDataSets() {
		RCollection<Query.CLASS<Query>> result = new RCollection<Query.CLASS<Query>>();

		for(IClass<? extends IObject> cls : members()) {
			if(cls instanceof Query.CLASS)
				result.add((Query.CLASS<Query>)cls);
		}
		return result;
	}

	@Override
	public int controlSum() {
		return Math.abs(classId().hashCode());
	}
}
