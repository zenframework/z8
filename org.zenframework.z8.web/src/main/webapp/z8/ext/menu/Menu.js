Ext.menu.Menu.prototype.autoHide = true;
Ext.menu.Menu.prototype.shadow = 'drop';

Ext.menu.Menu.prototype.within = function(event)
{
	if(Ext.menu.Menu.superclass.within.call(this, event))
	{
		return true;
	}
	
	if(this.items != null)
	{
		for(var i = 0; i < this.items.getCount(); i++)
		{
			var item = this.items.get(i);
				
			if(item.menu != null && item.menu.within(event))
			{
				return true;
			}
		}		
	}
	
	return false;
};