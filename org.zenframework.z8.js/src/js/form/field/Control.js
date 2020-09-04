Z8.define('Z8.form.field.Control', {
	extend: 'Component',
	shortClassName: 'Control',

	mixins: ['Z8.form.field.Field'],

	tabIndex: 0,

	label: null, // string | { text: String, align: 'left' | 'right' | 'top', icon: String, iconAlign: 'left' | 'right', textAlign: 'left' | 'right' | 'center' };

	readOnly: false,
	readOnlyLock: false,

	initComponent: function() {
		Component.prototype.initComponent.call(this);

		this.initField();
		this.readOnlyLock = this.isReadOnly();
		this.enabledLock = !this.isEnabled();
		this.label = String.isString(this.label) ? { text: this.label } : this.label;
	},

	subcomponents: function() {
		return [this.labelTools];
	},

	htmlMarkup: function() {
		var box = [{ cls: 'box', cn: this.controlMarkup() }];

		var label = this.label;

		if(label) {
			var align = label.align;
			var icon = label.icon != null ? { tag: 'i', cls: this.getIconCls(label.icon).join(' ') } : null;
			var title = label.title || label.text;
			var text = String.htmlText(label.text);

			var cn = [{ cls: 'text', cn: icon != null ? [icon, text] : [text] }];

			if(label.tools != null) {
				var tools = this.labelTools = label.tools;
				cn.add(tools.htmlMarkup());
			}

			var style = label.width != null ? 'min-width:' + label.width + ';width:' + label.width + ';' : '';
			label = { tag: 'span', name: 'label', cls: this.getLabelCls().join(' '), title: title || '', style: style, cn: cn };

			align == 'right' ? box.add(label) : box.insert(label, 0);
		}

		return { id: this.getId(), cls: this.getCls().join(' '), cn: box };
	},

	completeRender: function() {
		Component.prototype.completeRender.call(this);

		var label = this.label = this.selectNode('span[name=label]');
		if(label != null) {
			this.labelText = DOM.selectNode(label, '.text');
			this.labelIcon = DOM.selectNode(label, '.text>.icon');
		}

		DOM.on(this, 'mouseDown', this.onMouseDown, this);

		this.mixins.field.initEvents.call(this);
	},

	onDestroy: function() {
		this.mixins.field.clearEvents.call(this);

		DOM.un(this, 'mousedown', this.onMouseDown, this);

		this.label = this.labelText = this.labelIcon = null;

		Component.prototype.onDestroy.call(this);
	},

	setEnabled: function(enabled) {
		Component.prototype.setEnabled.call(this, enabled);

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

	getReadOnly: function() {
		return this.isReadOnly();
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
		this.setRawValue(this.valueToRaw(value));
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

	setValid: function(valid) {
		this.mixins.field.setValid.call(this, valid);
		DOM.swapCls(this, !valid, 'invalid');
		Z8.callback(this.validation, this, valid);
	},

	validate: function() {
		var isEmpty = this.isEmpty();
		DOM.swapCls(this, isEmpty, 'empty');
		this.setValid(!isEmpty || !this.isRequired());
	},

	getCls: function() {
		var cls = Component.prototype.getCls.call(this);

		if(!this.isVisible())
			cls.pushIf('display-none');
		if(!this.isEnabled())
			cls.pushIf('disabled');
		if(this.isReadOnly())
			cls.pushIf('readonly');
		if(this.isEmpty())
			cls.pushIf('empty');
		if(!this.isValid())
			cls.pushIf('invalid');
		if(this.isScrollable())
			cls.pushIf('scrollable');

		cls.pushIf('label-' + (this.label ? this.label.align || 'top' : 'none'));

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
			cls.pushIf('no-text');

		return cls;
	},

	setIcon: function(cls) {
		cls = this.getIconCls(cls);
		DOM.setCls(this.labelIcon, cls);
	},

	setLabel: function(label) {
		DOM.setValue(this.labelText, label);
	},

	onMouseDown: function(event, target) {
		if(target == DOM.get(this) || DOM.isParentOf(this.label, target)) {
			event.stopEvent();
			this.focus();
		}
	}
});