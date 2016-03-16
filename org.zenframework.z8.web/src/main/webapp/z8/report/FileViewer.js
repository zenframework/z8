Z8.FileViewer = {
	forceDownload: function(url) {
		
		Z8.PageManager.unregBeforeUnload();
		
		try {
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

	download: function(info, title, format) {
		var url = Z8.getRequestUrl() + '/' + info.source + '?sessionId=' + Z8.sessionId + '&serverId=' + info.serverId;
		this.forceDownload(url);
	},		

	show: function(source, title, format) {
		var url = Z8.getRequestUrl() + '/' + source + '?sessionId=' + Z8.sessionId;
		this.forceDownload(url);
	}
};