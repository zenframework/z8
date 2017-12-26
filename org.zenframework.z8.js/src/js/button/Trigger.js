Z8.define('Z8.button.Trigger', {
	extend: 'Z8.button.Button',

	tabIndex: -1,

	constructor: function(config) {
		config = config || {};
		config.cls = DOM.parseCls(config.cls).concat(['btn-trigger']);
		config.icon = config.icon || 'fa-caret-down';
		config.iconTag = config.iconTag || 'span';
		config.text = '';
		config.disableEvents = true;

		this.callParent(config);
	}
});