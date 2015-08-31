Z8.PageManager =
{
	regBeforeUnload: function()
	{
		window.onbeforeunload = function(e)
		{
			if(Z8.viewport != null)
			{
				var message = 'Приложение может содержать несохраненные данные...';
				
				e = e || window.event;
				
				if(e)
				{
					e.returnValue = message;
				}
				return message;
			}
		}
	},
	
	unregBeforeUnload: function()
	{
		window.onbeforeunload = null;
	}
		
};