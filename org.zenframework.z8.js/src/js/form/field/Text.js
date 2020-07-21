Z8.define('Z8.form.field.Text', {
	extend: 'Z8.form.field.Control',

	triggers: null,

	placeholder: '',

	tag: 'input',
	autocomplete: 'off',

	editor: true,

	initComponent: function() {
		this.triggers = this.triggers || [];
		Z8.form.field.Control.prototype.initComponent.call(this);
	},

	getCls: function() {
		var cls = Z8.form.field.Control.prototype.getCls.call(this);
		return Z8.isEmpty(this.triggers) ? cls : cls.pushIf('trigger').pushIf('trigger-' + this.triggers.length);
	},

	controlMarkup: function() {
		var value = this.valueToRaw(this.getValue());
		var enabled = this.isEnabled();
		var readOnly = this.isReadOnly();
		var length = this.length;

		var tag = this.getInputTag();
		var inputCls = this.getInputCls().join(' ');
		value = Format.htmlEncode(value);
		var input = { tag: tag, name: this.getId(), cls: inputCls, tabIndex: this.getTabIndex(), spellcheck: false, type: this.password ? 'password' : 'text', title: this.tooltip || '', placeholder: this.placeholder, autocomplete: this.autocomplete, value: tag == 'input' ? value : null, html: tag != 'input' ? value : null };

		if(!enabled)
			input.disabled = null;

		if(readOnly)
			input.readOnly = null;

		if(length != 0)
			input.maxlength = length;

		var result = [input];

		var triggers = this.triggers;

		if(!Z8.isEmpty(triggers)) {
			triggers = Array.isArray(triggers) ? triggers : [triggers];
			this.triggers = [];

			for(var i = 0, length = triggers.length; i < length; i++) {
				var trigger = triggers[i];
				var cls = DOM.parseCls(trigger.cls).pushIf('trigger-' + (length - i));
				trigger = new Z8.button.Trigger({ tooltip: trigger.tooltip, icon: trigger.icon, handler: trigger.handler, scope: trigger.scope, cls: cls });
				result.push(trigger.htmlMarkup());

				this.triggers.push(trigger);
			}
		}

		return result;
	},

	subcomponents: function() {
		return Z8.form.field.Control.prototype.subcomponents.call(this).concat(this.triggers);
	},

	completeRender: function() {
		Z8.form.field.Control.prototype.completeRender.call(this);

		DOM.on(this, 'click', this.onClick, this);
		DOM.on(this, 'keyDown', this.onKeyDown, this);

		var input = this.input = this.selectNode(this.getInputTag() + '[name=' + this.getId() + ']');

		if(this.editor)
			DOM.on(input, 'input', this.onInput, this);
	},

	onDestroy: function() {
		DOM.un(this.input, 'input', this.onInput, this);
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);

		this.input = null;

		Z8.form.field.Control.prototype.onDestroy.call(this);
	},


	setEnabled: function(enabled) {
		DOM.swapCls(this.input, !enabled, 'disabled');
		DOM.setDisabled(this.input, !enabled);
		Z8.form.field.Control.prototype.setEnabled.call(this, enabled);
	},

	setReadOnly: function(readOnly) {
		if(this.isReadOnly() != readOnly)
			DOM.setReadOnly(this.input, readOnly);
		Z8.form.field.Control.prototype.setReadOnly.call(this, readOnly);
	},

	getRawValue: function(value) {
		return DOM.getValue(this.input);
	},

	setRawValue: function(value) {
		value = this.isEmptyValue(value) ? '' : value;
		DOM.setValue(this.input, value);
		DOM.setAttribute(this.input, 'title', value);
	},

	setTabIndex: function(tabIndex) {
		tabIndex = Z8.form.field.Control.prototype.setTabIndex.call(this, tabIndex);
		DOM.setTabIndex(this.input, tabIndex);
		return tabIndex;
	},

	setLabel: function(label) {
		Z8.form.field.Control.prototype.setLabel.call(this, label);
		this.setPlaceholder(label);
	},

	setPlaceholder: function(placeholder) {
		this.placeholder = placeholder;
		DOM.setAttribute(this.input, 'placeholder', placeholder);
	},

	getInputTag: function() {
		return this.editor ? this.tag : 'div';
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
		if(target.type == 'file')
			return;

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