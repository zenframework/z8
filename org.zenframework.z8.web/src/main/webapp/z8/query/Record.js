Ext.data.Record.prototype.encode = function() {
	var data = {};
	var fields = this.store.fields;

	data[this.store.idProperty] = this.id;

	for(var i = 0; i < fields.getCount(); i++) {
		var field = fields.get(i);
		var value = this.data[field.name];

		if(field.serverType == Z8.ServerTypes.Date || field.serverType == Z8.ServerTypes.Datetime)
			data[field.name] = Ext.isDate(value) ? Z8.Format.isoDate(value) : '';
		else
			data[field.name] = this.data[field.name];
	}
	
	return Ext.encode(data);
}