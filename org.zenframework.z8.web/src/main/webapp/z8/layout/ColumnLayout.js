Z8.layout.ColumnLayout = Ext.extend(Ext.layout.ContainerLayout,
{
	columnLayoutSelector: 'z8-column-layout',
	rendered: false,

	getCells: function (target) {
		var result = [];

		var cells = target.dom.getElementsByTagName('td');

		for (var i = 0; i < cells.length; i++) {
			if (cells[i].className == this.columnLayoutSelector) {
				result.push(cells[i]);
			}
		}

		return result;
	},

	renderAll: function (ct, target) {
		var items = ct.items.items;

		if (!this.rendered) {
			this.rendered = true;

			var tableMarkup = '<table border="0" cellspacing="0" cellpadding="0">' +
							'<thead><tr style="vertical-align:top;">';

			for (var i = 0; i < items.length; i++) {
				tableMarkup += '<td class="' + this.columnLayoutSelector + '"></td>';
			}

			tableMarkup += '</tr></thead></table>',

			target.dom.innerHTML = tableMarkup;

			var cells = this.getCells(target);

			var defaultWidth = (this.defaults && this.defaults.width) ? this.defaults.width : null;

			for (var i = 0; i < items.length; i++) {
				var item = items[i];
				this.renderItem(item, i, Ext.fly(cells[i]));
				var width = item.width ? item.width : defaultWidth;
				if (width) {
					//item.setSize(width - item.getEl().getMargins('lr'));
				}
			}
		}
	}
});

Ext.Container.LAYOUTS['z8.column'] = Z8.layout.ColumnLayout;