Z8.List = Ext.extend(Z8.Dataview, {
	
	overClass: 'x-view-over',
	itemSelector: 'div.x-item',
	selectedClass: 'x-view-selected',
	singleSelect: true,
	autoScroll: true,
	checkable: true,
	editable: false,
	cls: 'z8-dataview',
	btnCls: null,
	overBtnCls: null,
	
	initComponent:function() {
		Z8.List.superclass.initComponent.apply(this, arguments);
		
		this.addEvents('listclick', 'checkclick', 'itemedit', 'btnclick'/*, 'drop'*/);
    },
    
    afterRender: function(container)
	{
    	Z8.List.superclass.afterRender.call(this, container);
		
		this.store.loadData(this.populateData(), false);
		
		//if(this.editable){
		//	this.getEl().on('dblclick', this.onDblClick, this, {delegate: '.x-item-wrap'}, this);
		//}
		
		this.on('click', this.onListClick, this);
		this.on('enterkey', this.onEnterKeyPress, this);
		
		if(this.overBtnCls){
			this.getEl().on('mousemove', this.onMouseMove, this);
		}
	},
	
	populateData: function()
	{
		return [];
	},
    
    buildFields: function()
	{
    	return [{name: 'index'}, {name: 'name'}, {name: 'id'}, {name: 'check'}];
	},
	
	buildTpl: function(config)
	{
		var html = '<tpl for="."><div class="x-item" id="{[this.genId()]}">';
			html += '<div class="<tpl if="index != null">'
			if (this.btnCls){
				html += 'x-item-iconwrap ';
			}
			html += '</tpl>">';
			
			html += '<div class="x-item-wrap ';
			
			if(this.checkable)
			{
				html += '<tpl if="check == true">x-item-checked </tpl>'
				+'<tpl if="check == false">x-item-unchecked </tpl>';
			}
			
			html += '<tpl if="index == null">x-subheader </tpl>">{name}</div>';
			html += '</div>';
			
			html += '</div></tpl>';
		
		config.tpl = new Ext.XTemplate(html, {
			genId: function() {
		      return Ext.id();
			}
		}, this);
	},
	
	onMouseMove: function(e)
	{
		var el = e.getTarget(null, null, true);
		var pos = this.calculatePosition(el, e);
		
		var btnEl = el.parent('.x-item-iconwrap');
		
		if (btnEl)
		{
			if(pos.button){
				btnEl.removeClass(this.btnCls);
				btnEl.addClass(this.overBtnCls);
			}else{
				btnEl.addClass(this.btnCls);
				btnEl.removeClass(this.overBtnCls);
			}
		}
	},
	
	onMouseOver : function(e)
	{
        var item = e.getTarget(this.itemSelector, this.getTemplateTarget());
        
        if(item && item !== this.lastItem){
            this.lastItem = item;
            Ext.fly(item).addClass(this.overClass);
            
            if (this.btnCls)
            {
            	var btnEl = Ext.get(this.lastItem).child('.x-item-iconwrap');
            	if(btnEl){
            		btnEl.addClass(this.btnCls);
            	}
            }
            
            this.fireEvent("mouseenter", this, this.indexOf(item), item, e);
        }
    },
    
    onMouseOut : function(e)
    {
        if(this.lastItem){
            if(!e.within(this.lastItem, true, true)){
                Ext.fly(this.lastItem).removeClass(this.overClass);
                
                if (this.btnCls)
                {
                	var btnEl = Ext.get(this.lastItem).child('.x-item-iconwrap');
                	if(btnEl){
                		btnEl.removeClass(this.btnCls);
                		if(this.overBtnCls){
                			btnEl.removeClass(this.overBtnCls);
                		}
                	}
                }
                
                this.fireEvent("mouseleave", this, this.indexOf(this.lastItem), this.lastItem, e);
                delete this.lastItem;
            }
        }
    },
    
    
    onEnterKeyPress: function(dataView, item, e) {},
    
    onDblClick: function(e, t)
	{
		var el = e.getTarget(null, null, true);
		var pdiv = el.findParent('.x-item-wrap', 10, true);
		
		if (this.editable)
			this.onInlineEdit(el, t, pdiv);
	},
	
	onInlineEdit: function(el, item, pdiv)
	{
		
		var value = el.dom.textContent || el.dom.innerText;
		
		var eDiv = Ext.DomHelper.insertAfter(item.id, {
			tag: 'div',
			id: Ext.id(),
			style: 'position:relative;z-index:10000;height:0;'
		});
		
		var editor = new Ext.Editor(new Ext.form.TextField({
			width: el.getWidth()
		}));
		editor.alignment = 'tl';
		
		editor.on('complete', function(editor, value, startValue) {
			el.update(value);
			editor.destroy();
			Ext.fly(eDiv.id).remove();
	
			
			var node = item.parentNode.parentNode;
			var rec = this.getRecord(node);
			
			if (rec.get('name') != value)
			{
				rec.set('name', value);
				this.fireEvent('itemedit', rec);
			}
			
		}, this);
	
		editor.render(eDiv);
		
		editor.startEdit(pdiv, value);
	},
	
	onListClick: function(dataView, index, item, e)
	{	
		var el = Ext.get(item);
		//el = el.parent('.x-item');
		
		var rec = this.getRecord(item);
		
		var clicks = this.calculatePosition(el, e);
		
		if( this.checkable && clicks.checkbox && !el.child('.x-subheader') ){
			this.toggleCheckBox(el, rec);
		}
		
		if( clicks.button && el.child('.x-item-iconwrap')){
			this.onButtonClick(el, rec);
		} else {
			this.fireEvent('listclick', rec, clicks, el);
		}
	},
	
	onButtonClick: function(el, rec)
	{
		this.fireEvent('btnclick', el, rec);
	},
	
	toggleCheckBox: function(el, rec)
	{
		if(!el.hasClass('x-item-checked') || !el.hasClass('x-item-unchecked')){
			el = el.child('.x-item-checked') || el.child('.x-item-unchecked');
		}
		
		if(el.hasClass('x-item-unchecked')){
			el.replaceClass('x-item-unchecked', 'x-item-checked');
			this.fireEvent('checkclick', true, rec);
		}else {
			el.replaceClass('x-item-checked', 'x-item-unchecked');
			this.fireEvent('checkclick', false, rec);
		}
	},
	
	calculatePosition: function(el, e)
	{
		var xstart = el.getXY()[0];
		var xend = el.getXY()[0] + el.getSize().width;
		var ystart = el.getXY()[1];
    	var yend = el.getXY()[1] + el.getSize().height;
    	var x = e.getXY()[0];
    	var y = e.getXY()[1];
    	
    	var pos = {checkbox: false, button: false};
    	
    	if( (x > xstart) && (x < xstart + 25) ){
    		pos.checkbox = true;
    	}
    	
    	if ( (x < xend) && (x > xend - 25) ){
    		pos.button = true;
    	}
    	
    	return pos;
	},
	
	onCheckBoxClick: function(e, t)
	{
		var rec = this.getRecord(t.parentNode.parentNode);
		var checked = true;

		if (Ext.get(t.parentNode).hasClass('x-item-unchecked'))
		{
			Ext.fly(t.parentNode).replaceClass('x-item-unchecked', 'x-item-checked');
			checked = true;
		}
		else
		{
			Ext.fly(t.parentNode).replaceClass('x-item-checked', 'x-item-unchecked');
			checked = false;
		}
		
		this.fireEvent('checked', rec, checked);
	}
});
