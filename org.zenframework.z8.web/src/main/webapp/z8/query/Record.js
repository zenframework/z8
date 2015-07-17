Ext.data.Record.prototype.encode = function()
{
	var data = {};
	var fields = this.store.fields;
	
	data[this.store.idProperty] = this.id;
	
	for(var i = 0; i < fields.getCount(); i++)
	{
		var field = fields.get(i);
		
		if(field.serverType == Z8.ServerTypes.Date)
		{
			data[field.id] = Ext.util.Format.date(this.data[field.id], Z8.Format.Date);
		}
		else if(field.serverType == Z8.ServerTypes.Datetime)
		{
			data[field.id] = Ext.util.Format.date(this.data[field.id], Z8.Format.Datetime);
		}
		else
		{
			data[field.id] = this.data[field.id];
		}
	}
	
	return Ext.encode(data);
}