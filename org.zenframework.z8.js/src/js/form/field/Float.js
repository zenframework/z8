Z8.define('Z8.form.field.Float', {
	extend: 'NumberBox',
	shortClassName: 'FloatBox',

	rawToValue: function(value) {
		return Parser.float(value);
	},

	valueToRaw: function(value) {
		return Format.float(value, this.format);
	}
});