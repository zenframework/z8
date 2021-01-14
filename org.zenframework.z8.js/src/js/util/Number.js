Number.isNumber = function(value) {
	return typeof value == 'number';
};

Number.compare = function(value1, value2) {
	return value1 - value2;
};

Number.prototype.round = function(digits) {
	if(digits == null || digits == 0)
		return Math.round(this);
	var power = Math.pow(10, digits);
	return Math.round(this * power) / power;
};

Number.prototype.ceil = function() {
	return Math.ceil(this);
};

Number.prototype.floor = function() {
	return Math.floor(this);
};

