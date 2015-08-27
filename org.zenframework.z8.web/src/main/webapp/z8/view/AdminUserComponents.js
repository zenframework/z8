Z8.view.AdminUserComponents = Ext.extend(Z8.Window, {
	
	columnLoginId: 'id1_1_13_8',
	
	initComponent: function() {
		
		// create config object
		var config = {
			title: 'Настройки пользователей',
			width: 800,
			height: 400,
			layout: 'fit',
			items: [{
				layout: 'border',
				items: [{
					region: 'west',
					layout: 'fit',
					border: false,
					width: 250
				}, {
					region: 'center',
					layout: 'fit',
					border: true,
					bodyStyle: 'padding: 5px;'
				}]
			}]
		};
		
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		Z8.view.AdminUserComponents.superclass.initComponent.apply(this, arguments);
	},
	
	afterRender: function(container) {
		Z8.view.AdminUserComponents.superclass.afterRender.call(this, container);
		
		var task = {
			id: "org.zenframework.z8.AppServer.SystemNonGeneratables.UserEntryPointsView",
			text: "Пользователи"
		};
		
		Z8.Ajax.request(task.id, this.onUsersGrid, Ext.emptyFn, { parameters: Ext.encode({}) }, this);
	},
	
	onUsersGrid: function(response) {
		
		var grid = createGrid(response, { border: false });
		
		var columns = grid.getColumnModel();
		
		console.log(columns);
		
		columns.setHidden(0, true);
		columns.setHidden(1, true);
		columns.setEditable(2, false);
		columns.setEditable(3, false);
		columns.setHidden(4, true);
		columns.setHidden(5, true);
		columns.setHidden(6, true);
		columns.setHidden(7, true);
		columns.setHidden(8, true);

		grid.on('rowclick', this.onUsersGridClick, this);

		var userListPanel = this.items.get(0).items.get(0);
		userListPanel.add(grid);
		userListPanel.doLayout();
	},
	
	onUsersGridClick: function(grid, rowIndex, columnIndex, e) {
		
		var record = grid.getStore().getAt(rowIndex);
		
		var login = record.get(this.columnLoginId);
		
		var userSettingsPanel = this.items.get(0).items.get(1);
		
		userSettingsPanel.add(new Z8.view.UserComponentsPanel());
		userSettingsPanel.doLayout();
	}
	
	/*onUserGrid: function(parameters, serverId, id) {
		var onSuccess = this.onUserGridShow;
		var onError = Ext.emptyFn;
		
		var params = { parameters: Ext.encode(parameters) };
		
		if(serverId != null)
		{
			params.serverId = serverId;
		}
		
		Z8.Ajax.request(id, onSuccess, onError, params, this);
	},
	
	
	onUserGridShow: function(response) {
		console.log(response);
	}*/
	
});