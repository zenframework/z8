Z8.define('Z8.Component', {
	extend: 'Z8.Object',
	shortClassName: 'Component',

	isComponent: true,

	enabled: true,
	visible: true,
	tabIndex: -1,

	inactive: true,

	autoAlign: false,

	statics: {
		idSeed: 0,

		nextId: function() {
			this.idSeed++;
			return 'id' + this.idSeed;
		},

		destroy: function(component) {
			if(component == null)
				return;

			var components = Array.isArray(component) ? component : [component];

			for(var i = 0, length = components.length; i < length; i++) {
				component = components[i];
				if(component == null || component.disposed)
					continue;
				if(component.isComponent)
					component.destroy();
				else if(DOM.isDom(component))
					DOM.remove(component);
				else if(component.dom != null) {
					DOM.remove(component.dom);
					component.dom = null;
				}
			}
		}
	},

	constructor: function(config) {
		Z8.Object.prototype.constructor.call(this, config);
		this.initComponent();
	},

	initComponent: function() {
		if(this.id == null)
			this.id = Z8.Component.nextId();

		this.enabledLock = !this.enabled;
	},

	getId: function() {
		return this.id;
	},

	getDom: function() {
		return this.dom;
	},

	getCls: function() {
		var cls = DOM.parseCls(this.cls);
		return this.visible ? cls : cls.pushIf('display-none');
	},

	getCell: function() {
		var dom = this.dom;
		return dom != null ? dom.parentNode : null;
	},

	getRow: function() {
		var dom = this.dom;
		return dom != null ? dom.parentNode.parentNode : null;
	},

	getTitle: function() {
		return this.title;
	},

	setTitle: function(title) {
		this.title = title;
		return this;
	},

	getIcon: function() {
		return this.icon;
	},

	setIcon: function(icon) {
		this.icon = icon;
		return this;
	},

	isEnabled: function() {
		return this.enabled;
	},

	setEnabled: function(enabled) {
		this.enabled = enabled;
		this.setTabIndex(enabled ? this.tabIndex: -1);
		return this;
	},

	isActive: function() {
		return !this.inactive;
	},

	setActive: function(active) {
		this.inactive = !active;
		return this;
	},

	getTabIndex: function() {
		return this.enabled ? this.tabIndex : -1;
	},

	setTabIndex: function(tabIndex) {
		if(tabIndex != -1)
			this.tabIndex = tabIndex;
		return tabIndex;
	},

	getText: function() {
		return this.getInnerHtml();
	},

	setText: function(text) {
		return this.setInnerHtml(text);
	},

	getInnerHtml: function() {
		return this.getDom() != null ? DOM.getInnerHtml(this) : this.html;
	},

	setInnerHtml: function(html) {
		if(this.getDom() != null)
			DOM.setInnerHtml(this, html);
		else
			this.html = html;
		return this;
	},

	isVisible: function() {
		return this.visible;
	},

	show: function(show) {
		if(show === false)
			return this.hide();

		this.visible = true;
		DOM.removeCls(this, 'display-none');
		return this;
	},

	hide: function(hide) {
		if(hide === false)
			return this.show();

		this.visible = false;
		DOM.addCls(this, 'display-none');
		return this;
	},

	focus: function() {
		return this.isEnabled() ? DOM.focus(this) : false;
	},

	selectNode: function(selector) {
		return DOM.selectNode(this, selector);
	},

	queryNodes: function(selector) {
		return DOM.query(this, selector);
	},

	appendTo: function(element) {
		var dom = this.dom;

		if(dom == null)
			this.render(element);
		else
			DOM.append(element, dom);
	},

	render: function(dom) {
		if(this.dom == null) {
			var markup = this.htmlMarkup();
			this.dom = DOM.append(dom, markup);
			this.renderDone();
		}
	},

	getWidth: function() {
		var dom = this.dom;
		return dom != null ? Ems.pixelsToEms(dom.offsetWidth) : 0;
	},

	getHeight: function() {
		var dom = this.dom;
		return dom != null ? Ems.pixelsToEms(dom.offsetHeight) : 0;
	},

	getMinWidth: function() {
		return this.minWidth || 0;
	},

	getMinHeight: function() {
		return this.minHeight || 0;
	},

	getSize: function() {
		var dom = this.dom;
		return { width: this.getWidth(), height: this.getHeight() };
	},

	updateSize: function() {
		var minWidth = this.getMinWidth();
		if(minWidth > 0 || String.isString(minWidth))
			DOM.setPoint(this, 'minWidth', minWidth);

		var minHeight = this.getMinHeight();
		if(minHeight > 0 || String.isString(minHeight))
			DOM.setPoint(this, 'minHeight', minHeight);
	},

	completeRender: function() {
		this.updateSize();

		var subcomponents = this.subcomponents();

		for(var i = 0, length = subcomponents.length; i < length; i++) {
			var component = subcomponents[i];
			if(component == null || component.dom != null)
				continue;
			if(component.id != null)
				component.dom = this.selectNode('#' + component.id);
			if(component.isComponent)
				component.completeRender();
		}

		if(this.autoAlign && !this.afterRenderCalled)
			DOM.on(window, 'resize', this.onWindowResize, this);
	},

	afterRender: function() {
		var subcomponents = this.subcomponents();

		for(var i = 0, length = subcomponents.length; i < length; i++) {
			var component = subcomponents[i];
			if(component == null || component.afterRenderCalled)
				continue;
			if(component.isComponent) {
				component.afterRender();
				component.afterRenderCalled = true;
			}
		}

		if(this.renderOptions != null)
			Z8.callback(this.renderOptions);
	},

	renderDone: function() {
		this.completeRender();
		this.afterRender();
	},

	destroy: function() {
		this.destroying = true;
		this.onDestroy();
		this.destroying = false;

		this.dispose();
	},

	onDestroy: function() {
		if(this.autoAlign)
			DOM.un(window, 'resize', this.onWindowResize, this);

		Component.destroy(this.subcomponents());
		DOM.remove(this);

		this.alignment = this.dom = null;
	},

	subcomponents: function() {
		return [];
	},

	htmlMarkup: function() {
		return { id: this.getId(), cls: this.getCls().join(' '), tabIndex: this.getTabIndex(), html: this.html || '' };
	},

	setPosition: function(left, top) {
		DOM.setPoint(this, 'left', left != null ? left : 'auto');
		DOM.setPoint(this, 'top', top != null ? top : 'auto');
		return this;
	},

	getAlignment: function() {
		return this.alignment;
	},

	setAlignment: function(alignment) {
		this.alignment = alignment;
		return this;
	},

	setAlignmentOffset: function(horizontal, vertical, margin, adjustHeight) {
		this.alignmentOffset = { width: horizontal, height: vertical, margin: margin, adjustHeight: adjustHeight };
		return this;
	},

	getClipping: function() {
		if(this.clipping == null)
			this.clipping = DOM.getClipping(this);
		return this.clipping;
	},

	align: function() {
		var alignment = this.alignment;

		var style = DOM.getComputedStyle(this);
		var fixed = style.position == 'fixed';

		var clipping = fixed ? document.body : this.getClipping();

		var viewport = new Rect(clipping);

		var align = new Rect(alignment || this);

		if(alignment == null) {
			align.height = align.width = 0;
			align.bottom = align.top;
			align.right = align.left;
		}

		var parent = new Rect(DOM.getParent(this));

		var offset = this.alignmentOffset || { width: 0, height: 0, margin: 1, adjustHeight: true };
		var style = DOM.getComputedStyle(this);

		var height = parseFloat(style.height) + parseInt(style.marginTop) + parseInt(style.marginBottom);
		height = Ems.pixelsToEms(height) + offset.height;
		var width = parseFloat(style.width) + parseInt(style.marginLeft) + parseInt(style.marginRight);
		width = Ems.pixelsToEms(width) + offset.width;

		var spaceLeft = align.left - viewport.left;
		var spaceAbove = align.top - viewport.top;
		var spaceRight = viewport.right - align.left;
		var spaceBelow = viewport.bottom - align.bottom;

		var above = false;
		var available = spaceBelow;

		if(spaceBelow < height) {
			if(spaceAbove >= height || spaceBelow < spaceAbove) {
				above = true;
				available = spaceAbove;
			}
		}

		var rect = new Rect();

		width += offset.margin;
		rect.left = align.left - (spaceRight < width ? width - spaceRight : 0);

		if(above) {
			rect.bottom = align.top - offset.height;
			rect.top = rect.bottom - (Math.min(available, height) - offset.height) + (available < height ? offset.margin : 0);
		} else {
			rect.top = align.top + align.height;
			rect.bottom = rect.top + (Math.min(available, height) - offset.height) - (available < height ? offset.margin : 0);
		}

		rect.left = rect.left - (fixed ? 0 : parent.left);
		rect.top = rect.top - (fixed ? 0 : parent.top);
		rect.bottom = (fixed ? viewport.bottom : parent.bottom) - rect.bottom;

		DOM.setLeft(this, Ems.emsToPixels(rect.left) + 'px');
		DOM.setTop(this, Ems.emsToPixels(rect.top) + 'px');
		if(alignment != null && offset.adjustHeight)
			DOM.setBottom(this, Ems.emsToPixels(rect.bottom) + 'px');
		DOM.swapCls(this, above, 'pull-up', 'pull-down');

		this.fireEvent('align', this);
	},

	onWindowResize: function() {
		if(this.visible)
			this.align();
	}
});