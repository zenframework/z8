Z8.define('guid', {
	statics: {
		Null: '00000000-0000-0000-0000-000000000000'
	}
});

Z8.define('Z8.server.type', {
	shortClassName: 'Type',

	statics: {
		Guid: 'guid',
		Integer: 'int',
		Float: 'float',
		Boolean: 'boolean',
		Date: 'date',
		Datetime: 'datetime',
		Datespan: 'datespan',
		String: 'string',
		Binary: 'binary',
		Text: 'text',
		File: 'file',
		Files: 'attachments',
		Json: 'json'
	}
});

Z8.define('Z8.RecordLock', {
	shortClassName: 'RecordLock',

	statics: {
		None: 0,
		Full: 1,
		Edit: 2,
		Destroy: 3
	}
});

Z8.define('Z8.Operation', {
	shortClassName: 'Operation',

	statics: {
		Eq: 'eq',
		NotEq: 'notEq',
		Contains: 'contains',
		NotContains: 'notContains',
		BeginsWith: 'beginsWith',
		NotBeginsWith: 'notBeginsWith',
		EndsWith: 'endsWith',
		NotEndsWith: 'notEndsWith',
		IsEmpty: 'isEmpty',
		IsNotEmpty: 'isNotEmpty',

		GT: 'gt',
		GE: 'ge',
		LT: 'lt',
		LE: 'le',

		Today: 'today',
		Tomorrow: 'tomorrow',
		Yesterday: 'yesterday',

		LastWeek: 'lastWeek',
		ThisWeek: 'thisWeek',
		NextWeek: 'nextWeek',

		LastMonth: 'lastMonth',
		ThisMonth: 'thisMonth',
		NextMonth: 'nextMonth',

		LastYear: 'lastYear',
		ThisYear: 'thisYear',
		NextYear: 'nextYear',

		LastDays: 'lastDays',
		NextDays: 'nextDays',

		LastHours: 'lastHours',
		NextHours: 'nextHours',

		isTrue: 'isTrue',
		isFalse: 'isFalse'
	}
});
