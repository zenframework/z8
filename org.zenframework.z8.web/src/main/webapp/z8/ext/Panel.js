Ext.Panel.prototype.onShow = function()
{
	if(this.floating)
	{
		this.el.show();
	}
	Ext.Panel.superclass.onShow.call(this);
};

    
Ext.Panel.prototype.onHide = function()
{
	if(this.floating)
	{
		this.el.hide();
	}
	Ext.Panel.superclass.onHide.call(this);
};
