Z8.define('Z8.button.Button', {
	extend: 'Z8.Component',

	tabIndex: 0,

	text: '',
	icon: null,

	primary: false,
	danger: false,
	success: false,
	info: false,

	split: false,
	trigger: null,
	menu: null,

	toggle: false,
	toggled: null,
	vertical: false,

	busy: false,

	tooltip: '',
	triggerTooltip: '',

	iconTag: 'i',

	initComponent: function() {
		this.callParent();
	},

	htmlMarkup: function() {
		this.setIcon(this.icon);

		this.toggle = this.toggle || this.toggled != null || this.toggleHandler != null;
		this.split = this.split || this.menu && this.trigger !== false;

		var iconCls = this.getIconCls().join(' ');

		var icon = { tag: this.iconTag, icon: this.getId(), cls: iconCls, html: String.htmlText() };
		var text = { tag: 'span', cls: 'text', html: String.htmlText(this.text) };
		var title = this.tooltip || '';

		var cn = [icon, text];

		var menu = this.menu;
		if(menu != null) {
			menu.setOwner(this);
			cn.push(menu.htmlMarkup());
		}

		var button = { tag: 'a', anchor: this.getId(), cls: this.getButtonCls().join(' '), tabIndex: this.getTabIndex(), title: title, cn: cn };

		if(!this.split) {
			button.id = this.getId(); 
			return button;
		}

		var cn = [button];

		trigger = this.trigger = new Z8.button.Trigger({ cls: this.cls, primary: this.primary, danger: this.danger, success: this.success, info: this.info, tooltip: this.triggerTooltip, icon: this.triggerIcon, tabIndex: this.getTabIndex(), enabled: this.enabled });
		cn.push(trigger.htmlMarkup());

		if(menu != null)
			menu.setOwner(trigger);

		return { tag: 'div', id: this.getId(), cls: 'btn ' + (this.vertical ? 'vertical' : ''), cn: cn };
	},

	subcomponents: function() {
		var components = [];
		if(this.trigger)
			components.add(this.trigger);
		if(this.menu)
			components.add(this.menu);
		return components;
	},

	completeRender: function() {
		this.callParent();

		this.button = this.selectNode('a[anchor=' + this.getId() + ']') || this.getDom();
		this.icon = this.selectNode(this.iconTag + '[icon=' + this.getId() + ']');
		this.textElement = this.selectNode('.text');

		if(!this.disableEvents) {
			DOM.on(this, 'click', this.onClick, this);
			DOM.on(this, 'keyDown', this.onKeyDown, this);
		}

		var menu = this.menu;

		if(menu != null) {
			menu.setAlignment(this);
			menu.on('show', this.onMenuShow, this);
			menu.on('hide', this.onMenuHide, this);
		}
	},

	onDestroy: function() {
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);

		this.button = this.icon = this.textElement = null;

		this.callParent();
	},

	setEnabled: function(enabled) {
		this.wasEnabled = enabled;

		DOM.swapCls(this.button, !enabled, 'disabled');

		if(this.trigger)
			this.trigger.setEnabled(enabled);

		this.callParent(enabled);
	},

	getButtonTypeCls: function() {
		if(this.primary)
			return 'btn-primary';
		else if(this.danger)
			return 'btn-danger';
		else if(this.success)
			return 'btn-success';
		else if(this.info)
			return 'btn-info';
		return 'btn-default';
	},

	getButtonCls: function() {
		var cls = this.getCls().pushIf('btn', this.getButtonTypeCls());

		if(!this.isEnabled())
			cls.pushIf('disabled');

		if(this.toggle && this.toggled)
			cls.pushIf('active');

		return cls;
	},

	getIconCls: function() {
		var cls = DOM.parseCls(this.iconCls).pushIf('fa').pushIf('icon');
		if(Z8.isEmpty(this.iconCls))
			cls.pushIf('no-icon');
		if(Z8.isEmpty(this.text))
			cls.pushIf('no-text');
		return cls;
	},

	setIcon: function(cls) {
		this.iconCls = cls;
		DOM.setCls(this.icon, this.getIconCls());
	},

	setPrimary: function(primary) {
		this.primary = primary;
		DOM.swapCls(this.button, primary, 'btn-primary', 'btn-default');

		if(this.trigger)
			this.trigger.setPrimary(primary);
	},

	setTabIndex: function(tabIndex) {
		tabIndex = this.callParent(tabIndex);

		DOM.setTabIndex(this.button, tabIndex);

		if(this.trigger)
			this.trigger.setTabIndex(tabIndex);
	},

	getText: function() {
		return this.text;
	},

	setText: function(text) {
		text = this.text = text || '';
		var noText = Z8.isEmpty(text);
		DOM.setValue(this.textElement, String.htmlText(text));
		DOM.setTitle(this.textElement, text);
		this.iconCls = DOM.swapCls(this.icon, noText, 'no-text') || this.iconCls;
	},

	setTooltip: function(tooltip) {
		this.tooltip = tooltip || '';
		DOM.setAttribute(this.button, 'title', tooltip);
	},

	isRadio: function() {
		var container = this.container;
		return container != null && container.radio;
	},

	isToggled: function() {
		return this.toggled;
	},

	setToggled: function(toggled, silent) {
		this.toggle = true;
		this.toggled = toggled;

		DOM.swapCls(this.button, toggled, 'active');

		if(this.isRadio() && toggled)
			this.container.onRadioToggle(this, toggled);

		if(silent)
			return;

		Z8.callback(this.toggleHandler, this.scope, this, toggled);

		this.fireEvent('toggle', this, toggled);
	},

	isBusy: function() {
		return this.busy;
	},

	setBusy: function(busy) {
		if(this.busy != busy) {
			this.busy = busy;

			if(busy) {
				this.cachedIconCls = this.iconCls;
				var wasEnabled = this.isEnabled()
				this.setEnabled(false);
				this.wasEnabled = wasEnabled;
				this.setIcon(['fa-circle-o-notch', 'fa-spin']);
			} else {
				this.setEnabled(this.wasEnabled);
				this.setIcon(this.cachedIconCls);
			}

			DOM.swapCls(this, busy, 'z-index-10');
		}
	},

	focus: function() {
		return this.isEnabled() ? DOM.focus(this.button) : false;
	},

	onClick: function(event, target) {
		event.stopEvent();

		if(!this.isEnabled())
			return;

		var button = DOM.isParentOf(this.button, target);
		var trigger = !button && DOM.isParentOf(this.trigger, target);

		if(button && this.toggle) {
			this.setToggled(!this.isRadio() ? !this.toggled : true, false);
			return;
		}

		if((trigger || !this.trigger) && this.menu) {
			this.toggleMenu();
			return;
		}

		if(!button && !trigger)
			return;

		var handler = button ? this.handler : this.triggerHandler;
		Z8.callback(handler, this.scope, this);

		this.fireEvent(button ? 'click' : 'triggerClick', this);
	},

	onKeyDown: function(event, target) {
		if(!this.isEnabled())
			return;

		var key = event.getKey();

		if(key == Event.ENTER || key == Event.SPACE)
			this.onClick(event, DOM.isParentOf(this.menu, target) ? (this.trigger ? this.trigger.button : this.button) : target);
		else if((key == Event.DOWN || key == Event.UP || key == Event.HOME || key == Event.END) && this.menu)
			this.onClick(event, (this.trigger ? this.trigger.button : this.button));
	},

	toggleMenu: function() {
		this.menu.toggle();
	},

	onMenuShow: function() {
		DOM.addCls(this, 'open');
		if(this.trigger)
			this.trigger.rotateIcon(180);
	},

	onMenuHide: function() {
		DOM.removeCls(this, 'open');

		if(this.trigger) {
			this.trigger.rotateIcon(0);
			DOM.focus(this.trigger);
		} else
			DOM.focus(this);
	},

	rotateIcon: function(degree) {
		DOM.rotate(this.icon, degree);
	}
});
