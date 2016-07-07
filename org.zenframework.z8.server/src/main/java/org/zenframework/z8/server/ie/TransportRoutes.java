package org.zenframework.z8.server.ie;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.table.system.SystemDomains;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_datetime;

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
		public final static String Domain = "TransportRoutes.domain";
		public final static String Protocol = "TransportRoutes.protocol";
		public final static String Address = "TransportRoutes.address";
		public final static String Priority = "TransportRoutes.priority";
		public final static String Active = "TransportRoutes.active";
		public final static String Error = "TransportRoutes.error";
	}

	public final SystemDomains.CLASS<SystemDomains> domains = new SystemDomains.CLASS<SystemDomains>(this);
	public final Link.CLASS<Link> domainLink = new Link.CLASS<Link>(this);
	public final IntegerField.CLASS<IntegerField> priority = new IntegerField.CLASS<IntegerField>(this);
	public final BoolField.CLASS<BoolField> active = new BoolField.CLASS<BoolField>(this);

	private TransportRoutes(IObject container) {
		super(container);
	}

	@Override
	public void constructor1() {
		super.constructor1();
		domainLink.get(CLASS.Constructor1).operatorAssign(domains);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		domains.setIndex("domains");

		domainLink.setName("Domain");
		domainLink.setIndex("domainLink");
		domains.get().id.setDisplayName(Resources.get(strings.Domain));

		id1.setDisplayName(Resources.get(strings.Protocol));
		id1.get().length = new integer(256);

		createdAt.setSystem(false);
		modifiedAt.setSystem(false);

		name.setDisplayName(Resources.get(strings.Address));

		priority.setName("Priority");
		priority.setIndex("priority");
		priority.setDisplayName(Resources.get(strings.Priority));

		active.setName("Active");
		active.setIndex("active");
		active.setDisplayName(Resources.get(strings.Active));

		description.setDisplayName(Resources.get(strings.Error));

		registerDataField(domainLink);
		registerDataField(priority);
		registerDataField(active);

		registerFormField(createdAt);
		registerFormField(modifiedAt);
		registerFormField(domainLink);
		registerFormField(domains.get().id);
		registerFormField(id1);
		registerFormField(name);
		registerFormField(description);
		registerFormField(priority);
		registerFormField(active);

		queries.add(domains);

		links.add(domainLink);
	}

	public String getDomain() {
		return domains.get().id.get().toString();
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

	public boolean readRoute(guid domain, String protocol, String address) {
		return readFirst(getWhere(domain, protocol, address));
	}

	public boolean readRoute(String domain, String protocol, String address) {
		return readFirst(getWhere(domain, protocol, address));
	}

	public List<TransportRoute> readRoutes(String domain, boolean activeOnly) {
		SqlToken where = new Equ(this.domains.get().id.get(), domain);
		if (activeOnly)
			where = new And(where, this.active.get().sql_bool());

		sort(Arrays.<Field> asList(priority.get()), where);
		List<TransportRoute> routes = new LinkedList<TransportRoute>();
		while (next()) {
			routes.add(new TransportRoute(domains.get().id.get().string().get(), id1.get().string().get(), name.get()
					.string().get(), priority.get().integer().getInt(), active.get().bool().get()));
		}
		return routes;
	}

	public guid setRoute(TransportRoute route) {
		return setRoute(route.getDomain(), route.getProtocol(), route.getAddress(), route.getPriority(), route.isActive());
	}

	/*public guid setRoute(String domain, String protocol, String address, int priority, boolean active) {
		if (domains.get().readFirst(new Rel(domains.get().id.get(), Operation.Eq, new sql_string(domain)))) {
			return setRoute(domains.get().recordId(), protocol, address, priority, active);
		} else {
			throw new exception("Domain '" + domain + "' does not exist");
		}
	}*/

	public guid setRoute(String domain, String protocol, String address, int priority, boolean active) {
		SystemDomains domains = SystemDomains.newInstance();
		if (!domains.readFirst(new Equ(domains.id.get(), domain)))
			throw new exception("Domain '" + domain + "' does not exist");
		guid domainId = domains.recordId();
		this.priority.get().set(priority);
		this.active.get().set(new bool(active));
		guid routeId;
		if (readRoute(domain, protocol, address)) {
			routeId = recordId();
			update(routeId);
		} else {
			this.domainLink.get().set(domainId);
			this.id1.get().set(protocol);
			this.name.get().set(address);
			routeId = create();
		}
		return routeId;
	}

	public boolean disableRoute(TransportRoute route, String description) {
		return disableRoute(route.getDomain(), route.getProtocol(), route.getAddress(), description);
	}

	public boolean disableRoute(String domain, String protocol, String address, String description) {
		active.get().set(new bool(false));
		return update(getWhere(domain, protocol, address)) > 0;
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

	public string z8_getDomain() {
		return new string(getDomain());
	}

	public string z8_getUrl() {
		return new string(getUrl());
	}

	public bool z8_readRoute(string domain, string protocol, string address) {
		return new bool(readRoute(domain.get(), protocol.get(), address.get()));
	}

	public bool z8_readRoute(guid domain, string protocol, string address) {
		return new bool(readRoute(domain, protocol.get(), address.get()));
	}

	public guid z8_setRoute(string domain, string protocol, string address, integer priority, bool active) {
		return setRoute(domain.get(), protocol.get(), address.get(), priority.getInt(), active.get());
	}

	public bool z8_disableRoute(string domain, string protocol, string address, string description) {
		return new bool(disableRoute(domain.get(), protocol.get(), address.get(), description.get()));
	}

	public static TransportRoutes newInstance() {
		return new CLASS<TransportRoutes>().get();
	}

	private SqlToken getWhere(String domain, String protocol, String address) {
		return new And(new And(new Equ(this.domains.get().id.get(), domain), new Equ(this.id1.get(), protocol)), new Equ(this.name.get(), address));
	}

	private SqlToken getWhere(guid domain, String protocol, String address) {
		return new And(new And(new Equ(this.domainLink.get(), domain), new Equ(this.id1.get(), protocol)), new Equ(this.name.get(), address));
	}

}
