Z8.define('Z8.form.field.Cron', {
	extend: 'TextBox',
	shortClassName: 'CronBox',

	autocomplete: 'off',

	triggers: [{ icon: 'fa-calendar' }],

	htmlMarkup: function() {
		var markup = this.callParent();

		var value = this.getValue();
		var dropdown = this.dropdown = new Z8Next.cron.Dropdown({ value: value });
		dropdown.on('preparedClick', this.setValue, this);
		dropdown.on('cancel', this.cancelDropdown, this);
		dropdown.on('show', this.onDropdownShow, this);
		dropdown.on('hide', this.onDropdownHide, this);

		markup.cn.push(dropdown.htmlMarkup());
		return markup;
	},

	subcomponents: function() {
		return this.callParent().add(this.dropdown);
	},

	completeRender: function() {
		this.callParent();
		this.dropdown.setAlignment(this);
	},
	
	showDropdown: function() {
		this.dropdown.show();
		DOM.addCls(this, 'open');
	},

	hideDropdown: function() {
		this.dropdown.hide();
		DOM.removeCls(this, 'open');
	},

	openDropdown: function() {
		var value = this.getValue();
		this.dropdown.set(value);
		this.showDropdown();
	},

	cancelDropdown: function() {
		this.hideDropdown();
	},

	toggleDropdown: function() {
		if(this.dropdown.isVisible())
			this.cancelDropdown();
		else
			this.openDropdown();
	},

	onDropdownShow: function() {
	},

	onDropdownHide: function() {
		DOM.focus(this.input);
	},

	onFocusOut: function(event) {
		if(!this.callParent(event))
			return false;

		this.cancelDropdown();
		return true;
	},
	
	onTriggerClick: function(trigger) {
		this.toggleDropdown();
	},
});
