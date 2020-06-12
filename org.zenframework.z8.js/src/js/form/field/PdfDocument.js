Z8.define('Z8.form.field.PdfDocument', {
	extend: 'Z8.form.field.Document',

	tag: 'iframe',
	toolbar: true,
	files: [],
	fileIndex: 0,

	getCls: function() {
		return Z8.form.field.Document.prototype.getCls.call(this).pushIf('pdf-document');
	},
	
	completeRender: function() {
		this.callParent();
		DOM.addCls(this.tools, 'display-none');
	},
	
	updateFrame: function() {
		this.setSource(this.getSource(this.getCurrentFile()));
		DOM.swapCls(this.prevButton, this.fileIndex == 0, 'disabled');
		DOM.swapCls(this.nextButton, this.fileIndex + 1 == this.files.length, 'disabled');

		DOM.setInnerHtml(this.fileNum, (this.fileIndex + 1) + '/' + this.files.length);
	},
	
	onPrevFile: function() {
		if (this.fileIndex > 0) {
			this.fileIndex--;
			this.updateFrame();
		}
	},
	
	onNextFile: function() {
		if (this.fileIndex + 1 < this.files.length) {
			this.fileIndex++;
			this.updateFrame();
		}
	},
	
	createTools: function() {
		var tools = [];
		
		var prev = this.prevButton = new Z8.button.Button({ icon: 'fa-arrow-left', tooltip: 'Предыдущий файл', handler: this.onPrevFile, scope: this });
		tools.push(prev);
		
		var fileNum = this.fileNum = new Z8.Component({ cls: 'text', cn: [ '1/1' ] });
		tools.push(fileNum);
		
		var next = this.nextButton = new Z8.button.Button({ icon: 'fa-arrow-right', tooltip: 'Следующий файл', handler: this.onNextFile, scope: this });
		tools.push(next);
		
		return tools;
	},
	
	htmlMarkup: function() {
		var label = this.label;
		if (label == null)
			label = this.label = {};
		if (label.tools == null)
			label.tools = this.tools = new Z8.button.Group({ items: this.createTools() });
		return this.callParent();
	},
	
	getCurrentFile: function() {
		return this.files[this.fileIndex];
	},
	
	setSource: function(source) {
		DOM.setProperty(this.document, 'src', source != null ? (window._DEBUG_ ? '/' : '') + source + '&preview=true#toolbar=' + (this.toolbar ? 1 : 0) : '');
	},
	
	getSource: function(value) {
		return value != null ? encodeURI(value.path.replace(/\\/g, '/')) + '?id=' + value.id +  '&session=' + Application.session : null;
	},
	
	setValue: function(value) {
		if (!Array.isArray(value) || value.length == 0)
			return;
		this.files = value;
		this.fileIndex = 0;
		this.callParent(value[0]);
		
		DOM.swapCls(this.tools, this.files.length < 2, 'display-none');
		DOM.swapCls(this.prevButton, true, 'disabled');
		DOM.swapCls(this.nextButton, this.fileIndex + 1 == this.files.length, 'disabled');
		DOM.setInnerHtml(this.fileNum, '1/' + this.files.length);
	},
});