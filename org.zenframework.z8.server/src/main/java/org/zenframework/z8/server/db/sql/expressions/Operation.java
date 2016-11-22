package org.zenframework.z8.server.db.sql.expressions;

public enum Operation {
	None(""),
	Not(Names.Not),
	Minus(Names.Minus),
	Add(Names.Add),
	Sub(Names.Sub),
	Mul(Names.Mul),
	Div(Names.Div),
	Mod(Names.Mod),
	And(Names.And),
	Or(Names.Or),
	Eq(Names.Eq),
	NotEq(Names.NotEq),
	LT(Names.LT),
	GT(Names.GT),
	LE(Names.LE),
	GE(Names.GE),

	BeginsWith(Names.BeginsWith),
	NotBeginsWith(Names.NotBeginsWith),
	EndsWith(Names.EndsWith),
	NotEndsWith(Names.NotEndsWith),
	Contains(Names.Contains),
	NotContains(Names.NotContains),
	IsEmpty(Names.IsEmpty),
	IsNotEmpty(Names.IsNotEmpty),

	Yesterday(Names.Yesterday),
	Today(Names.Today),
	Tomorrow(Names.Tomorrow),

	LastWeek(Names.LastWeek),
	ThisWeek(Names.ThisWeek),
	NextWeek(Names.NextWeek),

	LastMonth(Names.LastMonth),
	ThisMonth(Names.ThisMonth),
	NextMonth(Names.NextMonth),

	LastYear(Names.LastYear),
	ThisYear(Names.ThisYear),
	NextYear(Names.NextYear),

	LastDays(Names.LastDays),
	NextDays(Names.NextDays),

	LastHours(Names.LastHours),
	NextHours(Names.NextHours),

	IsTrue(Names.IsTrue),
	IsFalse(Names.IsFalse);

	class Names {
		static protected final String Not = "not";
		static protected final String Minus = "minus";
		static protected final String Add = "add";
		static protected final String Sub = "sub";
		static protected final String Mul = "mul";
		static protected final String Div = "div";
		static protected final String Mod = "mod";

		static protected final String And = "and";
		static protected final String Or = "or";
		static protected final String Eq = "eq";
		static protected final String NotEq = "notEq";
		static protected final String LT = "lt";
		static protected final String GT = "gt";
		static protected final String LE = "le";
		static protected final String GE = "ge";

		static protected final String BeginsWith = "beginsWith";
		static protected final String NotBeginsWith = "notBeginsWith";
		static protected final String EndsWith = "endsWith";
		static protected final String NotEndsWith = "notEndsWith";
		static protected final String Contains = "contains";
		static protected final String NotContains = "notContains";
		static protected final String IsEmpty = "isEmpty";
		static protected final String IsNotEmpty = "isNotEmpty";

		static protected final String Yesterday = "yesterday";
		static protected final String Today = "today";
		static protected final String Tomorrow = "tomorrow";

		static protected final String LastWeek = "lastWeek";
		static protected final String ThisWeek = "thisWeek";
		static protected final String NextWeek = "nextWeek";

		static protected final String LastMonth = "lastMonth";
		static protected final String ThisMonth = "thisMonth";
		static protected final String NextMonth = "nextMonth";

		static protected final String LastYear = "lastYear";
		static protected final String ThisYear = "thisYear";
		static protected final String NextYear = "nextYear";

		static protected final String LastDays = "lastDays";
		static protected final String NextDays = "nextDays";

		static protected final String LastHours = "lastHours";
		static protected final String NextHours = "nextHours";

		static protected final String IsTrue = "isTrue";
		static protected final String IsFalse = "isFalse";
	}

	private String fName = null;

	Operation(String name) {
		fName = name;
	}

	@Override
	public String toString() {
		return fName;
	}

