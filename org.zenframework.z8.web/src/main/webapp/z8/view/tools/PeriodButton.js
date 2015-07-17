Z8.January = 0;
Z8.February = 1;
Z8.March = 2;
Z8.April = 3;
Z8.May = 4;
Z8.June = 5;
Z8.July = 6;
Z8.August = 7;
Z8.September = 8;
Z8.October = 9;
Z8.November = 10;
Z8.December = 11;

Z8.Periods =
{
	Year: 'year',
	HalfYear: 'halfYear',
	Quarter: 'quarter',
	Month: 'month',
	Week: 'week',
	Default: 'default',
	
	name: function(period)
	{
		switch(period)
		{
			case Z8.Periods.Year: return 'Год';
			case Z8.Periods.HalfYear: return 'Полугодие';
			case Z8.Periods.Quarter: return 'Квартал';
			case Z8.Periods.Month: return 'Месяц';
			case Z8.Periods.Week: return 'Неделя';
		}
		
		return '...';
	},
	
	start: function(date, period)
	{
		date.clearTime();
		
		var year = date.getFullYear();
		var month = date.getMonth();
		var day = date.getDay();
		
		if(period == Z8.Periods.Year)
		{
			return new Date(year, Z8.January, 1);
		}
		else if(period == Z8.Periods.HalfYear)
		{
			return month < 6 ? new Date(year, Z8.January, 1) : new Date(year, Z8.July, 1);
		}
		else if(period == Z8.Periods.Quarter)
		{
			if(month < 3)
				return new Date(year, Z8.January, 1);
			else if(month < 6)
				return new Date(year, Z8.April, 1);
			else if(month < 9)
				return new Date(year, Z8.July, 1);
			else
				return new Date(year, Z8.October, 1);
		}
		else if(period == Z8.Periods.Month)
		{
			return new Date(year, month, 1);
		}
		else if(period == Z8.Periods.Week)
		{
			return date.add(Date.DAY, day == 0 ? -6 : (1 - day));
		}
		
		return date;
	},
	
	finish: function(date, period)
	{
		date.clearTime();
		
		var year = date.getFullYear();
		var month = date.getMonth();
		var day = date.getDay();
		
		if(period == Z8.Periods.Year)
		{
			return new Date(year, Z8.December, 31);
		}
		else if(period == Z8.Periods.HalfYear)
		{
			return month < 6 ? new Date(year, Z8.June, 30) : new Date(year, Z8.December, 31);
		}
		else if(period == Z8.Periods.Quarter)
		{
			if(month < 3)
				return new Date(year, Z8.March, 31);
			else if(month < 6)
				return new Date(year, Z8.June, 30);
			else if(month < 9)
				return new Date(year, Z8.September, 30);
			else
				return new Date(year, Z8.December, 31);
		}
		else if(period == Z8.Periods.Month)
		{
			return new Date(year, month, date.getDaysInMonth());
		}
		else if(period == Z8.Periods.Week)
		{
			return date.add(Date.DAY, day == 0 ? 0 : (6 - day + 1));
		}
		
		return date;
	},
	
	is: function(start, finish, period)
	{
		if(period == Z8.Periods.Default)
		{
			return true;
		}
		
		var s = Z8.Periods.start(start, period);
		var f = Z8.Periods.finish(start, period);
		
		return s.getTime() == start.getTime() && f.getTime() == finish.getTime();
	},
	
	get: function(start, finish)
	{
		if(Z8.Periods.is(start, finish, Z8.Periods.Year))
		{
			return Z8.Periods.Year;
		}
		else if(Z8.Periods.is(start, finish, Z8.Periods.HalfYear))
		{
			return Z8.Periods.HalfYear;
		}
		else if(Z8.Periods.is(start, finish, Z8.Periods.Quarter))
		{
			return Z8.Periods.Quarter;
		}
		else if(Z8.Periods.is(start, finish, Z8.Periods.Month))
		{
			return Z8.Periods.Month;
		}
		else if(Z8.Periods.is(start, finish, Z8.Periods.Week))
		{
			return Z8.Periods.Week;
		}
		
		return Z8.Periods.Default;
	},
	
	formatDates: function(start, finish, period)
	{
		if(period == Z8.Periods.Default)
		{
			return start.format(Z8.Format.LongDate) + ' - ' + finish.format(Z8.Format.LongDate);
		}
		
		if(period == Z8.Periods.Year || period == Z8.Periods.HalfYear || period == Z8.Periods.Quarter)
		{
			return start.format('j F') + ' - ' + finish.format('j F');
		}
		else if(period == Z8.Periods.Month)
		{
			return '1 - ' + start.getDaysInMonth();
		}
		else if(period == Z8.Periods.Week)
		{
			return start.format('j F') + ' - ' + finish.format('j F');
		}

		return 'unknown period \'' + period + '\'';
	},
	
	periodName: function(start, period)
	{
		if(period == Z8.Periods.Default)
		{
			return '';
		}

		var year = start.getFullYear();
		var month = start.getMonth();

		var yearText = start.getFullYear() + ' год';

		if(period == Z8.Periods.Year)
		{
			return yearText;
		}
		
		yearText += ', ';
		
		if(period == Z8.Periods.HalfYear)
		{
			return yearText + (month < 6 ? '1' : '2') + '-ое полугодие';
		}
		else if(period == Z8.Periods.Quarter)
		{
			return yearText + (month < 3 ? '1-ый' : (month < 6 ? '2-ой' : (month < 9 ? '3-ий' : '4-ый'))) + ' квартал';
		}
		else if(period == Z8.Periods.Month)
		{
			return yearText + Date.monthNames1[month];
		}
		else if(period == Z8.Periods.Week)
		{
			var week = start.getWeekOfYear();
			
			if(month == 11 && week == 1)
			{
				year += 1;
			}
			
			return year + ' год, ' + week + '-я неделя';
		}

		return 'unknown period \'' + period + '\'';
	},
	
	add: function(start, period, count)
	{
		if(period == Z8.Periods.Year)
		{
			return start.add(Date.YEAR, count);
		}
		else if(period == Z8.Periods.HalfYear)
		{
			return start.add(Date.MONTH, 6 * count);
		}
		else if(period == Z8.Periods.Quarter)
		{
			return start.add(Date.MONTH, 3 * count);
		}
		else if(period == Z8.Periods.Month)
		{
			return start.add(Date.MONTH, count);
		}
		else if(period == Z8.Periods.Week)
		{
			return start.add(Date.DAY, 7 * count);
		}

		return start;
	}
};

