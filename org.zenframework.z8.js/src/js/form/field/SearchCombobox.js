Z8.define('Z8.form.field.SearchCombobox', {
	extend: 'ComboBox',
	shortClassName: 'SearchComboBox',

	setValue: function(value) {
		ComboBox.prototype.setValue.call(this, value);
		var record = this.getSelectedRecord();
		this.fireEvent('search', this, record ? record.get(this.name) : '');
	},
	
	setBusy: function(busy) {
		this.getTrigger().setBusy(busy);
	}

});