	static public Operation fromString(String string) {
		string = string.toLowerCase();

		if(Names.Not.toLowerCase().equals(string))
			return Operation.Not;
		else if(Names.Minus.toLowerCase().equals(string))
			return Operation.Minus;
		else if(Names.Add.toLowerCase().equals(string))
			return Operation.Add;
		else if(Names.Sub.toLowerCase().equals(string))
			return Operation.Sub;
		else if(Names.Mul.toLowerCase().equals(string))
			return Operation.Mul;
		else if(Names.Div.toLowerCase().equals(string))
			return Operation.Div;
		else if(Names.Mod.toLowerCase().equals(string))
			return Operation.Mod;
		else if(Names.And.toLowerCase().equals(string))
			return Operation.And;
		else if(Names.Or.toLowerCase().equals(string))
			return Operation.Or;
		else if(Names.Eq.toLowerCase().equals(string))
			return Operation.Eq;
		else if(Names.NotEq.toLowerCase().equals(string))
			return Operation.NotEq;
		else if(Names.LT.toLowerCase().equals(string))
			return Operation.LT;
		else if(Names.GT.toLowerCase().equals(string))
			return Operation.GT;
		else if(Names.LE.toLowerCase().equals(string))
			return Operation.LE;
		else if(Names.GE.toLowerCase().equals(string))
			return Operation.GE;

		else if(Names.Yesterday.toLowerCase().equals(string))
			return Operation.Yesterday;
		else if(Names.Today.toLowerCase().equals(string))
			return Operation.Today;
		else if(Names.Tomorrow.toLowerCase().equals(string))
			return Operation.Tomorrow;

		else if(Names.BeginsWith.toLowerCase().equals(string))
			return Operation.BeginsWith;
		else if(Names.NotBeginsWith.toLowerCase().equals(string))
			return Operation.NotBeginsWith;
		else if(Names.EndsWith.toLowerCase().equals(string))
			return Operation.EndsWith;
		else if(Names.NotEndsWith.toLowerCase().equals(string))
			return Operation.NotEndsWith;
		else if(Names.Contains.toLowerCase().equals(string))
			return Operation.Contains;
		else if(Names.NotContains.toLowerCase().equals(string))
			return Operation.NotContains;
		else if(Names.IsEmpty.toLowerCase().equals(string))
			return Operation.IsEmpty;
		else if(Names.IsNotEmpty.toLowerCase().equals(string))
			return Operation.IsNotEmpty;

		else if(Names.LastWeek.toLowerCase().equals(string))
			return Operation.LastWeek;
		else if(Names.ThisWeek.toLowerCase().equals(string))
			return Operation.ThisWeek;
		else if(Names.NextWeek.toLowerCase().equals(string))
			return Operation.NextWeek;

		else if(Names.LastWeek.toLowerCase().equals(string))
			return Operation.LastWeek;
		else if(Names.ThisWeek.toLowerCase().equals(string))
			return Operation.ThisWeek;
		else if(Names.NextWeek.toLowerCase().equals(string))
			return Operation.NextWeek;

		else if(Names.LastMonth.toLowerCase().equals(string))
			return Operation.LastMonth;
		else if(Names.ThisMonth.toLowerCase().equals(string))
			return Operation.ThisMonth;
		else if(Names.NextMonth.toLowerCase().equals(string))
			return Operation.NextMonth;

		else if(Names.LastYear.toLowerCase().equals(string))
			return Operation.LastYear;
		else if(Names.ThisYear.toLowerCase().equals(string))
			return Operation.ThisYear;
		else if(Names.NextYear.toLowerCase().equals(string))
			return Operation.NextYear;

		else if(Names.LastDays.toLowerCase().equals(string))
			return Operation.LastDays;
		else if(Names.NextDays.toLowerCase().equals(string))
			return Operation.NextDays;

		else if(Names.LastHours.toLowerCase().equals(string))
			return Operation.LastHours;
		else if(Names.NextHours.toLowerCase().equals(string))
			return Operation.NextHours;

		else if(Names.IsTrue.toLowerCase().equals(string))
			return Operation.IsTrue;
		else if(Names.IsFalse.toLowerCase().equals(string))
			return Operation.IsFalse;

		else
			throw new RuntimeException("Unknown operation: '" + string + "'");
	}
}
