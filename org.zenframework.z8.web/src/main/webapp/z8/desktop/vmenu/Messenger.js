Z8.desktop.Messenger = Ext.extend(Z8.desktop.BaseMenuItem,
{
	iconCls: 'icon-messenger',
	vertical: true,
	
	menuAlign: 'bl-br',
	zIndex: 14001,
	
	messengerWindow: null,
	
	handler: function() {
		
		if (this.messenger.blinker != null)
		{
			this.getEl().stopFx();
			Ext.TaskMgr.stop(this.messenger.blinker);
		}
		this.messengerWindow.show();
	},
	

	initComponent: function()
	{
		Z8.desktop.Messenger.superclass.initComponent.call(this);

		this.messengerPanel = Z8.Messenger.getMessengerPanel();
		this.messenger = Z8.Messenger.getMessenger();
		
		
		this.messengerWindow = new Z8.messenger.Window(
		{
			items: this.messengerPanel
		});
	}
});