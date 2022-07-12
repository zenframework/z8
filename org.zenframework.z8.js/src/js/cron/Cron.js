Z8.define('Z8.cron.Markup', {
	statics: {
		get: function(cron) {
			var header = this.getHeader();
			var cronList = this.getCronList(cron.value);

			var cls = DOM.parseCls(cron.cls).pushIf('cron').join(' ');
			return { cls: cls, id: cron.id, cn: [header, cronList] };
		},

		getHeader: function() {
			return { cls: 'header', cn: [,
				{ tag: 'span', tabIndex: 0, html: 'Быстрый ввод CRON' },
				{ tag: 'hr', tabIndex: 0 }
			]};
		},
		
		getCronList: function(cron) {
			var prepared = this.getPreparedCron();
			var cn = [];
			for (var i = 0; i < prepared.length; ++i) {
				var prep = prepared[i];
				var elem = { html: prep.title };

				if (prep.cron != null)
					elem.cron = prep.cron;

				elem.cls = 'prepared';
				elem.tabIndex = 0;

				cn.push(elem);
			}
			return { cls: 'cron-list', cn: cn };
		},
		
		getPreparedCron: function() {
			return [
				{ title: 'Каждую минуту', cron: '* * * * *'},
				{ title: 'Каждые 5 минут', cron: '*/5 * * * *'},
				{ title: 'Каждые 15 минут', cron: '*/15 * * * *'},
				{ title: 'Каждые 30 минут', cron: '*/30 * * * *'},
				{ title: 'Каждый час', cron: '0 * * * *'},
				{ title: 'Каждые 2 часа', cron: '0 */2 * * *'},
				{ title: 'Каждые 12 часов', cron: '0 */12 * * *'},
				{ title: 'Каждый день (00:00)', cron: '0 0 * * *'},
				{ title: 'Каждый день (12:00)', cron: '0 12 * * *'},
				{ title: 'Каждый месяц (1-ого числа)', cron: '0 0 1 * *'},
				{ title: 'Каждый год, 1-ого января (00:00)', cron: '0 0 1 1 *'},
				{ title: 'Ручной ввод' },
			];
		}
	}
});

Z8.define('Z8Next.cron.Cron', {
	extend: 'Z8.Component',

	selectedDiv: null,

	htmlMarkup: function() {
		return Z8.cron.Markup.get(this);
	},

	completeRender: function() {
		this.callParent();

		this.prepareds = this.queryNodes('.cron>.cron-list>.prepared');
		this.selectedPrepared = this.selectNode('.cron>.cron-list>.prepared.selected');
		DOM.on(this, 'click', this.onClick, this);
	},

	deselect: function(value, property) {
		DOM.removeCls(value, 'selected');
		DOM.setTabIndex(value, -1);
	},

	select: function(value, property, focus) {
		if (this.selectedDiv != null)
			this.deselect(this.selectedDiv, property);

		DOM.addCls(value, 'selected');
		DOM.setTabIndex(value, 0);

		if(focus)
			DOM.focus(value);

		this.selectedDiv = value;
	},

	selectPrepared: function(prepared, focus) {
		this.select(prepared, 'selected', focus);

		var selectedCron = DOM.getAttribute(prepared, 'cron');
		if (selectedCron != null)
			this.value = selectedCron;
	},

	onPreparedClick: function(prepared) {
		this.selectPrepared(prepared);
		this.fireEvent('preparedClick', this.value, this);
	},

	is: function(target, attribute) {
		return target.hasAttribute(attribute);
	},

	onClick: function(event, target) {
		event.stopEvent();

		if(DOM.hasCls(target, 'prepared'))
			this.onPreparedClick(target);
	},
});

Z8.define('Z8Next.cron.Dropdown', {
	extend: 'Z8Next.cron.Cron',

	cls: 'dropdown-cron display-none',
	visible: false,
	autoAlign: true,

	show: function(top, left) {
		if(this.isVisible()) {
			return;
		}

		this.setPosition(top, left);

		this.callParent();
		this.align();

		this.fireEvent('show', this);

	},

	hide: function() {
		if(!this.isVisible())
			return;

		this.callParent();

		this.fireEvent('hide', this);
	},

	toggle: function() {
		this.visible ? this.hide() : this.show();
	},

	set: function(value) {
		this.init(value, true);
	},
	
	init: function(cron, force) {
		var prepareds = this.prepareds;
		if (this.selectedDiv != null)
			DOM.removeCls(this.selectedDiv, 'selected');
		var ok = false;
		for (var i = 0; i < prepareds.length - 1; ++i) {
			if (DOM.getAttribute(prepareds[i], 'cron') == cron) {
				ok = true;
				this.selectedDiv = prepareds[i];
				DOM.addCls(this.selectedDiv, 'selected');
			}
		}
		if (!ok) {
			this.selectedDiv = prepareds[prepareds.length - 1];
			DOM.addCls(this.selectedDiv, 'selected');
		}
	},

	onDayClick: function(day) {
		this.hide();
		this.callParent(day);
	},

	onCancel: function() {
		this.hide();
		this.fireEvent('cancel', this);
	},

	onKeyDown: function(event, target) {
		if(this.callParent(event, target))
			return;

		var key = event.getKey();
	
		if(key == Event.ESC)
			this.onCancel();
		else
			return false;

		event.stopEvent();
		return true;
	}
});