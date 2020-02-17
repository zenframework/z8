Z8.define('Z8.calendar.Button', {
	extend: 'Z8.button.Button',

	icon: 'fa-calendar',
	toggle: true,

	tooltip: 'Период',
	triggerTooltip: 'Выбрать период',

	period: null,
	property: null,

	htmlMarkup: function() {
		this.filterItems = [];

		this.on('toggle', this.onToggle, this);

		var periodControl = new Z8.calendar.Period({ period: this.period });
		periodControl.on('apply', this.onPeriodApply, this);
		periodControl.on('cancel', this.onPeriodCancel, this);

		var menu = this.menu = new Z8.menu.Menu({ items: [periodControl], useTab: false });
		menu.on('itemClick', this.onMenuItemClick, this);

		this.init();

		return this.callParent();
	},

	init: function() {
		var period = this.period;
		this.setText(period.getText());
		this.setToggled(period.isActive(), true);
	},

	onPeriodApply: function(control, start, finish) {
		this.menu.hide();

		var period = this.period;
		period.setStart(start);
		period.setFinish(finish);
		period.setActive(true);

		this.init();

		this.fireEvent('period', this, period, Period.Activate);
	},

	onPeriodCancel: function(control) {
		this.menu.hide();
		this.fireEvent('period', this, this.period, Period.NoAction);
	},

	onToggle: function(button, toggled) {
		var period = this.period;
		period.setActive(toggled);
		this.fireEvent('period', this, period, toggled ? Period.Apply : Period.Clear);
	}
});