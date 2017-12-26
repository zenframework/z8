Z8.define('Z8.list.HeaderIcon', {
	extend: 'Z8.list.HeaderBase',

	fixed: true,

	initComponent: function() {
		this.callParent();
		var field = this.field;
		this.icon = field != null ? field.icon : this.icon;
		this.title = (field != null ? field.header : this.title) || '';
	},

	getWidth: function() {
		return this.width || HeaderBase.Icon;
	},

	getMinWidth: function() {
		return this.getWidth();
	},

	htmlMarkup: function() {
		if(this.icon != null) {
			var cls = DOM.parseCls(this.icon).pushIf('fa').join(' ');
			var icon = { tag: 'i', cls: cls };
		} else
			var icon = { tag: 'span', html: this.title.substr(0, 2) };

		cls = DOM.parseCls(this.cls).pushIf('column', 'icon').join(' ');
		return { tag: 'td', id: this.getId(), cls: cls, tabIndex: this.getTabIndex(), cn: [icon], title: this.title };
	},

	completeRender: function() {
		this.callParent();
		this.icon = this.selectNode('.column>.fa');
	},

	onDestroy: function() {
		this.icon = null;
		this.callParent();
	}
});
