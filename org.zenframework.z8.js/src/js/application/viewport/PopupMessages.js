Z8.define('Z8.viewport.PopupMessages', {
	extend: 'Z8.Container',

	cls: 'messages popup',
	stacked: true,
	autoHide: true,

	show: function(message) {
		var items = this.items;
		var count = items.length;

		if(this.stacked) {
			var messages = Array.isArray(message) ? message : [message];

			var news = messages.length;

			if(count + news > 5) {
				for(var i = count - 1; i >= Math.max(5 - news, 0); i--)
					items[i].remove();
			}

			var items = [];
			for(var i = Math.max(0, news - 5); i < news; i++)
				items.insert(new Z8.viewport.PopupMessage(messages[i]), 0);
			this.add(items, 0);
		} else {
			if(count > 0)
				items[count - 1].remove();
			if(message != null)
				this.add(new Z8.viewport.PopupMessage(message));
		}
	}
});

Z8.define('Z8.viewport.PopupMessage', {
	extend: 'Z8.Component',

	cls: 'message',

	initComponent: function() {
		this.callParent();
		this.messageDelay = Application.popupMessageDelay || 4000;
	},

	htmlMarkup: function() {
		var icon = { cls: 'icon fa fa-fw fa-' + this.type + ' ' + this.type, html: String.htmlText() };
		var source = { tag: 'b', html: this.source || Application.title };
		var text = { cls: 'text', cn: [source, { tag: 'br' }, Format.nl2br(this.text)] };
		var body = { cls: 'body', cn: [icon, text] };
		return { cls: 'message ' + this.type, id: this.getId(), cn: [body] };
	},

	completeRender: function() {
		this.callParent();

		DOM.on(this, 'click', this.hide, this);
		if(this.container.autoHide)
			new Z8.util.DelayedTask().delay(this.messageDelay, this.hide, this);
	},

	onDestroy: function() {
		DOM.un(this, 'click', this.hide, this);
		this.callParent();
	},

	hide: function() {
		if(!this.isHiding) {
			this.isHiding = true;
			DOM.addCls(this, 'hiding');
			new Z8.util.DelayedTask().delay(1000, this.remove, this);
		}
	},

	remove: function() {
		this.container.remove(this);
	}
});