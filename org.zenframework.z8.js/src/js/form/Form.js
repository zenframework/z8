Z8.define('Z8.form.Form', {
	extend: 'Z8.form.Fieldset',

	isForm: true,
	autoSave: false,

	fields: null,
	controls: null,

	fieldsMap: null,

	constructor: function(config) {
		config = config || {};
		if(config.name == null)
			config.cls = DOM.parseCls(config.cls).pushIf('form');
		this.callParent(config);
	},

	initComponent: function() {
		this.callParent();
		this.initControls();
		this.initFields();

		this.setReadOnly(this.readOnly);
	},

	setReadOnly: function(readOnly) {
		this.callParent(readOnly || (!this.topLevel && (this.record == null || !this.record.isEditable())));
	},

	initControls: function() {
		var controls = this.controls;

		this.fields = [];
		this.fieldsMap = {};
		this.controls = [];

		for(var i = 0, length = controls.length; i < length; i++)
			this.initControl(controls[i], this);
	},

	getFields: function() {
		return this.fields;
	},

	getField: function(name) {
		return this.fieldsMap[name];
	},

	initControl: function(control, container) {
		if(control.isForm || control instanceof Z8.form.Form) {
			control = this.initForm(control);
		} else if(control.isFieldGroup || control instanceof Z8.form.field.Group) {
			control = this.initFieldGroup(control);
		} else if(control.isActionGroup || control instanceof Z8.form.action.Group) {
			control = this.initActionGroup(control);
		} else if(control.isSection || control instanceof Z8.form.Fieldset) {
			control = this.initFieldset(control);
		} else if(control.isTabControl || control instanceof Z8.form.Tabs) {
			control = this.initTabControl(control);
		} else if(control instanceof Z8.form.field.Listbox) {
			this.addField(control);
		} else if(control.isListbox) {
			control = Z8.form.Helper.createControl(control);
			this.addField(control);
		} else if(control instanceof Z8.form.field.Control) {
			this.addField(control);
		} else if(control.isText && control.isLink) {
			return;
		} else if(control.isText) {
			control = Z8.form.Helper.createControl(control);
			this.addField(control);
		} else if(control.isAction) {
			control = Z8.form.Helper.createControl(control);
			this.addField(control);
		} else if(control.isCombobox) {
			control = Z8.form.Helper.createControl(control);
			this.addField(control);
		} // else control is a button or some other config - add it as is

		container.add(control);
	},

	initForm: function(form) {
		if(form.isForm) {
			form.autoSave = true;
			form.plain = true;
			var cls = Application.getSubclass(form.ui);
			form = cls != null ? Z8.create(cls, form) : new Z8.form.Form(form);
		}

		this.addField(form);

		var fields = form.fields;
		for(var i = 0, length = fields.length; i < length; i++)
			this.addField(fields[i]);

		return form;
	},

	initFieldGroup: function(group) {
		var controls = group.controls || [];

		if(group.isFieldGroup)
			group = Z8.form.Helper.createFieldGroup(group);
		else
			group.controls = [];

		for(var i = 0, length = controls.length; i < length; i++)
			this.initControl(controls[i], group);

		this.addField(group);

		return group;
	},

	initActionGroup: function(group) {
		var actions = group.actions || [];

		if(group.isActionGroup)
			group = Z8.form.Helper.createActionGroup(group);
		else
			group.actions = [];

		for(var i = 0, length = actions.length; i < length; i++)
			this.initControl(actions[i], group);

		this.addField(group);

		return group;
	},

	initFieldset: function(fieldset) {
		var controls = fieldset.controls || [];

		if(fieldset.isSection)
			fieldset = Z8.form.Helper.createFieldset(fieldset);
		else
			fieldset.controls = [];

		for(var i = 0, length = controls.length; i < length; i++)
			this.initControl(controls[i], fieldset);

		this.addField(fieldset);

		return fieldset;
	},

	initTab: function(tab) {
		var controls = tab.controls || [];

		if(tab.isSection)
			tab = Z8.form.Helper.createTab(tab);
		else
			tab.controls = [];

		for(var i = 0, length = controls.length; i < length; i++)
			this.initControl(controls[i], tab);

		this.addField(tab);
		return tab;
	},

	initTabControl: function(tabControl) {
		var controls = [];
		var tabs = tabControl.tabs || [];

		for(var i = 0, length = tabs.length; i < length; i++) {
			var tab = this.initTab(tabs[i]);
			controls.push(tab);
		}

		var tabControl = new Z8.form.Tabs({ name: tabControl.name, controls: controls, actions: tabControl.actions, colSpan: tabControl.colSpan, readOnly: tabControl.readOnly, flex: tabControl.flex, height: tabControl.height })

		this.addField(tabControl);

		return tabControl;
	},

	addField: function(field) {
		if(field.name == null)
			return;

		this.fields.push(field);
		this.fieldsMap[field.name] = field;
		if(field.displayName != null)
			this.fieldsMap[field.displayName] = field;
		if(field.form == null)
			field.form = this;
	},

	initAutoSave: function(field) {
		if(this.autoSave && !field.isListbox && !field.isFile && !field.isForm && !field.isTabControl && field.form == this) {
			field.setAutoSave(true);
			field.on('change', this.autoSaveCallback, this);
		}
	},

	initDependencies: function(field) {
		if(field.dependenciesInitialized)
			return;

		var dependencies = field.field != null ? field.field.dependencies : field.dependencies;

		if(dependencies == null || (!field.isCombobox && !field.isListbox && !field.isForm))
			return;

		field.dependenciesInitialized = true;
		field.dependencies = null;

		var fieldsMap = this.fieldsMap;
		for(var i = 0, length = dependencies.length; i < length; i++) {
			var dependentField = this.getField(dependencies[i]);
			if(dependentField != null)
				field.addDependency(dependentField);
		}
	},

	initFields: function() {
		var fields = this.fields;
		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			this.initAutoSave(field);
			this.initDependencies(field);
		}
	},

	getUpdateParams: function(record) {
		var link = this.link;
		if(link == null || link.primaryKey == null)
			return null;

		var owner = link.primaryKey;
		var query = link.query.primaryKey;
		record.set(owner, record.get(owner), false, true);
		record.set(query, record.get(link.name), false, true);
		return { link: link.name };
	},

	autoSaveCallback: function(control, newValue, oldValue) {
		if(!this.autoSave || (!control.isValid() && !control.isDependent()))
			return;

		var record = this.record;
		if(record == null)
			return;

		record.beginEdit();

		record.set(control.name, newValue);
		if(control.displayName != null)
			record.set(control.displayName, control.getDisplayValue());

		var params = this.getUpdateParams(record);

		var callback = function(record, success) {
			this.autoSaving = true;

			if(success) {
				this.applyRecordChange(record, control);
				record.endEdit();
				this.updateDependencies(this.record);
			} else {
				control.initValue(control.originalValue, control.originalDisplayValue);
				record.cancelEdit();
			}

			this.autoSaving = false;
		};

		record.update({ fn: callback, scope: this}, params);
	},

	applyRecordChange: function(record, control) {
		var fields = this.getFields();
		var modified = record.getModifiedFields();

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			var name = field.name;
			var displayName = field.displayName;

			if(field == control || !(name in modified || displayName in modified))
				continue;

			var value = record.get(name) || null;
			var displayValue = record != null ? record.get(displayName) : null;

			field.initValue(value, displayValue);
		}
	},

	isMyRecord: function(record) {
		var link = this.link;

		if(record == null || link == null)
			return true;

		return link.owner == (record.getQuery() || '');
	},

	setRecord: function(record) {
		if(!this.isMyRecord(record))
			return;

		var current = this.record;

		if(current != null)
			current.un('change', this.onRecordChange, this);

		this.record = record;

		this.updateDependencies(record);

		if(record != null)
			record.on('change', this.onRecordChange, this);
	},

	onDestroy: function() {
		var record = this.record;
		if(record != null)
			record.un('change', this.onRecordChange, this);
		this.callParent();
	},

	onRecordChange: function(record, modified) {
		if(!this.autoSaving)
			this.applyRecordChange(record, null);

		var updated = {};

		for(var name in modified) {
			var control = this.getField(name);

			if(control == null || !control.isCombobox)
				continue;

			var id = control.getId();
			if(updated[id] == null) {
				control.updateDependencies(control.getSelectedRecord());
				updated[id] = true;
			}
		}
	},

	loadRecord: function(record) {
		var fields = this.fields;
		var current = this.record;

		this.setRecord(record);

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];

			if(field.isForm && !field.isMyRecord(record))
				continue;

			var form = field.form;
			if(form != this && !form.isMyRecord(record))
				continue;

			if(record != null) {
				var value = record.get(field.name);
				var displayValue = field.displayName != null ? record.get(field.displayName) : null;
				field.initValue(value, displayValue);
			} else
				field.initValue(null, null);

			field.setRecord(record);
		}

		if(!this.readOnlyLock)
			this.setReadOnly(record == null || !record.isEditable());

		this.fireEvent('change', this, this.record, current);
	},

	needsDependencyChange: function(dependency, record) {
		return !dependency.isForm || dependency.dependencies != null || !dependency.isMyRecord(record);
	},

	onDependencyChange: function(record, control) {
		this.loadRecord(record);
	}
});
