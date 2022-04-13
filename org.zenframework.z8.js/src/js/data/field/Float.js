Z8.define('Z8.data.field.Float', {
	extend: 'Z8.data.field.Field',

	type: Type.Float,

	convert: function(value) {
		if(Number.isNumber(value))
			return value;

		if(String.isEmpty(value))
			return 0;

		value = parseFloat(value);
		return isNaN(value) ? 0 : value;
	}
});
