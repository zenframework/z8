Z8.define('Z8.form.Fieldset', {
	extend: 'Z8.Component',
	shortClassName: 'Fieldset',

	mixins: ['Z8.form.field.Field'],

	statics: {
		MarginBottom: .71428571
	},

	isFieldset: true,
	colCount: 1,

	constructor: function(config) {
		this.controls = [];
		this.readOnlyLock = config != null ? config.readOnly : false;
		this.callParent(config);
	},

	getRecord: function() {
		return this.record;
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
		var legend = { cls: 'legend', title: text, cn: icon != null ? [icon, text] : [text] };

		var rows = (this.plain ? [] : [legend]).concat(this.rowsMarkup());

		var cls = DOM.parseCls(this.cls).pushIf('fieldset');

		if(this.plain)
			cls.pushIf('section');

		if(!this.isEnabled())
			cls.pushIf('disabled');

		if(this.isReadOnly())
			cls.pushIf('readonly');

		if(this.name != null)
			cls.pushIf(this.name.replace(/\./g, '-').toLowerCase());

		return { id: this.getId(), cls: cls.join(' '), cn: rows };
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

				var style = control.scrollable ? 'min-height:' + (control.getMinHeight() + Fieldset.MarginBottom) + 'em' : null;

				cn.push(isComponent ? control.htmlMarkup() : control);

				flex = Math.max(flex, controls[controlIndex].flex || 0);

				var width = Math.min(12, columns[columnIndex] * columnWidth);
				cells.push({ cls: 'col-lg-' + width + ' col-md-' + width + ' col-sm-' + width + ' cell', cn: cn, style: style });
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

		var actions = this.actions;

		if(actions != null)
			markup.push(this.actionsMarkup());

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
			var button = new Z8.button.Button({ name: action.id, text: action.text, tooltip: action.description, icon: action.icon, action: action, primary: type == 'primary', success: type == 'success', danger: type == 'danger', handler: this.onAction, scope: this });
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

	setActive: function(active) {
		this.callParent(active);

		var controls = this.controls;
		for(var i = 0, length = controls.length; i < length; i++)
			controls[i].setActive(active);
	},

	focus: function() {
		var controls = this.controls;
		for(var i = 0, length = controls.length; i < length; i++) {
			var control = controls[i];
			if(control.focus != null && control.focus())
				return true;
		}
		return false;
	},

	onAction: function(button) {
		button.setBusy(true);

		var action = button.action;

		if(!Z8.isEmpty(action.parameters))
			this.requestActionParameters(button);
		else
			this.runAction(button);
	},

	requestActionParameters: function(button) {
		this.runAction(button);
	},

	runAction: function(button) {
		var action = button.action;

		var record = this.getRecord();

		var params = {
			request: action.request,
			action: 'action',
			id: action.id,
			records: (record != null && !record.phantom) ? [record.id] : null,
			parameters: action.parameters
		};

		var callback = function(response, success) {
			button.setBusy(false);
			this.onActionComplete(button, record, response, success);
		};

		HttpRequest.send(params, { fn: callback, scope: this });
	},

	onActionComplete: function(button, record, response, success) {
		if(success && this.getRecord() == record) {
			var reloadCallback = function(record, success) {
				button.setBusy(false);
				this.form.loadRecord(record);
			};
			record.reload({ fn: reloadCallback, scope: this });
		}
	}
});
