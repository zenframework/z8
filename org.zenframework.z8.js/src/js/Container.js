Z8.define('Z8.Container', {
	extend: 'Component',
	shortClassName: 'Container',

	isContainer: true,

	constructor: function(config) {
		Component.prototype.constructor.call(this, config);

		this.setItems(this.items);
	},

	getItem: function(index) {
		return this.items[index];
	},

	getItems: function() {
		return this.items;
	},

	setItems: function(items) {
		this.items = items != null ? Array.asArray(items) : [];

		for(var item of this.items)
			item.container = this;
	},
/*
	setItems: function(items) {
		this.removeAll();
		this.add(items);
	},
*/
	subcomponents: function() {
		return this.getItems();
	},

	htmlMarkup: function() {
		var markup = [];

		for(var item of this.items) {
			item.container = this;
			markup.push(item.htmlMarkup != null ? item.htmlMarkup() : item);
		}

		return { id: this.getId(), cls: this.getCls().join(' '), tabIndex: this.getTabIndex(), cn: markup };
	},

	onDestroy: function() {
		Component.destroy(this.items);
		Component.prototype.onDestroy.call(this);
	},

	getCount: function() {
		return this.items.length;
	},

	indexOf: function(item) {
		return this.items.indexOf(item);
	},

	add: function(component, index) {
		var items = this.items;

		for(var component of Array.asArray(component)) {
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

		return component;
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
		for(var item of this.items) {
			if(item.focus != null && item.focus())
				return true;
		}
		return Component.prototype.focus.call(this);
	},

	setEnabled: function(enabled) {
		Component.prototype.setEnabled.call(this, enabled);

		for(var item of this.items) {
			if(item.isComponent && !item.enabledLock)
				item.setEnabled(enabled);
		}
	},

	setActive: function(active) {
		if(this.isActive() == active)
			return;

		Component.prototype.setActive.call(this, active);

		for(var item of this.items) {
			if(item.setActive != null)
				item.setActive(active);
		}
	}
});