String.ZeroWidthChar = String.fromCharCode(8203);
String.SpacerChar = '&#160;';

String.isEmpty = function(value) {
	return value == null || value == '';
},

String.isString = function(value) {
	return typeof value == 'string';
};

String.htmlText = function(text) {
	return (text == null || text.length == 0) ? String.ZeroWidthChar : text;
};

String.padLeft = function(string, size, ch) {
	var result = String(string);
	ch = ch || ' ';
	while(result.length < size)
		result = ch + result;
	return result;
};

String.padRight = function(string, size, ch) {
	var result = String(string);
	ch = ch || ' ';
	while(result.length < size)
		result += ch;
	return result;
};

String.repeat = function(pattern, count) {
	var result = '';
	for(var i = 0; i < count; i++)
		result += pattern;
	return result;
};

String.prototype.isEmpty = function() {
	return this == '';
};

String.prototype.hash = function() {
	var hash = 5381, i = this.length;
	while(i)
		hash = (hash * 33) ^ this.charCodeAt(--i);
	return hash >>> 0;
};

String.prototype.startsWith = function(string) {
	var length = this.length;
	if(string == null || length < string.length)
		return false;

	return this.substr(0, string.length) == string;
};

String.prototype.endsWith = function(string) {
	var length = this.length;
	if(string == null || length < string.length)
		return false;

	return this.substr(length - string.length, string.length) == string;
};

String.prototype.firstCharToLower = function() {
	return this.length ? this.charAt(0).toLowerCase() + this.substring(1) : this;
};

String.prototype.ellipsis = function(length) {
	return this.length > length ? this.substring(0, length) + '...' : this;
};

String.prototype.lastAfter = function(searchValue) {
	var index = this.lastIndexOf(searchValue);
	return index >= 0 ? this.substring(searchValue.length) : this;
};

