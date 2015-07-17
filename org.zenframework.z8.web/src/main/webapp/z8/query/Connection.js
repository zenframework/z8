Ext.data.Connection.prototype.handleResponse = function(response)
{
	var text = response.responseText;
	
	if(!Z8.isEmpty(text) && text.charAt(0) == '{') // этот if нужнен для поддержки help'а, хотя нужно help сделать нормально
	{	
		try
		{
			if(this.pollResponse(response))
			{
				return;
			}
		}
		catch(exception)
		{
			this.handleFailure(response);
			return;
		}
	}
	
	this.transId = false;
	
	var options = response.argument.options;
	response.argument = options ? options.argument : null;
	this.fireEvent('requestcomplete', this, response, options);
    
	if(options.success)
	{
		options.success.call(options.scope, response, options);
	}

	if(options.callback)
	{
		options.callback.call(options.scope, options, true, response);
	}
};

Ext.data.Connection.prototype.pollResponse = function(response)
{
	var result = Ext.decode(response.responseText);

	if(result.retry == null)
	{
		return false;
	}

	var options = response.argument.options;
	options.isUpload = false;
	options.form = null;
	options.params = { retry: result.retry, serverId: result.serverId, sessionId: Z8.sessionId };

	this.request(options);
	return true;
};

Ext.data.Connection.prototype.doFormUpload = function(o, ps, url)
{
	var BEFOREREQUEST = 'beforerequest';
	var REQUESTCOMPLETE = 'requestcomplete';
	var REQUESTEXCEPTION = 'requestexception';
	var UNDEFINED = undefined;
	var LOAD = 'load';
	var POST = 'POST';
	var GET = 'GET';
	var WINDOW = window;

	var id = Ext.id();
	var doc = document;
	var frame = doc.createElement('iframe');
	var form = Ext.getDom(o.form);
	var hiddens = [];
	var encoding = 'multipart/form-data';
	var buf = {target: form.target, method: form.method, encoding: form.encoding, enctype: form.enctype, action: form.action};

	Ext.fly(frame).set({id: id, name: id, cls: 'x-hidden', src: Ext.SSL_SECURE_URL}); 
	
	doc.body.appendChild(frame);

	if(Ext.isIE)
	{
		document.frames[id].name = id;
	}

	Ext.fly(form).set({target: id, method: POST, enctype: encoding, encoding: encoding, action: url || buf.action});

	Ext.iterate(Ext.urlDecode(ps, false), function(k, v)
	{
		var hd = doc.createElement('input');
		Ext.fly(hd).set({type: 'hidden', value: v, name: k});
		form.appendChild(hd);
		hiddens.push(hd);
	});

	function cb()
	{
		var me = this;
		var response = {responseText: '', responseXML: null, argument: o.argument};
		var doc, firstChild;

		try
		{
			doc = frame.contentWindow.document || frame.contentDocument || WINDOW.frames[id].document;
			
			if(doc)
			{
				if(doc.body)
				{
					if(/textarea/i.test((firstChild = doc.body.firstChild || {}).tagName))
					{ 
						response.responseText = firstChild.value;
					}
					else
					{
						response.responseText = doc.body.innerHTML;
					}
				}

				response.responseXML = doc.XMLDocument || doc;
			}
		}
		catch(e) {}

		Ext.EventManager.removeListener(frame, LOAD, cb, me);

		response.argument = { options: o };
		
		me.handleResponse(response);
/*
		me.fireEvent(REQUESTCOMPLETE, me, response, o);

		function runCallback(fn, scope, args)
		{
        	if(Ext.isFunction(fn))
        	{
				fn.apply(scope, args);
			}
		}

		runCallback(o.success, o.scope, [response, o]);
		runCallback(o.callback, o.scope, [o, true, response]);
*/

		if(!me.debugUploads)
		{
			setTimeout(function(){Ext.removeNode(frame);}, 100);
		}
	}

	Ext.EventManager.on(frame, LOAD, cb, this);
    form.submit();

	Ext.fly(form).set(buf);
	
	Ext.each(hiddens, function(h)
	{
		Ext.removeNode(h);
	});
	
	return frame;
};
