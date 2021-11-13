Z8.define('Z8.data.field.Boolean', {
	extend: 'Z8.data.field.Field',

	type: Type.Boolean,

	compare: function(left, right) {
		if((left ^ right) == 0)
			return 0;

		return left ? 1 : -1;
	},

	convert: function(value) {
		return typeof value != 'boolean' ? value == 'true' : value;
	}
});
