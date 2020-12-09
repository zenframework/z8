Z8.define('Z8.list.Item', {
	extend: 'Component',
	shortClassName: 'ListItem',

	constructor: function(config) {
		config = config || {};
		config.collapsed = config.collapsed !== false;
		Component.prototype.constructor.call(this, config);

		this.hidden = 0;
		this.valid = true;
		this.follow = true;
	},

	getRecord: function() {
		return this.record;
	},

	getList: function() {
		return this.list;
	},

	getIndex: function() {
		return this.list.getIndex(this);
	},

	initComponent: function() {
		Component.prototype.initComponent.call(this);

		var record = this.record; 

		if(record != null) {
			this.level = record.get('level');
			this.children = record.get('hasChildren');

			if(this.isTree())
				this.getList().on('contentChange', this.updateCollapsedState, this);

			if(record.on != null)
				record.on('change', this.onRecordChange, this);
		}
	},

	updateCollapsedState: function(list) {
		list.un('contentChange', this.updateCollapsedState, this);

		var parent = list.getParent(this);

		if(parent != null)
			this.hidden = parent.hidden + parent.isCollapsed();
	},

	isReadOnly: function() {
		var record = this.record;
		return record != null ? record.getLock() != RecordLock.None : false;
	},

	onRecordChange: function(record, modified) {
		for(var i = 0, fields = this.getFields(), length = fields.length; i < length; i++) {
			var field = fields[i];
			if(modified.hasOwnProperty(field.name))
				this.updateText(i, field);
		}

		DOM.setCls(this, this.getCls());

		if(this.getList().locks)
			DOM.swapCls(this.lockIcon, this.isReadOnly(), 'fa-lock', '');

		var icon = record.getIconProperty();
		if(icon != null && modified.hasOwnProperty(icon))
			this.updateIcon();
	},

	columnsMarkup: function() {
		var columns = [], icons = [];
		var list = this.getList();

		if(list.checks) {
			var check = { tag: 'i', cls: this.getCheckBoxCls(this.checked).join(' ') };
			var text = { cls: 'text', cn: [check] };
			var cell = { cls: 'cell', cn: [text] };
			columns.push({ tag: 'td', cls: 'check column', cn: [cell] });
		}

		if(list.locks) {
			var lock = { tag: 'i', cls: 'fa ' + (this.isReadOnly() ? 'fa-lock' : '')};
			var text = { cls: 'text', cn: [lock] };
			var cell = { cls: 'cell', cn: [text] };
			columns.push({ tag: 'td', cls: 'lock column', cn: [cell] });
		}

		if(this.isTree()) {
			var collapsed = this.isCollapsed();
			this.rotation = collapsed ? 90 : -90;

			var collapserIcon = { tag: 'i', cls: 'fa fa-caret-' + (collapsed ? 'right' : 'down') + ' icon', html: String.htmlText() };
			var collapser = { tag: 'span', cls: 'collapser', cn: [collapserIcon] };
			icons.push(collapser);
		}

		var record = this.record;

		var iconCls = this.getIconCls();
		if(iconCls != null)
			icons.push({ tag: 'i', cls: iconCls.join(' '), html: String.htmlText() });

		if(record != null) {
			var fields = list.getFields();

			for(var i = 0, length = fields.length; i < length; i++) {
				var field = fields[i];
				var text = this.renderText(field, record.get(field.name));


				text = this.textMarkup(text, DOM.parseCls(this.getTextCls(field, record)).pushIf('text').join(' '), field);
				var cell = { cls: this.getCellCls(field, record).join(' '), cn: i == 0 ? icons.add(text) : text };

				var type = field.type;
				columns.push({ tag: 'td', cls: 'column' + (type != null ? ' ' + type : '') + (i == 0 && !this.checks && !this.locks ? ' first' : (i == length - 1 ? ' last' : '')), field: i, cn: [cell] });
			}
		} else {
			var text = String.htmlText(this.text);
			var title = this.text || '';

			var shortcut = this.shortcut;
			if(shortcut != null) {
				title = text + ' (' + shortcut + ')';
				shortcut = { tag: 'span', cls: 'shortcut', html: [shortcut] };
			}

			text = { tag: 'div', cls: 'text' + (shortcut != null ? ' shortcuts' : ''), cn: shortcut != null ? [text, shortcut] : [text] };
			var cell = { cls: this.getCellCls().join(' '), cn: icons.concat([text]) };
			columns.push({ tag: 'td', cls: 'column', cn: [cell], title: title });
		}

		return columns;
	},

	getTextCls: function(field, record) {
		return field.source != null && this.follow ? ' follow' : '';
	},

	getCheckBoxCls: function(value) {
		return DOM.parseCls(value ? CheckBox.OnIconCls : CheckBox.OffIconCls).pushIf(value ? 'on' : 'off').pushIf('icon');
	},

	textMarkup: function(text, cls, field) {
		var markup = { tag: 'span', cls: cls, cn: [text] };
		if(String.isString(text))
			markup.title = Format.htmlEncode(Format.br2nl(text));
		return markup;
	},

	getCellCls: function(field, record) {
		return DOM.parseCls(this.cellCls).pushIf('cell');
	},

	renderText: function(field, value) {
		var renderer = field.renderer;
		if(renderer != null)
			return String.htmlText(renderer.call(field, value));

		return this.format(field.type, value, field.format);
	},

	format: function(type, value, format) {
		if(type == Type.Date)
			value = Format.date(value, format);
		else if(type == Type.Datetime)
			value = Format.datetime(value, format);
		else if(type == Type.Integer)
			value = Format.integer(value, format);
		else if(type == Type.Float)
			value = Format.float(value, format);
		else if(type == Type.Boolean)
			return { tag: 'i', cls: this.getCheckBoxCls(value).join(' ') };
		else if(type == Type.File)
			value = value != null && Array.isArray(value) && value.length > 0 ? value[0].name : null;
		else if(type == Type.Text)
			value = Format.nl2br(value);

		return String.htmlText(value);
	},

	formatField: function(field) {
		field = this.getField(field);
		return this.format(field.type, this.record.get(field.name), field.format);
	},

	getCls: function() {
		var cls = Component.prototype.getCls.call(this);

		cls.pushIf('item');

		if(!this.enabled)
			cls.pushIf('disabled');

		if(this.active)
			cls.pushIf('active');

		if(this.isHidden())
			cls.pushIf('display-none');

		if(!this.valid)
			cls.push('invalid');

		if(this.isTree()) {
			cls.pushIf('level-' + this.getLevel());
			if(!this.hasChildren())
				cls.pushIf('no-children');
		}

		return cls;
	},

	htmlMarkup: function() {
		return { tag: 'tr', id: this.getId(), cls: this.getCls().join(' '), cn: this.columnsMarkup() };
	},

	completeRender: function() {
		Z8.Component.prototype.completeRender.call(this);

		this.collapser = this.selectNode('.item>.column>.cell>.collapser');
		this.collapserIcon = this.selectNode('.item>.column>.cell>.collapser>.icon');
		this.iconElement = this.selectNode('.item>.column>.cell>.icon');
		this.checkIcon = this.selectNode('.item>.column.check>.cell>.text>.icon');
		this.checkElement = this.selectNode('.item>.column.check');
		this.lockIcon = this.selectNode('.item>.column.lock>.cell>.text>.icon');
		this.cells = this.queryNodes('.item>.column:not(.check):not(.lock)');

		DOM.on(this, 'mouseDown', this.onMouseDown, this);
		DOM.on(this, 'click', this.onClick, this);
		DOM.on(this, 'contextMenu', this.onContextMenu, this);
		DOM.on(this, 'dblClick', this.onDblClick, this);

		this.dom.listItem = this;
	},

	onDestroy: function() {
		if(this.dom != null)
			this.dom.listItem = null;

		DOM.un(this, 'mouseDown', this.onMouseDown, this);
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'contextMenu', this.onContextMenu, this);
		DOM.un(this, 'dblClick', this.onDblClick, this);

		if(this.record != null && this.record.un != null)
			this.record.un('change', this.onRecordChange, this);

		this.collapser = this.iconElement = this.checkIcon = this.checkElement = this.lockIcon = this.cells = null;

		Component.prototype.onDestroy.call(this);
	},

	getValid: function(valid) {
		return this.valid;
	},

	setValid: function(valid) {
		this.valid = valid;
		DOM.swapCls(this, !valid, 'invalid');
	},

	setActive: function(active) {
		this.active = active;
		DOM.swapCls(this, active, 'active');
	},

	toggleCheck: function() {
		var checked = !this.checked;
		this.setChecked(checked);
		this.getList().onItemCheck(this, checked);
	},

	isChecked: function() {
		return this.checked;
	},

	setChecked: function(checked) {
		this.checked = checked;
		DOM.setCls(this.checkIcon, this.getCheckBoxCls(checked));
	},

	getIcon: function() {
		var record = this.record;
		return (record != null ? record.get(record.getIconProperty()) : null) || this.icon;
	},

	getIconElement: function(index) {
		return this.iconElement;
	},

	setIcon: function(icon) {
		this.icon = icon;
		this.updateIcon();
	},

	updateIcon: function() {
		DOM.setCls(this.iconElement, this.getIconCls());
	},

	getIconCls: function() {
		var icon = this.getIcon();
		return icon != null ? DOM.parseCls(icon).add(['fa', 'fa-fw', 'icon']) : null;
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this, !enabled, 'disabled');
		Component.prototype.setEnabled.call(this, enabled);
	},

	getFields: function() {
		return this.getList().getFields();
	},

	getFieldByIndex: function(index) {
		return this.getList().getFields()[index];
	},

	getField: function(name) {
		if(name != null && (name.isField || typeof name == 'Object'))
			return name;
		return String.isString(name) ? this.getList().getField(name) || this.record.getField(name) : this.getFieldByIndex(name);
	},

	getValue: function() {
		return this.record != null ? this.record.id : this.getId();
	},

	getText: function(fieldName) {
		if(this.record != null) {
			var field = this.getField(fieldName);
			return field != null ? this.renderText(field, this.record.get(fieldName)) : '';
		} else
			return this.text;
	},

	isTree: function() {
		return this.record != null && this.record.parentId != null;
	},

	hasChildren: function() {
		return this.children;
	},

	setHasChildren: function(hasChildren) {
		if(this.isTree()) {
			this.children = hasChildren;
			this.updateCollapser();
		}
	},

	isRoot: function() {
		return this.level == 0;
	},

	isCollapsed: function() {
		return this.collapsed === true;
	},

	isExpanded: function() {
		return this.children && !this.collapsed;
	},

	isHidden: function() {
		return this.hidden != 0;
	},

	getLevel: function() {
		return this.level;
	},

	setHidden: function(hide) {
		if(this.hidden == 0 && hide)
			Component.prototype.hide.call(this);
		else if(this.hidden == 1 && !hide)
			Component.prototype.show.call(this);

		this.hidden = Math.max(this.hidden + (hide ? 1 : -1), 0);
	},

	updateCollapser: function() {
		var hasChildren = this.hasChildren();
		DOM.swapCls(this, !hasChildren, 'no-children');
	},

	collapse: function(collapsed) {
		if(this.collapsed != collapsed) {
			this.collapsed = collapsed;
			DOM.rotate(this.collapserIcon, this.rotation == 90 ? (collapsed ? 0 : 90) : (collapsed ? -90 : 0));
			this.getList().onItemCollapse(this, collapsed);
		}
	},

	getTextElement: function(index) {
		var cell = Number.isNumber(index) ? this.cells[index] : index;
		return cell != null ? cell.firstChild.lastChild : null;
	},

	setText: function(index, text) {
		var cell = this.cells[index];
		var textElement = this.getTextElement(cell);

		if(textElement == null)
			return;

		var isString = String.isString(text);
		DOM.setInnerHtml(textElement, isString ? String.htmlText(text) : DOM.markup(text));
		DOM.setAttribute(cell, 'title', isString ? Format.htmlEncode(Format.br2nl(text)) : '');
	},

	updateText: function(index, field) {
		var field = field || this.getFieldByIndex(index);
		this.setText(index, this.renderText(field, this.record.get(field.name)));
	},

	ensureVisible: function() {
		var list = this.getList()
		if(list != null)
			list.ensureVisible(this);
	},

	isItemClick: function(target) {
		return target != this.collapserIcon && target != this.collapser && target != this.iconElement && (!this.getList().checks || !DOM.isParentOf(this.checkElement, target));
	},

	onMouseDown: function(event, target) {
		var dom = DOM.get(this);

		if(!this.isEnabled())
			return;

		if(!this.isItemClick(target))
			return;

		var index = this.findCellIndex(target);

		var textClick = DOM.isParentOf(this.getTextElement(index), target);

		if(textClick && this.follow && this.followLink(index))
			return event.stopEvent();

		var list = this.getList();

		if(list.getFocused() && !list.isEditing() && this.canStartEdit(target) && this.startEdit(index, 300))
			event.stopEvent();
	},

	onClick: function(event, target) {
		if(target.type == 'file')
			return;

		event.stopEvent();

		if(!this.isEnabled())
			return;

		if(target == this.collapser || target == this.collapserIcon)
			return this.collapse(!this.collapsed);

		var list = this.getList();
	
		if(target == this.iconElement)
			return list.onIconClick(this);

		var index = -1;

		if(list.checks && DOM.isParentOf(this.checkElement, target))
			return this.toggleCheck();

		index = this.findCellIndex(target);
		list.onItemClick(this, index);
	},

	onContextMenu: function(event, target) {
		if(!this.isEnabled())
			return;

		var index = this.findCellIndex(target);
		this.getList().onItemContextMenu(this, index);
	},

	onDblClick: function(event, target) {
		event.stopEvent();

		if(!this.enabled)
			return;

		var index = this.findCellIndex(target);
		var list = this.getList();

		if(index != -1 && DOM.isParentOf(list.getEditors()[index], target) || list.checks && DOM.isParentOf(this.checkElement, target))
			return;;

		list.onItemDblClick(this, index);
	},

	followLink: function(index) {
		return index != -1 ? this.getList().onItemFollowLink(this, index) : false;
	},

	canStartEdit: function(target) {
		return true;
	},

	startEdit: function(index, delay) {
		var list = this.getList();

		if(!this.active || index == -1 || !list.canStartEdit(this, index || 0))
			return false;

		list.startEdit(this, index || 0, delay);
		return true;
	},

	getCell: function(index) {
		return this.cells[index];
	},

	getFirstCell: function() {
		return this.cells[0];
	},

	getLastCell: function() {
		var cells = this.cells;
		return cells[cells.length - 1];
	},

	findCell: function(target) {
		var index = this.findCellIndex(target);
		return index != -1 ? this.cells[index] : null;
	},

	findCellIndex: function(target) {
		var cells = this.cells;

		for(var i = 0, length = cells.length; i < length; i++) {
			var cell = cells[i];
			if(DOM.isParentOf(cell, target))
				return i;
		}

		return -1;
	}
});
