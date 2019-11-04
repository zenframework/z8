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
	ContainsWord(Names.ContainsWord),
	NotContainsWord(Names.NotContainsWord),
	IsSimilarTo(Names.IsSimilarTo),
	IsNotSimilarTo(Names.IsNotSimilarTo),
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
		static protected final String ContainsWord = "containsWord";
		static protected final String NotContainsWord = "notContainsWord";
		static protected final String IsSimilarTo = "isSimilarTo";
		static protected final String IsNotSimilarTo = "isNotSimilarTo";
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
		for (Operation value : values())
			if (value.name().equalsIgnoreCase(string))
				return value;
		throw new RuntimeException("Unknown operation: '" + string + "'");
	}
}
