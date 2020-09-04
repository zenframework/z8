Z8.define('Z8.button.File', {
	extend: 'Button',
	shortClassName: 'FileButton',

	initComponent: function() {
		Button.prototype.initComponent.call(this);
		this.on('click', this.processClick, this);
	},

	completeRender: function() {
		Button.prototype.completeRender.call(this);

		var fileInput = this.fileInput = DOM.append(this, { tag: 'input', type: 'file', accept: this.accept, multiple: this.multiple !== false });
		DOM.on(fileInput, 'change', this.onFileInputChange, this);
	},

	onDestroy: function() {
		DOM.un(this.fileInput, 'change', this.onFileInputChange, this);

		delete this.fileInput;

		Button.prototype.onDestroy.call(this);
	},

	processClick: function() {
		this.fileInput.value = null;
		this.fileInput.click();
	},

	onClick: function(event, target) {
		if(target != this.fileInput)
			Button.prototype.onClick.call(this, event, target);
	},

	onFileInputChange: function() {
		var files = this.fileInput.files;
		if(files.length != null) {
			Z8.callback(this.selectHandler, this.scope, this, files);
			this.fireEvent('select', this, files);
		}
	}
});