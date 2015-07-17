Z8.FileViewer = 
{
	download: function(info, title, format)
	{
		var params = {};
		
		if(info.serverId != null)
		{
			params.path = info.source;
			params.serverId = info.serverId;
		}
		else
		{
			params.path = info.source;
			params.image = info.base64;
		}

		Z8.Ajax.request(0, this.onDownload.createDelegate(this, [title, format], true), Ext.emptyFn, params, this);
	},		

	onDownload: function(info, title, format)
	{
		this.show(info.source, title, format);
	},

	forceDownload: function(url) {
		
		Z8.PageManager.unregBeforeUnload();
		
		try
		{
			Ext.destroy(Ext.get('downloadIframe'));
		}
		catch(e) {}
		
		Ext.DomHelper.append(document.body, {
			tag: 'iframe',
			id:'downloadIframe',
			frameBorder: 0,
			width: 0,
			height: 0,
			css: 'display:none;visibility:hidden;height:0px;',
			src: url
		});
		
		Z8.PageManager.regBeforeUnload();
	},

	show: function(source, title, format)
	{
		var url = Z8.getRequestUrl() + '/' + source + '?sessionId=' + Z8.sessionId;
		
		this.forceDownload(url);
	}
};