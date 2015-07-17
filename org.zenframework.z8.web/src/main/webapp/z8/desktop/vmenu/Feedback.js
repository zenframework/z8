Z8.desktop.FeedbackPanel = Ext.extend(Ext.Panel,
{
	border: false,
	layout: 'vbox',
	layoutConfig: { align: 'stretch' }, 

	initComponent: function()
	{
		Z8.desktop.FeedbackPanel.superclass.initComponent.call(this);
		
		var sendButton = new Ext.Button({ text: 'Спросить', iconCls:'silk-send-message', handler: this.sendMessage, scope: this });
		var text = new Ext.Panel({ border: false, bodyStyle: 'margin-top: 5px; margin-bottom: 5px;', html: Z8.vars.feedBackText, height: 50 });
		this.textArea = new Ext.form.TextArea({ flex: 1 });
		
		this.add([text, this.textArea]);
		this.addButton(sendButton);
	},
	
	sendMessage: function()
	{
		var text = this.textArea.getValue();

		if(!Z8.isEmpty(text))
		{
			var parameters = { message: 'send', text: Ext.util.Format.nl2br(Ext.util.Format.htmlEncode(text)), user: Z8.user.login, recipient: Ext.encode(['Support']) };
			Z8.Ajax.request(null, this.onMessageSent, Ext.emptyFn, parameters, this);
		}
	},
	
	onMessageSent: function()
	{
		Z8.showMessages("Z8", "Спасибо за вопрос! Пожалуйста, оставайтесь в системе в ближайшее время и я вам отвечу.");
		this.textArea.setValue('');
	}
});

Z8.desktop.Feedback = Ext.extend(Z8.desktop.BaseMenuItem,
{
	iconCls: 'icon-feedback',
	vertical: true,
	
	menuAlign: 'bl-br',
	zIndex: 14001,

	initComponent: function()
	{
		Z8.desktop.Feedback.superclass.initComponent.call(this);

		var panel = new Z8.desktop.FeedbackPanel({ height: 300, width: 400 });
		this.menu = new Z8.menu.MulticolumnMenu({ defaultOffsets: [-1, 40], zIndex: 14000, items: panel });
	}
});