Z8.define('Z8.window.Window', {
	extend: 'Z8.Container',
	shortClassName: 'Window',

	closable: true,
	autoClose: true,
	autoDestroy: true,
	isOpen: false,

	getCls: function() {
		return Z8.Container.prototype.getCls.call(this).pushIf('window');
	},

	htmlMarkup: function() {
		var icon = { tag: 'i', cls: DOM.parseCls(this.icon).pushIf('icon', 'fa', 'fw-fa').join(' ') };
		var text = this.text = { cls: 'text', html: this.header };

		var items = [icon, text, { cls: 'flex-1' }];

		if(this.closable && this.closeButton !== false) {
			this.closeButton = new Z8.button.Button({ cls: 'btn-tool', icon: 'fa-close', handler: this.cancel, scope: this });
			items.push(this.closeButton);
		}

		var header = this.header = new Z8.Container({ cls: 'header', items: items });

		var body = this.body = new Z8.form.Fieldset({ plain: true, flex: 1, cls: 'body', controls: this.controls, colCount: this.colCount || 1 });

		var buttons = this.buttons || [];

		if(Z8.isEmpty(this.buttons)) {
			var ok = this.okButton = new Z8.button.Button({ push: true, primary: true, text: this.okText || 'Готово', handler: this.ok, scope: this });
			buttons.push(ok);
		}

		if(this.closable && this.closeButton !== false) {
			this.cancelButton = new Z8.button.Button({ push: true, text: 'Отмена', handler: this.cancel, scope: this });
			buttons.insert(this.cancelButton, 0);
		}

		var footer = this.footer = new Z8.Container({ cls: 'footer', items: buttons });

		this.items = [header, body, footer];

		return this.callParent();
	},

	onDestroy: function() {
		this.close();
		this.callParent();
	},

	open: function() {
		if(this.isOpen)
			return;

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

		if(dom != target && !DOM.isParentOf(dom, target))
			this.cancel();
	},

	ok: function() {
		this.notifyAndClose(true);
	},

	cancel: function() {
		if(this.closable)
			this.notifyAndClose(false);
	},

	setBusy: function(busy) {
		if(this.busyButton != null)
			this.busyButton.setBusy(busy);
	},

	notifyAndClose: function(success) {
		var busyButton = this.busyButton = success ? this.okButton : this.cancelButton;

		if(!this.autoClose && busyButton != null)
			busyButton.setBusy(true);

		if(this.handler != null)
			this.handler.call(this.scope, this, success);

		if(this.autoClose)
			this.close();
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