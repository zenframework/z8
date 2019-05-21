Z8.define('Z8.list.Item', {
	extend: 'Z8.Component',

	/*
	* config:
	*     active: false, 
	*     icon: '',
	*     fields: [] | '',
	*     checked: false,
	*
	* private:
	*     list: false,
	*     icon: null,
	*     item: null,
	*     handlers: null
	*/

	constructor: function(config) {
		config = config || {};
		config.collapsed = config.collapsed !== false;
		config.hidden = 0;
		this.callParent(config);
	},

	initComponent: function() {
		this.callParent();

		var record = this.record; 

		if(record != null) {
			this.level = record.get('level');
			this.children = record.get('hasChildren');

			if(this.isTree())
				this.list.on('contentChange', this.updateCollapsedState, this);

			var icon = record.get(record.getIconProperty());
			this.icon = icon != null ? icon : this.icon;

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
		var fields = this.list.getFields();
		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			var name = field.name;
			if(modified.hasOwnProperty(name))
				this.setText(i, this.renderCellText(field, record.get(name)));
		}

		DOM.setCls(this, this.getCls());

		if(this.list.locks)
			DOM.swapCls(this.lockIcon, this.isReadOnly(), 'fa-lock', '');

		var icon = record.getIconProperty();
		if(icon != null && modified.hasOwnProperty(icon))
			this.setIcon(record.get(icon));
	},

	columnsMarkup: function() {
		var record = this.record;

		var columns = [];
		if(this.list.checks) {
			var check = { tag: 'i', cls: 'fa ' + (this.checked ? 'fa-check-square' : 'fa-square-o')};
			var text = { cls: 'text', cn: [check] };
			var cell = { cls: 'cell', cn: [text] };
			columns.push({ tag: 'td', cls: 'check column', cn: [cell] });
		}

		if(this.list.locks) {
			var lock = { tag: 'i', cls: 'fa ' + (this.isReadOnly() ? 'fa-lock' : '')};
			var text = { cls: 'text', cn: [lock] };
			var cell = { cls: 'cell', cn: [text] };
			columns.push({ tag: 'td', cls: 'lock column', cn: [cell] });
		}

		var icons = [];

		if(this.icon != null) {
			var iconCls = this.setIcon(this.icon);
			var icon = { tag: 'i', cls: iconCls.join(' '), html: String.htmlText() };
			icons.push(icon);
		}

		return columns.concat(this.fieldsMarkup(icons, record));
	},

	fieldsMarkup: function(icons, record) {
		var columns = [];

		var cls = 'column';

		if(record != null) {
			var fields = this.list.getFields();

			var isTree = this.isTree();
			var hasChildren = this.hasChildren();
			var collapsed = this.isCollapsed();

			if(isTree) {
				this.rotation = collapsed ? 90 : -90;

				var level = this.getLevel();
				var collapserIcon = { tag: 'i', cls: 'fa fa-chevron-' + (collapsed ? 'right' : 'down') + ' icon', html: String.htmlText() };
				var collapser = { tag: 'span', cls: 'collapser tree-level-' + level + (hasChildren ? '' : ' no-children'), cn: [collapserIcon] };
				icons.insert(collapser, 0);
			}

			var treeCls = this.isTree() ? 'tree' : '';

			for(var i = 0, length = fields.length; i < length; i++) {
				var field = fields[i];
				var title = null;
				var text = this.renderCellText(field, record.get(field.name));

				var type = field.type;

				if(String.isString(text))
					title = type != Type.Text ? Format.htmlEncode(text) : '';

				text = { tag: 'span', cls: this.getCellCls(field, record), cn: [text] };
				var cell = { cls: 'cell', cn: i == 0 ? icons.concat([text]) : [text] };
				columns.push({ tag: 'td', cls: cls + (type != null ? ' ' + type : '') + (i == 0 ? ' ' + treeCls : ''), field: i, cn: [cell], title: title });
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
			var cell = { cls: 'cell', cn: icons.concat([text]) };
			columns.push({ tag: 'td', cls: cls, cn: [cell], title: title });
		}

		return columns;
	},

	getCellCls: function(field, record) {
		return 'text' + (field.source != null ? ' ' + 'follow' : '');
	},

	renderCellText: function(field, value) {
		if(field.renderer != null) {
			value = String.htmlText(field.renderer.call(field, value));
			return String.htmlText(value);
		}

		var type = field.type;

		if(type == Type.Date)
			value = Format.date(value, field.format);
		else if(type == Type.Datetime)
			value = Format.datetime(value, field.format);
		else if(type == Type.Integer)
			value = Format.integer(value, field.format);
		else if(type == Type.Float)
			value = Format.float(value, field.format);
		else if(type == Type.Boolean)
			return { tag: 'i', cls: value ? 'fa fa-check-square' : 'fa fa-square-o' };
		else if(type == Type.File)
			value = value != Array.isArray(value) && value.length > 0 ? value[0].name : null;
		else if(type == Type.Text)
			value = Format.nl2br(value);

		return String.htmlText(value);
	},

	getCls: function() {
		var cls = ['item'];

		if(!this.enabled)
			cls.push(['disabled']);

		if(this.active)
			cls.pushIf('active');

		if(this.isHidden())
			cls.pushIf('display-none');

		return cls;
	},

	htmlMarkup: function() {
		return { tag: 'tr', id: this.getId(), cls: this.getCls().join(' '), tabIndex: this.getTabIndex(), cn: this.columnsMarkup() };
	},

	completeRender: function() {
		this.callParent();

		this.collapser = this.selectNode('.item .collapser');
		this.collapserIcon = this.selectNode('.item .collapser .icon');
		this.iconElement = this.selectNode('.item .icon');
		this.checkIcon = this.selectNode('.item>.column.check>.cell>.text>.fa');
		this.checkElement = this.selectNode('.item>.column.check');
		this.lockIcon = this.selectNode('.item>.column.lock>.cell>.text>.fa');
		this.cells = this.queryNodes('.item>.column:not(.check):not(.lock)');

		DOM.on(this, 'mouseDown', this.onMouseDown, this);
		DOM.on(this, 'click', this.onClick, this);
		DOM.on(this, 'dblClick', this.onDblClick, this);
		DOM.on(this, 'keyDown', this.onKeyDown, this);
	},

	onDestroy: function() {
		DOM.un(this, 'mouseDown', this.onMouseDown, this);
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'dblClick', this.onDblClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);

		if(this.record != null && this.record.un != null)
			this.record.un('change', this.onRecordChange, this);

		this.collapser = this.iconElement = this.checkIcon = this.checkElement = this.lockIcon = this.cells = null;

		this.callParent();
	},

	setTabIndex: function(tabIndex) {
		tabIndex = this.callParent(tabIndex);
		DOM.setTabIndex(this, tabIndex);
		return tabIndex;
	},

	setActive: function(active) {
		this.active = active;
		DOM.swapCls(this, active, 'active');
	},

	focus: function() {
		return this.enabled ? DOM.focus(this) : false;
	},

	toggleCheck: function() {
		var checked = !this.checked;
		this.setChecked(checked);
		this.list.onItemCheck(this, checked);
	},

	isChecked: function() {
		return this.checked;
	},

	setChecked: function(checked) {
		this.checked = checked;
		DOM.swapCls(this.checkIcon, checked, 'fa-check-square', 'fa-square-o');
	},

	setIcon: function(icon) {
		this.icon = icon;
		var cls = this.iconCls = DOM.parseCls(icon).pushIf('fa', 'fa-fw', 'icon');
		DOM.setCls(this.iconElement, this.iconCls);
		return cls;
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this, !enabled, 'disabled');
		this.callParent(enabled);
	},

	getValue: function() {
		var record = this.record;
		return record != null ? record.id : this.getId();
	},

	getText: function(fieldName) {
		var record = this.record;
		return this.renderCellText(this.list.getField(fieldName), record != null ? record.get(fieldName) : '');
	},

	isTree: function() {
		var record = this.record;
		return record != null && record.parentId != null;
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

	hide: function(hide) {
		if(this.hidden == 0 && hide)
			DOM.addCls(this, 'display-none');
		else if(this.hidden == 1 && !hide)
			DOM.removeCls(this, 'display-none');

		this.hidden = Math.max(this.hidden + (hide ? 1 : -1), 0);
	},

	updateCollapser: function() {
		var hasChildren = this.hasChildren();
		DOM.swapCls(this.collapser, !hasChildren, 'no-children');
	},

	collapse: function(collapsed) {
		if(this.collapsed != collapsed) {
			this.collapsed = collapsed;
			DOM.rotate(this.collapserIcon, this.rotation == 90 ? (collapsed ? 0 : 90) : (collapsed ? -90 : 0));
			this.list.onItemCollapse(this, collapsed);
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

		if(String.isString(text)) {
			DOM.setValue(textElement, String.htmlText(text));
			DOM.setAttribute(cell, 'title', text);
		} else {
			DOM.setInnerHTML(textElement, DOM.markup(text));
			DOM.setAttribute(cell, 'title', '');
		}
	},

	onMouseDown: function(event, target) {
		var dom = DOM.get(this);

		if(!this.isEnabled() || target == this.collapserIcon || target == this.collapser || this.list.checks && DOM.isParentOf(this.checkElement, target))
			return;

		var index = this.findCellIndex(target);

		var textClick = DOM.isParentOf(this.getTextElement(index), target);

		if(textClick && this.followLink(index)) {
			event.stopEvent();
			return;
		}

		var focused = DOM.selectNode(dom.parentNode, 'tr:focus') == dom;

		if(focused && this.startEdit(index))
			event.stopEvent();
	},

	onClick: function(event, target) {
		if(target.type == 'file')
			return;

		event.stopEvent();

		if(!this.isEnabled())
			return;

		if(target == this.collapser || target == this.collapserIcon)
			this.collapse(!this.collapsed);
		else if(this.list.checks && DOM.isParentOf(this.checkElement, target)) {
			this.toggleCheck();
			this.list.onItemClick(this, -1);
		} else {
			var index = this.findCellIndex(target);
			this.list.onItemClick(this, index);
		}
	},

	onDblClick: function(event, target) {
		event.stopEvent();

		if(!this.enabled)
			return;

		var index = this.findCellIndex(target);

		if(index != -1 || !DOM.isParentOf(this.list.getEditors()[index], target))
			this.list.onItemDblClick(this, index);
	},

	onKeyDown: function(event, target) {
		var list = this.list;
		if(list.isEditing())
			return;

		var key = event.getKey();

		if(key == Event.ENTER) {
			if(this.useENTER) {
				list.onItemClick(this, -1);
				event.stopEvent();
			}
		} else if(key == Event.SPACE) {
			if(list.checks) {
				event.stopEvent();
				this.toggleCheck();
			} else {
				list.onItemClick(this, -1);
				event.stopEvent();
			}
		}
	},

	followLink: function(index) {
		return index != -1 ? this.list.onItemFollowLink(this, index) : false;
	},

	startEdit: function(index) {
		if(!this.active || index == -1)
			return false;
		return this.list.onItemStartEdit(this, index);
	},

	getCell: function(index) {
		return this.cells[index];
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
