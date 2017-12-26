Z8.define('Z8.filter.fields.Model', {
	extend: 'Z8.data.Model',

	local: true,
	idProperty: 'id',

	fields: [
		new Z8.data.field.String({ name: 'id' }),
		new Z8.data.field.String({ name: 'name' }),
		new Z8.data.field.String({ name: 'icon' }),
		new Z8.data.field.String({ name: 'type' }),
		new Z8.data.field.String({ name: 'description' })
	]
});

Z8.define('Z8.filter.Expression', {
	extend: 'Z8.filter.Group',

	button: false,

	logical: 'and',

	htmlMarkup: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('expression');
		this.expression = this.expression || { logical: 'and', expressions: [] };
		return this.callParent();
	},

	onDestroy: function() {
		var fields = this.fields;
		if(fields != null && fields.isStore) {
			fields.dispose();
			this.fields = null;
		}
		this.callParent();
	},

	getFields: function() {
		var fields = this.fields || [];

		if(!Array.isArray(fields))
			return this.fields;

		var data = [];

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			if(!field.isPrimaryKey && !field.isParentKey && !field.isLink)
				data.push({ id: field.name, name: field.header || field.name, icon: field.icon, type: field.type, description: field.descripton });
		}

		var fields = this.fields = new Z8.data.Store({ model: 'Z8.filter.fields.Model', data: data });
		fields.use();
		return fields;
	},

	onChildToggle: function(toggle, child) {
		this.onContainerToggle(toggle, child.container);
		this.fireEvent('select', this.getSelection(), this);
	},

	onChildChange: function(child) {
		this.fireEvent('change', this.getExpression(), this);
	},

	getSelection: function() {
		var result = this.callParent();
		return result.length != 0 ? result : this.getSelectedLines(this.getLines());
	},

	getSelectedLines: function(lines) {
		var result = [];

		for(var i = 0, length = lines.length; i < length; i++) {
			var line = lines[i];
			if(!line.isSelected()) {
				if(line instanceof Z8.filter.Group) {
					var selection = this.getSelectedLines(line.getLines());
					if(selection.length != 0)
						return selection;
				}
			} else
				result.push(line);
		}
		return result;
	}
});
