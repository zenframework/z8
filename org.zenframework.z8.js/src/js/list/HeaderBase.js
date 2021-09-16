Z8.define('Z8.list.HeaderBase', {
	extend: 'Component',
	shortClassName: 'HeaderBase',

	statics: {
		Numeric: 6.42857143,     // 90px
		Date: 5.83333333,        // 70px
		Datetime: 10.83333333,   // 130px

		Icon: 2,                 // 24px

		Min: 9.16666667,         // 110
		Stretch: 416.66666667    // 5000px
	},

	initComponent: function() {
		Component.prototype.initComponent.call(this);
	},

	getName: function() {},

	getSortProperty: function() {
		return null;
	},

	isSortable: function() {
		return false;
	},

	isSortableWith: function(sorter) {
		return false;
	},

	getSort: function() {},
	setSort: function() {},

	getFilter: function() {},
	setFilter: function() {}
});
