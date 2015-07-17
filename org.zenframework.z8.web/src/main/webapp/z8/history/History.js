Z8.History = {
		
	hashMap: new Ext.util.MixedCollection(),
	
	init: function()
	{
		Ext.History.init();
		Ext.History.on('change', this.onChange, this);
	},
	
	add: function(string)
	{
		var hash = 'h' + string.hashCode();
		this.hashMap.add(hash, string);
		Ext.History.add(hash);
	},

	onChange: function(string)
	{
		if(Z8.TaskManager.current)
		{
			if(string)
			{
				var hash;
				var pairs = string.split('&');
				var d = decodeURIComponent;

				Ext.each(pairs, function(pair) {
					pair = pair.split('=');
					hash = d(pair[0]);
				});

	        	task = this.hashMap.get(hash);
	        	
	        	if(task != Z8.TaskManager.current)
	        	{
	        		Z8.viewport.open({id: task});
	        	}
	     
	        }
			else
			{
	        	if('desktopId' != Z8.TaskManager.current)
	        	{
	        		Z8.viewport.open({id: 'desktopId'});
	        	}
	        }
    	}
	}
};