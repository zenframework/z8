Z8.Helper.Panel = Ext.extend(Z8.Panel, 
{
	title: 'Приступая к работе',
	closable: true,
	layout: 'fit',
	autoUrl: Z8.getRequestUrl() + '/helper/_desktop.html',

	historyPrev: [],
	historyNext: [],

	margins: { top: 3, left: 0, bottom: 3, right: 3 },
	
	initComponent: function()
	{
		Z8.Helper.Panel.superclass.initComponent.call(this);

		this.prevBtn = new Z8.Button({ iconCls: 'icon-prev-page', align: 'right', disabled:true, tooltip:'Назад', text: '', handler: this.onClickHistoryPrev, scope: this });
		this.nextBtn = new Z8.Button({ iconCls: 'icon-next-page',  align: 'right', disabled:true, tooltip:'Вперед', text: '', handler: this.onClickHistoryNext, scope: this });
		
		this.contentPanel = new Ext.Container({ autoScroll: true });
		this.contentPanel.on('afterrender', this.onInitShow.createDelegate(this, [true]), this);

		var toolbar = new Z8.Toolbar({ cls: 'z8-toolbar', items: [this.prevBtn, this.nextBtn] });
		var wrapper = new Ext.Panel({ layout: 'fit', border: false, tbar: toolbar, items: this.contentPanel });
		
		this.add(wrapper);
	},
	
	onInitShow: function(pushInHistory)
	{
		if(pushInHistory)
		{
			this.historyPrev.push({type:'initShow'});
		}
	
		var items = [];
	
		try
		{
			var settings = Ext.decode(Z8.user.settings);
		}
		catch(e) {}
		
		if(settings === undefined || Ext.isArray(settings))
		{
			settings = {};
		}
		
		Ext.each(Z8.viewport.loginInfo.user.components, function(component, index) {
			var components = settings.components;
			var componentSettings = components != null ? components[component.id] : null;
			if(componentSettings == null || componentSettings.state === true) {
				items.push(component);
			}
		});
		
		var tpl = new Ext.XTemplate(
			'<div class="helper-content">',
				'<p>Добро пожаловать в систему Z8! Система позволяет планировать, вести оперативный учет, контролировать выполнение плана, сверяя его с фактом и показывая отклонения, вести бухгалтерский и налоговый учет.</p>',
				'<p>Начиная работу, заполните все <a href="#" class="viewlink" id="ресурсы.юридическиеЛица.представления.ВедениеНашегоПредприятияView" title="Профиль">сведения о Вашем предприятии</a>, которые будут использованы в соответствующих нормативных документах. Иначе придется возвращаться к Профилю, как только Вы увидите пустой открывающий список в полях, которые используют эти данные.</p>',
				'<p>Ниже представлены модули, ознакомившись с которыми, Вам легко будет работать в Z8. Для более полной информации зайдите в нужный из них. (некоторые описания в доработке, приносим извинения за временные трудности)</p>',
				'<tpl for=".">',
					'<p><a href="#" class="menulink" id="{id}" title="{text}">{text}</a></p>',
				'</tpl>',
			'</div>'
		);
		
		var content = this.contentPanel.getEl();
		tpl.overwrite(content, items);
		
		content.on('click', this.onViewClick.createDelegate(this, [content], 3), this, {delegate: 'a.viewlink'});
		content.on('click', this.onMenuClick.createDelegate(this, [content], 3), this, {delegate: 'a.menulink'});
	},

	onClickHistoryPrev: function()
	{	
		n = this.historyPrev.length-2;
		n2 = this.historyPrev.length-1;
		
		if (this.historyPrev[n].type == 'initShow') {
			this.onInitShow(false);
		} else if (this.historyPrev[n].type == 'url') {
			this.loadUrl(this.historyPrev[n].url);
		} else {
			//this.openView(this.historyNext[n].view.id, this.historyNext[n].view.title);
			this.loadUrl(Z8.getRequestUrl() + '/helper/' + cyr2lat(this.historyPrev[n].view.id) + '.html');
		}
		
		this.historyNext.push(this.historyPrev[n2]);
		

		this.historyPrev.pop();
		
		if (this.historyPrev.length == 1) this.prevBtn.setDisabled(true);
		if (this.historyNext.length > 0) this.nextBtn.setDisabled(false);
	},
	
	onClickHistoryNext: function()
	{
		n = this.historyNext.length-1;
		
		if (this.historyNext[n].type == 'initShow') {
			this.onInitShow(false);
		} else if (this.historyNext[n].type == 'url') {
			this.loadUrl(this.historyNext[n].url);
		} else {
			//this.openView(this.historyNext[n].view.id, this.historyNext[n].view.title);
			this.loadUrl(Z8.getRequestUrl() + '/helper/' + cyr2lat(this.historyNext[n].view.id) + '.html');
		}
		
		this.historyPrev.push(this.historyNext[n]);		
		this.historyNext.pop();
		
		if (this.historyPrev.length>1) this.prevBtn.setDisabled(false);
		if (this.historyNext.length==0) this.nextBtn.setDisabled(true);
	},
	
	loadUrl: function(url)
	{
		var mgr = this.contentPanel.el.getUpdater();
		mgr.showLoadIndicator = false;
		mgr.disableCaching = true;
	
		mgr.on('update', function(el, response)
		{
			if(! el.child('div.helper-content'))
			{
				this.onLoadUrlFailure();
			}
			else
			{
				el.removeAllListeners();
				
				el.select('a.viewlink').removeAllListeners();
				el.select('a.viewlink').on('click', this.onViewClick.createDelegate(this, [el], 3), this);
				//el.select('a.contentlink').on('click', this.onContentLink.createDelegate(this, [el], 3), this);
				
				el.select('a.mainlink').removeAllListeners();
				el.select('a.mainlink').on('click', this.onInitShow.createDelegate(this, [el], 3), this);
			}
		}, this);

		mgr.on('failure', this.onLoadUrlFailure, this);

		mgr.update({url : url});
	
		if(this.historyPrev.length > 1)
			this.prevBtn.setDisabled(false);
	},
	
	onMenuClick: function(e, t, o , el)
	{
		el.removeAllListeners();
		Ext.get(t).removeAllListeners();
		this.openMenu(e, t);
	},

	openMenu: function(e, t)
	{
		var url = Z8.getRequestUrl() + '/helper/' + cyr2lat(t.id) + '.html';
	
		this.historyPrev.push({type:'url', url:url});
	
		this.loadUrl(url);
	},
	
	onViewClick: function(e, t, o, el)
	{
		el.removeAllListeners();
		Ext.get(t).removeAllListeners();
		this.openView(e);
	},

	openView: function(e)
	{
		e.stopEvent();
		var target = e.getTarget();
		
		this.historyPrev.push({
			type:'view',
			view: {
				id: target.id,
				title: target.title
			}
		});
		
		this.showView(target.id, target.title);
	},
	
	showView: function(id, title)
	{
		Z8.viewport.open({id: id, text: title});
	},
	
	onLoadUrlFailure: function()
	{
		this.contentPanel.el.update('<div class="helper-content"><p>Справка для выбранного пункта временно недоступна</p></div>');
	}
});