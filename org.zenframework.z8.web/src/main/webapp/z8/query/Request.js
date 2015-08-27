Z8.Ajax.Request = Ext.extend(Ext.util.Observable, 
{
	requestId: null,
	parameters: null,

	/*
		error: function(message, errors)
		success: function(query)
	*/
	scope: null,
	error: null,
	success: null,

	constructor : function(config)
	{
		Ext.apply(this, config);

		this.scope = this.scope != null ? this.scope : this;
		this.requestParams = Ext.apply({ requestId: this.requestId }, this.parameters);
		Z8.Ajax.Request.superclass.constructor.call(this);
	},

	send: function()
	{
		this.requestParams.sessionId = Z8.sessionId;
		this.requestParams.requestUrl = Z8.getRequestUrl();
		
		Ext.Ajax.request(
		{
			url: Z8.request.url,
			params: this.requestParams,
			object: this,
			success: this.onSuccess,
			failure: this.onFailure
		});
	},

	onSuccess: function(response, request)
	{
		var object = request.object;
		var scope = object.scope;

		var result = null;
		
		try
		{
			var result = Ext.decode(response.responseText);
		}
		catch(exception)
		{
			if(object.error == null || object.error == Ext.emptyFn)
			{
				Z8.showMessages('Z8', Z8.Format.nl2br(exception.message));
			}
			else
			{
				var info = { messages: [exception.message] };
				object.error.call(scope, info);
			}
			return;
		}

		if(result.success)
		{
			object.success.call(scope, result.isQuery != null ? new Z8.query.Query(result) : result);
		}
		else
		{
			if(!object.relogin(result.status))
			{
				if(object.error == null || object.error == Ext.emptyFn)
				{
					Z8.showMessages(object.requestParams.title || 'Z8', Z8.Format.nl2br(result.info.messages));
				}
				else
				{
					object.error.call(scope, result.info);
				}
			}
		}
	},

	onFailure: function(response, request)
	{
		var me = request.object;

		if(!me.relogin(response.status))
		{
			var messages = [response.statusText ? response.statusText : 'Undefined server message.'];
			
			if(me.error == null || me.error == Ext.emptyFn)
			{
				Z8.showMessages(me.requestParams.title || 'Z8', Z8.Format.nl2br(messages));
			}
			else
			{
				me.error.call(me.scope, { messages: messages });
			}
		}
	},
	
	relogin: function(status)
	{
		if(!this.login && status == Z8.Status.AccessDenied)
		{
			Z8.LoginManager.login(this.onRelogin, this, true);
			return true;
		}
		return false;
	},
	
	onRelogin: function(result)
	{
		this.send();
	}
});

Ext.apply(Z8.Ajax, {
	request: function(requestId, callback, errorCallback, parameters, scope)
	{
		new Z8.Ajax.Request(
		{
			requestId: requestId,
			parameters: parameters,
			scope: scope,
			success: callback,
			error: errorCallback
		}).send();
	}
});

Ext.apply(Z8.Ajax, {
	login: function(callback, parameters, scope)
	{
		new Z8.Ajax.Request(
		{
			login: true,
			parameters: parameters,
			scope: scope,
			success: callback,
			error: Ext.emptyFn
		}).send();
	}
});
