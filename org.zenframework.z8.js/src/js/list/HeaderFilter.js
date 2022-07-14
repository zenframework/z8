Z8.define('Z8.list.HeaderFilter', {
	extend: 'Z8.Component',

	/*
	* config:
	* field: {},
	*
	* private:
	*
	*/

	getCls: function() {
		return Z8.Component.prototype.getCls.call(this).pushIf('column');
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
			searchBox = new Z8.form.field.SearchText({ field: field, placeholder: field.header, confirmSearch: this.confirmSearch, searchIcon: this.searchIcon, clearIcon: this.clearIcon, list: this.list });
			searchBox.on('search', this.onSearch, this);
			searchBox.on('cancel', this.onCancel, this);
			searchBox.on('focusIn', this.onFocusIn, this);
			searchBox.on('focusOut', this.onFocusOut, this);
			searchBox.on('keyDown', this.onKeyDown, this);
			break;
		case Type.Date:
			searchBox = new Z8.form.field.SearchDateBox({ field: field, placeholder: field.header, confirmSearch: this.confirmSearch, searchIcon: this.searchIcon, clearIcon: this.clearIcon, list: this.list, format: Format.Date });
			searchBox.on('search', this.onSearch, this);
			searchBox.on('cancel', this.onCancel, this);
			searchBox.on('focusIn', this.onFocusIn, this);
			searchBox.on('focusOut', this.onFocusOut, this);
			searchBox.on('keyDown', this.onKeyDown, this);
			break;
		case Type.Datetime:
			searchBox = new Z8.form.field.SearchDateBox({ field: field, placeholder: field.header, confirmSearch: this.confirmSearch, searchIcon: this.searchIcon, clearIcon: this.clearIcon, list: this.list, format: Format.Datetime });
			searchBox.on('search', this.onSearch, this);
			searchBox.on('cancel', this.onCancel, this);
			searchBox.on('focusIn', this.onFocusIn, this);
			searchBox.on('focusOut', this.onFocusOut, this);
			searchBox.on('keyDown', this.onKeyDown, this);
			break;
		}

		this.searchBox = searchBox;

		return { tag: 'td', id: this.getId(), cls: this.getCls().join(' '), tabIndex: this.getTabIndex(), cn: searchBox != null ? [searchBox.htmlMarkup()] : [] };
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

	onCancel: function(control) {
		this.list.onHeaderFilterCancel(this);
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
