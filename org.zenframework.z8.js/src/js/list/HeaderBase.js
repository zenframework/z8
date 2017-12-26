Z8.define('Z8.list.HeaderBase', {
	extend: 'Z8.Component',
	shortClassName: 'HeaderBase',

	statics: {
		Numeric: Ems.pixelsToEms(70),
		Date: Ems.pixelsToEms(70),
		Datetime: Ems.pixelsToEms(130),

		Icon: Ems.pixelsToEms(24),

		Min: Ems.pixelsToEms(110),
		Stretch: Ems.pixelsToEms(5000)
	},

	initComponent: function() {
		this.callParent();
	},

	getSort: function() {},
	setSort: function() {},
	getFilter: function() {},
	setFilter: function() {}
});
