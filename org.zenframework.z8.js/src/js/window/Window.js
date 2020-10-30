Z8.define('Z8.window.Window', {
	extend: 'Container',
	shortClassName: 'Window',

	closable: true,
	autoClose: true,
	autoDestroy: true,
	isOpen: false,

	okText: 'Готово',
	cancelText: 'Отменить',

	getCls: function() {
		return Container.prototype.getCls.call(this).pushIf('window');
	},

	initComponent: function() {
		Container.prototype.initComponent.call(this);

		var icon = { tag: 'i', cls: DOM.parseCls(this.icon).pushIf('icon', 'fa', 'fw-fa').join(' ') };
		var text = this.text = { cls: 'text', html: this.header };

		var items = [icon, text, { cls: 'flex-1' }];

		if(this.closable && this.closeButton !== false) {
			this.closeButton = new Z8.button.Button({ cls: 'btn-tool', icon: 'fa-close', handler: this.cancel, scope: this });
			items.push(this.closeButton);
		}

		var header = this.header = new Container({ cls: 'header', items: items });

		var body = this.body = new Fieldset({ plain: true, flex: 1, cls: 'body', controls: this.controls, colCount: this.colCount || 1 });

		var buttons = this.buttons || [];

		if(Z8.isEmpty(this.buttons)) {
			var ok = this.okButton = new Button({ push: true, primary: true, text: this.okText, handler: this.ok, scope: this });
			buttons.push(ok);
		}

		if(this.closable && this.closeButton !== false) {
			this.cancelButton = new Button({ push: true, text: this.cancelText, handler: this.cancel, scope: this });
			buttons.insert(this.cancelButton, 0);
		}

		var footer = this.footer = new Z8.Container({ cls: 'footer', items: buttons });

		this.items = [header, body, footer];
	},

	htmlMarkup: function() {
		if(!Z8.isEmpty(this.controls))
			this.body.setControls(this.controls);
		return Container.prototype.htmlMarkup.call(this);
	},

	onDestroy: function() {
		this.close();
		Container.prototype.onDestroy.call(this);
	},

	open: function() {
		if(this.isOpen)
			return this;

		this.isOpen = true;

		if(this.getDom() == null) {
			var body = Viewport.getBody();
			body.items.push(this);

			this.mask = DOM.append(body, { cls: 'window-mask' }); 
			this.aligner = DOM.append(body, { cls: 'window-aligner' });
			this.render(this.aligner);
		} else {
			DOM.removeCls(this.mask, 'display-none');
			DOM.removeCls(this.aligner, 'display-none');
		}

		DOM.on(Viewport, 'mouseDown', this.monitorOuterClick, this);
		DOM.on(this, 'keyDown', this.onKeyDown, this);

		this.setActive(true);
		this.focus();

		return this;
	},

	close: function() {
		if(!this.isOpen)
			return;

		this.isOpen = false;
		this.setActive(false);

		DOM.un(Viewport, 'mouseDown', this.monitorOuterClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);

		if(this.autoDestroy) {
			var body = Viewport.getBody();
			body.items.remove(this);

			DOM.remove(this.aligner);
			DOM.remove(this.mask);

			this.aligner = this.mask = null;

			if(!this.destroying)
				this.destroy();
		} else {
			DOM.addCls(this.mask, 'display-none');
			DOM.addCls(this.aligner, 'display-none');
		}
	},

	monitorOuterClick: function(event) {
		var dom = DOM.get(this);
		var target = event.target;

		if(!this.isBusy() && dom != target && !DOM.isParentOf(dom, target))
			this.cancel();
	},

	ok: function() {
		this.notifyAndClose(true);
	},

	cancel: function() {
		if(this.closable)
			this.notifyAndClose(false);
	},

	isBusy: function() {
		return this.okButton != null ? this.okButton.isBusy() : false;
	},

	setBusy: function(busy) {
		if(this.okButton != null)
			this.okButton.setBusy(busy);
	},

	notifyAndClose: function(success) {
		if(!this.autoClose)
			this.setBusy(true);

		this.notify(success);

		if(this.autoClose)
			this.close();
	},

	notify: function(success) {
		Z8.callback(this.handler, this.scope, this, success);
	},

	focus: function() {
		return this.isEnabled() ? (this.body.focus() ? true : DOM.focus(this)) : false;
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ESC) {
			this.cancel();
			event.stopEvent();
		} else if(key == Event.ENTER) {
			this.ok();
			event.stopEvent();
		}
	}
});