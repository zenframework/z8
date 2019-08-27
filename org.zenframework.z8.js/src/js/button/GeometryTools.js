Z8.define('Z8.button.GeometryTools', {
	extend: 'Z8.button.Group',

	compact: true,

	select: true,
	box: false,
	lasso: false,
	ruler: true,
	move: true,
	edit: true,
	pick: false,
	draw: true,
	erase: true,
	rotate: true,

	location: true,
	yandex: true,

	constructor: function(config) {
		config = config || {};
		this.callParent(config);

		this.createTools();

		if (this.compact)
			this.createMenuButton();
		else {
			this.radio = true;
			this.createToolButtons();
		}

		var tools = this.tools;
		if(tools.length > 0)
			this.active = tools[0];
	},

	completeRender: function() {
		this.callParent();
	},

	createTools: function() {
		var tools = this.tools = [];

		if(this.select !== false)
			tools.add({ tool: 'select', config: { text: 'Выбор объекта', icon: 'fa-mouse-pointer', isTool: true, isSelect: true } });

		if(this.box !== false)
			tools.add({ tool: 'box', config: { text: 'Прямоугольное выделение', icon: 'fa-select-box', isTool: true, isBox: true } });
		
		if(this.lasso !== false)
			tools.add({ tool: 'lasso', config: { text: 'Лассо', icon: 'fa-line-lasso', isTool: true, isLasso: true } });

		if(this.ruler !== false)
			tools.add({ tool: 'ruler', config: { text: 'Линейка', shortcut: 'Ctrl+L', icon: 'fa-ruler-combined', isTool: true, isRuler: true } });

		if(this.move !== false)
			tools.add({ tool: 'move', config: { text: 'Переместить объект', shortcut: 'Ctrl+M', icon: 'fa-hand-paper-o', isTool: true, isMove: true } });

		if(this.edit !== false)
			tools.add({ tool: 'edit', config: { text: 'Изменить объект', shortcut: 'Ctrl+E', icon: 'fa-pencil', isTool: true, isEdit: true } });

		if(this.pick !== false)
			tools.add({ tool: 'pick', config: { text: 'Взять с подложки', /*shortcut: 'Ctrl+E',*/ icon: 'fa-hand-lizard-o', isTool: true, isPick: true } });

		if(this.draw !== false)
			tools.add({ tool: 'draw', config: { text: 'Нарисовать объект', shortcut: 'Ctrl+D', icon: 'fa-pencil-square-o', isTool: true, isDraw: true } });

		if(this.erase !== false)
			tools.add({ tool: 'erase', config: { text: 'Стереть объект', shortcut: 'Ctrl+Q', icon: 'fa-eraser', isTool: true, isErase: true } });

		if(this.rotate !== false)
			tools.add({ tool: 'rotate', config: { text: 'Повернуть объект', shortcut: 'Ctrl+G', icon: 'fa-repeat', isTool: true, isRotate: true } });

		this.dividerPos = tools.length;

		if(this.location !== false)
			tools.add({ tool: 'location', config: { text: 'Мое местоположение', icon: 'fa-crosshairs', isTool: false, isLocation: true } });

		if(this.yandex !== false)
			tools.add({ tool: 'yandex', config: { text: 'Открыть в Яндекс', icon: 'fa-yandex', isTool: false, isYandex: true } });
	},
	
	createMenuButton: function() {
		var tools = this.tools;
		var dividerPos = this.dividerPos;
		
		for(var i = 0, n = tools.length; i < n; i++) {
			var tool = tools[i];
			this[tool.tool] = tools[i] = new Z8.menu.Item(tool.config);
		}
		
		if(dividerPos != 0 && dividerPos != tools.length)
			tools.insert(new Z8.list.Divider(), dividerPos);

		var menuButton = this.menuButton = new Z8.button.Tool({ icon: 'fa-mouse-pointer', tooltip: 'Инструменты' });
		menuButton.on('click', this.toggleMenu, this);

		var menu = menuButton.menu = new Z8.menu.Menu({ items: tools });
		menu.on('itemClick', this.onMenuItemClick, this);

		this.add(menuButton);
	},
	
	createToolButtons: function() {
		var tools = this.tools;
		var dividerPos = this.dividerPos;
		
		for(var i = 0, n = tools.length; i < n; i++) {
			var tool = tools[i];
			var config = tool.config;
			config.tooltip = config.text;
			config.text = undefined;
			config.toggle = config.isTool;
			tool = this[tool.tool] = tools[i] = new Z8.button.Tool(config);
			tool.on(tool.toggle ? 'toggle' : 'click', this.onToolButtonClick, this);
			this.add(tool);
		}
		
		if(tools.length > 0)
			tools[0].setToggled(true, true);
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
			if (this.menuButton != null)
				this.menuButton.setIcon(tool.icon);
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
	
	isLassoActive: function() {
		return this.isToolActive(this.lasso);
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
	
	isLassoEnabled: function() {
		return this.isToolEnabled(this.lasso);
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
	
	activateLasso: function() {
		this.activateTool(this.lasso);
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
	
	enableLasso: function(enable) {
		this.enableTool(this.lasso, enable);
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

	onMenuItemClick: function(menu, tool) {
		this.activateTool(tool);
	},
	
	onToolButtonClick: function(tool) {
		this.activateTool(tool);
	}
});