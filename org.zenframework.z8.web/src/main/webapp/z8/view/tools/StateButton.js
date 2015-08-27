Z8.view.StateButton = Ext.extend(Z8.SplitButton,
{
	query: null,
	settings: null,
	
	iconAlign: 'top',
	iconCls: 'icon-table',
	split: false,
	tooltip: 'Представления',
	text: 'Представления',
	
	initComponent: function()
	{
		Z8.view.DetailButton.superclass.initComponent.call(this);
		
		this.addEvents('stateChanged', 'initClick');
		
		var requestId = this.query.requestId;
		var items = [];
		
		var defaultState = new Ext.menu.CheckItem({
			closable: false,
			menuid: 'defaultState',
			checked: true,
			group: 'states',
			text: 'По умолчанию',
			handler: this.onMenu,
			scope: this
		});
		
/*		var initState = new Ext.menu.Item({
			text: 'Загружать при открытии',
			handler: this.onInitState,
			scope: this
		});
*/		
/*		var s = new Ext.menu.Item({
			menuid: 'sendState',
			text: 'Передать',
			handler: this.onMenu,
			scope: this
		});
*/		
		items.push([/*sendState, initState, '-', */defaultState]);
		
		if (this.settings.dataviews && this.settings.dataviews[requestId])
		{
			Ext.iterate(this.settings.dataviews[requestId], function(key, value) {
				var item = new Ext.menu.CheckItem({closable: true, menuid: key, checked: false, group: 'states', text: value.text, handler: this.onMenu, scope: this});
				item.on('closeclick', this.onCloseClick, this);
				items.push(item);
			}, this);
		}
		
		if(items.length > 0)
		{
			this.menu = new Z8.menu.MulticolumnMenu({ items: items });
			this.split = true; 
		}
		
		this.on('click', this.saveState, this);

	},
	
	setState: function(state)
	{
		this.menu.items.each(function(item) {
			if (item.menuid == state) {
				item.setChecked(true);
			}
		}, this);
	},
	
	onInitState: function(menuItem)
	{
		this.fireEvent('initClick', this, menuItem);
	},
	
	onCloseClick: function(item, e)
	{	
		var settings = this.settings;
		var requestId = this.query.requestId;
		
		if (settings.dataviews[requestId][item.menuid])
			delete settings.dataviews[requestId][item.menuid];
		
		Z8.Ajax.request('settings', this.onStateRemoved.createDelegate(this, [item]), Ext.emptyFn, { data: Ext.encode(settings) }, this);
	},
	
	onStateRemoved: function(item)
	{
		var parent = item.ownerCt;
    	item.destroy();
    	parent.doLayout();
	},
	
	saveState: function()
	{
		Z8.MessageBox.prompt('Представления', 'Введите имя для нового представления:', function(btn, text) {
			if (btn == 'ok' && text != null && text != '') {
				this.onSaveState(text);
			}
		}, this);
	},
	
	onSaveState: function(text)
	{
		var settings = this.settings;
		var requestId = this.query.requestId;

		var state = {};
		state.text = text;
		state.master = this.panel.masterStateManager.getState();
		if(this.panel.detail != null) {
			state.detail = this.panel.detailStateManager.getState();
			state.dimensions = this.calculateHeighths();
			state.collapsed = this.panel.detailPanel.collapsed;
		}
	
		var id = new Ext.ux.UUID();
	
		if (settings.dataviews == undefined)
		{
			settings.dataviews = {};
		}
		if (settings.dataviews[requestId] == undefined) {
			settings.dataviews[requestId] = {};
		}
	
		var dataState = {};
		dataState[id] = state;
	
		Ext.apply(settings.dataviews[requestId], dataState);
	
		Z8.Ajax.request('settings', this.onStateSaved.createDelegate(this, [id, text]), Ext.emptyFn, { data: Ext.encode(settings) }, this);
	},
	
	onStateSaved: function(id, text)
	{
		if (this.split === false)
		{
			this.menu = new Z8.menu.MulticolumnMenu();
			this.setSplit(true);
		}
		
		var menuItem = new Ext.menu.CheckItem({closable: true, menuid: id, text: text, checked: true, group: 'states', handler: this.onMenu, scope: this });
		menuItem.on('closeclick', this.onCloseClick, this);
		
		this.menu.add(menuItem);
		this.menu.doLayout();
	},
	
	onMenu: function(menuItem)
	{
		this.fireEvent('stateChanged', this, menuItem);
	},
	
	calculateHeighths: function() {
		var panelHeight = this.panel.getEl().getHeight();
		var masterHeight = this.panel.masterPanel.getEl().getHeight();
		var detailHeight = this.panel.detailPanel.getEl().getHeight();
		
		var masterPrc = (masterHeight / panelHeight);
		var detailPrc = (detailHeight / panelHeight);
		
		return {
			master: masterPrc,
			detail: detailPrc
		};
	},
	
	isDetailCollapsed: function()
	{
		//console.log(this.panel.detailPanel);
	}
});