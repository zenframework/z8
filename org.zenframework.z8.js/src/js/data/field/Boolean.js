Z8.define('Z8.data.field.Boolean', {
	extend: 'Z8.data.field.Field',

	type: Type.Boolean,

	convert: function(value) {
		return typeof value != 'boolean' ? value == 'true' : value;
	}
});
