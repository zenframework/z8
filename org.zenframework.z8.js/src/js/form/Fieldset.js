Z8.define('Z8.form.Fieldset', {
	extend: 'Z8.Component',
	shortClassName: 'Fieldset',

	mixins: ['Z8.form.field.Field'],

	statics: {
		MarginBottom: 1.71428572
	},

	isFieldset: true,
	colCount: 1,

	constructor: function(config) {
		this.controls = [];
		this.readOnlyLock = config != null ? config.readOnly : false;
		this.callParent(config);
	},

	add: function(control) {
		this.controls.push(control);
	},

	getRecord: function() {
		return this.record;
	},

	getControls: function() {
		return this.controls;
	},

	setControls: function(controls) {
		this.controls = controls;
	},

	getControl: function(name) {
		return this.traverseControls(this.controls, name);
	},

	traverseControls: function(controls, name) {
		if(controls == null)
			return null;

		for(var control of controls) {
			if(control.name == name || control.displayName == name)
				return control;
			var control = this.traverseControls(control.controls, name);
			if(control != null)
				return control;
		}

		return null;
	},

	getCls: function() {
		var cls = Z8.Component.prototype.getCls.call(this);

		if(this.plain)
			cls.pushIf('section');
		if(!this.isEnabled())
			cls.pushIf('disabled');
		if(this.isReadOnly())
			cls.pushIf('readonly');
		if(this.name != null)
			cls.pushIf(this.name.replace(/\./g, '-').toLowerCase());

		return cls.pushIf('fieldset');
	},

	subcomponents: function() {
		return this.controls;
	},

	htmlMarkup: function() {
		this.setReadOnly(this.isReadOnly());

		if(this.icon != null) {
			var icon = DOM.parseCls(this.icon).pushIf('fa').join(' ');
			var icon = { tag: 'i', cls: icon };
		}

		var text = this.header || '';
		var legend = { cls: 'legend', title: text, cn: icon != null ? [icon, text] : [text] };

		var rows = (this.plain ? [] : [legend]).concat(this.rowsMarkup());

		return { id: this.getId(), cls: this.getCls().join(' '), cn: rows };
	},

	rowsMarkup: function() {
		var markup = [];

		var rows = this.getRows();

		var controls = this.controls;
		var rowsCount = rows.length;
		var columnWidth = Math.floor(12 / Math.max(1, this.colCount));

		var controlIndex = 0;

		for(var rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
			var cells = [];

			var columns = rows[rowIndex];

			var flex = 0;
			var totalWidth = 0;

			for(var columnIndex = 0, columnsCount = columns.length; columnIndex < columnsCount; columnIndex++) {
				var cn = [];
				var control = controls[controlIndex];
				var isComponent = control.isComponent;
				var isAction = control.isAction;

				var style = control.scrollable ? 'min-height:' + (control.getMinHeight() + Fieldset.MarginBottom) + 'em' : null;

				cn.add(isComponent ? control.htmlMarkup() : control);

				flex = Math.max(flex, controls[controlIndex].flex || 0);

				var width = Math.min(12, columns[columnIndex] * columnWidth);
				cells.push({ cls: 'col-lg-' + width + ' col-md-' + width + ' col-sm-' + width + ' cell' + (isAction ? ' action' : ''), cn: cn, style: style });
				totalWidth += width;

				controlIndex++;
			}

			if(totalWidth < 12) {
				var padding = 12 - totalWidth;
				cells.push({ cls: 'col-lg-' + padding + ' col-md-' + padding + ' col-sm-' + padding + ' cell' });
			}

			var cls = 'row' + (flex != 0 ? ' flex-' + flex : '');
			var row = { cls: cls, cn: cells };

			markup.push(row);
		}

		return markup;
	},

	getRows: function() {
		var controls = this.controls;

		var row = [];
		var rows = [row];

		var colCount = this.colCount;

		var totalColSpan = 0;

		for(var i = 0, length = controls.length; i < length; i++) {
			var colSpan = controls[i].colSpan || 1;
			totalColSpan += colSpan;
			if(totalColSpan > colCount && i != 0) {
				row = [colSpan];
				totalColSpan = colSpan;
				rows.push(row);
			} else
				row.push(colSpan);
		}

		return rows[0].length != 0 ? rows : [];
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this, !enabled, 'disabled');
		this.callParent(enabled);
	},

	isReadOnly: function() {
		return this.readOnly;
	},

	setReadOnly: function(readOnly) {
		for(var control of this.controls) {
			if(!control.readOnlyLock && control.setReadOnly != null)
				control.setReadOnly(readOnly);
		}

		this.readOnly = readOnly;
		DOM.swapCls(this, readOnly, 'readonly');
	},

	setActive: function(active) {
		this.callParent(active);

		for(var control of this.controls) {
			if(control.isComponent)
				control.setActive(active);
		}
	},

	focus: function() {
		for(var control of this.controls) {
			if(control.focus != null && control.focus())
				return true;
		}
		return false;
	},

	isRequired: function() {
		for(var control of this.controls) {
			if(control.required)
				return true;
		}
		return false;
	}
});
