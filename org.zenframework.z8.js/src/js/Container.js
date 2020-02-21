Z8.define('Z8.Container', {
	extend: 'Z8.Component',

	isContainer: true,

	constructor: function(config) {
		config = config || {};
		var items = config.items = config.items || [];

		for(var i = 0, length = items.length; i < length; i++)
			items[i].container = this;

		this.callParent(config);
	},

	getItems: function() {
		return this.items;
	},

	subcomponents: function() {
		return this.getItems();
	},

	htmlMarkup: function() {
		var markup = [];

		var items = this.items;
		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			item.container = this;
			markup.push(item.htmlMarkup != null ? item.htmlMarkup() : item);
		}

		return { id: this.getId(), cls: this.getCls().join(' '), tabIndex: this.getTabIndex(), cn: markup };
	},

	onDestroy: function() {
		Component.destroy(this.items);
		this.callParent();
	},

	getCount: function() {
		return this.items.length;
	},

	indexOf: function(item) {
		return this.items.indexOf(item);
	},

	add: function(component, index) {
		var items = this.items;
		var result = component;

		var components = Array.isArray(component) ? component : [component];

		for(var i = 0, length = components.length; i < length; i++) {
			component = components[i];

			if(this.indexOf(component) != -1)
				continue;

			var container = component.container;

			if(container != null && container != this)
				container.items.remove(component);

			component.container = this;

			var before = index != null ? items[index] : null;

			items.insert(component, index);

			if(index != null)
				index++;

			if(this.getDom() == null)
				continue;

			var dom = component.dom;

			if(dom == null) {
				var markup = component.isComponent != null ? component.htmlMarkup() : component;
				component.dom = before != null ? DOM.insertBefore(before, markup) : DOM.append(this, markup);
				if(component.isComponent)
					component.renderDone();
			} else
				before != null ? DOM.insertBefore(before, dom) : DOM.append(this, dom);
		}

		return result;
	},

	remove: function(component) {
		var items = this.items;

		if(Array.isArray(component))
			component = items.removeAll(component);
		else if(Number.isNumber(component))
			component = items.removeAt(component);
		else
			component = items.remove(component);

		Component.destroy(component);
	},

	removeAll: function() {
		Component.destroy(this.items);
		this.items = [];
	},

	focus: function() {
		var items = this.items;
		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(item.focus != null && item.focus())
				return true;
		}
		return this.callParent();
	},

	setEnabled: function(enabled) {
		this.callParent(enabled);

		var items = this.items;
		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(item.isComponent && !item.enabledLock)
				item.setEnabled(enabled);
		}
	},

	setActive: function(active) {
		if(this.isActive() == active)
			return;

		this.callParent(active);

		var items = this.items;
		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(item.setActive != null)
				item.setActive(active);
		}
	}
});