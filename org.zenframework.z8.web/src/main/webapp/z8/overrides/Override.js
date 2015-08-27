/*Ext.chromeVersion = Ext.isChrome ? parseInt(( /chrome\/(\d{2})/ ).exec(navigator.userAgent.toLowerCase())[1],10) : NaN;

Ext.override(Ext.grid.ColumnModel, {
	getTotalWidth : function(includeHidden) {
		if (!this.totalWidth) {
			var boxsizeadj = (Ext.isChrome && Ext.chromeVersion > 18 ? 2 : 0);
			this.totalWidth = 0;
			for (var i = 0, len = this.config.length; i < len; i++) {
				if (includeHidden || !this.isHidden(i)) {
					this.totalWidth += (this.getColumnWidth(i) + boxsizeadj);
				}
			}
		}
		return this.totalWidth;
	}
});*/

String.prototype.hashCode = function() {
	for(var ret = 0, i = 0, len = this.length; i < len; i++) {
		ret = (31 * ret + this.charCodeAt(i)) << 0;
	}
	return ret;
};

String.prototype.replaceAll = function(replace, with_this) {
	return this.replace(new RegExp(replace, 'g'), with_this);
};

Ext.QuickTip.override({
    showAt : function(xy){
         var t = this.activeTarget;
         if(t){
             if(!this.rendered){
                 this.render(Ext.getBody());
                 this.activeTarget = t;
             }
             if(t.width){
                 this.setWidth(t.width);
                 this.body.setWidth(this.adjustBodyWidth(t.width - this.getFrameWidth()));
                 this.measureWidth = false;
             } else{
                 this.measureWidth = true;
             }
             this.setTitle(t.title || '');
             this.body.update(t.text);
             this.autoHide = t.autoHide;
             this.dismissDelay = t.dismissDelay || this.dismissDelay;
             if(this.lastCls){
                 this.el.removeClass(this.lastCls);
                 delete this.lastCls;
             }
             if(t.cls){
                 this.el.addClass(t.cls);
                 this.lastCls = t.cls;
             }
             if(this.anchor){
                 this.constrainPosition = false;
             }else if(t.align){
                 Ext.QuickTip.superclass.showAt.call(this, -1000, -1000);
                 xy = this.el.getAlignToXY(t.el, t.align);
                 this.constrainPosition = false;
             }else{
                 this.constrainPosition = true;
             }
         }
         Ext.QuickTip.superclass.showAt.call(this, xy);
    }
});