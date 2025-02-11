Z8.define('Z8.data.file.Model', {
	extend: 'Z8.data.Model',

	local: true,
	idProperty: 'id',

	fields: [
		new Z8.data.field.Guid({ name: 'id' }),
		new Z8.data.field.String({ name: 'name' }),
		new Z8.data.field.String({ name: 'path' }),
		new Z8.data.field.Integer({ name: 'size' }),
		new Z8.data.field.Datetime({ name: 'time' }),
		new Z8.data.field.String({ name: 'author' })
	]
});

Z8.define('Z8.form.field.Files', {
	extend: 'ListBox',
	shortClassName: 'FilesBox',

	tools: true,
	checks: true,

	fields: [
		{ name: 'name', header: Z8.$('FilesBox.file'), type: Type.String, icon: 'fa-file-o' },
		{ name: 'size', header: Z8.$('FilesBox.fileSize'), type: Type.Integer, renderer: Format.fileSize },
		{ name: 'time', header: Z8.$('FilesBox.date'), type: Type.Datetime, renderer: Format.dateOrTime, icon: 'fa-clock-o' },
		{ name: 'author', header: Z8.$('FilesBox.author'), type: Type.String, icon: 'fa-user-o' }
	],

	initComponent: function() {
		this.store = new Z8.data.Store({ model: 'Z8.data.file.Model' });
		this.callParent();
	},

	setValue: function(value, displayValue) {
		this.callParent(value, displayValue);

		if(this.store != null)
			this.store.loadData(value);
	},

	onRecordChange: function(record, modified) {
		if(modified.hasOwnProperty(this.name))
			this.setValue(record.get(this.name));
	},

	afterRecordSet: function(record) {
	},

	createTools: function() {
		var upload = this.uploadTool = new Z8.button.Button({ icon: 'fa-upload', tooltip: Z8.$('FilesBox.uploadFiles'), handler: this.onUploadFile, scope: this });
		var download = this.downloadTool = new Z8.button.Button({ icon: 'fa-download', tooltip: Z8.$('FilesBox.downloadFiles'), handler: this.onDownloadFile, scope: this });
		var remove = this.removeTool = new Z8.button.Button({ cls: 'remove', danger: true, icon: 'fa-trash', tooltip: Z8.$('FilesBox.deleteFiles'), handler: this.onRemoveFile, scope: this });
		return [upload, download, remove];
	},

	completeRender: function() {
		this.callParent();

		var fileInput = this.fileInput = DOM.append(this, { tag: 'input', type: 'file', multiple: true });

		DOM.on(fileInput, 'change', this.onFileInputChange, this);

		DOM.on(this, 'dragEnter', this.onDragEnter, this);
		DOM.on(this, 'dragOver', this.onDragOver, this);
		DOM.on(this, 'drop', this.onDrop, this);

		DOM.on(window, 'dragEnter', this.onWindowDragEnter, this);
		DOM.on(window, 'dragOver', this.onWindowDragOver, this);
		DOM.on(window, 'dragLeave', this.onWindowDragLeave, this);
		DOM.on(window, 'drop', this.onWindowDrop, this);
	},

	onDestroy: function() {
		DOM.un(window, 'dragEnter', this.onWindowDragEnter, this);
		DOM.un(window, 'dragOver', this.onWindowDragOver, this);
		DOM.un(window, 'dragLeave', this.onWindowDragLeave, this);
		DOM.un(window, 'drop', this.onWindowDrop, this);

		DOM.un(this, 'dragEnter', this.onDragEnter, this);
		DOM.un(this, 'dragOver', this.onDragOver, this);
		DOM.un(this, 'drop', this.onDrop, this);

		DOM.un(this.fileInput, 'change', this.onFileInputChange, this);

		DOM.remove(this.fileInput);

		this.fileInput = null;

		this.callParent();
	},

	updateTools: function() {
		if(this.getDom() == null || !this.tools)
			return;

		var enabled = this.isEnabled();
		var readOnly = this.isReadOnly();

		this.uploadTool.setEnabled(!readOnly && enabled);

		enabled = enabled && this.getChecked().length != 0;
		this.downloadTool.setEnabled(enabled);
		this.removeTool.setEnabled(!readOnly && enabled);
	},

	onUploadFile: function(button) {
		this.fileInput.value = null;
		this.fileInput.click();
	},

	onRemoveFile: function(button) {
		var files = [];
		var selected = this.getChecked();

		for(var i = 0, length = selected.length; i < length; i++)
			files.push(selected[i].id);

		this.removeTool.setBusy(true);

		var callback = function(record, files, success) {
			this.removeTool.setBusy(false);
		};

		var record = this.getRecord();
		record.detach(this.name, files, { fn: callback, scope: this });
	},

	onDownloadFile: function(button) {
		var tool = this.downloadTool;
		var files = this.getChecked();
		tool.setBusy(true);

		var downloadCallback = function(success) {
			tool.setBusy(false);
		};

		if (files.length == 1) {
			var file = files[0];
			DOM.download(file.get('path'), file.id, null, { fn: downloadCallback, scope: this });
			return;
		}

		var fileList = files.map(function(file) {
			return {
				id: file.id,
				path: file.get('path'),
				name: file.get('name')
			};
		});

		var params = {
			request: this.form.model,
			action: 'archive',
			archive: JSON.encode(fileList)
		};

		var callback = function(response, success) {
			if (success && response.source) {
				var url = response.source;
				DOM.download(url, null, response.server, downloadCallback, true);
			} else {
				tool.setBusy(false);
			}
		};

		HttpRequest.send(params, { fn: callback, scope: this });
	},

	onFileInputChange: function() {
		this.upload(this.fileInput.files);
	},

	onDragEnter: function(event) {
		var dataTransfer = event.dataTransfer;
		if(dataTransfer == null || this.isReadOnly() || !this.isEnabled())
				return;

		dataTransfer.effectAllowed = dataTransfer.dropEffect = 'copy';
		event.stopEvent();
	},

	onDragOver: function(event) {
		if(event.dataTransfer != null && !this.isReadOnly() && this.isEnabled())
			event.stopEvent();
	},

	onDrop: function(event) {
		var dataTransfer = event.dataTransfer;
		if(dataTransfer == null || this.isReadOnly() || !this.isEnabled())
			return;

		dataTransfer.effectAllowed = dataTransfer.dropEffect = 'copy';
		event.stopEvent();

		this.upload(dataTransfer.files);
	},

	onWindowDragEnter: function(event) {
	},

	onWindowDragLeave: function(event) {
	},

	onWindowDragOver: function(event) {
		var dataTransfer = event.dataTransfer;
		if(!event.dataTransfer)
			return;

		dataTransfer.effectAllowed = dataTransfer.dropEffect = 'none';
		event.stopEvent();
	},

	onWindowDrop: function(event) {
		if(event.dataTransfer != null)
			event.stopEvent();
	},

	upload: function(files) {
		if(files.length == 0)
			return;

		this.uploadTool.setBusy(true);

		var callback = function(record, files, success) {
			this.uploadTool.setBusy(false);
			if(success && this.getRecord() == record) {
				var file = files.last();
				this.select(file != null ? file.id : null);
				this.focus();
			}
		};

		var record = this.getRecord();
		record.attach(this.name, files, { fn: callback, scope: this });
	}
});
