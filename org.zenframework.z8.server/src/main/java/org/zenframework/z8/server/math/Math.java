package org.zenframework.z8.server.math;

import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datespan;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class Math {
	static public final decimal PI = new decimal(java.lang.Math.PI);
	
	static public integer z8_max(integer a, integer b) {
		return new integer(a.operatorMoreEqu(b).get() ? a : b);
	}

	static public decimal z8_max(integer a, decimal b) {
		return new decimal(a.operatorMoreEqu(b).get() ? new decimal(a) : b);
	}

	static public decimal z8_max(decimal a, integer b) {
		return new decimal(a.operatorMoreEqu(b).get() ? a : new decimal(b));
	}

	static public decimal z8_max(decimal a, decimal b) {
		return new decimal(a.operatorMoreEqu(b).get() ? a : b);
	}

	static public integer z8_min(integer a, integer b) {
		return new integer(a.operatorLessEqu(b).get() ? a : b);
	}

	static public decimal z8_min(integer a, decimal b) {
		return new decimal(a.operatorLessEqu(b).get() ? new decimal(a) : b);
	}

	static public decimal z8_min(decimal a, integer b) {
		return new decimal(a.operatorLessEqu(b).get() ? a : new decimal(b));
	}

	static public decimal z8_min(decimal a, decimal b) {
		return new decimal(a.operatorLessEqu(b).get() ? a : b);
	}

	static public date z8_max(date a, date b) {
		return new date(a.operatorMoreEqu(b).get() ? a : b);
	}

	static public datespan z8_max(datespan a, datespan b) {
		return new datespan(a.operatorMoreEqu(b).get() ? a : b);
	}

	static public string z8_max(string a, string b) {
		return new string(a.operatorMoreEqu(b).get() ? a : b);
	}

	static public date z8_min(date a, date b) {
		return new date(a.operatorLessEqu(b).get() ? a : b);
	}

	static public datespan z8_min(datespan a, datespan b) {
		return new datespan(a.operatorLessEqu(b).get() ? a : b);
	}

	static public string z8_min(string a, string b) {
		return new string(a.operatorLessEqu(b).get() ? a : b);
	}

	static public decimal z8_pow(decimal a, decimal b) {
		return new decimal(java.lang.Math.pow(a.get().doubleValue(), b.get().doubleValue()));
	}

	static public decimal z8_hypot(decimal x, decimal y) {
		return new decimal(java.lang.Math.hypot(x.get().doubleValue(), y.get().doubleValue()));
	}

	static public decimal z8_sin(decimal a) {
		return new decimal(java.lang.Math.sin(a.getDouble()));
	}

	static public decimal z8_cos(decimal a) {
		return new decimal(java.lang.Math.cos(a.getDouble()));
	}

	static public decimal z8_random() {
		return new decimal(java.lang.Math.random());
	}
}
