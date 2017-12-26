Z8.define('Z8.util.Parser', {
	shortClassName: 'Parser',

	statics: {
		datetime: function(value, format) {
			if(Z8.isEmpty(value))
				return null;

			if(String.isString(value))
				value = value.replace(/^(\d\d?)([\.\-//])(\d\d?)([\.\-//])/, '$3$2$1$4');

			value = new Date(value);
			return isNaN(value) ? null : value;
		},

		date: function(value, format) {
			return Z8.util.Parser.datetime(value, format);
		},

		integer: function(value, radix) {
			if(Number.isNumber(value))
				return value.round();

			var value = parseInt(value.replace(/\s/g, ''), radix);
			return isNaN(value) ? null : value;
		},

		float: function(value) {
			if(Number.isNumber(value))
				return value;

			var value = parseFloat(value.replace(/,/g, '.').replace(/\s/g, ''));
			return isNaN(value) ? null : value;
		},

		boolean: function(value) {
			return String.isString(value) ? value === 'true' : value;
		}
	}
});