Z8.define('Z8.list.Header', {
	extend: 'Z8.list.HeaderBase',

	/*
	* config:
	* icon: '',
	* field: {},
	*
	*/

	initComponent: function() {
		this.callParent();

		var field = this.field;
		this.text = field.header;
		this.icon = field.icon;
		this.width = field != null ? field.width : null;
		this.sortDirection = field.sortDirection;
	},

	getWidth: function() {
		var defaultWidth = this.getDefaultWidth();
		var width = this.width;
		return width != null && width != 0 ? Math.max(width, this.getMinWidth()) : defaultWidth;
	},

	getMinWidth: function() {
		var field = this.field;
		var type = field.type;
		var format = field.format;

		switch(type) {
		case Type.Integer:
		case Type.Float:
			return HeaderBase.Numeric;
		case Type.Date:
			return Format.measureDate(format || Format.Date) + 1;
		case Type.Datetime:
			return Format.measureDate(format || Format.Datetime) + 1;
		default:
			return HeaderBase.Min;
		}
	},

	getDefaultWidth: function() {
		switch(this.field.type) {
		case Type.Integer:
		case Type.Float:
		case Type.Date:
		case Type.Datetime:
			return this.getMinWidth();
		default:
			return HeaderBase.Stretch;
		}
	},

	setWidth: function(width) {
		return this.width = width;
	},

	htmlMarkup: function() {
		if(this.icon != null) {
			var cls = this.setIcon(this.icon);
			var icon = { tag: 'i', cls: cls.join(' '), html: String.htmlText() };
		}

		cls = this.setSort(this.sortDirection);
		var sort = { tag: 'i', cls: cls.join(' '), html: String.htmlText() };

		cls = this.setFilter(false);
		var filter = { tag: 'i', cls: cls.join(' '), html: String.htmlText() };

		var text = String.htmlText(this.text);
		text = { cls: 'text', title: text, cn: icon != null ? [icon, text] : [text] } ;

		var cls = this.getCls().join(' ');

		var leftHandle = { cls: 'resize-handle-left' };
		var rightHandle = { cls: 'resize-handle-right' };
		return { tag: 'td', id: this.getId(), cls: cls, tabIndex: this.getTabIndex(), cn: [leftHandle, text, sort, filter, rightHandle], title: this.text };
	},

	completeRender: function() {
		this.callParent();

		this.textElement = this.selectNode('.text');
		this.iconElement = this.selectNode('.icon');
		this.sortElement = this.selectNode('.sort');
		this.filterElement = this.selectNode('.filter');

		this.resizeHandleLeft = this.selectNode('.resize-handle-left');
		this.resizeHandleRight = this.selectNode('.resize-handle-right');

		DOM.on(this, 'click', this.onClick, this);
		DOM.on(this, 'mouseDown', this.onMouseDown, this);
	},

	onDestroy: function() {
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'mouseDown', this.onMouseDown, this);

		this.textElement = this.iconElement = this.sortElement = this.filterElement = null;
		this.resizeHandleLeft = this.resizeHandleRight = null;

		this.callParent();
	},

	getCls: function() {
		var cls = this.cls = DOM.parseCls(this.cls).pushIf('column');
		if(this.sortDirection != null)
			cls.pushIf('sort');
		if(this.filtered)
			cls.pushIf('filter');
		return cls;
	},

	setBusy: function(busy) {
	},

	setIcon: function(icon) {
		this.icon = icon;
		var cls = this.iconCls = DOM.parseCls(icon).pushIf('fa', 'fa-fw', 'icon');
		DOM.setCls(this.iconElement, this.iconCls);
		return cls;
	},

	getSortIcon: function() {
		var direction = this.sortDirection;

		if(direction == null)
			return 'display-none';

		var icon = 'fa-sort-';

		switch(this.field.type) {
		case Type.Date:
		case Type.Datetime:
		case Type.Integer:
		case Type.Float:
			icon += 'numeric-';
			break;
		default:
			icon += 'alpha-';
		}
		return icon + direction;
	},

	getSort: function() {
		return this.sortDirection;
	},

	setSort: function(direction) {
		this.sortDirection = direction;
		var cls = this.sortCls = DOM.parseCls(this.getSortIcon()).pushIf('fa', 'fa-fw', 'sort');
		DOM.setCls(this.sortElement, cls);
		DOM.swapCls(this, direction != null, 'sort');
		return cls;
	},

	getFilterIcon: function() {
		return this.filtered ? 'fa-filter' : 'display-none';
	},

	setFilter: function(filtered) {
		this.filtered = filtered;
		var cls = this.filterCls = DOM.parseCls(this.getFilterIcon()).pushIf('fa', 'fa-fw', 'filter');
		DOM.setCls(this.filterElement, cls);
		DOM.swapCls(this, filtered, 'filter');
		return cls; 
	},

	getText: function(field) {
		return this.text;
	},

	setText: function(index, text) {
		this.text = text;
		text = String.htmlText(text);
		DOM.setValue(this.textElement, text);
		DOM.setTitle(this.textElement, text);
	},

	onMouseDown: function(event, target) {
		if(target == this.resizeHandleLeft || target == this.resizeHandleRight) {
			event.stopEvent();

			this.lastPageX = event.pageX;
			this.currentHandle = target;

			var resizer = this.resizer = DOM.append(document.body, { cls: 'column-resizer-mask' });

			DOM.on(resizer, 'mouseMove', this.onMouseMove, this);
			DOM.on(resizer, 'mouseUp', this.onMouseUp, this);
		}
	},

	onMouseMove: function(event, target) {
		event.stopEvent();

		var change = Ems.pixelsToEms(event.pageX - this.lastPageX);
		this.list.onHeaderResize(this, change, this.currentHandle == this.resizeHandleLeft ? 'left' : 'right');
		this.lastPageX = event.pageX;
	},

	onMouseUp: function(event, target) {
		var resizer = this.resizer;
		DOM.un(resizer, 'mouseMove', this.onMouseMove, this);
		DOM.un(resizer, 'mouseUp', this.onMouseUp, this);
		DOM.remove(this.resizer);
		this.resizer = null;

		this.list.onHeaderResized(this);
	},

	onClick: function(event, target) {
		event.stopEvent();
		this.list.onHeaderSort(this);
	}
});
