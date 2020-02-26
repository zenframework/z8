Z8.define('Z8.application.viewport.SourceCode', {
	extend: 'Z8.Container',

	tabIndex: 0,

	cls: 'source-code display-none',

	history: null,

	htmlMarkup: function() {
		this.history = [];
		this.historyPosition = -1;

		var backward = this.backward = new Z8.button.Button({ icon: 'fa-play fa-flip-horizontal', enabled: false, handler: this.onBackward, scope: this });
		var forward = this.forward = new Z8.button.Button({ icon: 'fa-play', enabled: false, handler: this.onForward, scope: this });
		var buttons = new Z8.button.Group({ items: [backward, forward] });
		var title = this.title = new Z8.Component({ cls: 'text' });

		var toolbar = new Z8.toolbar.Toolbar({ items: [buttons, title] });
		var frame = this.frame = new Z8.Component({ cls: 'code' });

		this.items = [toolbar, frame];

		return this.callParent();
	},

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'click', this.onClick, this);
		DOM.on(this, 'keyDown', this.onKeyDown, this);
	},

	onDestroy: function() {
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);
		DOM.un(document.body, 'mouseDown', this.monitorOuterClick, this);
		this.callParent();
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this, !enabled, 'disabled');
		this.callParent(enabled);
	},

	load: function(url, callback) {
		if(url == null || this.loadLock)
			return;

		var frame = this.frame;

		var index = url.indexOf('#');
		var hash = index != -1 ? url.substring(index) : null;
		var currentUrl = index != -1 ? url.substring(0, index) : url;

		if(currentUrl == this.current) {
			this.select(hash);
			this.addToHistory(url);
			Z8.callback(callback, true);
			return;
		}

		var loadCallback = function(response, success) {
			DOM.setInnerHtml(frame, success ? response.text : '');
	
			index = currentUrl.lastIndexOf('.');
			var title = index != -1 ? (currentUrl.substring(index + 1) + ' - ' + currentUrl.substring(0, index)) : currentUrl;
			this.title.setText(title);

			this.select(hash);
			this.addToHistory(url);
			this.currentUrl = currentUrl;

			if(hash == null)
				DOM.scroll(this.frame, 0, 0);

			Z8.callback(callback, success);

			if(this.isOpen)
				this.focus();

			this.loadLock = false;
		};

		this.backward.setEnabled(false);
		this.forward.setEnabled(false);
		this.loadLock = true;

		HttpRequest.get('src/' + url, { fn: loadCallback, scope: this });
	},

	focus: function() {
		this.frame.focus();
	},

	open: function() {
		if(this.isOpen)
			return;

		this.isOpen = true;

		DOM.removeCls(this, 'display-none');
		DOM.addCls(this, 'open', 10);
		DOM.on(document.body, 'mouseDown', this.monitorOuterClick, this);

		new Z8.util.DelayedTask().delay(500, this.focus, this);

		this.fireEvent('show', this);
	},

	close: function() {
		if(!this.isOpen)
			return;

		DOM.un(document.body, 'mouseDown', this.monitorOuterClick, this);
		DOM.removeCls(this, 'open');
		DOM.addCls(this, 'display-none');

		this.fireEvent('hide', this);

		this.isOpen = false;
	},

	toggle: function() {
		this.isOpen ? this.close() : this.open();
	},

	select: function(id) {
		DOM.removeCls(this.selected, 'selected');

		if(id == null)
			return;

		var selected = this.selected = DOM.selectNode(this, id);

		if(selected == null)
			return;

		var parent = selected.offsetParent;

		var left = selected.offsetLeft;
		var top = selected.offsetTop;
		var width = selected.offsetWidth;
		var height = selected.offsetHeight;
		var scrollTop = parent.scrollTop;
		var scrollLeft = parent.scrollLeft;

		var parentWidth = parent.clientWidth;
		var parentHeight = parent.clientHeight;

		var scrollRight = scrollLeft + parentWidth;
		var scrollBottom = scrollTop + parentHeight;

		if(top < scrollTop)
			scrollTop = selected.offsetTop - 10;
		else if(top + height >= scrollBottom)
			scrollTop = scrollTop + (height + top - scrollBottom) + 10;

		if(left < scrollLeft)
			scrollLeft = selected.offsetLeft - 10 < parentWidth ? 0 : (selected.offsetLeft - 10);
		else if(left + width >= scrollRight)
			scrollLeft = scrollLeft + (width + left - scrollRight) + 10;

		DOM.scroll(parent, scrollLeft, scrollTop);

		DOM.addCls(this.selected, 'selected');
	},

	addToHistory: function(url) {
		if(this.historyLock)
			return;

		if(this.history[this.historyPosition] == url)
			return;

		var position = ++this.historyPosition;
		var history = this.history = this.history.splice(0, position);
		history.push(url);

		this.updateButtons();
	},

	updateButtons: function() {
		var position = this.historyPosition;
		this.backward.setEnabled(position > 0);
		this.forward.setEnabled(position != this.history.length - 1);
	},

	onBackward: function(button) {
		this.historyLock = true;

		var url = this.history[this.historyPosition - 1];

		var callback = function(success) {
			button.setBusy(false);
			this.historyPosition--;
			this.historyLock = false;
			this.updateButtons();
		};

		button.setBusy(true);
		this.load(url, { fn: callback, scope: this });
	},

	onForward: function(button) {
		this.historyLock = true;

		var url = this.history[this.historyPosition + 1];

		var callback = function(success) {
			button.setBusy(false);
			this.historyPosition++;
			this.historyLock = false;
			this.updateButtons();
		};

		button.setBusy(true);
		this.load(url, { fn: callback, scope: this });
	},

	monitorOuterClick: function(event) {
		var target = event.target;

		if(!DOM.isParentOf(this, target) && !DOM.isParentOf(this.owner, target))
			this.close();
	},

	onClick: function(event, target) {
		var tag = target.tagName;

		if(tag == 'SPAN')  // <a class="type"><span class="keyword">class</span></a>
			target = target.parentNode;

		if(target.tagName != 'A') {
			this.select(null);
			return;
		}

		event.stopEvent();

		var url = target.outerHTML.match(/href="([^"]*)"/)[1];
		this.load(url);
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ESC && this.isOpen) {
			event.stopEvent();
			this.close();
		}
	}
});