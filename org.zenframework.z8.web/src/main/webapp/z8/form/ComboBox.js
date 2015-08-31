Ext.form.ComboBox.prototype.within = function(event)
{
	return event.within(this.wrap) || event.within(this.list);
};

Ext.form.ComboBox.prototype.getParentZIndex = function()
{
	var zindex;
	
	if(this.ownerCt)
	{
		this.findParentBy(function(ct)
		{
			zindex = parseInt(ct.getPositionEl().getStyle('z-index'), 10);
			return !!zindex;
		});
	}
	return zindex;
};

Ext.form.ComboBox.prototype.getZIndex = function(listParent)
{
	var zindex = this.getParentZIndex();

	if(zindex == null)
	{
		listParent = listParent || Ext.getDom(this.getListParent() || Ext.getBody());
    	zindex = parseInt(Ext.fly(listParent).getStyle('z-index'), 10);
	}
	
	return (zindex || 12000) + 5;
};