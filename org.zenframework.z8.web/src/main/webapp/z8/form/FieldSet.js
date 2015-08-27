Ext.form.FieldSet.prototype.setDisabled = function(disabled)
{
	this.items.each(function(item, index, length)
	{
		if(item.setDisabled != null)
		{
			item.setDisabled(disabled);
		}
	});
};
