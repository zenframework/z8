Ext.Container.prototype.within = function(event)
{
	if(Ext.Container.superclass.within.call(this, event))
	{
		return true;
	}
	
	if (this.items)
	{
		for(var i = 0; i < this.items.getCount(); i++)
		{
			var item = this.items.get(i);
			
			if(item.within != null && item.within(event))
			{
				return true;
			}
		}
	}

	return false;
};

Ext.Container.prototype.extOnShow = Ext.Container.prototype.onShow;
Ext.Container.prototype.extOnHide = Ext.Container.prototype.onHide;

Ext.Container.prototype.onShow = function()
{
	if(this.autoHide)
	{
		this.hookOuterClicks.defer(100, this);
	}
	
	if(!this.floating)
	{
		Ext.Container.prototype.extOnShow.call(this);
	}
};
	
Ext.Container.prototype.onHide = function()
{
	if(this.autoHide)
	{
		this.unhookOuterClicks();
	}

	if(!this.floating)
	{
		Ext.Container.prototype.extOnHide.call(this);
	}
};

Ext.Container.prototype.hookOuterClicks = function()
{
	Ext.getDoc().on('mousewheel', this.onOuterClick, this);
	Ext.getDoc().on('mousedown', this.onOuterClick, this);
};

Ext.Container.prototype.unhookOuterClicks = function()
{
	Ext.getDoc().un('mousewheel', this.onOuterClick, this);
	Ext.getDoc().un('mousedown', this.onOuterClick, this);
};
	
Ext.Container.prototype.onOuterClick = function(event)
{
	if(this.isVisible() && !this.within(event))
	{
		this.hide();
	}
};
