Z8.define('Z8.form.field.SearchCombobox', {
	extend: 'Z8.form.field.Combobox',

	setValue: function(value) {
		this.callParent(value);
		var record = this.getSelectedRecord();
		this.fireEvent('search', this, record ? record.get(this.name) : '');
	},
	
	setBusy: function(busy) {
		this.getTrigger().setBusy(busy);
	}

});
