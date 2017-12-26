Z8.define('Z8.form.field.Text', {
	extend: 'Z8.form.field.Control',

	triggers: null,

	placeholder: '',

	tag: 'input',

	editable: true,

	initComponent: function() {
		this.triggers = this.triggers || [];
		this.callParent();
	},

	controlMarkup: function() {
		var value = this.valueToRaw(this.getValue());
		var enabled = this.isEnabled();
		var readOnly = this.isReadOnly();

		var tag = this.getInputTag();
		var inputCls = this.getInputCls().join(' ');
		value = Format.htmlEncode(value);
		var input = { tag: tag, name: 'input', cls: inputCls, tabIndex: this.getTabIndex(), spellcheck: false, type: this.password ? 'password' : 'text', title: this.tooltip || '', placeholder: this.placeholder, value: tag == 'input' ? value : null, html: tag != 'input' ? value : null };

		if(!enabled)
			input.disabled = null;

		if(readOnly)
			input.readOnly = null;

		var result = [input];

		var triggers = this.triggers;

		if(!Z8.isEmpty(triggers)) {
			triggers = Array.isArray(triggers) ? triggers : [triggers];
			this.triggers = [];

			this.cls = DOM.parseCls(this.cls).pushIf('trigger-' + triggers.length);

			for(var i = 0, length = triggers.length; i < length; i++) {
				var trigger = triggers[i];
				var cls = DOM.parseCls(trigger.cls).pushIf('trigger-' + (length - i));
				if(!enabled || readOnly)
					cls.pushIf('hidden');
				trigger = new Z8.button.Trigger({ primary: true, tooltip: trigger.tooltip, icon: trigger.icon, handler: trigger.handler, scope: trigger.scope, cls: cls });
				result.push(trigger.htmlMarkup());

				this.triggers.push(trigger);
			}
		}

		return result;
	},

	subcomponents: function() {
		return this.callParent().concat(this.triggers);
	},

	completeRender: function() {
		DOM.on(this, 'click', this.onClick, this);
		DOM.on(this, 'keyDown', this.onKeyDown, this);

		var input = this.input = this.selectNode(this.getInputTag() + '[name=input]');

		if(this.editable)
			DOM.on(input, 'input', this.onInput, this);

		this.callParent();
	},

	onDestroy: function() {
		DOM.un(this.input, 'input', this.onInput, this);
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);

		this.input = null;

		this.callParent();
	},

	swapTriggersCls: function(condition, trueCls, falseCls) {
		var triggers = this.triggers;
		for(var i = 0, length = triggers.length; i < length; i++)
			DOM.swapCls(triggers[i], condition, trueCls, falseCls);
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this.input, !enabled, 'disabled');
		DOM.setDisabled(this.input, !enabled);
		this.swapTriggersCls(!enabled || this.isReadOnly(), 'hidden');
		this.callParent(enabled);
	},

	setReadOnly: function(readOnly) {
		if(this.isReadOnly() != readOnly) {
			DOM.setReadOnly(this.input, readOnly);
			this.swapTriggersCls(!this.isEnabled() || readOnly, 'hidden');
		}
		this.callParent(readOnly);
	},

	getRawValue: function(value) {
		return DOM.getValue(this.input);
	},

	setRawValue: function(value) {
		DOM.setValue(this.input, value);
		DOM.setAttribute(this.input, 'title', value);
	},

	setTabIndex: function(tabIndex) {
		tabIndex = this.callParent(tabIndex);
		DOM.setTabIndex(this.input, tabIndex);
		return tabIndex;
	},

	getInputTag: function() {
		return this.editable ? this.tag : 'div';
	},

	getInputCls: function() {
		var cls = DOM.parseCls(this.inputCls).pushIf('control');
		if(!this.isEnabled())
			cls.pushIf('disabled');
		return cls;
	},

	focus: function(select) {
		return this.isEnabled() ? DOM.focus(this.input, select) : false;
	},

	getTrigger: function(index) {
		var triggers = this.triggers;
		return triggers[index || triggers.length - 1];
	},

	onClick: function(event, target) {
		event.stopEvent();

		var triggers = this.triggers;

		for(var i = 0, length = triggers.length; i < length; i++) {
			var trigger = triggers[i];
			if(DOM.isParentOf(trigger, target) && trigger.isEnabled()) {
				var handler = trigger.handler;
				if(handler != null)
					handler.call(trigger.scope, trigger);
				else
					this.onTriggerClick(trigger);
				return;
			}
		}
	},

	onTriggerClick: function(trigger) {
	},

	onKeyEvent: function(event, target) {
	},

	onKeyDown: function(event, target) {
		if(!this.isEnabled() || this.isReadOnly())
			return;

		if(this.onKeyEvent(event, target))
			event.stopEvent();
	},

	onInput: function(event, target) {
		var rawValue = this.getRawValue();
		var value = this.rawToValue(rawValue);
		this.mixins.field.setValue.call(this, value);
	}
});