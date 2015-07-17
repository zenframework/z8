Ext.data.JsonReader.prototype.extReadRecords = Ext.data.JsonReader.prototype.readRecords;

Ext.data.JsonReader.prototype.readRecords = function(o)
{
	var result = Ext.data.JsonReader.prototype.extReadRecords.call(this, o);
	result.raw = o;
	return result;
};

Z8.data.JsonWriter = Ext.extend(Ext.data.JsonWriter,
{
	constructor : function(config)
	{
		Z8.data.JsonWriter.superclass.constructor.call(this, config);    
	},

	getFieldById: function(id)
	{
		for(var i = 0; i < this.meta.fields.length; i++)
		{
			if(this.meta.fields[i].id == id)
			{
				return this.meta.fields[i];
			}
		}
		
		return null;
	},
	
	render : function(params, baseParams, data)
	{
		if (this.encode === true)
		{
			Ext.apply(params, baseParams);
			
			var visitorFn = function(name, value)
			{
				if(Ext.isDate(value))
				{
					var field = this.getFieldById(name);

					if(field.serverType == Z8.ServerTypes.Datetime)
					{
						value.isDatetime = true;
					}
				}
			};

			if(Ext.isArray(data))
			{
				for(var i = 0; i < data.length; i++)
				{
					Ext.iterate(data[i], visitorFn, this);
				}
			}
			else
			{
				Ext.iterate(data, visitorFn, this);
			}
			
			
			params[this.meta.root] = Ext.encode(data);
		}
		else
		{
			Z8.data.JsonWriter.superclass.render.call(this, params, baseParams, data);    
		}
	}
});