Ext.data.Record.prototype.encode = function() {
	var data = {};
	var fields = this.store.fields;

	data[this.store.idProperty] = this.id;

	for(var i = 0; i < fields.getCount(); i++) {
		var field = fields.get(i);
		var value = this.data[field.id];

		if(field.serverType == Z8.ServerTypes.Date || field.serverType == Z8.ServerTypes.Datetime)
			data[field.id] = Ext.isDate(value) ? Z8.Format.isoDate(value) : '';
		else
			data[field.id] = this.data[field.id];
	}
	
	return Ext.encode(data);
}