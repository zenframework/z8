Z8.define('Z8.button.GeometryTools', {
	extend: 'Z8.button.Button',

	icon: 'fa-mouse-pointer', 

	tooltip: 'Инструменты',

	select: true,
	box: false,
	ruler: true,
	move: true,
	edit: true,
	pick: false,
	draw: true,
	erase: true,
	rotate: true,

	location: true,
	yandex: true,

	htmlMarkup: function() {
		var tools = [];

		if(this.select !== false) {
			var select = this.select = this.active = new Z8.menu.Item({ text: 'Выбор объекта', icon: 'fa-mouse-pointer', isTool: true, isSelect: true });
			tools.add(select);
		}

		if(this.box !== false) {
			var box = this.box = new Z8.menu.Item({ text: 'Лассо', icon: 'fa-arrows-alt', isTool: true, isBox: true });
			tools.add(box);
		}

		if(this.ruler !== false) {
			var ruler = this.ruler = new Z8.menu.Item({ text: 'Линейка', shortcut: 'Ctrl+L', icon: 'fa-ruler-combined', isTool: true, isRuler: true });
			tools.add(ruler);
		}

		if(this.move !== false) {
			var move = this.move = new Z8.menu.Item({ text: 'Переместить объект', shortcut: 'Ctrl+M', icon: 'fa-hand-paper-o', isTool: true, isMove: true });
			tools.add(move);
		}

		if(this.edit !== false) {
			var edit = this.edit = new Z8.menu.Item({ text: 'Изменить объект', shortcut: 'Ctrl+E', icon: 'fa-pencil', isTool: true, isEdit: true });
			tools.add(edit);
		}

		if(this.pick !== false) {
			var pick = this.pick = new Z8.menu.Item({ text: 'Взять с подложки', /*shortcut: 'Ctrl+E',*/ icon: 'fa-hand-lizard-o', isTool: true, isPick: true });
			tools.add(pick);
		}

		if(this.draw !== false) {
			var draw = this.draw = new Z8.menu.Item({ text: 'Нарисовать объект', shortcut: 'Ctrl+D', icon: 'fa-pencil-square-o', isTool: true, isDraw: true });
			tools.add(draw);
		}

		if(this.erase !== false) {
			var erase = this.erase = new Z8.menu.Item({ text: 'Стереть объект', shortcut: 'Ctrl+Q', icon: 'fa-eraser', isTool: true, isErase: true });
			tools.add(erase);
		}

		if(this.rotate !== false) {
			var rotate = this.rotate = new Z8.menu.Item({ text: 'Повернуть объект', shortcut: 'Ctrl+G', icon: 'fa-reply', isTool: true, isRotate: true });
			tools.add(rotate);
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

	isToolEnabled: function(tool) {
		return tool && tool.isEnabled();
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

	isBoxActive: function() {
		return this.isToolActive(this.box);
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

	isPickActive: function() {
		return this.isToolActive(this.pick);
	},
	
	isDrawActive: function() {
		return this.isToolActive(this.draw);
	},

	isEraseActive: function() {
		return this.isToolActive(this.erase);
	},

	isRotateActive: function() {
		return this.isToolActive(this.rotate);
	},

	isSelectEnabled: function() {
		return this.isToolEnabled(this.select);
	},

	isBoxEnabled: function() {
		return this.isToolEnabled(this.box);
	},

	isRulerEnabled: function() {
		return this.isToolEnabled(this.ruler);
	},

	isMoveEnabled: function() {
		return this.isToolEnabled(this.move);
	},

	isEditEnabled: function() {
		return this.isToolEnabled(this.edit);
	},

	isDrawEnabled: function() {
		return this.isToolEnabled(this.draw);
	},

	isEraseEnabled: function() {
		return this.isToolEnabled(this.erase);
	},

	isRotateEnabled: function() {
		return this.isToolEnabled(this.rotate);
	},

	activateSelect: function() {
		this.activateTool(this.select);
	},

	activateBox: function() {
		this.activateTool(this.box);
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

	activateP: function() {
		this.activateTool(this.pick);
	},

	activateDraw: function() {
		this.activateTool(this.draw);
	},

	activateErase: function() {
		this.activateTool(this.erase);
	},

	activateRotate: function() {
		this.activateTool(this.rotate);
	},

	enableSelect: function(enable) {
		this.enableTool(this.select, enable);
	},

	enableBox: function(enable) {
		this.enableTool(this.box, enable);
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

	enablePick: function(enable) {
		this.enableTool(this.pick, enable);
	},

	enableDraw: function(enable) {
		this.enableTool(this.draw, enable);
	},

	enableErase: function(enable) {
		this.enableTool(this.erase, enable);
	},

	enableRotate: function(enable) {
		this.enableTool(this.rotate, enable);
	},

	onToolClick: function(menu, tool) {
		this.activateTool(tool);
	}
});