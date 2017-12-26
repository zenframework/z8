Z8.define('Z8.data.field.Json', {
	extend: 'Z8.data.field.String',

	convert: function(value) {
		if(!String.isString(value))
			return value;
		try {
			return value != '' ? JSON.decode(value) : null;
		} catch(e) {
			return null;
		}
	},

	serialize: function(value) {
		return value != null ? JSON.encode(value) : null;
	},

	compare: function(left, right) {
		left = String.isString(left) ? left : JSON.encode(left);
		right = String.isString(right) ? right : JSON.encode(right);
		return left == right ? 0 : -1;
	}
});
