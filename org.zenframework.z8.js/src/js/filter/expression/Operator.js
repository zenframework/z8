Z8.define('Z8.filter.operator.Model', {
	extend: 'Z8.data.Model',

	local: true,
	idProperty: 'id',

	fields: [
		new Z8.data.field.String({ name: 'id' }),
		new Z8.data.field.String({ name: 'name' }),
		new Z8.data.field.String({ name: 'icon' }),
		new Z8.data.field.String({ name: 'type' })
	]
});

Z8.define('Z8.filter.String', {
	statics: {
		contains: function(string, what) {
			return string != null && what != null ? string.toLowerCase().indexOf(what.toLowerCase()) != -1 : false;
		},

		notContains: function(string, what) {
			return string != null && what != null ? string.toLowerCase().indexOf(what.toLowerCase()) == -1 : false;
		},

		containsWord: function(string, what) {
			return string != null && what != null ? new RegExp('(^|.*\\W+)' + what + '($|\\W+.*)').test(string) : false;
		},

		notContainsWord: function(string, what) {
			return string != null && what != null ? !(new RegExp('(^|.*\\W+)' + what + '($|\\W+.*)').test(string)) : false;
		},

		isSimilarTo: function(string, what) {
			throw 'Unsupported operation'; // TODO
		},

		isNotSimilarTo: function(string, what) {
			throw 'Unsupported operation'; // TODO
		},

		eq: function(string, what) {
			return string != null ? string.toLowerCase() == (what || '').toLowerCase() : false;
		},

		notEq: function(string, what) {
			return string != null ? string.toLowerCase() != (what || '').toLowerCase() : false;
		},

		gt: function(string, what) {
			return string != null ? string > (what || '') : false;
		},

		ge: function(string, what) {
			return string != null ? string >= (what || '') : false;
		},

		lt: function(string, what) {
			return string != null ? string < (what || '') : false;
		},

		le: function(string, what) {
			return string != null ? string <= (what || '') : false;
		},

		beginsWith: function(string, what) {
			return string != null ? string.startsWith(what || '') : false;
		},

		notBeginsWith: function(string, what) {
			return string != null ? !string.startsWith(what || '') : false;
		},

		endsWith: function(string, what) {
			return string != null ? string.endsWith(what || '') : false;
		},

		notEndsWith: function(string, what) {
			return string != null ? !string.endsWith(what || '') : false;
		},

		isEmpty: function(string) {
			return Z8.isEmpty(string);
		},

		isNotEmpty: function(string) {
			return !Z8.isEmpty(string);
		}
	}
});

Z8.define('Z8.filter.Number', {
	statics: {
		eq: function(number, what) {
			return number == what;
		},

		notEq: function(string, what) {
			return number != what;
		},

		gt: function(string, what) {
			return number > what;
		},

		ge: function(string, what) {
			return number >= what;
		},

		lt: function(string, what) {
			return number < what;
		},

		le: function(string, what) {
			return number <= what;
		}
	}
});

