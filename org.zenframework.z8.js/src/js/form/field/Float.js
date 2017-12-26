Z8.define('Z8.form.field.Float', {
	extend: 'Z8.form.field.Number',

	rawToValue: function(value) {
		return Parser.float(value);
	},

	valueToRaw: function(value) {
		return Format.float(value);
	}
});