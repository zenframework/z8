Ext.onReady(function() 
{
	Z8.initialize();
	
	Z8.History.init();
	
	var isDemoMode = window['demoLogin'] != null;
	var useMessenger = window['messagesOff'] == null;
	
	function loginCallback(result)
	{
		Z8.sessionId = result.sessionId;
		Z8.user = result.user;

		if(isDemoMode && useMessenger)
		{
			Z8.Messenger.start();
		}
		
		Z8.viewport = new Z8.desktop.Desktop({ loginInfo : result });
	
		if(!Z8.isEmpty(formToOpen))
		{
			Z8.viewport.open(formToOpen, { filterBy: formId });
			formToOpen = null;
		}
		
		// Accordion menu for selection
		Z8.componentsList = result.user.components;
	}
	
	function onLogout()
	{
		if(Z8.viewport.desktopView != null)
		{
			Z8.viewport.desktopView.destroy();
		}
		
		Z8.viewport.destroy();
		Z8.viewport = null;

		if(useMessenger)
		{
			Z8.Messenger.stop();
		}
		
		Z8.Console.stop();
		Z8.Console.clear();

		Z8.LoginManager.login(loginCallback);
	}	

	function onLogin(result)
	{
		Z8.sessionId = result.sessionId;
		Z8.user = result.user;
		
		if(useMessenger)
		{
			Z8.Messenger.start();
		}
	}
	
	if(isDemoMode)
	{
		Z8.Ajax.login(loginCallback, { requestId: Z8.request.desktopId, login: demoLogin, password: demoPassword });
	}
	else
	{
		Z8.LoginManager.login(loginCallback, null, false);
	}
	
	Ext.EventManager.onWindowResize(function(){
		if (Z8.LoginManager.loginWindow)
		{
			Z8.LoginManager.loginWindow.center();
		}
	});

	Z8.LoginManager.on('logout', onLogout);
	Z8.LoginManager.on('login', onLogin);
	
});
