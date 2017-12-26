Z8.define('Z8.form.field.Integer', {
	extend: 'Z8.form.field.Number',

	radix: 10,

	rawToValue: function(value) {
		return Parser.integer(value, this.radix);
	},

	valueToRaw: function(value) {
		return Format.integer(value);
	}
});