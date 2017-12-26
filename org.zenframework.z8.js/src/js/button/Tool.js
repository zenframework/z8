Z8.define('Z8.button.Tool', {
	extend: 'Z8.button.Button',

	tabIndex: -1,

	initComponent: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('btn-sm');
		this.callParent();
	}
});