Z8.define('Z8.data.Filter', {
	shortClassName: 'Filter',

	statics: {
		NoAction: 0,
		Apply: 1,
		Clear: 2
	},

	active: false,
	current: null,

	constructor: function(config) {
		this.callParent(config);
		this.filters = this.filters || {};
	},

	isActive: function() {
		return this.active && !this.isEmpty();
	},

	isEmpty: function() {
		return this.current == null;
	},

	setActive: function(active) {
		this.active = active;
	},

	getActive: function() {
		return this.active ? this.getCurrent() : null;
	},

	getNames: function() {
		return Object.keys(this.filters);
	},

	get: function(name) {
		return this.filters[name];
	},

	getCurrent: function() {
		return this.get(this.current);
	},

	setCurrent: function(current) {
		this.current = current;
	},

	add: function(name, filter) {
		this.filters[name] = filter;
	},

	remove: function(name) {
		delete this.filters[name];
		if(this.current == name)
			this.current = null;
	},

	clear: function() {
		this.filters = {};
		this.current = null;
	},

	toJson: function() {
		return { active: this.active, current: this.current, filters: this.filters };
	},

	toStoreData: function() {
		var records = [];

		var filters = this.filters;

		for(var name in filters)
			records.push({ name: name, filter: filters[name] });

		return records;
	}
});