Z8.ButtonPanel = Ext.extend(Z8.Panel,
{
	autoHide: true,

	floating: true,
	header: false,
	border: false,

	defaultOffsets: [0, -2],

	cls: 'x-menu x-multicolumn-menu z8-panel-menu',
	
	render: function(container, pos)
	{
		if(!this.rendered)
		{
			Z8.ButtonPanel.superclass.render.call(this, Ext.getBody());
		}
	},
	
	show: function(el, pos)
	{
		this.render();
		this.showAt(this.el.getAlignToXY(el, pos || this.defaultAlign, this.defaultOffsets));
	},

	showAt: function(xy, parentMenu)
	{
		if(this.fireEvent('beforeshow', this) !== false)
		{
			this.render();
			
			//constrain to the viewport.
			xy = this.el.adjustForConstraints(xy);

			this.el.setXY(xy);
			this.el.show();

			Ext.Panel.superclass.onShow.call(this);
			this.doLayout(false, true);

			if(Ext.isIE)
			{
				// internal event, used so we don't couple the layout to the menu
				this.fireEvent('autosize', this);
				if(!Ext.isIE8)
				{
					this.el.repaint();
				}
			}

			this.hidden = false;
			this.focus();
			this.fireEvent('show', this);
		}
	}	
});

Z8.PeriodButton = Ext.extend(Z8.SplitButton,
{
	split: true,
		
	start: new Date(),
	finish: new Date(),
	period: Z8.Periods.Quarter,
	
	initComponent: function()
	{
		Z8.PeriodButton.superclass.initComponent.call(this);
		
		this.addEvents('changed');

		this.start = Ext.isDate(this.start) ? this.start : Date.parseDate(this.start, Z8.Format.Date);
		this.finish = Ext.isDate(this.finish) ? this.finish : Date.parseDate(this.finish, Z8.Format.Date);
		
		if(this.period == Z8.Periods.Default)
		{
			this.period = Z8.Periods.get(this.start, this.finish);
		}

		this.previous = new Z8.Button({ cls: 'x-period-prev', width: 16, height: 16, x: 5, y: 4, handler: this.onPrevious, scope: this });
		this.periodText = new Ext.Container({ cls:'x-period-text', width: 298 });
		this.next = new Z8.Button({ cls: 'x-period-next', width: 16, height: 16, x: 277, y: 4, handler: this.onNext, scope: this });
		
		var headerItems = [ this.periodText, this.previous, this.next ];
		this.periodHeader = new Ext.Container({ cls:'x-period-header',  layout: 'absolute', width: 298, items: headerItems });
		
		this.startLabel = new Ext.form.Label({ cls:'x-form-item x-form-item-label', text: 'С:', x: 10, y: 43 });
		this.startItem = new Ext.form.DateField({ x: 30, y: 40, enableKeyEvents: true });
		this.finishLabel = new Ext.form.Label({ cls:'x-form-item x-form-item-label', text: 'По:', x: 150, y: 43 });
		this.finishItem = new Ext.form.DateField({ x: 175, y: 40, enableKeyEvents: true });

		var periodItems = 
		[
			[ Z8.Periods.name(Z8.Periods.Year), Z8.Periods.Year ],
			[ Z8.Periods.name(Z8.Periods.HalfYear), Z8.Periods.HalfYear ],
			[ Z8.Periods.name(Z8.Periods.Quarter), Z8.Periods.Quarter ],
			[ Z8.Periods.name(Z8.Periods.Month), Z8.Periods.Month ],
			[ Z8.Periods.name(Z8.Periods.Week), Z8.Periods.Week ]
		];
		
		var store = new Ext.data.ArrayStore({ fields: [ 'text', 'period' ], data: periodItems });
		
		this.periodsMenu = new Ext.menu.Menu({ items: periodItems });
		this.periodLabel = new Ext.form.Label({ cls:'x-form-item x-form-item-label', text: 'Период:', x: 10, y: 83 });
		this.periodItem = new Ext.form.ComboBox({ editable: false, triggerAction: 'all', mode: 'local', listEmptyText: Z8.emptyString, displayField: 'text', valueField: 'period', store: store, width: 100, menu: this.periodsMenu, x: 65, y: 80 });
		
		var items = [ this.periodHeader,
		              this.startLabel, this.startItem, this.finishLabel, this.finishItem, 
		              this.periodLabel, this.periodItem ];
		
		var okButton = new Z8.Button({ text: 'OK', width: 60, handler: this.onOk, scope: this });
		this.menu = new Z8.ButtonPanel({ layout: 'absolute', width: 300, height: 170, items: items, bbar: [ '->', okButton ] });

		this.periodItem.on('select', this.onPeriodChanged, this);

		this.startItem.on('select', this.onStartChanged, this);
		this.startItem.on('keyup', this.onStartKeyup, this);

		this.finishItem.on('select', this.onFinishChanged, this);
		this.finishItem.on('keyup', this.onFinishKeyup, this);

		this.on('click', this.toggleMenu, this);
		
		this.changePeriod(this.period, true);
		this.updateButtonText();
	},
	
	getPeriod: function()
	{
		return { start: this.start.format(Z8.Format.Date), finish: this.finish.format(Z8.Format.Date), period: this.period };
	},
	
	onPeriodChanged: function(combo, record, index)
	{
		this.changePeriod(record.get('period'));
	},
	
	changePeriod: function(period, noEvent)
	{
		this.period = period;

		this.periodItem.setValue(period != Z8.Periods.Default ? period : '');
		
		this.start = Z8.Periods.start(this.start, period);
		this.finish = period != Z8.Periods.Default ? Z8.Periods.finish(this.start, period) : this.finish;

		if(String(this.startItem.getValue()) != String(this.start))
		{
			this.startItem.setValue(this.start);
		}
		
		if(String(this.finishItem.getValue()) != String(this.finish))
		{
			this.finishItem.setValue(this.finish);
		}

		var text = Z8.Periods.formatDates(this.start, this.finish, this.period);;
		
		if(period != Z8.Periods.Default)
		{
			text = Z8.Periods.periodName(this.start, this.period) + ', ' + text;
		}
		
		this.setHeaderText(text);
		
		this.previous.setDisabled(this.period == Z8.Periods.Default);
		this.next.setDisabled(this.period == Z8.Periods.Default);
	},

	onOk: function()
	{
		this.updateButtonText();
		this.hideMenu();
		this.fireEvent('changed', this, this.getPeriod());
	},
	
	updateButtonText: function()
	{
		var text = this.period == Z8.Periods.Default ? 
				this.start.format(Z8.Format.Date) + ', ' + this.finish.format(Z8.Format.Date) : 
				Z8.Periods.periodName(this.start, this.period);
		text = text.replace(new RegExp(', ', 'g'), '<br>');
		
		this.setText(text);
	},

	setHeaderText: function(text)
	{
		if(this.periodText.rendered)
		{
			this.periodText.getEl().dom.innerHTML = text;
		}
		else
		{
			this.periodText.html = text;
		}
	},
	
	onStartChanged: function(picker, date)
	{
		this.start = date;
			
		var period = Z8.Periods.get(this.start, this.finish);
			
		this.changePeriod(period);
	},
	
	onStartKeyup: function()
	{
		var value = this.startItem.getValue();
		
		if(Ext.isDate(value))
		{
			this.onStartChanged(this.startItem, value);
		}
	},

	onFinishChanged: function(picker, date)
	{
		this.finish = date;
		this.onStartChanged(picker, this.start);
	},
	
	onFinishKeyup: function()
	{
		var value = this.finishItem.getValue();
		
		if(Ext.isDate(value))
		{
			this.onFinishChanged(this.finishItem, value);
		}
	},
	
	onPrevious: function()
	{
		if(this.period != Z8.Periods.Default)
		{
			this.start = Z8.Periods.add(this.start, this.period, -1);
			this.changePeriod(this.period);
		}
	},
	
	onNext: function()
	{
		if(this.period != Z8.Periods.Default)
		{
			this.start = Z8.Periods.add(this.start, this.period, 1);
			this.changePeriod(this.period);
		}
	}
});
