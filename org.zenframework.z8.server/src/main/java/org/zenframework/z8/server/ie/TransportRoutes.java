package org.zenframework.z8.server.ie;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_datetime;
import org.zenframework.z8.server.types.sql.sql_string;

public class TransportRoutes extends Table {

	public static final String TableName = "SystemTransportRoutes";

	public static class CLASS<T extends TransportRoutes> extends Table.CLASS<T> {

		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(TransportRoutes.class);
			setName(TableName);
			setDisplayName(Resources.get(strings.Title));
		}

		@Override
		public Object newObject(IObject container) {
			return new TransportRoutes(container);
		}

	}

	static public class strings {
		public final static String Title = "TransportRoutes.title";
		public final static String Receiver = "TransportRoutes.receiver";
		public final static String Protocol = "TransportRoutes.protocol";
		public final static String Address = "TransportRoutes.address";
		public final static String Priority = "TransportRoutes.priority";
		public final static String Active = "TransportRoutes.active";
		public final static String Error = "TransportRoutes.error";
	}

	public IntegerField.CLASS<IntegerField> priority = new IntegerField.CLASS<IntegerField>(this);
	public BoolField.CLASS<BoolField> active = new BoolField.CLASS<BoolField>(this);

	private TransportRoutes(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();
		id.setDisplayName(Resources.get(strings.Receiver));
		id.get().length = new integer(256);
		id1.setDisplayName(Resources.get(strings.Protocol));
		id1.get().length = new integer(256);
		createdAt.get().system.set(false);
		modifiedAt.get().system.set(false);
		name.setDisplayName(Resources.get(strings.Address));
		priority.setName("Priority");
		priority.setIndex("priority");
		priority.setDisplayName(Resources.get(strings.Priority));
		active.setName("Active");
		active.setIndex("active");
		active.setDisplayName(Resources.get(strings.Active));
		description.setDisplayName(Resources.get(strings.Error));
		registerDataField(priority);
		registerDataField(active);
	}

	public String getReceiver() {
		return id.get().get().toString();
	}

	public String getProtocol() {
		return id1.get().get().toString();
	}

	public String getAddress() {
		return name.get().get().toString();
	}

	public String getUrl() {
		return getProtocol() + ':' + getAddress();
	}

	public boolean readRoute(String receiver, String protocol, String address) {
		SqlToken where = new And(new And(new Rel(this.id.get(), Operation.Eq, new sql_string(receiver)), new Rel(
				this.id1.get(), Operation.Eq, new sql_string(protocol))), new Rel(this.name.get(), Operation.Eq,
				new sql_string(address)));
		return readFirst(where);
	}

	public List<TransportRoute> readActiveRoutes(String receiver) {
		sort(Arrays.<Field> asList(priority.get()), new And(new Rel(this.id.get(), Operation.Eq, new sql_string(receiver)),
				this.active.get().sql_bool()));
		List<TransportRoute> routes = new LinkedList<TransportRoute>();
		while (next()) {
			routes.add(new TransportRoute(recordId(), id.get().get().string().get(), id1.get().get().string().get(), name
					.get().get().string().get(), TransportRoutes.this.priority.get().get().integer().getInt(),
					TransportRoutes.this.active.get().get().bool().get()));
		}
		return routes;
	}

	public guid setRoute(TransportRoute route) {
		return setRoute(route.getReceiver(), route.getProtocol(), route.getAddress(), route.getPriority(), route.isActive());
	}

	public guid setRoute(String receiver, String protocol, String address, int priority, boolean active) {
		this.priority.get().set(priority);
		this.active.get().set(new bool(active));
		if (readRoute(receiver, protocol, address)) {
			update(recordId());
			return recordId();
		} else {
			this.id.get().set(receiver);
			this.id1.get().set(protocol);
			this.name.get().set(address);
			return create();
		}
	}

	public boolean disableRoute(guid routeId, String description) {
		this.active.get().set(new bool(false));
		this.description.get().set(new string(description));
		return update(routeId) > 0;
	}

	public void checkInactiveRoutes() {
		int timeout = Integer.parseInt(Properties.getProperty(ServerRuntime.InactiveRouteTimeoutProperty));
		read(new And(new Unary(Operation.Not, this.active.get().sql_bool()), new Rel(this.modifiedAt.get(), Operation.LT,
				new sql_datetime(new datetime().addMinute(-timeout)))));
		while (next()) {
			this.active.get().set(new bool(true));
			this.description.get().set("");
			update(recordId());
		}
	}

	public string z8_getAddress() {
		return new string(getAddress());
	}

	public string z8_getProtocol() {
		return new string(getProtocol());
	}

	public string z8_getReceiver() {
		return new string(getReceiver());
	}

	public string z8_getUrl() {
		return new string(getUrl());
	}

	public bool z8_readRoute(string receiver, string protocol, string address) {
		return new bool(readRoute(receiver.get(), protocol.get(), address.get()));
	}

	public guid z8_setRoute(string receiver, string protocol, string address, integer priority, bool active) {
		return setRoute(receiver.get(), protocol.get(), address.get(), priority.getInt(), active.get());
	}

	public bool z8_disableRoute(guid routeId, string description) {
		return new bool(disableRoute(routeId, description.get()));
	}

	public static TransportRoutes instance() {
		return new CLASS<TransportRoutes>().get();
	}

}
