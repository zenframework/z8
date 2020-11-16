Z8.define('Z8.form.field.Number', {
	extend: 'TextBox',
	shortClassName: 'NumberBox',

	increment: 1,

	getInputCls: function() {
		return this.callParent().pushIf('number');
	},

	rawToValue: function(value) {
		return Parser.float(value);
	},

	valueToRaw: function(value) {
		return Format.number(value, this.format || Format.Float);
	},

	isEmptyValue: function(value) {
		return value == null;
	},

	onKeyEvent: function(event, target) {
		var key = event.getKey();

		var increment = 0;

		if(key == Event.UP && event.ctrlKey || key == Event.PAGE_UP)
			increment = 10 * this.increment;
		else if(key == Event.UP && event.shiftKey)
			increment = 100 * this.increment;
		else if(key == Event.DOWN && event.ctrlKey || key == Event.PAGE_DOWN)
			increment = -10 * this.increment;
		else if(key == Event.DOWN && event.shiftKey)
			increment = -100 * this.increment;
		else if(key == Event.UP)
			increment = this.increment;
		else if(key == Event.DOWN)
			increment = -this.increment;

		if(increment != 0) {
			this.setValue((this.getValue() || 0) + increment);
			return true;
		}

		return Event.A <= key && key <= Event.Z && !event.ctrlKey && !event.altKey && !event.metaKey;
	}
});