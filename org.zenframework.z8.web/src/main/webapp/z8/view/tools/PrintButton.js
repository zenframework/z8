Z8.view.PrintWindow = Ext.extend(Z8.Window, {
	
	width:420,
	height:240,
	title: 'Настройки печати',
	layout:'fit',
	
	initComponent: function()
	{
		this.saveButton = new Z8.Button({
			tooltip: 'Сохранить',
			iconCls: 'icon-save',
			iconAlign: 'top',
			text: 'Сохранить',
			scope: this,
			handler: this.onSave
		});
		
		this.cancelButton = new Z8.Button({
			tooltip: 'Отмена',
			iconCls: 'icon-close',
			iconAlign: 'top',
			text: 'Отмена',
			scope: this,
			handler: this.onCancel
		});
		
		this.printButton = new Z8.Button({
			tooltip: 'Печать',
			iconCls: 'icon-print',
			iconAlign: 'top',
			text: 'Печать',
			scope: this,
			handler: this.onPrint
		});
		
		this.form = new Ext.form.FormPanel({
			xtype: 'form',
			labelWidth: 70,
			frame: true,
			style: {paddingTop: '5px'},
			items:[{
				xtype:'combo',
				name: 'pageFormat',
				anchor:'-18',
				fieldLabel:'Размер',
				mode: 'local',
				store : new Ext.data.SimpleStore({
					data : [
					        ['A1', 'A1'],
							['A2', 'A2'],
							['A3', 'A3'],
							['A4', 'A4']
					],
					fields : ['value', 'text']
				}),
				valueField : 'value',
				value: this.defaultValues.pageFormat,
				displayField : 'text',
				triggerAction : 'all',
				editable : false
			},{
	            xtype: 'radiogroup',
	            fieldLabel: 'Ориентация',
	            items: [
	                {boxLabel: 'Книжная', name: 'pageOrientation', inputValue: 'portrait', checked: this.defaultValues.pageOrientation == 'portrait' ? true: false },
	                {boxLabel: 'Альбомная', name: 'pageOrientation', inputValue: 'landscape', checked: this.defaultValues.pageOrientation == 'landscape' ? true: false }
	            ]
	        },{
	        	xtype: 'fieldset',
	        	title: 'Поля',
	        	items: [{
		        	layout:'column',
					defaults:{
						columnWidth: 0.5,
						layout: 'form',
						border: false,
						xtype: 'panel',
						bodyStyle:'padding:0 18px 0 0'
					},
					items:[{
						defaults:{anchor:'100%'},
						items:[{
							xtype: 'textfield',
							fieldLabel: 'Левое',
							name: 'leftMargin',
							value: this.defaultValues.leftMargin
						},{
							xtype: 'textfield',
							fieldLabel: 'Верхнее',
							name: 'topMargin',
							value: this.defaultValues.topMargin
						}]
					},{
						defaults:{anchor:'100%'},
						items:[{
							xtype: 'textfield',
							fieldLabel: 'Правое',
							name: 'rightMargin',
							value: this.defaultValues.rightMargin
						},{
							xtype: 'textfield',
							fieldLabel: 'Нижнее',
							name: 'bottomMargin',
							value: this.defaultValues.bottomMargin
						}]
					}]
		        		
		        }]
	        }]
		});
		
		var config = {
			items:this.form,
			buttons: [this.printButton, this.saveButton, this.cancelButton]
		};
	
		// apply config
	    Ext.apply(this, Ext.apply(this.initialConfig, config));
    
	    Z8.view.PrintWindow.superclass.initComponent.call(this);
	},
	
	onCancel: function()
	{
		this.onClose();
	},
	
	onSave: function()
	{
		Z8.Ajax.request('settings', this.onClose, Ext.emptyFn, { data: Ext.encode(this.setSettings()) }, this);
	},
	
	setSettings: function()
	{
		var values = this.form.getForm().getValues();
		var settings = Z8.getUserSettings();
		
		if(!settings.print)
		{
			settings.print = {};
		}
		
		settings.print[this.query.requestId] = values;
		
		return settings;
	},
	
	onPrint: function()
	{
		Z8.Ajax.request('settings', this.doPrint, Ext.emptyFn, { data: Ext.encode(this.setSettings()) }, this);
	},
	
	doPrint: function()
	{
		this.printButton.fireEvent('report', this.printButton, this.printButton.menu.format, null);
		this.onClose();
	},
	
	onClose: function()
	{
		this.hide();
	}
});

Z8.view.PrintButton = Ext.extend(Z8.SplitButton,
{
	query: null,

	text: 'Печать',
	tooltip: 'Печать (Ctrl+E)',
	disabled: true,
	iconCls: 'icon-print',
	iconAlign: 'top',
	split: true,
	
	initComponent: function()
	{
		Z8.view.PrintButton.superclass.initComponent.call(this);
		
		this.addEvents('report');
		
		var reports = [];
		reports[0] = { id: '', text: 'Печать', handler: this.onMenu, scope: this };
		reports[1] = { id: '', text: 'Настройка печати', handler: this.onSettings, scope: this };

		if(!Ext.isEmpty(this.query.reports))
		{
			for(var i = 0; i < this.query.reports.length; i++)
			{
				var report = { id: this.query.reports[i].id, text: this.query.reports[i].text, handler: this.onMenu, scope: this };
				reports.push(report);
			}
		}
		
		this.on('click', this.toggleMenu, this);

		this.menu = new Z8.view.PrintButtonMenu({ reports: reports });
		this.menu.on('formatChanged', this.onFormatChanged, this);
	},
	
	onClicked: function()
	{
		this.fireEvent('report', this, this.menu.format, null);
	},
	
	onMenu: function(menuItem)
	{
		this.fireEvent('report', this, this.menu.format, menuItem.menuId);
	},
	
	onSettings: function()
	{
		if(!this.settingsWindow)
		{
			this.settingsWindow = new Z8.view.PrintWindow({
				printButton: this,
				query: this.query,
				defaultValues: Z8.ReportManager.getOptions(this.query)
			});
		}
		
		this.settingsWindow.show();
	},
	
	onFormatChanged: function(format)
	{
	}
});

Z8.view.PrintButtonMenu = Ext.extend(Z8.menu.MulticolumnMenu,
{
	reports: [],
	
	initComponent: function()
	{
		Z8.view.PrintButtonMenu.superclass.initComponent.call(this);

		this.addEvents('formatChanged');

		this.format = "pdf";
		
		formats = [];
		formats[0] = this.createFormatItem(Z8.Report.pdf, true);
		formats[1] = this.createFormatItem(Z8.Report.xls, false);
		formats[2] = this.createFormatItem(Z8.Report.doc, false);
		
		this.add('Формат');
		this.add('-');
		this.add(formats);
		this.add('-');
					
		for(var i = 0; i < this.reports.length; i++)
		{
			if(i==2)
			{
				this.add('Экспорт');
				this.add('-');
			}
			
			this.add(this.createReportItem(this.reports[i]));
		}
	},
	
	createReportItem: function(report)
	{
		var config = { menuId: report.id, text: report.text, handler: report.handler, scope: report.scope };
		return new Ext.menu.Item(config);
	},

	createFormatItem: function(menuId, checked)
	{
		return new Ext.menu.CheckItem({menuId: menuId, group: 'format', checked: checked, text: Z8.Report.names[menuId], iconCls: Z8.Report.icons[menuId], hideOnClick:false, handler: this.onFormat, scope: this });
	},

	onFormat: function(menuItem)
	{		
		this.format = menuItem.menuId;
		this.fireEvent('formatChanged', this.format);
	}
});