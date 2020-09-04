Z8.define('Z8.form.field.Integer', {
	extend: 'NumberBox',
	shortClassName: 'IntegerBox',

	radix: 10,

	rawToValue: function(value) {
		return Parser.integer(value, this.radix);
	},

	valueToRaw: function(value) {
		return Format.integer(value, this.format);
	}
});