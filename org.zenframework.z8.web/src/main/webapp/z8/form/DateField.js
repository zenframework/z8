Ext.form.DateField.prototype.within = function(event)
{
	return event.within(this.wrap) || (this.menu != null && event.within(this.menu.getEl()));
};

Ext.form.DateField.prototype.format = Z8.Format.Date;