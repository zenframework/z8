Z8.define('Z8.form.action.Report', {
	extend: 'Z8.form.action.Action',

	constructor: function(config) {
		this.callParent(config);
		this.action.parameters = this.action.parameters || [{ id: 'format', value: 'pdf' }];
	},

	htmlMarkup: function() {
		var items = [
			new Z8.menu.Item({ text: 'Acrobat Reader (*.pdf)', icon: this.getFormatIcon('pdf'), format: 'pdf' }),
			new Z8.menu.Item({ text: 'Microsoft Excel (*.xls)', icon: this.getFormatIcon('xls'), format: 'xls' })
		];

		var menu = this.menu = new Z8.menu.Menu({ items: items });
		menu.on('itemClick', this.onMenuItemClick, this);

		this.icon = this.getFormatIcon();

		return this.callParent();
	},

	getFormat: function() {
		return this.action.parameters[0].value;
	},

	setFormat: function(format) {
		this.action.parameters[0].value = format;
	},

	getFormatIcon: function() {
		return this.getFormat() == 'xls' ? 'fa-file-excel-o' : 'fa-file-pdf-o';
	},

	onMenuItemClick: function(menu, item) {
		this.setFormat(item.format);
		this.setIcon(this.getFormatIcon());
		Z8.callback(this.handler, this.scope, this);
	}
});
