Z8.define('Z8.data.HttpRequest', {
	extend: 'Z8.Object',
	shortClassName: 'HttpRequest',

	defaultUrl: 'request.json',

	statics: {
		Timeout: 0,

		AccessDenied: 401,
		Success: 200,

		Unitialized: 0,
		Loading: 1,
		Loaded: 2,
		Interactive: 3,
		Complete: 4,

		url: function(url) {
			if(url[0] == '/')
				return url;

			var location = window.location;
			var path = location.pathname;
			var length = path.length;

			if(length != 0) {
				var index = path.lastIndexOf('/');
				if(index != -1)
					path = path.substr(0, index);
			}
			return location.origin + path + '/' + url;
		},

		parseUrlParameters: function(url) {
			return new URL(document.location).searchParams;
//			for(var key of urlSearchParams.keys())
		},

		send: function(params, callback, type) {
			new HttpRequest().send(params, callback, type);
		},

		upload: function(params, files, callback) {
			params.files = files;
			new HttpRequest().send(params, callback);
		}
	},

	constructor: function(config) {
		this.callParent(config);

		var xhr = this.xhr = new XMLHttpRequest();

		DOM.on(xhr, 'load', this.onLoad, this);
		DOM.on(xhr, 'error', this.onError, this);
		DOM.on(xhr, 'abort', this.onError, this);
		DOM.on(xhr, 'loadEnd', this.onLoadEnd, this);
	},

	send: function(data, callback, type) {
		this.data = data || {};
		this.callback = callback;
		this.type = type || '';

		var xhr = this.xhr;

		xhr.open('POST', HttpRequest.url(this.defaultUrl), true);
		xhr.responseType = this.type;
		xhr.timeout = 0;
		xhr.send(this.encodeData(data));
	},

	onLoad: function() {
		this.isLoaded = true;

		var xhr = this.xhr;

		var contentType = xhr.getResponseHeader('content-type');
		if(contentType == null || contentType.indexOf('application/json') == -1) {
			Z8.callback(this.callback, { text: xhr.response, data: xhr.response }, xhr.status == HttpRequest.Success, this.info);
			return;
		}

		this.resolveResponse();
	},

	resolveResponse: function() {
		var xhr = this.xhr;

		switch(xhr.responseType) {
		case 'blob':
			var me = this;
			xhr.response.text().then(
				json => { me.processJsonResponse(json) },
				exception => { me.processException(exception) });
			break;
		default:
			this.processJsonResponse(xhr.response);
			break;
		}
	},

	processJsonResponse: function(json) {
		var response = {};

		try {
			response = JSON.decode(json);
		} catch(exception) {
			this.processException(exception);
			return;
		}

		if(response.success) {
			if(response.retry == null) {
				Z8.callback(this.callback, response, true, this.info);
				this.processResponse(response);
			} else
				HttpRequest.send({ retry: response.retry, session: response.session, server: response.server }, this.callback, this.type, this.info);
		} else if(!this.relogin(response.status)) {
			response.info = response.info || {};
			Z8.callback(this.callback, response, false, this.info);
			this.processResponse(response);
		}
	},

	onLoadEnd: function() {
		var xhr = this.xhr;

		DOM.un(xhr, 'load', this.onLoad, this);
		DOM.un(xhr, 'timeout', this.onError, this);
		DOM.un(xhr, 'error', this.onError, this);
		DOM.un(xhr, 'abort', this.onError, this);
		DOM.un(xhr, 'loadEnd', this.onLoadEnd, this);

		this.xhr = null;
	},

	onError: function(event) {
		this.processException(new Error('Communication failure'));
	},

	relogin: function(status) {
		if(!this.isLogin && status == HttpRequest.AccessDenied) {
			Application.login({ fn: this.onRelogin, scope: this });
			return true;
		}
		return false;
	},

	onRelogin: function() {
		HttpRequest.send(this.data, this.callback, this.type, this.info);
	},

	encodeData: function(data) {
		var result = [];

		var isLogin = this.isLogin = data.request == 'login';

		if(!isLogin)
			data.session = Application.session;

		var files = data.files || [];

		var formData = new FormData();

		for(var name in data) {
			if(name == 'files')
				continue;

			var value = data[name];
			if(!Z8.isEmpty(value))
				formData.append(name, String.isString(value) ? value : (Date.isDate(value) ? value.toISOString() : JSON.encode(value)));
		}

		for(var i = 0, length = files.length; i < length; i++) {
			var file = files[i];
			formData.append(file.name, file);
		}

		return formData;
	},

	processResponse: function(response) {
		var info = response.info;
		Application.message(info.messages || []);

		var files = info.files || [];
		for(var i = 0, length = files.length; i < length; i++)
			DOM.download(files[i].path, files[i].id, info.serverId);
	},

	processException: function(exception) {
		var messages = [{ time: new Date(), type: 'error', text: exception.message }];
		var response = { info: { messages: messages } };

		Z8.callback(this.callback, response, false, this.info);
		this.processResponse(response);
	}
});