Z8.define('Z8.filter.Operator', {
	statics: {
		data: {
			boolean: [
				{ id: Operator.isTrue, name: Z8.$('Operator.true'), icon: 'fa-dot-circle-o' },
				{ id: Operator.isFalse, name: Z8.$('Operator.false'), icon: 'fa-circle-o' }
			],

			datetime: [
				{ id: Operator.GT, name: Z8.$('Operator.after'), icon: 'fa-math-gt', type: Type.Datetime },
				{ id: Operator.GE, name: Z8.$('Operator.notEarlierThan'), icon: 'fa-math-ge', type: Type.Datetime },
				{ id: Operator.LT, name: Z8.$('Operator.before'), icon: 'fa-math-lt', type: Type.Datetime },
				{ id: Operator.LE, name: Z8.$('Operator.notLaterThan'), icon: 'fa-math-le', type: Type.Datetime },

				{ id: Operator.Yesterday, name: Z8.$('Operator.yesterday') },
				{ id: Operator.Today, name: Z8.$('Operator.today') },
				{ id: Operator.Tomorrow, name: Z8.$('Operator.tomorrow') },

				{ id: Operator.LastWeek, name: Z8.$('Operator.lastWeek') },
				{ id: Operator.ThisWeek, name: Z8.$('Operator.thisWeek') },
				{ id: Operator.NextWeek, name: Z8.$('Operator.nextWeek') },

				{ id: Operator.LastMonth, name: Z8.$('Operator.lastMonth') },
				{ id: Operator.ThisMonth, name: Z8.$('Operator.thisMonth') },
				{ id: Operator.NextMonth, name: Z8.$('Operator.nextMonth') },

				{ id: Operator.LastYear, name: Z8.$('Operator.lastYear') },
				{ id: Operator.ThisYear, name: Z8.$('Operator.thisYear') },
				{ id: Operator.NextYear, name: Z8.$('Operator.nextYear') },

				{ id: Operator.LastDays, name: Z8.$('Operator.lastDays'), type: Type.Integer },
				{ id: Operator.NextDays, name: Z8.$('Operator.nextDays'), type: Type.Integer },

				{ id: Operator.LastHours, name: Z8.$('Operator.lastHours'), type: Type.Integer },
				{ id: Operator.NextHours, name: Z8.$('Operator.nextHours'), type: Type.Integer }
			],

			date: [
				{ id: Operator.Eq, name: Z8.$('Operator.equalTo'), icon: 'fa-math-eq', type: Type.Date },
				{ id: Operator.NotEq, name: Z8.$('Operator.notEqualTo'), icon: 'fa-math-not-eq', type: Type.Date },

				{ id: Operator.GT, name: Z8.$('Operator.after'), icon: 'fa-math-gt', type: Type.Date },
				{ id: Operator.GE, name: Z8.$('Operator.notEarlierThan'), icon: 'fa-math-ge', type: Type.Date },
				{ id: Operator.LT, name: Z8.$('Operator.before'), icon: 'fa-math-lt', type: Type.Date },
				{ id: Operator.LE, name: Z8.$('Operator.notLaterThan'), icon: 'fa-math-le', type: Type.Date },

				{ id: Operator.Yesterday, name: Z8.$('Operator.yesterday') },
				{ id: Operator.Today, name: Z8.$('Operator.today') },
				{ id: Operator.Tomorrow, name: Z8.$('Operator.tomorrow') },

				{ id: Operator.LastWeek, name: Z8.$('Operator.lastWeek') },
				{ id: Operator.ThisWeek, name: Z8.$('Operator.thisWeek') },
				{ id: Operator.NextWeek, name: Z8.$('Operator.nextWeek') },

				{ id: Operator.LastMonth, name: Z8.$('Operator.lastMonth') },
				{ id: Operator.ThisMonth, name: Z8.$('Operator.thisMonth') },
				{ id: Operator.NextMonth, name: Z8.$('Operator.nextMonth') },

				{ id: Operator.LastYear, name: Z8.$('Operator.lastYear') },
				{ id: Operator.ThisYear, name: Z8.$('Operator.thisYear') },
				{ id: Operator.NextYear, name: Z8.$('Operator.nextYear') },

				{ id: Operator.LastDays, name: Z8.$('Operator.lastDays'), type: Type.Integer },
				{ id: Operator.NextDays, name: Z8.$('Operator.nextDays'), type: Type.Integer },

				{ id: Operator.LastHours, name: Z8.$('Operator.lastHours'), type: Type.Integer },
				{ id: Operator.NextHours, name: Z8.$('Operator.nextHours'), type: Type.Integer }
			],

			integer: [
				{ id: Operator.Eq, name: Z8.$('Operator.equalTo'), icon: 'fa-math-eq', type: Type.Integer },
				{ id: Operator.NotEq, name:  Z8.$('Operator.notEqualTo'), icon: 'fa-math-not-eq', type: Type.Integer },

				{ id: Operator.GT, name: Z8.$('Operator.greaterThan'), icon: 'fa-math-gt', type: Type.Integer },
				{ id: Operator.GE, name: Z8.$('Operator.numberGreaterThanOrEqualTo'), icon: 'fa-math-ge', type: Type.Integer },
				{ id: Operator.LT, name: Z8.$('Operator.lessThan'), icon: 'fa-math-lt', type: Type.Integer },
				{ id: Operator.LE, name: Z8.$('Operator.numberLessThanOrEqualTo'), icon: 'fa-math-le', type: Type.Integer }
			],

			float: [
				{ id: Operator.Eq, name: Z8.$('Operator.equalTo'), icon: 'fa-math-eq', type: Type.Float },
				{ id: Operator.NotEq, name: Z8.$('Operator.notEqualTo'), icon: 'fa-math-not-eq', type: Type.Float },

				{ id: Operator.GT, name: Z8.$('Operator.greaterThan'), icon: 'fa-math-gt', type: Type.Float },
				{ id: Operator.GE, name: Z8.$('Operator.numberGreaterThanOrEqualTo'), icon: 'fa-math-ge', type: Type.Float },
				{ id: Operator.LT, name: Z8.$('Operator.lessThan'), icon: 'fa-math-lt', type: Type.Float },
				{ id: Operator.LE, name: Z8.$('Operator.numberLessThanOrEqualTo'), icon: 'fa-math-le', type: Type.Float }
			],

			string: [
				{ id: Operator.Contains, name: Z8.$('Operator.contains'), icon: 'fa-superset-of', type: Type.String, filter: Z8.filter.String.contains },
				{ id: Operator.NotContains, name: Z8.$('Operator.doesNotContain'), icon: 'fa-not-superset-of', type: Type.String, filter: Z8.filter.String.notContains },

				{ id: Operator.ContainsWord, name: Z8.$('Operator.containsAWord'), icon: 'fa-superset-of-or-eq', type: Type.String, filter: Z8.filter.String.contains },
				{ id: Operator.NotContainsWord, name: Z8.$('Operator.doesNotContainAWord'), icon: 'fa-not-superset-of-or-eq', type: Type.String, filter: Z8.filter.String.notContains },

				{ id: Operator.IsSimilarTo, name: Z8.$('Operator.isSimilarTo'), icon: 'fa-math-tilde', type: Type.String, filter: Z8.filter.String.isSimilarTo },
				{ id: Operator.IsNotSimilarTo, name: Z8.$('Operator.isNotSimilarTo'), icon: 'fa-math-not-tilde', type: Type.String, filter: Z8.filter.String.isNotSimilarTo },

				{ id: Operator.Eq, name: Z8.$('Operator.equalTo'), icon: 'fa-math-eq', type: Type.String, filter: Z8.filter.String.eq },
				{ id: Operator.NotEq, name: Z8.$('Operator.notEqualTo'), icon: 'fa-math-not-eq', type: Type.String, filter: Z8.filter.String.notEq },

				{ id: Operator.GT, name: Z8.$('Operator.greaterThan'), icon: 'fa-math-gt', type: Type.String },
				{ id: Operator.GE, name: Z8.$('Operator.stringGreaterThanOrEqualTo'), icon: 'fa-math-ge', type: Type.String },
				{ id: Operator.LT, name: Z8.$('Operator.lessThan'), icon: 'fa-math-lt', type: Type.String },
				{ id: Operator.LE, name: Z8.$('Operator.stringLessThanOrEqualTo'), icon: 'fa-math-le', type: Type.String },

				{ id: Operator.BeginsWith, name: Z8.$('Operator.beginsWith'), type: Type.String },
				{ id: Operator.NotBeginsWith, name: Z8.$('Operator.doesNotBeginWith'), type: Type.String },
				{ id: Operator.EndsWith, name: Z8.$('Operator.endsWith'), type: Type.String },
				{ id: Operator.NotEndsWith, name: Z8.$('Operator.doesNotEndWith'), type: Type.String },

				{ id: Operator.IsEmpty, name: Z8.$('Operator.isEmptyString') },
				{ id: Operator.IsNotEmpty, name: Z8.$('Operator.isNotEmptyString') }
			]
		},

		filters: null,

		fields: ['id', 'name', 'icon', 'type'],

		getOperators: function(type) {
			switch(type) {
			case Type.String:
			case Type.Text:
			case Type.Guid:
				if(this.string == null) {
					this.string = new Z8.data.Store({ model: 'Z8.filter.operator.Model', data: this.data.string });
					this.string.use();
				}
				return this.string;
			case Type.Boolean:
				if(this.boolean == null) {
					this.boolean = new Z8.data.Store({ model: 'Z8.filter.operator.Model', data: this.data.boolean });
					this.boolean.use();
				}
				return this.boolean;
			case Type.Date:
				if(this.date == null) {
					this.date = new Z8.data.Store({ model: 'Z8.filter.operator.Model', data: this.data.date });
					this.date.use();
				}
				return this.date;
			case Type.Datetime:
				if(this.datetime == null) {
					this.datetime = new Z8.data.Store({ model: 'Z8.filter.operator.Model', data: this.data.datetime });
					this.datetime.use();
				}
				return this.datetime;
			case Type.Integer:
				if(this.integer == null) {
					this.integer = new Z8.data.Store({ model: 'Z8.filter.operator.Model', data: this.data.integer });
					this.integer.use();
				}
				return this.integer;
			case Type.Float:
				if(this.float == null) {
					this.float = new Z8.data.Store({ model: 'Z8.filter.operator.Model', data: this.data.float });
					this.float.use();
				}
				return this.float;
			default:
				throw 'Unknown type: ' + type;
			}
		},

		getFilters: function() {
			if(this.filters != null)
				return this.filters;

			var filters = this.filters = {};

			filters.string = this.getStringFilters();
			filters.integer = this.getNumberFilters();
			filters.float = this.getNumberFilters();

			return filters;
		},

		getStringFilters: function() {
			var filters = {};

			filters[Operator.IsSimilarTo] = Z8.filter.String.isSimilarTo;
			filters[Operator.IsNotSimilarTo] = Z8.filter.String.isNotSimilarTo;

			filters[Operator.Contains] = Z8.filter.String.contains;
			filters[Operator.NotContains] = Z8.filter.String.notContains;

			filters[Operator.ContainsWord] = Z8.filter.String.containsWord;
			filters[Operator.NotContainsWord] = Z8.filter.String.notContainsWord;

			filters[Operator.Eq] = Z8.filter.String.eq;
			filters[Operator.NotEq] = Z8.filter.String.notEq;

			filters[Operator.GT] = Z8.filter.String.gt;
			filters[Operator.GE] = Z8.filter.String.ge;
			filters[Operator.LT] = Z8.filter.String.lt;
			filters[Operator.LE] = Z8.filter.String.le;
			filters[Operator.Eq] = Z8.filter.String.eq;

			filters[Operator.BeginsWith] = Z8.filter.String.beginsWidth;
			filters[Operator.NotBeginsWith] = Z8.filter.String.notBeginsWidth;
			filters[Operator.EndsWith] = Z8.filter.String.endsWidth;
			filters[Operator.NotEndsWith] = Z8.filter.String.notEndsWidth;

			filters[Operator.IsEmpty] = Z8.filter.String.isEmpty;
			filters[Operator.IsNotEmpty] = Z8.filter.String.isNotEmpty;

			return filters;
		},

		getNumberFilters: function() {
			var filters = {};

			filters[Operator.Eq] = Z8.filter.Number.eq;
			filters[Operator.NotEq] = Z8.filter.Number.notEq;

			filters[Operator.GT] = Z8.filter.Number.gt;
			filters[Operator.GE] = Z8.filter.Number.ge;
			filters[Operator.LT] = Z8.filter.Number.lt;
			filters[Operator.LE] = Z8.filter.Number.le;
			filters[Operator.Eq] = Z8.filter.Number.eq;

			return filters;
		},

		applyFilter: function(record, filter) {
			var property = filter.property;
			var operator = filter.operator;
			var parameter = filter.value;

			if(Z8.isEmpty(property) || Z8.isEmpty(operator))
				return true;

			var field = record.getField(property);
			if(field == null)
				return true;

			var type = field.getType();

			var filters = this.getFilters()[type];
			if(filters == null)
				return true;

			var filterFn = filters[operator];
			return filterFn != null ? filterFn(record.get(property), field.convert(parameter)) : true;
		}
	}
});
