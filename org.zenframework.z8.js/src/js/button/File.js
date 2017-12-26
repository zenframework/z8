Z8.define('Z8.button.File', {
	extend: 'Z8.button.Button',

	initComponent: function() {
		this.callParent();

		this.on('click', this.processClick, this);
	},

	completeRender: function() {
		this.callParent();

		var fileInput = this.fileInput = DOM.append(this, { tag: 'input', type: 'file', multiple: true });
		DOM.on(fileInput, 'change', this.onFileInputChange, this);
	},

	onDestroy: function() {
		DOM.un(this.fileInput, 'change', this.onFileInputChange, this);

		delete this.fileInput;

		this.callParent();
	},

	processClick: function() {
		this.fileInput.value = null;
		this.fileInput.click();
	},

	onClick: function(event, target) {
		if(target != this.fileInput)
			this.callParent(event, target);
	},

	onFileInputChange: function() {
		var files = this.fileInput.files;
		if(files.length != null)
			this.fireEvent('select', this, files);
	}
});