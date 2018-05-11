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
	extend: 'Z8.form.field.Listbox',

	tools: true,
	checks: true,

	fields: [
		{ name: 'name', header: 'Файл', type: Type.String, icon: 'fa-file-o' },
		{ name: 'size', header: 'Размер', type: Type.Integer, renderer: Format.fileSize },
		{ name: 'time', header: 'Дата', type: Type.Datetime, renderer: Format.dateOrTime, icon: 'fa-clock-o' },
		{ name: 'author', header: 'Автор', type: Type.String, icon: 'fa-user-o' }
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
		var upload = this.uploadTool = new Z8.button.Tool({ cls: 'btn-sm', icon: 'fa-upload', tooltip: 'Загрузить файл(ы)', handler: this.onUploadFile, scope: this });
		var download = this.downloadTool = new Z8.button.Tool({ cls: 'btn-sm', icon: 'fa-download', tooltip: 'Скачать файл(ы)', handler: this.onDownloadFile, scope: this });
		var remove = this.removeTool = new Z8.button.Tool({ cls: 'btn-sm remove', danger: true, icon: 'fa-trash', tooltip: 'Удалить файл(ы)', handler: this.onRemoveFile, scope: this });
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
		if(this.getDom() == null)
			return;

		var enabled = !this.isReadOnly() && this.isEnabled();

		this.uploadTool.setEnabled(enabled);

		enabled = enabled && this.getChecked().length != 0;
		this.downloadTool.setEnabled(enabled);
		this.removeTool.setEnabled(enabled);
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
		var files = this.getChecked();
		var count = files.length;

		var callback = function(success) {
			if(--count == 0)
				this.downloadTool.setBusy(false);
		};

		this.downloadTool.setBusy(true);

		for(var i = 0, length = files.length; i < length; i++)
			DOM.download(files[i].get('path'), null, { fn: callback, scope: this });
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