Z8.define('Z8.toolbar.Toolbar', {
	extend: 'Z8.Container',

	cls: 'toolbar',

	initComponent: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('toolbar');
		this.callParent();
	}
});