Z8.define('Z8.list.Header', {
	extend: 'Z8.list.HeaderBase',
	shortClassName: 'ListHeader',

	/*
	* config:
	* icon: '',
	* field: {},
	*
	*/

	statics: {
		NumericSort09IconCls: 'fa fa-fw fa-sort-numeric-asc',
		NumericSort90IconCls: 'fa fa-fw fa-sort-numeric-desc',
		AlphaSortAZIconCls: 'fa fa-fw fa-sort-alpha-asc',
		AlphaSortZAIconCls: 'fa fa-fw fa-sort-alpha-desc',
		FilterIconCls: 'fa fa-fw fa-filter'
	},

	initComponent: function() {
		this.callParent();

		var field = this.field;
		this.text = Format.nl2br(field.columnHeader || field.header);
		this.title = field.description || this.text;
		this.icon = field.icon;
		this.width = field != null ? field.width : null;
		this.fixed = field != null ? field.fixed : false;
		this.sortDirection = field.sortDirection;
	},

	getWidth: function() {
		var defaultWidth = this.getDefaultWidth();
		var width = this.width;

		if(this.fixed)
			return this.width || defaultWidth;

		return this.hidden ? 0 : (width != null && width != 0 ? Math.max(width, this.getMinWidth()) : defaultWidth);
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
			return Math.max(Format.measureDate(format || Format.Date) + 1, HeaderBase.Date);
		case Type.Datetime:
			return Math.max(Format.measureDate(format || Format.Datetime) + 1, HeaderBase.Datetime);
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
		text = { cls: 'text', cn: icon != null ? [icon, text] : [text] } ;

		var leftHandle = { cls: 'resize-handle-left' };
		var rightHandle = { cls: 'resize-handle-right' };
		return { tag: 'td', id: this.getId(), cls: this.getCls().join(' '), tabIndex: this.getTabIndex(), cn: [leftHandle, text, sort, filter, rightHandle], title: this.title };
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
		var cls = Z8.list.HeaderBase.prototype.getCls.call(this);

		if(this.sortDirection != null)
			cls.pushIf('sort');
		if(this.filtered)
			cls.pushIf('filter');

		return cls.pushIf('column');
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

		switch(this.field.type) {
		case Type.Date:
		case Type.Datetime:
		case Type.Integer:
		case Type.Float:
			return direction == 'asc' ? ListHeader.NumericSort09IconCls : ListHeader.NumericSort90IconCls;
		default:
			return direction == 'asc' ? ListHeader.AlphaSortAZIconCls : ListHeader.AlphaSortZAIconCls;
		}
	},

	getSort: function() {
		return this.sortDirection;
	},

	setSort: function(direction) {
		this.sortDirection = direction;
		var cls = DOM.parseCls(this.getSortIcon());
		if(direction != null)
			cls.pushIf('sort', 'icon');
		DOM.setCls(this.sortElement, cls);
		return cls;
	},

	getFilterIcon: function() {
		return this.filtered ? ListHeader.FilterIconCls : 'display-none';
	},

	setFilter: function(filtered) {
		this.filtered = filtered;
		var cls = DOM.parseCls(this.getFilterIcon());
		if(filtered)
			cls.pushIf('filter', 'icon');
		DOM.setCls(this.filterElement, cls);
		return cls; 
	},

	getText: function(field) {
		return this.text;
	},

	setText: function(index, text, title) {
		this.text = text;
		text = String.htmlText(text);
		DOM.setValue(this.textElement, Format.nl2br(text));
		DOM.setTitle(this.textElement, title || text);
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
