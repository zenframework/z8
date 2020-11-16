Z8.define('Z8.form.field.Text', {
	extend: 'Control',
	shortClassName: 'TextBox',

	triggers: null,

	placeholder: '',

	tag: 'input',
	autocomplete: 'off',

	editor: true,

	initComponent: function() {
		this.triggers = this.triggers != null ? (Array.isArray(this.triggers) ? this.triggers : [this.triggers]) : [];
		Control.prototype.initComponent.call(this);

		this.initTriggers();
	},

	getCls: function() {
		var cls = Control.prototype.getCls.call(this);
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
		var input = {
			tag: tag,
			name: this.getInputName(),
			cls: inputCls,
			tabIndex: this.getTabIndex(),
			spellcheck: false,
			type: this.password ? 'password' : 'text',
			title: this.tooltip || '',
			placeholder: this.placeholder,
			autocomplete: this.autocomplete,
			value: tag == 'input' ? value : null,
			html: tag != 'input' ? value : null
		};

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

			for(var i = 0, length = triggers.length; i < length; i++) {
				var trigger = this.initTrigger(triggers[i]);
				var cls = DOM.parseCls(trigger.cls).pushIf('trigger-' + (length - i));
				trigger.cls = cls.join(' ');
				trigger.enabled = this.isEnabled();

				result.push(trigger.htmlMarkup());
			}
		}

		return result;
	},

	subcomponents: function() {
		return Control.prototype.subcomponents.call(this).concat(this.triggers);
	},

	completeRender: function() {
		Control.prototype.completeRender.call(this);

		DOM.on(this, 'click', this.onClick, this);
		DOM.on(this, 'keyDown', this.onKeyDown, this);

		var input = this.input = this.selectNode(this.getInputTag() + '[name=' + this.getInputName() + ']');

		if(this.editor)
			DOM.on(input, 'input', this.onInput, this);
	},

	onDestroy: function() {
		DOM.un(this.input, 'input', this.onInput, this);
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);

		this.input = null;

		Control.prototype.onDestroy.call(this);
	},

	initTriggers: function () {
		var triggers = this.triggers;
		this.triggers = [];
		for (var i = 0, length = triggers.length; i < length; i++) {
			this.triggers.push(this.initTrigger(triggers[i]));
		}
	},

	initTrigger: function (trigger) {
		if (trigger != null && !trigger.isComponent) {
			trigger = new Z8.button.Trigger({
				cls: trigger.cls,
				tooltip: trigger.tooltip,
				icon: trigger.icon,
				handler: trigger.handler,
				scope: trigger.scope
			});
		}
		return trigger;
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this.input, !enabled, 'disabled');
		DOM.setDisabled(this.input, !enabled);

		for(var trigger of this.triggers) {
			if(trigger.isComponent)
				trigger.setEnabled(enabled);
		}

		Control.prototype.setEnabled.call(this, enabled);
	},

	setReadOnly: function(readOnly) {
		if(this.isReadOnly() != readOnly)
			DOM.setReadOnly(this.input, readOnly);
		Control.prototype.setReadOnly.call(this, readOnly);
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
		tabIndex = Control.prototype.setTabIndex.call(this, tabIndex);
		DOM.setTabIndex(this.input, tabIndex);
		return tabIndex;
	},

	setLabel: function(label) {
		Control.prototype.setLabel.call(this, label);
		this.setPlaceholder(label);
	},

	getPlaceholder: function() {
		return this.placeholder;
	},

	setPlaceholder: function(placeholder) {
		this.placeholder = placeholder;
		DOM.setAttribute(this.input, 'placeholder', placeholder);
	},

	getInputName: function () {
		return this.getId() + '-input';
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

		for(var trigger of this.triggers) {
			if(DOM.isParentOf(trigger, target) && trigger.isEnabled()) {
				var handler = trigger.handler;
				return handler != null ? handler.call(trigger.scope, trigger) : this.onTriggerClick(trigger);
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