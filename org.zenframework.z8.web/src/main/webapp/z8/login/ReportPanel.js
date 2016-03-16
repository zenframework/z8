Z8.view.ReportPanel = Ext.extend(Z8.Panel,
{
	cls: 'z8-report-panel',
	header: false,
	closable: true,
	clearable: true,
	
	initComponent: function()
	{
		this.reportContent = new Ext.Container({cls: 'x-report-panel', style: {padding: '10px'}, autoScroll: true });
		
		var clearButton = new Z8.Button({iconCls: 'icon-clear', align: 'right', handler: this.onClear, scope: this});
		var closeButton = new Z8.Button({iconCls: 'icon-close', align: 'right', handler: this.onClose, scope: this});
		var text = new Ext.Toolbar.TextItem({text: 'Сообщения'});
		
		var toolButtons = [text];

		if (this.clearable) {
			toolButtons.push(clearButton);
		}
		if(this.closable) {
			toolButtons.push(closeButton);
		}
	
		var config = { items: this.reportContent };
		
		if(this.showToolbar)
		{
			config.tbar = new Z8.Toolbar({ cls: 'z8-toolbar', items: toolButtons });
		}
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		
		Z8.view.ReportPanel.superclass.initComponent.call(this);
		
		this.reportContent.on('afterrender', this.onReportContentRender.createDelegate(this), this);
	},
	
	onReportContentRender: function()
	{
		this.reportContent.getEl().on('click', this.onViewClick.createDelegate(this), this, {delegate: 'a.viewlink'});
		this.reportContent.getEl().on('click', this.onRequiredClick.createDelegate(this), this, {delegate: 'a.requiredlink'});
		this.reportContent.getEl().on('click', this.onLogClick.createDelegate(this), this, {delegate: 'a.log'});
	},
	
	onClear: function()
	{
		this.clear();
	},
	
	clear: function()
	{
		if(this.reportContent.getEl())
		{
			this.reportContent.getEl().update('');
		}
	},
	
	onClose: function()
	{
		this.collapse();
	},
	
	onMessage: function(title, info, overwrite)
	{
		if(info != null)
		{
			var now = new Date().format('Y-m-d h:i:s');

			var html = '';
			
			Ext.each(info.messages, function(message)
			{
				message = Z8.Format.nl2br(message);
				message = message.replace(/{a/g, '<a href="#" class="viewlink" ').replace(/{\\a}/g, '</a>').replace(/a}/g, '>');
				
				html += '<div class="report-result">';				
				
				if(title != null)
				{
					html += '<div class="report-command">' + title + ' (' + now + ')</div>';
				}
				
				html += '<div>' + message + '</div></div>';
			}, this);

			if(info.log != null)
			{
				
				html += '<div class="report-result">';				
				html += '<div>Операция завершилась с ошибками.</div>';
				html += '<div><a href="#" class="log" file="' + info.log + '" serverId="' + info.serverId + '">Подробнее...</a></div></div>'
			}
			
			var el = this.reportContent.getEl();
			Ext.DomHelper[overwrite ? 'overwrite' : 'append'](el, html);
			/*			
			if(info.log != null)
			{
				var html = '<div class="report-result">';				
			
				if(title != null)
				{
					html += '<div class="report-command">' + title + ' (' + now + ')</div>';
				}
				
				html += '<div>' + info.log + '</div></div>';

				this.reportContent.getEl().createChild(html);
			}
*/			
			this.scrollToLastReportMsg();
		}
	},
	
	onRequired: function(column, record, panel)
	{
		if(this.collapsed)
		{
			this.toggleCollapse();
		}
		else
		{
			this.show();
			this.ownerCt.doLayout();
		}
		
		var p;
		if (panel == 'master'){
			p = this.ownerCt.master;
		}else{
			p = this.ownerCt.detail;
		}
		
		var cm = p.grid.getColumnModel();
		var colIndex = cm.findColumnIndex(column.dataIndex);

		var now = new Date().format('Y-m-d h:i:s');
		
		var html = '<div class="report-result error">';
			html += '<div class="report-command">Невозможно сохранить форму (' + now + ')</div>';
			html += '<div>Небходимо заполнить поле ';
			html += '<a href="#" class="requiredlink" columnindex="' + colIndex + '" recordid="' + record.id + '" panel="' + panel + '" >' + column.header + '</a>';
			html += ' в таблице <strong>' + p.query.text + '</strong></div></div>';
		
		this.reportContent.getEl().createChild(html);
		this.scrollToLastReportMsg();
	},
	
	onRequiredClick: function(e, t, o)
	{
		e.stopEvent();
		
		var target = e.getTarget();
		
		var	panel = target.getAttribute('panel'),
			colIndex = target.getAttribute('columnindex'),
			recordId = target.getAttribute('recordid');
		
		var grid;
		if (panel == 'master'){
			grid = this.ownerCt.master.grid;
		}else{
			grid = this.ownerCt.detail.grid;
		}
		
		var record = grid.getStore().getById(recordId);
		
		var rowIndex = grid.getStore().indexOf(record);
		grid.getView().focusRow(rowIndex);
		grid.startEditing(record, true);
		
		var cm = grid.getColumnModel();
		var editor = cm.getCellEditor(colIndex, rowIndex);
		editor.field.focus(true, 500);
	},
	
	onViewClick: function(e, t, o)
	{
		e.stopEvent();
		
		var target = e.getTarget();
		var recordId = target.getAttribute('recordId')
		var params = {};
		
		if(recordId){
			params.filterBy = recordId;
		}
		
		Z8.viewport.open(target.id, params);
	},
	
	onLogClick: function(e, t, o)
	{
		e.stopEvent();
		
		var target = e.getTarget();
		
		var params = 
		{
			source: target.getAttribute('file'),
			serverId: target.getAttribute('serverId')
		};
	
		Z8.FileViewer.show(params.source);		
	},

	scrollToLastReportMsg: function()
	{
		var el = this.reportContent.getEl();
    	if(el)
    	{
    		oScroll = el.dom;
    		scrollDown = (oScroll.scrollHeight - oScroll.scrollTop <= oScroll.offsetHeight );
    		if ( ! scrollDown)
    		{
    			sc = (oScroll.scrollTop > 0) ? oScroll.scrollTop : oScroll.scrollHeight;
    			el.scroll('b', sc, true);
    		}
    	}
	}
});