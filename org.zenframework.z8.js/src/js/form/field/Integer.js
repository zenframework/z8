Z8.define('Z8.form.field.Integer', {
	extend: 'Z8.form.field.Number',

	radix: 10,
	only_positive: false,

	rawToValue: function(value) {
		return Parser.integer(value, this.radix, this.only_positive);
	},

	valueToRaw: function(value) {
		return Format.integer(value, this.format);
	},

	onInput: function(event, target) {
		var rawValue = this.getRawValue();
		var value = this.rawToValue(rawValue);
		this.mixins.field.setValue.call(this, value);
		if(this.only_positive)
			this.setRawValue(value);
	}
});