Z8.define('Z8.form.field.SearchText', {
	extend: 'Z8.form.field.Text',
	shortClassName: 'SearchTextBox',

	triggers: { icon: '' },

	confirm: true,
	searchPending: false,
	lastSearchValue: '',

	constructor: function(config) {
		config = config || {};
		config.confirm = config.confirm !== false;
		config.searchIcon =  config.searchIcon || 'fa-search';
		config.clearIcon = config.clearIcon || 'fa-times';

		Z8.form.field.Text.prototype.constructor.call(this, config);
	},

	initTriggers: function (){
		var triggers = this.triggers;
		triggers.push({ icon: 'fa-copy', tooltip: 'Вставить данные из выбранной записи', handler: this.getSelection, scope: this });

		TextBox.prototype.initTriggers.call(this);
	},

	isValid: function() {
		return true;
	},

	getValue: function() {
		return Z8.form.field.Text.prototype.getValue.call(this) || '';
	},

	completeRender: function() {
		Z8.form.field.Text.prototype.completeRender.call(this);
		this.updateTrigger();
	},

	setBusy: function(busy) {
		this.triggers[0].setBusy(busy);
	},

	updateTrigger: function() {
		var trigger = this.triggers[0];
		if(trigger.isBusy())
			return;

		if(this.confirm) {
			trigger.setIcon(this.searchPending || this.lastSearchValue.isEmpty() ? this.searchIcon : this.clearIcon);
			trigger.setEnabled(this.searchPending || this.lastSearchValue != '');
		} else
			trigger.setIcon(this.clearIcon);
	},

	onInput: function(event, target) {
		Z8.form.field.Text.prototype.onInput.call(this, event, target);

		var value = this.getValue();

		if(this.confirm) {
			this.searchPending = value != this.lastSearchValue;
			this.updateTrigger();
		} else
			this.search();
	},

	show: function(show) {
		Z8.form.field.Text.prototype.show.call(this, show);
		this.reset('');
	},

	search: function() {
		if(!this.triggers[0].isEnabled())
			return;

		if(this.confirm && !this.searchPending)
			this.setValue('');

		var value = this.lastSearchValue = this.getValue();
		this.searchPending = false;
		this.updateTrigger();

		if(this.searchHandler != null)
			Z8.callback(this.searchHandler, this.scope, this, value);

		this.fireEvent('search', this, value);
	},

	clear: function() {
		if(!this.triggers[0].isEnabled())
			return false;

		this.setValue('');

		if(this.searchPending && this.lastSearchValue.isEmpty()) {
			this.searchPending = false;
			this.updateTrigger();
		} else
			this.search();

		return true;
	},

	cancel: function() {
		this.setValue('');

		if(this.cancelHandler != null)
			Z8.callback(this.cancelHandler, this.scope, this);

		this.fireEvent('cancel', this);

		return true;
	},

	getSelection: function() {
		if (this.list == null)
			return;
		var currentItem = this.list.getCurrentItem();
		if (currentItem == null)
			return;
		var record = currentItem.getRecord();
		if (record != null) {
			this.setValue(record.get(this.field.name));
			this.searchPending = this.value != this.lastSearchValue;
			this.updateTrigger();
		}
	},

	onTriggerClick: function(trigger) {
		this.confirm ? this.search() : this.cancel();
	},

	onKeyEvent: function(event, target) {
		var key = event.getKey();

		switch(key) {
		case Event.ENTER:
			if(this.confirm)
				this.search();
			return true;
		case Event.ESC:
			return this.confirm || this.getValue() != '' ? this.clear() : this.cancel();
		}

		return false;
	},

	reset: function() {
		Z8.form.field.Text.prototype.reset.call(this);
		this.searchPending = false;
		this.lastSearchValue = '';
		this.updateTrigger();
	},

	getFilter: function() {
		if(this.field == null)
			return null;

		var value = this.getValue();
		return !Z8.isEmpty(value) ? { property: this.field.name, operator: this.field.operator || Operator.Contains, value: value } : null;
	}
});