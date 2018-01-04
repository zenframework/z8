Z8.define('Z8.form.field.Search', {
	extend: 'Z8.form.field.Text',

	triggers: [{ icon: 'fa-search' }],

	searchPending: false,
	lastSearchValue: '',

	initComponent: function() {
		this.callParent();
	},

	isValid: function() {
		return true;
	},

	getValue: function() {
		return this.callParent() || '';
	},

	completeRender: function() {
		this.callParent();
		this.updateTrigger();
	},

	setBusy: function(busy) {
		this.getTrigger().setBusy(busy);
	},

	updateTrigger: function() {
		var trigger = this.getTrigger();

		if(trigger.isBusy())
			return;

		trigger.setIcon(this.searchPending || this.lastSearchValue == '' ? 'fa-search' : 'fa-times');
		trigger.setEnabled(this.searchPending || this.lastSearchValue != '');
	},

	onInput: function(event, target) {
		this.callParent(event, target);

		var value = this.getValue();

		this.searchPending = value != this.lastSearchValue;
		this.updateTrigger();
	},

	search: function() {
		if(!this.getTrigger().isEnabled())
			return;

		if(!this.searchPending)
			this.setValue('');

		var value = this.lastSearchValue = this.getValue();
		this.searchPending = false;
		this.updateTrigger();
		this.fireEvent('search', this, value);
	},

	clear: function() {
		if(!this.getTrigger().isEnabled())
			return false;

		this.setValue('');

		if(this.searchPending && this.lastSearchValue == '') {
			this.searchPending = false;
			this.updateTrigger();
		} else
			this.search();

		return true;
	},

	onTriggerClick: function(trigger) {
		this.search();
	},

	onKeyEvent: function(event, target) {
		var key = event.getKey();

		if(key == Event.ENTER)
			this.search();
		else if(key == Event.ESC)
			return this.clear();
		else {
			this.fireEvent('keyDown', this, event, target);
			return false;
		}

		return true;
	},

	reset: function() {
		this.callParent();
		this.searchPending = false;
		this.lastSearchValue = '';
		this.updateTrigger();
	},

	getFilter: function() {
		if(this.field == null)
			return null;

		var value = this.getValue();
		return !Z8.isEmpty(value) ? { property: this.field.name, operator: Operation.Contains, value: value } : null;
	}
});