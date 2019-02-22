Z8.define('Z8.filter.Button', {
	extend: 'Z8.button.Button',

	toggle: true,
	icon: 'fa-filter', 

	tooltip: 'Фильтрация',
	triggerTooltip: 'Настроить фильтрацию',

	filter: null,
	fields: null,

	htmlMarkup: function() {
		this.filterItems = [];

		this.on('toggle', this.onToggle, this);

		var items = [ new Z8.menu.Item({ text: 'Настроить', icon: 'fa-filter' }) ];
		var menu = this.menu = new Z8.menu.Menu({ items: items });
		menu.on('itemClick', this.onMenuItemClick, this);

		this.init();

		return this.callParent();
	},

	init: function() {
		var filter = this.filter;
		var current = filter.current;

		this.setText(filter.current);
		this.setToggled(filter.isActive(), true);

		var menu = this.menu;
		var filterItems = this.filterItems;
		menu.removeItems(filterItems);
		filterItems = this.filterItems = [];

		var names = filter.getNames();
		for(var i = 0, length = names.length; i < length; i++) {
			var name = names[i];
			var isCurrent = name == current;
			var item = new Z8.menu.Item({ text: name, filter: name, icon: isCurrent ? 'fa-check-square' : '' });
			if(isCurrent)
				this.currentItem = item;
			filterItems.add(item);
		}

		if(filterItems.length != 0)
			filterItems.add(new Z8.list.Divider());

		menu.addItems(filterItems, 0);
	},

	setFilter: function(filter) {
		this.filter = filter;
		this.init();
	},

	onMenuItemClick: function(menu, item) {
		var name = item.filter;

		if(name != null) {
			var filter = this.filter;
			filter.setCurrent(name);
			filter.setActive(true);

			if(this.currentItem != null)
				this.currentItem.setIcon('');

			item.setIcon('fa-check-square');
			this.currentItem = item;

			this.setText(name);
			this.setToggled(true, true);

			this.fireEvent('filter', this, filter, Filter.Apply);
		} else
			this.onSettings();
	},

	onSettings: function() {
		var callback = function(dialog, success) {
			if(success) {
				var filter = dialog.getFilter();
				this.setFilter(filter);
				this.fireEvent('filter', this, filter, Filter.Apply);
			} else
				this.fireEvent('filter', this, this.filter, Filter.NoAction);
		};

		new Z8.filter.Dialog({ header: 'Настройка фильтрации', icon: 'fa-filter', fields: this.fields, filter: this.filter, handler: callback, scope: this }).open();
	},

	onToggle: function(button, toggled) {
		var filter = this.filter;

		if(!toggled || !filter.isEmpty()) {
			filter.setActive(toggled);
			this.fireEvent('filter', this, filter, toggled ? Filter.Apply : Filter.Clear);
		} else {
			this.setToggled(false, true);
			this.onSettings();
		}
	}
});