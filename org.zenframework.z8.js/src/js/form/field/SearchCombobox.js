Z8.define('Z8.form.field.SearchCombobox', {
	extend: 'Z8.form.field.Combobox',

	setValue: function(value) {
		this.callParent(value);
		this.fireEvent('search', this, value);
	},
	
	setBusy: function(busy) {
		this.getTrigger().setBusy(busy);
	}

});
