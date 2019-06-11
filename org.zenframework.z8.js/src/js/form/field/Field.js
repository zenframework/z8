Z8.define('Z8.form.field.Field', {
	extend: 'Z8.Object',

	mixinId: 'field',

	autoSave: false,
	instantAutoSave: false,

	updatingDependencies: 0,
	suspendCheckChange: 0,

	valid: true,

	initField: function() {
		this.initValue();
	},

	initValue: function(value, displayValue) {
		value = value !== undefined ?  value : this.value;

		this.initializing = true;
		this.suspendCheckChange++;
		this.setValue(value, displayValue);
		this.suspendCheckChange--;
		this.initializing = false;

		this.initialValue = this.originalValue = this.lastValue = this.getValue();
		this.originalDisplayValue = displayValue;
	},

	getName: function() {
		return this.name;
	},

	getRecord: function() {
		return this.record;
	},

	getRecordId: function() {
		return this.record != null ? this.record.id : null;
	},

	setRecord: function(record) {
		this.record = record;
	},

	getValue: function() {
		return this.value;
	},

	getDisplayValue: function() {
		return this.getValue();
	},

	setValue: function(value, displayValue) {
		this.value = value;
		this.validate();
		this.checkChange();
	},

	reset: function() {
		this.setValue(this.originalValue);
	},

	isEqual: function(value1, value2) {
		return String(value1 || null) === String(value2 || null);
	},

	checkChange: function() {
		if(this.suspendCheckChange <= 0) {
			var newValue = this.getValue();
			var oldValue = this.lastValue;
			if(!this.disposed && this.didValueChange(newValue, oldValue)) {
				this.lastValue = newValue;
				this.fireEvent('change', this, newValue, oldValue);
			}
		}
	},

	didValueChange: function(newValue, oldValue) {
		return !this.isEqual(newValue, oldValue);
	},

	setAutoSave: function(autoSave) {
		if(this.autoSave != autoSave) {
			this.autoSave = autoSave;
			if(!this.instantAutoSave)
				this.suspendCheckChange += autoSave ? 1 : -1;
		}
	},

	initEvents: function() {
		DOM.on(this, 'focus', this.onFocusIn, this, true);
		DOM.on(this, 'blur', this.onFocusOut, this, true);
	},

	clearEvents: function() {
		DOM.un(this, 'focus', this.onFocusIn, this, true);
		DOM.un(this, 'blur', this.onFocusOut, this, true);
	},

	onFocusIn: function(event, target) {
		DOM.addCls(this, 'focus');
		this.fireEvent('focusIn', this);
	},

	onFocusOut: function(event, target) {
		var dom = DOM.get(this);
		target = event.relatedTarget;

		if(dom == target || DOM.isParentOf(dom, target))
			return false;

		if(this.autoSave && !this.instantAutoSave) {
			if(this.isValid()) {
				this.suspendCheckChange--;
				this.setValue(this.getValue(), this.getDisplayValue());
				this.suspendCheckChange++;
			} else
				this.initValue(this.originalValue, this.originalDisplayValue);
		}

		DOM.removeCls(this, 'focus');
		this.fireEvent('focusOut', this);
		return true;
	},

	isFocused: function() {
		return DOM.getCls(this).indexOf('focus') != -1;
	},

	isValid: function() {
		return this.valid;
	},

	setValid: function(valid) {
		this.valid = valid;
	},

	validate: function() {
	},

	isDependent: function() {
		var field = this.field;
		return field != null && field.dependency != null || this.dependsOn != null;
	},

	getDependencyField: function() {
		return this.field.dependency;
	},

	hasDependsOnField: function() {
		return this.field != null && this.field.dependsOn != null;
	},

	getDependsOnField: function() {
		return this.field.dependsOn;
	},

	getDependsOnValue: function() {
		return this.dependsOnValue;
	},

	setDependsOnValue: function(value) {
		this.dependsOnValue = value;
	},

	hasDependsOnValue: function() {
		return !Z8.isEmpty(this.dependsOnValue) && this.dependsOnValue != guid.Null;
	},

	addDependency: function(field) {
		if(this.dependencies == null)
			this.dependencies = [];

		field.dependsOn = this;
		this.dependencies.push(field);
	},

	updateDependencies: function(record) {
		if(this.dependencies == null || !this.isActive() || this.updatingDependencies != 0 || this.disposed)
			return;

		this.updatingDependencies++;

		var dependencies = this.dependencies;
		for(var i = 0, length = dependencies.length; i < length; i++) {
			var dependency = dependencies[i];
			if(dependency.updatingDependencies == 0 && this.needsDependencyChange(dependency, record))
				dependency.onDependencyChange(record, this);
		}

		this.updatingDependencies--;
	},

	needsDependencyChange: function(dependency, record) {
		return true;
	},

	onDependencyChange: function(value, control) {
	}
});