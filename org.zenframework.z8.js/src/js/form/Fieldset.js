Z8.define('Z8.form.Fieldset', {
	extend: 'Z8.Component',
	shortClassName: 'Fieldset',

	statics: {
		MarginTop: .71428571,
		MarginBottom: .71428571,

		PaddingTop: 1.07142857,
		PaddingBottom: 0,

		Border: .07142857,

		extraHeight: function() {
			return Fieldset.MarginTop + Fieldset.MarginBottom + Fieldset.PaddingTop + Fieldset.PaddingBottom + 2 * Fieldset.Border;
		}
	},

	isFieldset: true,
	colCount: 1,

	constructor: function(config) {
		this.controls = [];
		this.readOnlyLock = config != null ? config.readOnly : false;
		this.callParent(config);
	},

	getControls: function() {
		return this.controls;
	},

	getControl: function(name) {
		return this.traverseControls(this.controls, name);
	},

	traverseControls: function(controls, name) {
		if(controls == null)
			return null;

		for(var i = 0, length = controls.length; i < length; i++) {
			var control = controls[i];
			if(control.name == name || control.displayName == name)
				return control;
			var control = this.traverseControls(control.controls, name);
			if(control != null)
				return control;
		}

		return null;
	},

	getBoxMinHeight: function() {
		var minHeight = this.getMinHeight();
		return minHeight != 0 ? minHeight + (this.plain ? 0 : Fieldset.extraHeight()) : 0;
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

		var text = this.legend || '';
		var legend = { cls: 'legend', cn: icon != null ? [icon, text] : [text] };

		var rows = (this.plain ? [] : [legend]).concat(this.rowsMarkup());

		var cls = DOM.parseCls(this.cls).pushIf('fieldset', this.plain ? 'section' : '',
			!this.isEnabled() ? 'disabled' : '', this.isReadOnly() ? 'readonly' : '', this.flex ? 'flexed' : '').join(' ');

		return { id: this.getId(), cls: cls, cn: rows };
	},

	rowsMarkup: function() {
		var markup = [];

		var rows = this.getRows();

		var controls = this.controls;
		var rowsCount = rows.length;
		var columnWidth = Math.floor(12 / Math.max(1, this.colCount));

		var minHeight = 0;
		var unitHeight = Ems.UnitHeight;
		var unitSpacing = Ems.UnitSpacing;
		var defaultRowHeight = unitHeight + unitSpacing;

		var controlIndex = 0;

		for(var rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
			var cells = [];

			var columns = rows[rowIndex];

			var flex = 0;
			for(var columnIndex = 0, columnsCount = columns.length; columnIndex < columnsCount; columnIndex++)
				flex = Math.max(flex, controls[controlIndex].flex || 0);

			var colFlexCls = flex != 0 ? ' flexed' : '';

			var totalWidth = 0;
			var rowMinHeight = 0;

			for(var columnIndex = 0, columnsCount = columns.length; columnIndex < columnsCount; columnIndex++) {
				var cn = [];
				var control = controls[controlIndex];

				cn.push(control.htmlMarkup != null ? control.htmlMarkup() : control);

				rowMinHeight = Math.max(rowMinHeight, control.isComponent ? control.getBoxMinHeight() : 0);

				var width = Math.min(12, columns[columnIndex] * columnWidth);
				cells.push({ cls: 'col-lg-' + width + ' col-md-' + width + ' col-sm-' + width + ' cell' + colFlexCls, cn: cn });
				totalWidth += width;

				controlIndex++;
			}

			if(totalWidth < 12) {
				var padding = 12 - totalWidth;
				cells.push({ cls: 'col-lg-' + padding + ' col-md-' + padding + ' col-sm-' + padding + ' cell' + colFlexCls });
			}

			minHeight += rowMinHeight || defaultRowHeight;

			var cls = 'row' + (flex != 0 ? ' flex-' + flex : '');
			var row = { cls: cls, cn: cells };
			if(rowMinHeight != 0)
				row.style = 'min-height: ' + rowMinHeight + 'em';

			markup.push(row);
		}

		var actions = this.actions;

		if(actions != null) {
			markup.push(this.actionsMarkup());
			minHeight += defaultRowHeight;
		}

		if(this.minHeight !== false)
			this.minHeight = minHeight;

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

	actionsMarkup: function() {
		var actions = this.actions;
		var controls = this.controls;

		var markup = [];
		for(var i = 0, length = actions.length; i < length; i++) {
			var action = actions[i];
			var type = action.type;
			var button = new Z8.button.Button({ name: action.id, text: action.text, tooltip: action.description, icon: action.icon, action: action, primary: type == 'primary', success: type == 'success', danger: type == 'danger' });
			controls.push(button);
			markup.insert(button.htmlMarkup());
		}

		var cell = { cls: 'col-lg-12 col-md-12 col-sm-12 cell actions', cn: markup };
		return { cls: 'row', cn: [cell] };
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this, !enabled, 'disabled');
		this.callParent(enabled);
	},

	isReadOnly: function() {
		return this.readOnly;
	},

	setReadOnly: function(readOnly) {
		var controls = this.controls;

		for(var i = 0, length = controls.length; i < length; i++) {
			var control = controls[i];

			if(!control.readOnlyLock && control.setReadOnly != null)
				control.setReadOnly(readOnly);
		}

		this.readOnly = readOnly;
		DOM.swapCls(this, readOnly, 'readonly');
	},

	focus: function() {
		var controls = this.controls;
		for(var i = 0, length = controls.length; i < length; i++) {
			var control = controls[i];
			if(control.focus != null && control.focus())
				return true;
		}
		return false;
	}
});
