Z8.define('Z8.filter.Group', {
	extend: 'Z8.filter.Element',

	button: true,

	isGroup: true,
	logical: 'or',

	initComponent: function() {
		this.initExpression(this.expression);
		this.callParent();
	},

	getCls: function() {
		return Z8.filter.Element.prototype.getCls.call(this).pushIf('group');
	},

	initExpression: function(expression) {
		expression = this.expression = expression || { logical: this.logical, expressions: [] };
		this.logical = expression.logical;
	},

	getExpression: function() {
		var result = { logical: this.logical, expressions: [] };

		var lines = this.getLines();
		for(var i = 0, length = lines.length; i < length; i++) {
			expression = lines[i].getExpression();
			if(expression != null)
				result.expressions.push(expression);
		}

		return result.expressions.length != 0 ? result : null;
	},

	setExpression: function(expression) {
		this.removeAll();
		this.initExpression(expression);
		this.add(this.createItems());
	},

	createItems: function() {
		var items = [];

		var expression = this.expression;

		if(this.button) {
			var menuItems = [
				new Z8.menu.Item({ text: 'Новая строка', icon: 'fa-plus', handler: this.onNewLine }),
				new Z8.menu.Item({ text: 'Разгруппировать', icon: 'fa-unlink', handler: this.onUngroup }),
				new Z8.menu.Item({ text: 'Изменить и/или', icon: 'fa-exchange', handler: this.onChangeAndOr }),
				'-'
			];

			var deleteItem = this.deleteItem = new Z8.menu.Item({ text: 'Delete', icon: 'fa-trash', handler: this.onDeleteLine });
			menuItems.push(deleteItem);

			var menu = new Z8.menu.Menu({ items: menuItems });
			var menuItemCallback = function(menu, item) {
				item.handler.call(this);
			};
			menu.on('itemClick', menuItemCallback, this);

			var button = this.button = new Z8.button.Button({ icon: 'fa-square-o', text: this.getOperatorText(), toggled: false, menu: menu, vertical: true });
			button.on('toggle', this.onToggle, this);

			items.push(button);
		}

		var expressions = expression.expressions || [];
		for(var i = 0, length = expressions.length; i < length; i++) {
			var expression = expressions[i];
			var line = expression.logical != null ? new Z8.filter.Group({ expression: expression }) : new Z8.filter.Line({ expression: expression });
			items.push(line);
		}

		return items;
	},

	htmlMarkup: function() {
		this.items = this.createItems();
		return this.callParent();
	},

	getLines: function() {
		return this.button ? this.items.slice(1) : this.items;
	},

	getCount: function() {
		return this.items.length - this.button ? 1 : 0;
	},

	focus: function() {
		if(!this.isEnabled())
			return false;

		var lines = this.getLines();
		return lines.length != 0 ? lines[0].focus() : false;
	},

	getSelection: function() {
		var result = [];

		var lines = this.getLines();
		for(var i = 0, length = lines.length; i < length; i++) {
			var line = lines[i];
			if(line.isSelected())
				result.push(line);
		}

		return result;
	},

	isSelected: function() {
		return this.button ? this.button.toggled : false;
	},

	onToggle: function(button, toggled) {
		this.toggle(toggled);
	},

	toggle: function(toggle) {
		if(this.button) {
			this.button.setToggled(toggle, true);
			this.button.setIcon(toggle ? 'fa-check-square' : 'fa-square-o');
		}

		this.callParent(toggle);
	},

	onContainerToggle: function(toggle, container) {
		this.callParent(toggle, container);

		var lines = this.getLines();
		for(var i = 0, length = lines.length; i < length; i++)
			lines[i].onContainerToggle(toggle, container);
	},

	onChildToggle:  function(toggle, child) {
		this.callParent(toggle, child);
		this.deleteItem.setEnabled(this.getSelection().length != 0);
	},

	onNewLine: function() {
		this.newLine();
	},

	onUngroup: function() {
		this.ungroup();
	},

	onChangeAndOr: function() {
		this.changeAndOr();
	},

	onDeleteLine: function() {
		this.removeLine(this.getSelection());
	},

	newLine: function() {
		var line = new Z8.filter.Line();
		this.add(line);
		line.focus();

		this.onChildChange(this);
	},

	removeLine: function(lines) {
		var lines = Array.isArray(lines) ? lines : [lines];

		for(var i = 0, length = lines.length; i < length; i++)
			this.remove(lines[i]);

		lines = this.getLines();

		if(lines.length == 0) {
			var container = this.container;
			if(container instanceof Z8.filter.Element)
				container.removeLine(this);
		} else if(lines.length == 1 && this.button)
			this.ungroup();

		this.onChildChange(this);
	},

	group: function(lines) {
		var group = this.add(new Z8.filter.Group(), this.indexOf(lines[0]));
		group.add(lines);
		this.onChildChange(this);
	},

	ungroup: function() {
		var container = this.container;
		var index = container.indexOf(this);

		var lines = this.getLines();
		this.container.add(lines, index);

		this.container.remove(this);

		this.onChildChange(this);
	},

	changeAndOr: function() {
		this.logical = this.logical == 'or' ? 'and' : 'or';
		this.button.setText(this.getOperatorText());
		this.onChildChange(this);
	},

	getOperatorText: function() {
		return this.logical == 'or' ? 'или' : 'и';
	},

	getExpressionText: function() {
		var expressions = [];

		var lines = this.getLines();
		for(var i = 0, length = lines.length; i < length; i++) {
			var line = lines[i];
			var text = line.getExpressionText();
			if(text == null)
				continue;
			var parenthesis = line.isGroup && this.logical == 'and' && line.logical == 'or';
			expressions.push({ parenthesis: parenthesis, text: text});
		}

		if(expressions.length == 0)
			return null;

		if(expressions.length == 1)
			return expressions[0].text;

		var text = '';
		var operator = this.getOperatorText();
		for(var i = 0, length = expressions.length; i < length; i++) {
			var expression = expressions[i];
			expression = expression.parenthesis ? '(' + expression.text + ')' : expression.text;
			text += (text != '' ? ' ' + operator + ' ' : '') + expression;
		}

		return text;
	}
});
