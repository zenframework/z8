Number.isNumber = function(value) {
	return typeof value == 'number';
};

Number.compare = function(value1, value2) {
	return value1 - value2;
};

Number.prototype.round = function(digits) {
	if(digits == null)
		return Math.round(this);
	var power = Math.pow(10, digits);
	return Math.round(this * power) / power;
};
