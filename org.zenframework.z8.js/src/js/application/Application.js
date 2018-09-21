Z8.define('Z8.application.Application', {
	extend: 'Z8.Object',

	id: 'Z8™ Application',
	name: 'Revolt',
	title: 'Revolt software',

	hashPassword: true,

	viewportCls: null,
	userCls: null,

	listbox: {
		locks: true,
		checks: true,
		collapsed: true
	},

	geometry: {
		tiles: { url: '', serverType: 'geoserver', params: {} },
		code: '',
		extent: [0, 0, 0, 0]
	},

	subclasses: {
	},

	getSubclass: function(id) {
		return this.subclasses[id];
	},

	login: function(options) {
		var callback = function(loginData) {
			this.session = loginData.session;
			this.maxUploadSize = loginData.maxUploadSize;

			var cls = this.userCls;
			User = cls == null ? new Z8.application.User(loginData.user) : Z8.create(cls, loginData.user);

			if(options != null)
				Z8.callback(options, loginData);
		};

		if(Viewport == null) {
			this.setTitle();
			var cls = this.viewportCls;
			Viewport = cls == null ? new Z8.application.viewport.Viewport() : Z8.create(cls);
			Viewport.render();
		}

		Application.session = null;
		User = null;
		Viewport.login({ fn: callback, scope: this });
	},

	setTitle: function(title) {
		document.title = (title != null ? title + ' - ' : '') + Application.title;
	},

	message: function(message) {
		Viewport.message(message);
	},

	checkFileSize: function(file) {
		var size = file.size;
		var maxSize = Application.maxUploadSize * 1024 * 1024;
		if(maxSize != 0 && size > maxSize) {
			Application.message({ text: 'Размер файла \'' + file.name + '\' ' + 
				Z8.util.Format.fileSize(size) + ' превышает максимально допустимые ' + Format.fileSize(maxSize), type: 'error', time: new Date() });
			return false;
		}
		return true;
	}
});

var Application = new Z8.application.Application();

DOM.onReady(Application.login, Application);

