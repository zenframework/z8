Z8.define('Z8.data.Period', {
	shortClassName: 'Period',

	statics: {
		NoAction: 0,
		Apply: 1,
		Clear: 2
	},

	constructor: function(config) {
		this.callParent(config);
		this.start = Parser.date(this.start);
		this.finish = Parser.date(this.finish);
	},

	isActive: function() {
		return this.active;
	},

	setActive: function(active) {
		this.active = active;
	},

	getStart: function() {
		return this.active ? this.start : null;
	},

	setStart: function(start) {
		this.start = Parser.date(start);
	},

	getFinish: function() {
		return this.active ? this.finish : null;
	},

	setFinish: function(finish) {
		this.finish = Parser.date(finish);
	},

	getActive: function(property) {
		return this.active ? this.get() : null;
	},

	get: function(property) {
		var filter = [];
		if(this.start != null)
			filter.push({ property: property, operator: Operator.GE, value: this.start });
		if(this.finish != null)
			filter.push({ property: property, operator: Operator.LE, value: this.finish });
		return filter;
	},

	getText: function() {
		var start = this.start;
		var finish = this.finish;

		if(start == null && finish == null)
			return null;

		if(start != null && finish != null) {
			if(Date.isEqualDate(start, finish))
				return Format.date(start, Format.Date);

			var startFormat = Format.Date;

			if(start.getYear() == finish.getYear())
				startFormat = start.getMonth() == finish.getMonth() ? 'd' : 'd.m';

			return Format.date(start, startFormat) + ' - ' + Format.date(finish, Format.Date);
		}

		return (start != null ? Z8.$('Period.after') : Z8.$('Period.before')) + ' ' + Format.date(start || finish, Format.Date);
	},

	toJson: function() {
		return { active: this.active, start: this.start, finish: this.finish };
	}
});
