Z8.define('Z8.application.system.HubServerView', {
	extend: 'Z8.application.form.Navigator',

	createTools: function() {
		var store = this.store;

		var buttons = [];
		var oneRecord = this.oneRecord;

		if(!oneRecord && store.hasCreateAccess()) {
			var add = this.addButton = new Z8.button.Button({ icon: 'fa-file-o', tooltip: Z8.$('Navigator.newRecord'), handler: this.addRecord, scope: this });
			buttons.push(add);
		}

		if(!oneRecord && store.hasCopyAccess()) {
			var copy = this.copyButton = new Z8.button.Button({ icon: 'fa-copy', tooltip: Z8.$('Navigator.copyRecord'), handler: this.copyRecord, scope: this });
			buttons.push(copy);
		}

		if(store.hasReadAccess()) {
			var refresh = this.refreshButton =  new Z8.button.Button({ icon: 'fa-refresh', tooltip: Z8.$('Navigator.refresh'), handler: this.refreshRecords, scope: this });
			buttons.push(refresh);
		}

		var files = this.filesButton = this.createFilesButton();
		if(files != null)
			buttons.push(files);

		if(store.hasDestroyAccess()) {
			var remove = this.removeButton = new Z8.button.Button({ danger: true, icon: 'fa-trash', tooltip: Z8.$('Navigator.deleteRecord'), handler: this.removeRecord, scope: this });
			buttons.push(remove);
		}

		if(!oneRecord) {
			var quickFilters = this.quickFilters = this.createQuickFilters();
			buttons.add(quickFilters);

			var period = this.periodButton = this.createPeriodButton();
			if(period != null)
				buttons.push(period);

			var filter = this.filterButton = this.createFilterButton();
			if(filter != null)
				buttons.push(filter);

/*
			var sort = this.sortButton = new Z8.button.Button({ enabled: false, icon: 'fa-sort', tooltip: 'Порядок сортировки', triggerTooltip: 'Настроить порядок сортировки', split: true, handler: this.toggleSortOrder, scope: this });
			buttons.push(sort);
*/

			var formTable = this.createFormTableGroup();
			if(formTable != null)
				buttons.push(formTable);
		}

		var reports = this.reportsButton = this.createReportsButton();
		if(reports != null)
			buttons.push(reports);

		var actions = this.actionsButton = this.createActionsButton();
		if(actions != null)
			buttons.push(actions);

		return buttons;
	}
});