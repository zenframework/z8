Z8.define('Z8.data.HttpRequest', {
	extend: 'Z8.Object',
	shortClassName: 'HttpRequest',

	isUpload: false,
	isGet: false,

	defaultUrl: 'request.json',

	statics: {
		Timeout: 0,

		status: {
			AccessDenied: 401
		},

		state: {
			Unitialized: 0,
			Loading: 1,
			Loaded: 2,
			Interactive: 3,
			Complete: 4
		},

		contentType: {
			FormUrlEncoded: 'application/x-www-form-urlencoded; charset=UTF-8',
			FormMultipart: 'multipart/form-data'
		},

		url: function() {
			var location = window.location;
			var path = location.pathname;
			var length = path.length;

			if(length != 0) {
				var index = path.lastIndexOf('/');
				if(index != -1)
					path = path.substr(0, index);
			}
			return location.origin + path;
		},

		send: function(params, callback) {
			new HttpRequest().send(params, callback);
		},

		get: function(url, callback) {
			new HttpRequest().get(url, callback);
		},

		upload: function(params, files, callback) {
			new HttpRequest().upload(params, files, callback);
		}
	},

	constructor: function(config) {
		this.callParent(config);

		var xhr = this.xhr = new XMLHttpRequest();
		xhr.timeout = 0;

		DOM.on(xhr, 'load', this.onLoad, this);
		DOM.on(xhr, 'error', this.onError, this);
		DOM.on(xhr, 'abort', this.onError, this);
		DOM.on(xhr, 'loadEnd', this.onLoadEnd, this);
	},

	send: function(data, callback) {
		this.data = data;
		this.callback = callback;

		var xhr = this.xhr;
		xhr.open('POST', HttpRequest.url() + '/' + this.defaultUrl, true);
		xhr.setRequestHeader('Content-Type', HttpRequest.contentType.FormUrlEncoded);
		xhr.send(this.encodeData(data));
	},

	get:function(url, callback) {
		this.callback = callback;
		this.isGet = true;

		var xhr = this.xhr;
		xhr.open('GET', HttpRequest.url() + '/' + url, true);
		xhr.setRequestHeader('Content-Type', HttpRequest.contentType.FormUrlEncoded);
		xhr.send();
	},

	upload: function(data, files, callback) {
		this.data = data;
		this.files = files;
		this.callback = callback;
		this.isUpload = true;

		var xhr = this.xhr;
		xhr.open('POST', HttpRequest.url() + '/request.json', true);
		xhr.send(this.encodeFormData(data, files));
	},

	onLoad: function() {
		this.isLoaded = true;

		var xhr = this.xhr;

		if(this.isGet) {
			Z8.callback(this.callback, { text: xhr.responseText }, xhr.status == 200);
			return;
		}

		var response = {};

		try {
			response =  JSON.decode(xhr.responseText);
		} catch(exception) {
			var messages = [{ time: new Date(), type: 'error', text: exception.message }];
			response.info = { messages: messages };

			Z8.callback(this.callback, response, false);
			this.processResponse(response);
			return;
		}

		if(response.success) {
			if(response.retry == null) {
				Z8.callback(this.callback, response, true);
				this.processResponse(response);
			} else
				HttpRequest.send({ retry: response.retry, session: response.session, server: response.server }, this.callback);
		} else if(!this.relogin(response.status)) {
			response.info = response.info || {};
			Z8.callback(this.callback, response, false);
			this.processResponse(response);
		}
	},

	relogin: function(status) {
		if(!this.isLogin && status == HttpRequest.status.AccessDenied) {
			Application.login({ fn: this.onRelogin, scope: this });
			return true;
		}
		return false;
	},

	onRelogin: function() {
		this.isUpload ? HttpRequest.upload(this.data, this.files, this.callback) : HttpRequest.send(this.data, this.callback);
	},

	onError: function(event) {
		if(this.isLoaded)
			throw 'HttpRequest.onError: this.isLoaded == true;';

		var messages = [{ time: new Date(), type: 'error', text: 'Communication failure' }];
		var response = { info: { messages: messages } };
		Z8.callback(this.callback, response, false);
		this.processResponse(response);
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

	encodeData: function(data) {
		var result = [];

		var isLogin = this.isLogin = data.login != null;

		if(isLogin)
			data.request = 'login';
		else
			data.session = data.session || Application.session;

		for(var name in data) {
			var value = data[name];
			if(Z8.isEmpty(value))
				continue;
			if(!String.isString(value))
				value = JSON.encode(value);
			result.push(encodeURIComponent(name) + '=' + encodeURIComponent(value));
		}

		return result.join('&');
	},

	encodeFormData: function(data, files) {
		data.session = Application.session;

		var formData = new FormData();
		for(var name in data) {
			var value = data[name];
			if(!Z8.isEmpty(value))
				formData.append(name, String.isString(value) ? value : JSON.encode(value));
		}

		for(var i = 0, length = files.length; i < length; i++) {
			var file = files[i];
			formData.append(file.name, file);
		}

		return formData;
	},

	processResponse: function(response) {
		var info = response.info;
		Application.message(info.messages);

		var files = info.files;
		if(files == null)
			return;

		for(var i = 0, length = files.length; i < length; i++)
			DOM.download(files[i].path, info.serverId);
	}
});