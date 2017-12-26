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
			return string != null ? string.indexOf(what || '') != -1 : false;
		},

		notContains: function(string, what) {
			return string != null ? string.indexOf(what || '') == -1 : false;
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
	shortClassName: 'Operator',

	statics: {
		data: {
			boolean: [
				{ id: Operation.isTrue, name: 'да', icon: 'fa-dot-circle-o' },
				{ id: Operation.isFalse, name: 'нет', icon: 'fa-circle-o' }
			],

			datetime: [
				{ id: Operation.GT, name: 'после', icon: 'fa-math-gt', type: Type.Datetime },
				{ id: Operation.GE, name: 'не ранее', icon: 'fa-math-ge', type: Type.Datetime },
				{ id: Operation.LT, name: 'до', icon: 'fa-math-lt', type: Type.Datetime },
				{ id: Operation.LE, name: 'не позже', icon: 'fa-math-le', type: Type.Datetime },

				{ id: Operation.Yesterday, name: 'вчера' },
				{ id: Operation.Today, name: 'сегодня' },
				{ id: Operation.Tomorrow, name: 'завтра' },

				{ id: Operation.LastWeek, name: 'прошлая неделя' },
				{ id: Operation.ThisWeek, name: 'текущая неделя' },
				{ id: Operation.NextWeek, name: 'следующая неделя' },

				{ id: Operation.LastMonth, name: 'прошлый месяц' },
				{ id: Operation.ThisMonth, name: 'текущий месяц' },
				{ id: Operation.NextMonth, name: 'следующий месяц' },

				{ id: Operation.LastYear, name: 'прошлый год' },
				{ id: Operation.ThisYear, name: 'текущий год' },
				{ id: Operation.NextYear, name: 'следующий год' },

				{ id: Operation.LastDays, name: 'последние X дней', type: Type.Integer },
				{ id: Operation.NextDays, name: 'следующие X дней', type: Type.Integer },

				{ id: Operation.LastHours, name: 'последние X часов', type: Type.Integer },
				{ id: Operation.NextHours, name: 'следующие X часов', type: Type.Integer }
			],

			date: [
				{ id: Operation.Eq, name: 'равно', icon: 'fa-math-eq', type: Type.Date },
				{ id: Operation.NotEq, name: 'не равно', icon: 'fa-math-not-eq', type: Type.Date },

				{ id: Operation.GT, name: 'после', icon: 'fa-math-gt', type: Type.Date },
				{ id: Operation.GE, name: 'не ранее', icon: 'fa-math-ge', type: Type.Date },
				{ id: Operation.LT, name: 'до', icon: 'fa-math-lt', type: Type.Date },
				{ id: Operation.LE, name: 'не позже', icon: 'fa-math-le', type: Type.Date },

				{ id: Operation.Yesterday, name: 'вчера' },
				{ id: Operation.Today, name: 'сегодня' },
				{ id: Operation.Tomorrow, name: 'завтра' },

				{ id: Operation.LastWeek, name: 'прошлая неделя' },
				{ id: Operation.ThisWeek, name: 'текущая неделя' },
				{ id: Operation.NextWeek, name: 'следующая неделя' },

				{ id: Operation.LastMonth, name: 'прошлый месяц' },
				{ id: Operation.ThisMonth, name: 'текущий месяц' },
				{ id: Operation.NextMonth, name: 'следующий месяц' },

				{ id: Operation.LastYear, name: 'прошлый год' },
				{ id: Operation.ThisYear, name: 'текущий год' },
				{ id: Operation.NextYear, name: 'следующий год' },

				{ id: Operation.LastDays, name: 'последние X дней', type: Type.Integer },
				{ id: Operation.NextDays, name: 'следующие X дней', type: Type.Integer },

				{ id: Operation.LastHours, name: 'последние X часов', type: Type.Integer },
				{ id: Operation.NextHours, name: 'следующие X часов', type: Type.Integer }
			],

			integer: [
				{ id: Operation.Eq, name: 'равно', icon: 'fa-math-eq', type: Type.Integer },
				{ id: Operation.NotEq, name: 'не равно', icon: 'fa-math-not-eq', type: Type.Integer },

				{ id: Operation.GT, name: 'больше', icon: 'fa-math-gt', type: Type.Integer },
				{ id: Operation.GE, name: 'не меньше', icon: 'fa-math-ge', type: Type.Integer },
				{ id: Operation.LT, name: 'меньше', icon: 'fa-math-lt', type: Type.Integer },
				{ id: Operation.LE, name: 'не больше', icon: 'fa-math-le', type: Type.Integer }
			],

			float: [
				{ id: Operation.Eq, name: 'равно', icon: 'fa-math-eq', type: Type.Float },
				{ id: Operation.NotEq, name: 'не равно', icon: 'fa-math-not-eq', type: Type.Float },

				{ id: Operation.GT, name: 'больше', icon: 'fa-math-gt', type: Type.Float },
				{ id: Operation.GE, name: 'не меньше', icon: 'fa-math-ge', type: Type.Float },
				{ id: Operation.LT, name: 'меньше', icon: 'fa-math-lt', type: Type.Float },
				{ id: Operation.LE, name: 'не больше', icon: 'fa-math-le', type: Type.Float }
			],

			string: [
				{ id: Operation.Contains, name: 'содержит', icon: 'fa-superset-of', type: Type.String, filter: Z8.filter.String.contains },
				{ id: Operation.NotContains, name: 'не содержит', icon: 'fa-not-superset-of', type: Type.String, filter: Z8.filter.String.notContains },

				{ id: Operation.Eq, name: 'равно', icon: 'fa-math-eq', type: Type.String, filter: Z8.filter.String.eq },
				{ id: Operation.NotEq, name: 'не равно', icon: 'fa-math-not-eq', type: Type.String, filter: Z8.filter.String.notEq },

				{ id: Operation.GT, name: 'больше', icon: 'fa-math-gt', type: Type.String },
				{ id: Operation.GE, name: 'больше или равно', icon: 'fa-math-ge', type: Type.String },
				{ id: Operation.LT, name: 'меньше', icon: 'fa-math-lt', type: Type.String },
				{ id: Operation.LE, name: 'меньше или равно', icon: 'fa-math-le', type: Type.String },

				{ id: Operation.BeginsWith, name: 'начинается с', type: Type.String },
				{ id: Operation.NotBeginsWith, name: 'не начинается с', type: Type.String },
				{ id: Operation.EndsWith, name: 'оканчивается на', type: Type.String },
				{ id: Operation.NotEndsWith, name: 'не оканчивается на', type: Type.String },

				{ id: Operation.IsEmpty, name: 'пустая строка' },
				{ id: Operation.IsNotEmpty, name: 'непустая строка' }
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

			filters[Operation.Contains] = Z8.filter.String.contains;
			filters[Operation.NotContains] = Z8.filter.String.notContains;

			filters[Operation.Eq] = Z8.filter.String.eq;
			filters[Operation.NotEq] = Z8.filter.String.notEq;

			filters[Operation.GT] = Z8.filter.String.gt;
			filters[Operation.GE] = Z8.filter.String.ge;
			filters[Operation.LT] = Z8.filter.String.lt;
			filters[Operation.LE] = Z8.filter.String.le;
			filters[Operation.Eq] = Z8.filter.String.eq;

			filters[Operation.BeginsWith] = Z8.filter.String.beginsWidth;
			filters[Operation.NotBeginsWith] = Z8.filter.String.notBeginsWidth;
			filters[Operation.EndsWith] = Z8.filter.String.endsWidth;
			filters[Operation.NotEndsWith] = Z8.filter.String.notEndsWidth;

			filters[Operation.IsEmpty] = Z8.filter.String.isEmpty;
			filters[Operation.IsNotEmpty] = Z8.filter.String.isNotEmpty;

			return filters;
		},

		getNumberFilters: function() {
			var filters = {};

			filters[Operation.Eq] = Z8.filter.Number.eq;
			filters[Operation.NotEq] = Z8.filter.Number.notEq;

			filters[Operation.GT] = Z8.filter.Number.gt;
			filters[Operation.GE] = Z8.filter.Number.ge;
			filters[Operation.LT] = Z8.filter.Number.lt;
			filters[Operation.LE] = Z8.filter.Number.le;
			filters[Operation.Eq] = Z8.filter.Number.eq;

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