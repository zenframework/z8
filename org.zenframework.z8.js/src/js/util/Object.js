if(Object.values == null) {
	Object.values = function(object) {
		var values = [];
		for(var key in object)
			values.push(object[key]);
		return values;
	}
}
