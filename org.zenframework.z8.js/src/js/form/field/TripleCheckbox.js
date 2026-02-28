Z8.define('Z8.form.field.TripleCheckbox', {
	extend: 'CheckBox',
	shortClassName: 'TripleCheckbox',

	statics: {
		TrueIconCls: 'fa fa-check-square',
		FalseIconCls: 'fa fa-square-o',
		OffIconCls: 'fa fa-square-o disabled',

		VALUES: [true, false, null]
	},

	cls: 'triple-checkbox',

	initComponent: function() {
		this.callParent();

		this.trueCls = DOM.parseCls(TripleCheckbox.TrueIconCls).pushIf('control');
		this.falseCls = DOM.parseCls(TripleCheckbox.FalseIconCls).pushIf('control');
		this.offCls = DOM.parseCls(TripleCheckbox.OffIconCls).pushIf('control');
	},

	completeRender: function() {
		DOM.on(this, 'contextMenu', this.onContextMenu, this);

		this.callParent();
	},

	onDestroy: function() {
		DOM.un(this, 'contextMenu', this.onContextMenu, this);

		this.callParent();
	},

	getIconCls: function() {
		let value = this.getValue();
		return Z8.isEmpty(value) ? this.offCls : (value ? this.trueCls : this.falseCls);
	},

	onClick: function(event, target) {
		event.stopEvent();

		if(!this.isEnabled() || this.isReadOnly())
			return;

		this.nextValue();
	},

	onContextMenu: function(event, target) {
		event.stopEvent();

		if(!this.isEnabled() || this.isReadOnly())
			return;

		this.nextValue(-1);
	},

	nextValue: function(direction) {
		if (Z8.isEmpty(direction))
			direction = 1;

		let sequence = this.getSequence();
		let nextIndex = sequence.indexOf(this.getValue()) + direction;

		if (nextIndex < 0)
			nextIndex = sequence.length - 1;
		else if (nextIndex >= sequence.length)
			nextIndex = 0;

		this.setValue(sequence[nextIndex]);
		return this.getValue();
	},

	getSequence: function() {
		return TripleCheckbox.VALUES;
	},
});