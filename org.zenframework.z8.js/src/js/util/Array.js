if(Array.prototype.fill == null) {
	Array.prototype.fill = function(value) {
		var values = [];
		for(var i = 0, length = this.length; i < length; i++)
			this[i] = value;
		return this;
	};
}

Array.appendCount = function(array, value, count) {
	return count != 0 ?  array.concat(Array(count).fill(value)) : array;
};

Array.prototype.equals = function(array, comparator) {
	if(array == null || array.length != this.length)
		return false;

	for(var i = 0, length = this.length; i < length; i++) {
		var left = this[i];
		var right = array[i];

		if(comparator != null) {
			if(!comparator(left, right))
				return false;
		} else if(left != right)
			return false;
	}
	return true;
};

Array.prototype.pushIf = function() {
	for(var i = 0, length = arguments.length; i < length; i++) {
		var element = arguments[i];
		if(this.indexOf(element) == -1)
			this.push(element);
	}
	return this;
};

Array.prototype.insert = function(element, index) {
	element = Array.isArray(element) ? element : [element];
	var args = [index != null ? index : this.length, 0].concat(element);
	this.splice.apply(this, args);
	return this;
};

Array.prototype.add = function(element) {
	return this.insert(element, null);
};

Array.prototype.append = function(element) {
	return this.insert(element, null);
};

Array.prototype.prepend = function(element) {
	return this.insert(element, 0);
};

Array.prototype.remove = function() {
	var removed = [];
	for(var i = 0, length = arguments.length; i < length; i++) {
		var element = arguments[i];
		var index = this.indexOf(element);
		if(this.indexOf(element) != -1) {
			removed.push(element);
			this.splice(index, 1);
		}
	}
	return removed;
};

Array.prototype.removeAll = function(array) {
	if(array == null)
		return [];

	var array = Array.isArray(array) ? array : [array];
	return this.remove.apply(this, array);
};

Array.prototype.removeAt = function(index) {
	return this.splice(index, 1);
};

Array.prototype.first = function() {
	var length = this.length;
	return length > 0 ? this[0] : null;
};

Array.prototype.last = function() {
	var length = this.length;
	return length > 0 ? this[length - 1] : null;
};

Array.prototype.contains = function(value) {
	return this.indexOf(value) != -1;
};