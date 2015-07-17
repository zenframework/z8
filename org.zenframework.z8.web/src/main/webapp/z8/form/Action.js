Ext.form.Action.Submit.prototype.run = function()
{
	var o = this.options, method = this.getMethod(), isGet = method == 'GET';

	if(o.clientValidation === false || this.form.isValid())
	{
		if(o.submitEmptyText === false)
		{
			var fields = this.form.items;
			var emptyFields = [];
			
			var setupEmptyFields = function(f)
			{
				if(f.el.getValue() == f.emptyText)
				{
					emptyFields.push(f);
					f.el.dom.value = "";
				}
				
				if(f.isComposite && f.rendered)
				{
					f.items.each(setupEmptyFields);
				}
			};

			fields.each(setupEmptyFields);
		}
		
		this.transactionId = Ext.Ajax.request(Ext.apply(this.createCallback(o), {
			form:this.form.el.dom,
			url:this.getUrl(isGet),
			method: method,
			headers: o.headers,
			params:!isGet ? this.getParams() : null,
			isUpload: this.form.fileUpload
		}));

		if(o.submitEmptyText === false)
		{
			Ext.each(emptyFields, function(f)
			{
            	if(f.applyEmptyText)
            	{
					f.applyEmptyText();
				}
			});
		}
	}
	else if(o.clientValidation !== false)
	{
		this.failureType = Ext.form.Action.CLIENT_INVALID;
		this.form.afterAction(this, false);
	}
};
