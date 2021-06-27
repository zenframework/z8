Z8.define('Z8.button.Button', {
	extend: 'Component',
	shortClassName: 'Button',

	tabIndex: 0,

	text: '',
	icon: null,
	activeIcon: null,

	push: false,
	primary: false,
	danger: false,
	success: false,
	info: false,

	split: false,
	trigger: null,
	menu: null,

	toggle: false,
	toggled: null,
	toggledIcon: null,

	bubbleClick: false,

	vertical: false,

	busy: false,

	tooltip: '',
	triggerTooltip: '',

	iconTag: 'i',

	statics: {
		BusyIconCls: 'fa-circle-o-notch fa-spin',
		TriggerIconCls: 'fa-caret-down'
	},

	htmlMarkup: function() {
		this.setIcon(this.icon);
		this.setActiveIcon(this.activeIcon);

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

		trigger = this.trigger = new Z8.button.Trigger({ cls: this.getCls(), primary: this.primary, danger: this.danger, success: this.success, info: this.info, tooltip: this.triggerTooltip, icon: this.triggerIcon, tabIndex: this.getTabIndex(), enabled: this.enabled });
		cn.push(trigger.htmlMarkup());

		if(menu != null)
			menu.setOwner(trigger);

		var cls = this.getCls().pushIf('btn');
		if(this.vertical)
			cls.pushIf('vertical');

		return { tag: 'div', id: this.getId(), cls: cls.join(' '), cn: cn };
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
		Component.prototype.completeRender.call(this);

		this.button = this.selectNode('a[anchor=' + this.getId() + ']') || this.getDom();
		this.icon = this.selectNode(this.iconTag + '[icon=' + this.getId() + ']');
		this.textElement = this.selectNode('.text');

		if(!this.disableEvents) {
			DOM.on(this, 'click', this.onClick, this);
			DOM.on(this, 'keyDown', this.onKeyDown, this);
		}

		var menu = this.menu;

		if(menu != null) {
			menu.on('show', this.onMenuShow, this);
			menu.on('hide', this.onMenuHide, this);
		}
	},

	onDestroy: function() {
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);

		if(this.setBusyTask != null)
			this.setBusyTask.cancel();

		this.button = this.icon = this.textElement = this.setBusyTask = null;

		Component.prototype.onDestroy.call(this);
	},

	setEnabled: function(enabled) {
		this.wasEnabled = enabled;

		Component.prototype.setEnabled.call(this, enabled);

		DOM.setCls(this.button, this.getButtonCls());

		if(this.trigger)
			this.trigger.setEnabled(enabled);
	},

	getButtonTypeCls: function() {
		if(this.primary)
			return 'primary';
		else if(this.danger)
			return 'danger';
		else if(this.success)
			return 'success';
		else if(this.info)
			return 'info';
		return 'default';
	},

	getButtonCls: function() {
		var cls = this.getCls().pushIf('btn', this.getButtonTypeCls());

		if(this.push)
			cls.pushIf('push');

		if(!this.isEnabled())
			cls.pushIf('disabled');

		if(this.toggle && this.toggled)
			cls.pushIf('active');

		if(Z8.isEmpty(this.text))
			cls.pushIf('no-text');

		return cls;
	},

	getIconCls: function() {
		var cls = DOM.parseCls(this.busy ? Button.BusyIconCls : ((this.toggled ? this.activeIconCls : null) || this.iconCls)).pushIf('fa').pushIf('icon');
		if(Z8.isEmpty(this.busy ? Button.BusyIconCls : this.iconCls))
			cls.pushIf('empty');
		return cls;
	},

	setIcon: function(cls) {
		this.iconCls = cls;
		DOM.setCls(this.icon, this.getIconCls());
		return this;
	},

	setActiveIcon: function(cls) {
		this.activeIconCls = cls;
		DOM.setCls(this.icon, this.getIconCls());
		return this;
	},

	setPrimary: function(primary) {
		this.primary = primary;
		DOM.setCls(this.button, this.getButtonCls());

		if(this.trigger)
			this.trigger.setPrimary(primary);
		return this;
	},

	setTabIndex: function(tabIndex) {
		tabIndex = Component.prototype.setTabIndex.call(this, tabIndex);

		DOM.setTabIndex(this.button, tabIndex);

		if(this.trigger)
			this.trigger.setTabIndex(tabIndex);
	},

	getText: function() {
		return this.text;
	},

	setText: function(text) {
		text = this.text = text || '';
		DOM.setValue(this.textElement, String.htmlText(text));
		DOM.setTitle(this.textElement, text);
		DOM.setCls(this.button, this.getButtonCls());
		return this;
	},

	setTooltip: function(tooltip) {
		this.tooltip = tooltip || '';
		DOM.setAttribute(this.button, 'title', tooltip);
		return this;
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

		DOM.setCls(this.button, this.getButtonCls());
		DOM.setCls(this.icon, this.getIconCls());

		if(this.isRadio() && toggled)
			this.container.onRadioToggle(this, toggled);

		if(silent)
			return this;

		Z8.callback(this.toggleHandler, this.scope, this, toggled);

		this.fireEvent('toggle', this, toggled);
		return this;
	},

	isBusy: function() {
		return this.busy;
	},

	setBusy: function(busy, delay) {
		if(this.busy && busy || !this.busy && !busy)
			return;

		this.busy = busy;

		if(busy) {
			var wasEnabled = this.isEnabled();
			this.setEnabled(false);
			this.wasEnabled = wasEnabled;

			var callback = function() {
				DOM.setCls(this.icon, this.getIconCls());
			};

			if(delay != 0) {
				this.setBusyTask = this.setBusyTask || new Z8.util.DelayedTask();
				this.setBusyTask.delay(delay || 1000, callback, this);
			} else
				callback.call(this);
		} else {
			if(this.setBusyTask != null)
				this.setBusyTask.cancel();

			this.setEnabled(this.wasEnabled);
			DOM.setCls(this.icon, this.getIconCls());
		}
	},

	focus: function() {
		return this.isEnabled() ? DOM.focus(this.button) : false;
	},

	onClick: function(event, target) {
		if(!this.bubbleClick)
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
		this.menu.setAlignment(this);
		DOM.append(this, this.menu);
		this.menu.toggle();
	},

	onMenuShow: function() {
		DOM.addCls(this, 'open');
		if(this.trigger)
			this.trigger.rotateIcon(180);

		Z8.callback(this.showMenuHandler, this.scope);
		this.fireEvent('menuShow', this);
	},

	onMenuHide: function() {
		DOM.removeCls(this, 'open');

		if(this.trigger) {
			this.trigger.rotateIcon(0);
			DOM.focus(this.trigger);
		} else
			DOM.focus(this);

		Z8.callback(this.hideMenuHandler, this.scope);
		this.fireEvent('menuHide', this);
	},

	rotateIcon: function(degree) {
		DOM.rotate(this.icon, degree);
	}
});