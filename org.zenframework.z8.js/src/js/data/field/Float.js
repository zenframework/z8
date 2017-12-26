Z8.define('Z8.data.field.Float', {
	extend: 'Z8.data.field.Field',

	type: Type.Integer,

	convert: function(value) {
		if(Number.isNumber(value))
			return value;

		if(value == null || value == '')
			return 0;

		value = parseFloat(value);
		return isNaN(value) ? 0 : value;
	}
});
