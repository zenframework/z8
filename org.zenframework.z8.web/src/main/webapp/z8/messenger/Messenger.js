Z8.messenger.Messenger = Ext.extend(Ext.util.Observable,
{
	messages: new Ext.util.MixedCollection(),
	readedMesages: 0,
	selectedContact: null,
	currentDate: null,
	
	contactsStore: new Ext.data.ArrayStore({
		fields: [{name: 'name'}, {name: 'online'}, {name: 'count'}],
		data: []
	}),
	
	viewsToSend: [],
	
	constructor: function()
	{
		if(this.messenger == null || this.messenger.isDestroyed)
		{ 
			this.messenger = new Z8.messenger.MessengerPanel({ contactsStore: this.contactsStore });
		}
		
		this.messages.on('add', this.onMessageAdded, this);
		this.messenger.contactList.on('listclick', this.onContactClick, this);
		this.messenger.sendBtn.on('click', this.sendMessage, this);
		
		this.messenger.textArea.on('specialkey', function(o, e) {
			if (e.ctrlKey && Ext.EventObject.ENTER == e.getKey()){
				this.sendMessage();
			}
		}, this);
		
		this.messenger.msgBox.on('afterrender', this.onMsgBoxAfterrender.createDelegate(this), this);
	},

	getMessengerPanel: function()
	{
		return this.messenger;
	},
	
	getMessenger: function()
	{
		return this;
	},
	
	onMsgBoxAfterrender: function()
	{
		this.messenger.msgBox.getEl().on('click', this.onViewClick.createDelegate(this), this, {delegate: 'a.viewlink'});
	},
	
	onViewClick: function(e, t, o)
	{
		e.stopEvent();
		var target = e.getTarget();
		var masterState = target.getAttribute('masterstate');
		var detailState = target.getAttribute('detailstate');
			
		Z8.viewport.open(target.id, {}, {master: Ext.decode(masterState), detail: Ext.decode(detailState)} );
	},
	
	start: function()
	{
		if(this.provider == null)
		{
			this.provider = new Ext.direct.PollingProvider(
			{
				type:'polling',
				url: Z8.request.url,
				baseParams: { message: 'get', sessionId: Z8.sessionId, user: Z8.user.login }
			});

			this.provider.on('data', this.onData, this);
			this.provider.connect();
		}
	},
	
	stop: function()
	{
		if(this.provider != null)
		{
			this.provider.disconnect();
			this.provider.un('data', this.onData, this);
			this.provider = null;
		}
	},
	
	onData: function(provider, event)
	{
		if(event.type == 'event')
		{
			if(event.success)
			{
				this.updateContactList(event.users);
				this.updateMessages(event.message);
			}
			else if(event.status == Z8.Status.AccessDenied)
			{
				this.stop();
			}
		}
	},
	
	updateMessages: function(messages)
	{
		if( (messages.length != 0) && (Z8.viewport.messenger.messengerWindow.isVisible() == false) )
		{
			if (this.blinker)
			{
				Ext.TaskMgr.stop(this.blinker);
			}
			
			this.blinker = Ext.TaskMgr.start({
			    run: this.runBlink,
			    interval: 3000
			}, this);
		}
		
		Ext.each(messages, function(message, index) {
			
			var msgObject = {
				from: message.sender,
				to: Z8.user.login,
				message: message.text,
				time: new Date().format('h:i'),
				date: new Date().format('j F Y') + ' г.'
			};
			
			this.addMessage(msgObject);
		}, this);
	},
	
	runBlink: function()
	{
		Z8.viewport.messenger.getEl().sequenceFx().fadeOut().fadeIn();
	},
	
	addMessage: function(msgObject)
	{	
		if (Ext.isIE){
			msgObject.message = this.splitLine(msgObject.message, 40);
		}
		
		this.messages.add(msgObject);
	},
	
	splitLine: function(st, n) {
		var b = '';
		var s = st;
		
		while (s.length > n)
		{
			var c = s.substring(0, n);
			var d = c.lastIndexOf(' ');
			var e =c.lastIndexOf('\n');
			if (e != -1) d = e;
			if (d == -1) d = n;
			b += c.substring(0,d) + '\n';
			s = s.substring(d+1);
		}
		return b+s;
	},
	
	onMessageAdded: function(key, msgObject)
	{
		if (this.selectedContact == msgObject.from || this.selectedContact == msgObject.to)
		{
			this.readedMesages++;
			this.showMessage(msgObject, false);
		}
		else
		{
			this.updateCounter(msgObject.from || msgObject.to)
		}
	},
	
	updateCounter: function(name)
	{
		var index = this.contactsStore.find('name', name);
		if(index !== -1)
		{
			var record = this.contactsStore.getAt(index);
			var count = record.get('count');
			record.set('count', count+1);
		}
	},
	
	showMessage: function(msgObject, plain)
	{
		if (this.currentDate == null || this.currentDate != msgObject.date)
		{
			this.currentDate = msgObject.date;
			var dateHtml = '<div class="box-chat-date">' + msgObject.date + '</div>';
			this.messenger.msgBox.append(dateHtml);
		}
		
		if(!plain){
			msgObject.message = this.decodeHTMLEntities(msgObject.message);
		}

		var classname = 'box-chat-wrap-alt';
		if (this.readedMesages % 2 == 0)
			classname = 'box-chat-wrap';
		
		var messageWidth = this.messenger.ownerCt.getWidth() - 360;
		
		var html = new Ext.Template(
        	'<div class="box-chat">',
        		'<div class="' + classname + '">',
        			'<div class="box-chat-contact">',
        				'<div>{from}</div>',
					'</div>',
					'<div class="box-chat-message" style="width:' + messageWidth + ';">{message}</div>',
					'<div class="box-chat-time">{time}</div>',
					'<div style="clear:left;height:1px;font-size:1px;border:none;margin:0; padding:0;background:transparent;">&nbsp;</div>',
				'</div>',
			'</div>'
    	).compile().apply(msgObject); 
    	
		this.messenger.msgBox.append(html);
    	
    	this.messenger.textArea.focus();
    	
    	this.scrollToLastMessage();
	},
	
	scrollToLastMessage: function()
	{
		var msgBoxEl = this.messenger.msgBox.getEl();
    	if(msgBoxEl)
    	{
    		oScroll = msgBoxEl.dom;
    		scrollDown = (oScroll.scrollHeight - oScroll.scrollTop <= oScroll.offsetHeight );
    		if ( ! scrollDown)
    		{
    			sc = (oScroll.scrollTop > 0) ? oScroll.scrollTop : oScroll.scrollHeight;
    			msgBoxEl.scroll('b',sc, true);
    		}
    	}
	},
	
	updateContactList: function(users)
	{
		Ext.each(users, function(user, index)
		{
			if (user != Z8.user.login)
			{
				var index = this.contactsStore.find('name',user);
				
				if(index !== -1)
				{
					var record = this.contactsStore.getAt(index);
				}
				else
				{
					this.contactsStore.add(new Ext.data.Record({
						name: user, online: true, count: 0
					}));
				}
			}
		}, this);
	},
	
	onContactClick: function(rec, clicks, el)
	{
		rec.set('count', 0);
		this.currentDate = null;
		
		this.readedMesages = 0;
		this.prepareMessageBox();
		this.selectedContact = rec.data.name;
		this.readMessages(rec.data.name);
	},
	
	readMessages: function(name)
	{
		this.messages.find(function(item, key) {
			if ((item.from == Z8.user.login && item.to == name) || (item.from == name && item.to == Z8.user.login)){
				this.readedMesages++;
				this.showMessage(item, false);
			}
		}, this);
	},
	
	prepareMessageBox: function()
	{
		this.messenger.sendBtn.enable();
		this.messenger.textArea.enable();
		this.messenger.msgBox.setValue('');
	},
	
	sendMessage: function()
	{
		var recipients = [];
		var text = this.messenger.textArea.getValue();
		var contactRecords = this.messenger.contactList.getSelectedRecords();
		
		Ext.each(contactRecords, function(contactRecord){
			recipients.push(contactRecord.data.name);
		});
		
		Ext.each(this.viewsToSend, function(view){
			text = text.replace(view.name, view.link);
		});
		
		var parameters = {
			message: 'send',
			text: Ext.util.Format.nl2br(Ext.util.Format.htmlEncode(text)),
			user: Z8.user.login,
			recipient: Ext.encode(recipients)
		};
		
		Z8.Ajax.request(null, this.onSendMessage, Ext.emptyFn, parameters, this);
	},
	
	onSendMessage: function()
	{
		var text = this.messenger.textArea.getValue();
		var contactRecords = this.messenger.contactList.getSelectedRecords();
		
		Ext.each(contactRecords, function(contactRecord){
			var msgObject = {
				from: Z8.user.login,
				to: contactRecord.data.name,
				message: text,
				time: new Date().format('h:i'),
				date: new Date().format('j F Y') + ' г.'
			};
			
			this.addMessage(msgObject);
		}, this);
		
		this.viewsToSend = [];
		this.messenger.textArea.setValue('');
		this.messenger.textArea.focus();
	},
	
	decodeHTMLEntities: function(str)
	{
		var temp_div = document.createElement('div');
		temp_div.innerHTML = str;
		return temp_div.firstChild.nodeValue;
	}
});

Ext.apply(Z8.Messenger, new Z8.messenger.Messenger());
