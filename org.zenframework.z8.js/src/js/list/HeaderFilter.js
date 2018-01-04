Z8.define('Z8.list.HeaderFilter', {
	extend: 'Z8.Component',

	/*
	* config:
	* field: {},
	*
	* private:
	*
	*/

	initComponent: function() {
		this.callParent();
	},

	subcomponents: function() {
		return [this.searchBox];
	},

	htmlMarkup: function() {
		var field = this.field || {};

		var searchBox = null;

		switch(field.type) {
		case Type.String:
		case Type.Text:
			searchBox = new Z8.form.field.Search({ field: field, placeholder: field.header });
			searchBox.on('search', this.onSearch, this);
			searchBox.on('focusIn', this.onFocusIn, this);
			searchBox.on('focusOut', this.onFocusOut, this);
			searchBox.on('keyDown', this.onKeyDown, this);
			break;
		}

		this.searchBox = searchBox;

		var cls = DOM.parseCls(this.cls).pushIf('column').join(' ');
		return { tag: 'td', id: this.getId(), cls: cls, tabIndex: this.getTabIndex(), cn: searchBox != null ? [searchBox.htmlMarkup()] : [] };
	},

	focus: function() {
		return this.searchBox != null ? this.searchBox.focus() : false;
	},

	reset: function() {
		var searchBox = this.searchBox;
		if(searchBox != null)
			searchBox.reset();
	},

	onSearch: function(control, value) {
		this.list.onHeaderFilter(this, value);
	},

	getFilter: function() {
		var searchBox = this.searchBox;
		return searchBox == null ? null : searchBox.getFilter();
	},

	onFocusIn: function(search) {
		this.fireEvent('focusIn', this);
	},

	onFocusOut: function(search) {
		this.fireEvent('focusOut', this);
	},

	onKeyDown: function(search, event, target) {
		var key = event.getKey();

		if(key == Event.DOWN) {
			this.list.focus();
			event.stopEvent();
		}
	}
});
