Z8.query.Section = Ext.extend(Ext.util.Observable,
{
	header: '',
	controls: [],

	constructor : function(config)
	{
		Ext.apply(this, config);

		for(var i = 0; i < this.controls.length; i++)
		{
			var control = this.controls[i];
			
			if(control.isSection)
			{
				this.controls[i] = new Z8.query.Section(control);
			}
			else
			{
				if(config.hidable)
				{
					control.hidable = true;
				}
				
				this.controls[i] = control;
			}
		}

		Z8.query.Section.superclass.constructor.call(this);
	},

	getAllColumns: function()
	{
		var columns = [];

		for(var i = 0; i < this.controls.length; i++)
		{
			var control = this.controls[i];
			
			if(control.isSection)
			{
				columns = columns.concat(control.getAllColumns());
			}
			else
			{
				columns.push(control);
			}
		}
		
		return columns;
	}
});
