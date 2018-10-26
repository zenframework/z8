Z8.define('Z8.button.GeometryTools', {
	extend: 'Z8.button.Button',

	icon: 'fa-mouse-pointer', 

	tooltip: 'Инструменты',

	select: true,
	ruler: true,
	move: true,
	edit: true,
	draw: true,

	location: true,
	yandex: true,

	htmlMarkup: function() {
		var tools = [];

		if(this.select !== false) {
			var select = this.select = this.active = new Z8.menu.Item({ text: 'Выбор объектов', icon: 'fa-mouse-pointer', isTool: true, isSelect: true });
			tools.add(select);
		}

		if(this.ruler !== false) {
			var ruler = this.ruler = new Z8.menu.Item({ text: 'Линейка', icon: 'fa-ruler-combined', isTool: true, isRuler: true });
			tools.add(ruler);
		}

		if(this.move !== false) {
			var move = this.move = new Z8.menu.Item({ text: 'Переместить объект', icon: 'fa-hand-paper-o', isTool: true, isMove: true });
			tools.add(move);
		}

		if(this.edit !== false) {
			var edit = this.edit = new Z8.menu.Item({ text: 'Изменить объект', icon: 'fa-pencil', isTool: true, isEdit: true });
			tools.add(edit);
		}

		if(this.draw !== false) {
			var draw = this.draw = new Z8.menu.Item({ text: 'Нарисовать объект', icon: 'fa-pencil-square-o', isTool: true, isDraw: true });
			tools.add(draw);
		}

		var  dividerPos = tools.length;

		if(this.location !== false) {
			var location = this.location = new Z8.menu.Item({ text: 'Мое местоположение', icon: 'fa-crosshairs', isTool: false, isLocation: true });
			tools.add(location);
		}

		if(this.yandex !== false) {
			var yandex = this.yandex = new Z8.menu.Item({ text: 'Открыть в Яндекс', icon: 'fa-yandex', isTool: false, isYandex: true });
			tools.add(yandex);
		}

		if(dividerPos != 0 && dividerPos != tools.length)
			tools.insert(new Z8.list.Divider(), dividerPos);

		var menu = this.menu = new Z8.menu.Menu({ items: tools });
		menu.on('itemClick', this.onToolClick, this);

		this.on('click', this.toggleMenu, this);

		return this.callParent();
	},

	isToolActive: function(tool) {
		return this.active && this.active == tool;
	},

	enableTool: function(tool, enable) {
		if(tool)
			tool.setEnabled(enable);
	},

	activateTool: function(tool) {
		if(!tool)
			return;

		if(tool.isTool) {
			this.setIcon(tool.icon);
			this.active = tool;
		}
		this.fireEvent('select', this, tool);
	},

	isSelectActive: function() {
		return this.isToolActive(this.select);
	},

	isRulerActive: function() {
		return this.isToolActive(this.ruler);
	},

	isMoveActive: function() {
		return this.isToolActive(this.move);
	},

	isEditActive: function() {
		return this.isToolActive(this.edit);
	},

	isDrawActive: function() {
		return this.isToolActive(this.draw);
	},

	activateSelect: function() {
		this.activateTool(this.select);
	},

	activateRuler: function() {
		this.activateTool(this.ruler);
	},

	activateMove: function() {
		this.activateTool(this.move);
	},

	activateEdit: function() {
		this.activateTool(this.edit);
	},

	activateDraw: function() {
		this.activateTool(this.draw);
	},

	enableSelect: function(enable) {
		this.enableTool(this.select, enable);
	},

	enableRuler: function(enable) {
		this.enableTool(this.ruler, enable);
	},

	enableMove: function(enable) {
		this.enableTool(this.move, enable);
	},

	enableEdit: function(enable) {
		this.enableTool(this.edit, enable);
	},

	enableDraw: function(enable) {
		this.enableTool(this.draw, enable);
	},

	onToolClick: function(menu, tool) {
		this.activateTool(tool);
	}
});