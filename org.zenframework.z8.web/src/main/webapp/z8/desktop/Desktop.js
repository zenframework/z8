Z8.desktop.Desktop = Ext.extend(Ext.Viewport, 
{
	layout: 'fit',
	cls: 'x-unselectable',
	loginInfo: null,

	helpWindows: null,
	
	defaults: { border:false, frame:false },

	initComponent: function()
	{
		Z8.desktop.Desktop.superclass.initComponent.call(this);
        
		Z8.viewport = this;
		
		this.userSettings = Z8.getUserSettings();
		
		this.desktopBody = new Ext.Container({ flex: 1, layout: 'vbox', cls: 'z8-desktop-body', layoutConfig: { align: 'stretch' } });
		
		var titleBar = new Ext.Container({ cls: 'z8-bar z8-title-bar', layout: 'hbox', layoutConfig: { overflowHandler: 'HorizontalMenu', align: 'stretch', pack: 'end' }, items: this.createTitleBarItems() });
		this.menuBar = new Ext.Container({ cls: 'z8-bar z8-menu-bar', layout: 'hbox', layoutConfig: { overflowHandler: 'HorizontalMenu', align: 'stretch' }, items: this.createMenuBarItems() });

		var lastTasks = new Z8.desktop.LastTasks();
		var feedback = new Z8.desktop.Feedback();
		this.messenger = new Z8.desktop.Messenger();

		var verticalMenuItems = [new Z8.Toolbar.Separator(), lastTasks, new Z8.Toolbar.Separator(), this.messenger, new Z8.Toolbar.Separator()];

		if(Z8.vars.feedBackBtn)
		{
			verticalMenuItems.push(feedback);
			verticalMenuItems.push(new Z8.Toolbar.Separator());
		}
		
		var verticalMenu = new Z8.Toolbar({vertical: true, items: verticalMenuItems });

		var desktop = new Ext.Container(
		{
			flex: 1,
			cls: 'z8-desktop',
			layout: 'hbox',
			layoutConfig: { align: 'stretch', pack: 'start' },
			items: [ verticalMenu, this.desktopBody ]
		});
		
		var panel = new Ext.Container(
		{
			layout:'vbox',
			layoutConfig: { align: 'stretch', pack: 'start' },
			items: [ titleBar, this.menuBar, desktop ]
		});		

		this.add(panel);
		
		this.dashboardPanel = new Z8.desktop.Dashboard({id: 'desktopId', flex: 1});
		Z8.TaskManager.register(this.dashboardPanel, this.dashboardPanel.id, false);
		Z8.TaskManager.activate(this.dashboardPanel, this.dashboardPanel.id);
	},
	
	afterRender: function()
	{   
		Z8.desktop.Desktop.superclass.afterRender.call(this);
		this.logo.getEl().on('click', this.onLogoClick, this);
    },
    
    onLogoClick: function()
    {
   		this.onShowDesktop();
    },
	
	userName: function()
	{
		var name = this.loginInfo.user.name;
		return name != null && name != '' ? name : this.loginInfo.user.login;
	},

	createTitleBarItems: function()
	{
		var items = [];
		items.push({ text: 'Настройки', handler: this.onSettings, scope: this });
		items.push({ text: 'Изменить пароль', handler: this.onChangePassword, scope: this });
		
		items.push({ text: 'Рабочий стол', handler: this.onShowDesktop, scope: this });
		
		var settingsMenu = new Z8.menu.MulticolumnMenu({ items: items });
		
		var settings = new Z8.SplitButton({ iconCls: 'icon-profile', text: Z8.user.login, menu: settingsMenu, handler: this.onShowDesktop, scope:this });

		var profile = new Z8.Button({ iconCls: 'icon-factory', text: 'Наше предприятие', handler: this.onProfile });

		var exit = new Z8.Button({ iconCls: 'icon-exit', text: 'Выход', handler: this.onExit });
		var spacer = new Ext.Container({ margins: {top: 0, right: 0, bottom: 0, left: 70} });
		
		var items = [];
		
		items.push(settings);
		
		if(Z8.vars.profileBtn)
		{
			items.push(profile);
		}
		
		items = items.concat([ exit, spacer]);
		
		return items;
	},
	
	createMenuBarItems: function()
	{
		var settings = Z8.getUserSettings();
		
		settings.components = settings.components || {};
		
		var menuItems = this.applyMenuBarItemsSettings(settings);
		
		var items = [];
		
		this.logo = new Ext.Container({ cls: 'z8-menu-bar-logo', margins: {top:0, right:10, bottom:0, left:0} });
		items.push(this.logo);
		
		Ext.each(menuItems, function(item, index){
			item.value.text = item.text;
			var menuItem = new Z8.desktop.MenuItem({component: item.value, height:38, hidden: !item.state });
			items.push(menuItem);
		});
		
		return items;
	},
	
	applyMenuBarItemsSettings: function(settings)
	{
		var menuItems = this.loginInfo.user.components;
		
		if (settings === undefined) {
			settings = {};
		}
		
		var components = settings.components;
		
		var items = [];
		
		Ext.each(menuItems, function(item, index){
			var menuItem = {key: item.id, value: item, text: item.text, state: true};
			
			if(components)
			{
				var setted = components[item.id];		
				if (setted)
				{
					menuItem.text = setted.newname || item.text;
					menuItem.state = setted.state == true ? true : false;
				}
			}
			
			items.push(menuItem);
		});
		
		
		var added = components.added;
		
		if(added)
		{
			Ext.each(added, function(item, index){
				var menuItem = {key: item.id, value: item, text: item.name, state: true};
				
				var setted = components[item.id];
				if (setted)
				{
					menuItem.text = setted.newname || item.name;
					menuItem.state = setted.state == true ? true : false;
				}
				
				items.push(menuItem);
			});
		}
		
		
		var componentsorder = settings.componentsorder;
		
		if (componentsorder)
		{
			var orderedItems = [];
			var nonOrderedItems = [];
			
			Ext.each(items, function(item, index) {
				var cindex = componentsorder.indexOf(item.key);
					
				if(cindex !== -1){
					orderedItems[cindex] = item;
				} else {
					nonOrderedItems.push(item);
				}
			});
			
			items = orderedItems.concat(nonOrderedItems);
		}
		
		
		return items;
	},
	
	getTaskId: function(requestId, parameters)
	{
		var id = requestId;
		
		if(parameters != null)
		{
			id += Ext.encode(parameters);
		}
		
		return id;
	},
	
	open: function(task, queryParameters, states)
	{
		var id = Ext.isString(task) ? task : task.id;
		
//		if(this.shortcutsBtn)
//		{
//			if(id != 'shortcutsId')
//			{
//				this.shortcutsBtn.enable();
//			}
//			else
//			{
//				this.shortcutsBtn.disable();
//			}
//		}
		
		var view = Z8.TaskManager.getTask(this.getTaskId(id, queryParameters));
		
		if(view != null)
		{
			Z8.TaskManager.activate(view);
		}
		else
		{
			if(task.service != null)
			{
				queryParameters = queryParameters || {};
				queryParameters.service = task.service; 
			}
				
			if(task.parameters != null)
			{
				var parameters = task.parameters;
				

				if(parameters.length != 0)
				{
					var parametersWindow = new Z8.view.ParametersWindow({ title: task.text, record: task.record, parameters: parameters, baseParams: queryParameters });
					parametersWindow.on('OK', this.onParameters.createDelegate(this, [id, queryParameters], true), this, { single: true});
					parametersWindow.show();
					return;
				}	
			}	
					
			this.onParameters({}, null, id, queryParameters, states);
		}
	},

	onParameters: function(parameters, serverId, id, queryParameters, states)
	{
		var onSuccess = this.onTaskStart.createDelegate(this, [queryParameters, parameters, states], true);
		var onError = Ext.emptyFn;
		
		var params = { parameters: Ext.encode(parameters) };
		
		if(serverId != null)
		{
			params.serverId = serverId;
		}
		
		Ext.apply(params, queryParameters);
		
		Z8.Ajax.request(id, onSuccess, onError, params, this);
    },
	
	followLink: function(link, query, success, error)
	{
		var onSuccess = this.onFollowLink.createDelegate(this, [success], true);
		var onError = this.onFollowLinkError.createDelegate(this, [error], true);
		
		var params = { xaction: 'follow', link: Ext.encode(link) };
		
		query.request(onSuccess, onError, params, this);
	},
    
	onFollowLink: function(response, callback)
	{
		this.onTaskStart(response);

    	
		if(callback != null)
		{
			callback.call(response.info);
		}

	},

	onFollowLinkError: function(info, callback)
	{
		callback.call(info);
	},
	
	onTaskStart: function(response, queryParameters, parameters, states)
	{

		if(response.jobId != null)
		{
			this.startJob(response, queryParameters, parameters);
		}
		else
		{
			this.createMainView(response, queryParameters, states);
		}
	},

	createMainView: function(query, queryParameters, states)
	{
		var view = new Z8.view.MasterDetailPanel( { flex: 1, query: query, states: states } );
		Z8.TaskManager.register(view, this.getTaskId(query.requestId, queryParameters));
	},
	
	startJob: function(job, queryParameters, parameters)
	{
		Z8.Console.start(job, queryParameters, parameters);
	},
 
	showMessenger: function()
	{
		//this.messenger.showMenu();
		this.messenger.messengerWindow.show();
	},
	
 	onHelp: function()
 	{
		if (!this.helpWindow)
			this.helpWindow = new Z8.HelpWindow();
		
		this.helpWindow.setPagePosition((Ext.getBody().getViewSize().width - 545), '70');
		this.helpWindow.show();
		this.helpWindow.activate();
 	},
 	
 	onProfile: function()
	{
 		Z8.viewport.open('ресурсы.юридическиеЛица.представления.ВедениеНашегоПредприятияView');
	},

 	
 	onSettings: function()
	{
		if(!this.settingsWindow)
			this.settingsWindow = new Z8.view.UserSettings();
		this.settingsWindow.show();
	},

 	onChangePassword: function()
	{
		new Z8.view.ChangePasswordDialog().show();
	},
	
	onShowDesktop: function(button)
	{
		Z8.TaskManager.activate(this.dashboardPanel);
	},

	onExit: function()
 	{
		var handler = Ext.emptyFn;
		var dirtyForms = Z8.TaskManager.getDirtyViews();
		
		if(!Z8.isEmpty(dirtyForms))
		{
			var msg = 'У вас есть несохраненные формы:';
			msg += '<ol>';
			
			Ext.each(dirtyForms, function(dirtyForm)
			{
				msg += '<li>' + dirtyForm.query.text + '</li>';
			});
			
			msg += '</ol>';
			
			msg += 'Сохранить их перед закрытием?';
			
			handler = function(action)
			{
				if(action == 'yes')
				{
					Z8.viewport.saveDirtyForms(dirtyForms);
				}
			};
			
			Z8.MessageBox.confirm('Выход', msg, handler, this);
		}
		else
		{
			handler = function(action)
			{
				if(action == 'yes')
				{
					Z8.LoginManager.logout();            
				}
			};
		
			Z8.MessageBox.confirm('Выход', 'Вы действительно хотите выйти из системы?', handler);
		}
	},
	
	saveDirtyForms: function(dirtyForms)
	{
		var total = dirtyForms.length;
		var succeeded = [];
		var failed = [];
		
		var callback = function(form, result)
		{
			(result ? succeeded : failed).push(form);
			
			if(succeeded.length == total)
			{
				Z8.LoginManager.logout();
			}
			
			if(succeeded.length + failed.length == total && failed.length > 0)
			{
				var msg = 'Возникли ошибки при сохранении форм:';
				
				msg += '<ol>';
				
				Ext.each(failed, function(form)
				{
					msg += '<li>' + form.query.text + '</li>';
				});
				
				msg += '</ol>';
				
				Z8.MessageBox.alert('Ошибка при выходе', 'Возникли ошибки при сохранении форм:');
			}
		};
		
		Ext.each(dirtyForms, function(dirtyForm)
		{
			var cb = Z8.Callback.create(callback, this, dirtyForm);
			dirtyForm.save(cb, true);
		});
	},
	
	onDestroy: function()
	{
		Z8.desktop.Desktop.superclass.onDestroy.call(this);
	}
});