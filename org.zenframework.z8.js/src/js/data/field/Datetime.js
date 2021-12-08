Z8.define('Z8.data.field.Datetime', {
	extend: 'Z8.data.field.Field',

	type: Type.Datetime,
	format: Format.Datetime,

	compare: function(left, right) {
		if(Date.isDate(left) && Date.isDate(right))
			return left.getTime() - right.getTime();
		return left == right ? 0 : (left == null ? -1 : 1);
	},

	convert: function(value) {
		if(Date.isDate(value))
			return value;
		return Z8.isEmpty(value) ? null : new Date(value);
	},

	serialize: function(value) {
		return Date.isDate(value) ? value.toISOString() : '';
	}
});
