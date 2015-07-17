Z8.messenger.ContactList = Ext.extend(Z8.List, {
	
	multiSelect: true,
	checkable: false,
	editable: false,
	
	initComponent:function()
	{
		Z8.messenger.ContactList.superclass.initComponent.apply(this, arguments);
	},
	
	buildTpl: function(config)
	{	
		config.tpl = new Ext.XTemplate(
			'<tpl for=".">',
				'<div class="x-item" id="{[this.genId()]}">',
					'<div class="x-item-wrap">{name} <span id="contact_counter_{index}" class="contact-counter">',
					'<tpl if="count &gt; 0">{count}</tpl>',
					'</span></div>',
				'</div>',
			'</tpl>'
		, {
			genId: function() {
				return Ext.id();
			}
		}, this);
	}
});

Z8.messenger.MessengerPanel = Ext.extend(Ext.Container,
{
	width: 560,
	height: 350,
	layout: 'border',
	contactsWidth: 190,
	msgBoxHeight: 60,
	
	initComponent: function ()
	{
		this.contactList = new Z8.messenger.ContactList({ store: this.contactsStore });
		this.textArea = new Ext.form.TextArea({name:'txtMsg', region:'center', enableKeyEvents : true, disabled: true });
		this.sendBtn = new Ext.Button({ id: 'sendButton', disabled: true, text:'Отправить', iconCls:'silk-send-message', flex:1 });
		this.msgBox = new Ext.form.DisplayField({ id: 'messageBox', style: { whiteSpace: 'normal' }, border: true, width: 200, region: 'center', cls: 'x-form-text', autoScroll: true });

		Ext.apply(this, {
			items: [{
				id: 'userList',
				region: 'west',
				width: this.contactsWidth,
				margins: '2 2 2 2',
				autoScroll: true,
				border: false,
				bodyStyle: 'padding:5px;',
				items: this.contactList
			}, {
				id:'chatPanel',
				border: false,
				margins: '2 2 0 0',
				region:'center',
				layout:'border',
				items:[
				    this.msgBox,
					{
						region: 'south',
						split: true,
						height: this.msgBoxHeight,
						layout: 'border',
						border: false,
						margins: '2 2 2 0',
						items: [this.textArea, this.buildSendButton()]
					}
				]
			}]
       	});

       	Z8.messenger.MessengerPanel.superclass.initComponent.apply(this, arguments);
       	
       	
    },
	
	buildSendButton: function()
	{
		return {
			border: false,
			region: 'east',
			margins: '0 0 0 2',
			width: 90,
			layout: 'vbox',
			layoutConfig: {
				padding:'0',
				align:'stretch'
			},
			items: this.sendBtn
		};
	}
});

Z8.messenger.Window = Ext.extend(Z8.Window, {
	
	initComponent: function() {
		
		var maxWidth = Ext.getBody().getViewSize().width * 0.8;
		var maxHeight = Ext.getBody().getViewSize().height * 0.8;

		var config = {
			title: 'Коммуникатор',
			width: (maxWidth < 600) ? maxWidth : 600,
			height: (maxHeight < 400) ? maxWidth : 400,
			layout: 'fit'
		};

		Ext.apply(this, Ext.apply(this.initialConfig, config));

		Z8.messenger.Window.superclass.initComponent.apply(this, arguments);
		
		this.on('resize', this.onWindowResize, this);
	},
	
	onWindowResize: function(window, width, height)
	{
		var messengerPanel = this.items.get(0);
		var el = messengerPanel.msgBox.getEl();
		if(el)
		{
			var msgEls = el.select('.box-chat-message').setStyle('width', width - 360);
		}
	},
	
	onClose: function() {
		this.hide();
	}
	
});