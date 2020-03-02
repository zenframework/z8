Z8.define('Z8.form.field.Control', {
	extend: 'Z8.Component',

	mixins: ['Z8.form.field.Field'],

	tabIndex: 0,

	label: null, // string | { text: String, align: 'left' | 'right' | 'top', icon: String, iconAlign: 'left' | 'right', textAlign: 'left' | 'right' | 'center' };

	readOnly: false,
	readOnlyLock: false,

	initComponent: function() {
		this.callParent();
		this.initField();
		this.readOnlyLock = this.isReadOnly();
		this.enabledLock = !this.isEnabled();
		this.label = String.isString(this.label) ? { text: this.label } : this.label;
	},

	subcomponents: function() {
		return [this.labelTextControl, this.labelTools];
	},

	htmlMarkup: function() {
		var controlMarkup = this.controlMarkup();
		controlMarkup = Array.isArray(controlMarkup) ? controlMarkup : [controlMarkup];

		var label = this.label;

		if(label) {
			var align = label.align;
			var icon = label.icon != null ? { tag: 'i', cls: this.getIconCls(label.icon).join(' ') } : null;
			var labelText = label.text;
			var isControl = labelText != null && typeof labelText == 'object';
			var control = this.labelTextControl = isControl ? labelText : null;
			var title = label.title || (isControl ? '' : labelText);
			var text = isControl ? (control.htmlMarkup != null ? control.htmlMarkup() : control) : String.htmlText(labelText);

			var cn = [{ cls: 'text' + (control != null ? ' has-control' : ''), cn: icon != null ? [icon, text] : [text] }];

			if(label.tools != null) {
				var tools = this.labelTools = label.tools;
				cn.add(tools.htmlMarkup());
			}

			var style = label.width != null ? 'min-width:' + label.width + ';width:' + label.width + ';' : '';
			label = { tag: 'span', name: 'label', cls: this.getLabelCls().join(' '), title: title || '', style: style, cn: cn };

			align == 'right' ? controlMarkup.add(label) : controlMarkup.insert(label, 0);
		}

		return { id: this.getId(), cls: this.getCls().join(' '), cn: controlMarkup };
	},

	completeRender: function() {
		this.callParent();

		var label = this.label = this.selectNode('span[name=label]');
		if(label != null) {
			this.labelText = DOM.selectNode(label, '.text');
			this.labelIcon = DOM.selectNode(label, '.text>.icon');
		}

		DOM.on(label, 'mouseDown', this.onLabelMouseDown, this);

		this.mixins.field.initEvents.call(this);
	},

	onDestroy: function() {
		this.mixins.field.clearEvents.call(this);

		DOM.un(this.label, 'mousedown',this.onLabelMouseDown, this);

		this.label = this.labelText = this.labelIcon = null;

		this.callParent();
	},

	setEnabled: function(enabled) {
		this.callParent(enabled);

		DOM.swapCls(this, !enabled, 'disabled');
		DOM.swapCls(this.label, !enabled, 'disabled');

		this.updateTools();
	},

	isLocked: function() {
		return this.readOnlyLock;
	},

	isReadOnly: function() {
		return this.readOnly;
	},

	setReadOnly: function(readOnly) {
		this.readOnly = readOnly;

		DOM.swapCls(this, readOnly, 'readonly');
		DOM.swapCls(this.label, readOnly, 'readonly');

		this.updateTools();
	},

	isScrollable: function() {
		return this.scrollable;
	},

	updateTools: function() {
		DOM.swapCls(this.labelTools, this.isReadOnly() || !this.isEnabled(), 'display-none');
	},

	getRawValue: function() {
	},

	setRawValue: function(value) {
	},

	setValue: function(value, displayValue) {
		this.mixins.field.setValue.call(this, value, displayValue);
		value = this.valueToRaw(value);
		this.setRawValue(value);
	},

	isEqualValues: function(value1, value2) {
		return String(value1) == String(value2);
	},

	valueToRaw: function(value) {
		return value;
	},

	rawToValue: function(value) {
		return value;
	},

	isRequired: function() {
		return this.required;
	},

	isEmptyValue: function(value) {
		return Z8.isEmpty(value);
	},

	setValid: function(valid) {
		this.mixins.field.setValid.call(this, valid);
		DOM.swapCls(this, !valid, 'invalid');
		Z8.callback(this.validation, this, valid);
	},

	validate: function() {
		var value = this.getValue();
		this.setValid(!this.isEmptyValue(value) || !this.isRequired());
	},

	getCls: function() {
		var cls = Z8.Component.prototype.getCls.call(this);

		if(!this.isVisible())
			cls.pushIf('display-none');
		if(!this.isEnabled())
			cls.pushIf('disabled');
		if(this.isReadOnly())
			cls.pushIf('readonly');
		if(!this.isValid())
			cls.pushIf('invalid');
		if(this.isScrollable())
			cls.pushIf('scrollable');

		var label = this.label;
		if(!label)
			cls.pushIf('label-none');
		else if(label && label.align != null)
			cls.pushIf('label-' + label.align);

		return cls.pushIf('control-group');
	},

	getIconCls: function(cls) {
		return cls != null ? DOM.parseCls(cls).pushIf('fa', 'fa-fw', 'icon') : null;
	},

	getLabelCls: function() {
		var label = this.label;

		if(label == null)
			return null;

		var cls = DOM.parseCls(this.labelCls).pushIf('label');

		if(!this.isEnabled())
			cls.pushIf('disabled');
		if(this.isReadOnly())
			cls.pushIf('readonly');
		if(label.textAlign != null)
			cls.push('text-' + label.textAlign);
		if(label.tools != null)
			cls.push('toolbar');
		if(Z8.isEmpty(label.text))
			cls.pushIf('empty');

		return cls;
	},

	setIcon: function(cls) {
		cls = this.getIconCls(cls);
		DOM.setCls(this.labelIcon, cls);
	},

	setLabel: function(label) {
		DOM.setValue(this.labelText, label);
	},

	onLabelMouseDown: function(event, target) {
		if(target == DOM.get(this.label) || target == this.labelText) {
			if(this.labelTextControl == null)
				event.stopEvent();
			this.focus();
		}
	}
});