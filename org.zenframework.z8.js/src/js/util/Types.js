Z8.define('guid', {
	statics: {
		Null: '00000000-0000-0000-0000-000000000000',

		getByteToHex: function() {
			var byteToHex = guid.ByteToHex;
			if(byteToHex != null)
				return byteToHex;

			byteToHex = guid.ByteToHex = new Array(256);
			for(var i = 0; i < 256; i++)
				byteToHex[i] = (i + 0x100).toString(16).substr(1).toUpperCase();

			return byteToHex;
		},

		create: function() {
			var makeLong = function() {
				return (Math.random() * Math.pow(10, 17)).round();
			};

			var bytes = new Uint8Array(16);

			for(var i = 0, long = makeLong(); i < 16; i++) {
				var byte = long & 0xff;
				bytes[i] = byte;
				long = i == 6 || i == 12 ? makeLong() : ((long - byte) / 256);
			}

			var byteToHex = guid.getByteToHex();
			return byteToHex[bytes[0]] +  byteToHex[bytes[1]] + byteToHex[bytes[2]] + byteToHex[bytes[3]] + '-' +
				byteToHex[bytes[4]] + byteToHex[bytes[5]] + '-' + byteToHex[bytes[6]] + byteToHex[bytes[7]] + '-' + byteToHex[bytes[8]] + byteToHex[bytes[9]] + '-' +
				byteToHex[bytes[10]] + byteToHex[bytes[11]] + byteToHex[bytes[12]] + byteToHex[bytes[13]] + byteToHex[bytes[14]] + byteToHex[bytes[15]];
		}
	}
});

Z8.define('Z8.server.type', {
	shortClassName: 'Type',

	statics: {
		Binary: 'binary',
		Boolean: 'boolean',
		Date: 'date',
		Datetime: 'datetime',
		Datespan: 'datespan',
		File: 'file',
		Files: 'attachments',
		Float: 'float',
		Guid: 'guid',
		Integer: 'int',
		Json: 'json',
		String: 'string',
		Text: 'text'
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

Z8.define('Z8.Operator', {
	shortClassName: 'Operator',

	statics: {
		Eq: 'eq',
		NotEq: 'notEq',
		Contains: 'contains',
		NotContains: 'notContains',
		ContainsWord: 'containsWord',
		NotContainsWord: 'notContainsWord',
		IsSimilarTo: 'isSimilarTo',
		IsNotSimilarTo: 'isNotSimilarTo',
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
		isFalse: 'isFalse',

		Intersects: 'intersects'
	}
});
