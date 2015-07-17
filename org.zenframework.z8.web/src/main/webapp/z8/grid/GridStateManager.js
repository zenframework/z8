Z8.grid.GridStateManager = function(config){
	
	this.addEvents('statechanged');
	
	this.state = {
        hidden: {},
        locked: {},
        widths: {}
    };
	
	this.defaultState = {};
	
	Z8.grid.GridStateManager.superclass.constructor.call(this, config);
};

Ext.extend(Z8.grid.GridStateManager, Ext.util.Observable,
{
	init : function(grid){
	
		this.grid = grid;

		grid.getColumnModel().on('hiddenchange', this.onHiddenChange, this);
		grid.on('columnresize', this.onColumnResize, this);
		grid.on('sortchange', this.onSort, this);
		grid.getColumnModel().on('columnlockchange', this.onLockChange, this);
		grid.getColumnModel().on('columnmoved', this.onColumnMove, this);
		
		this.setDefaultState();
	},
	
	setDefaultState: function()
	{
		this.defaultState = this.getState();
	},
	
	getState: function()
	{
		var cm = this.grid.getColumnModel();
		var columns = cm.columns;
		var store = this.grid.getStore();
		
		var states = {
			hidden: {},
			locked: {},
			widths: {}
		};
		
		Ext.each(columns, function(column, index) {
			states.widths[column.id] = column.width;
			states.hidden[column.id] = column.hidden;
			
			if (column.locked === true) {
				states.locked[column.id] = true;
			}
		});
		
		var colIds = [];
		for(var i = 0; i < cm.getColumnCount(); i++){
			colIds.push(cm.getColumnId(i));
		} 
		states.colIds = colIds;
		
		if(store.sortInfo != null)
		{
			states.sortInfo = {
				field: store.sortInfo.field,
				direction: store.sortInfo.direction
			};
		}
		
		if(store.hasGrouping())
		{
			states.groupInfo = {
				groupBy: store.getGroupFields(),
				groupDir: store.groupDir
			};
		}
		
		states.filterInfo = this.saveFilters();
		
		return states;
	},
	
	saveFilters: function()
	{
		var state = [];
		var filterData = this.grid.filters.getFilterData();
		var filterState = this.grid.filters.saveState(null, {});
		
		Ext.each(filterData, function(data, index){
			Ext.iterate(filterState, function(key, value){
				if (data.field == key) {
					state.push({type: data.data.type, val: value, key: key});
				}
			});
		});

		return state;
	},
	
	prepareFilters: function(filterInfo)
	{
		state = {};
		Ext.each(filterInfo, function(filter, index){
			if(filter.type=='date'){
				var ovjval = {};
				Ext.iterate(filter.val, function(key,val){
					var tmp = val.split('/');
					var date = tmp[1]+'/'+tmp[0]+'/'+tmp[2];
					ovjval[key] = new Date(date);
				});
				state[filter.key] = ovjval;
			}else{ 
				state[filter.key] = filter.val;
			}
		});
		
		return {filters: state};
	},
	
	resetStates: function()
	{
		this.grid.filters.clearFilters();
		
		this.grid.store.on('groupchange', function(){
			
		}, this);
		
		if(this.defaultState.widths){
			this.restoreWidths(this.defaultState.widths);
		}
		
		if(this.defaultState.locked){
			this.restoreLocked(this.defaultState.locked);
		}
		
		if(this.defaultState.hidden){
			this.restoreHidden(this.defaultState.hidden);
		}
		
		this.grid.getView().forceFit = true;
		
		this.grid.getView().fitColumns();
		
		this.grid.store.clearGrouping();
	},
	
	restoreStates: function(state)
	{
		this.restoreWidths(state.widths);
		this.restoreHidden(state.hidden);
		this.restoreLocked(state.colIds);
		this.restoreSort(state.sortInfo);
		this.restoreMoves(state.colIds);
		this.restoreGroup(state.groupInfo);
		this.restoreFilters(state.filterInfo);
		
		this.grid.getStore().reload();
	},
	
	restoreFilters: function(filterInfo)
	{
		if (filterInfo && ! Z8.isEmpty(filterInfo)) {
			this.filterInfo = filterInfo;
			
			this.grid.filters.applyState(this.grid, this.prepareFilters(filterInfo));
				
		} else {
			this.grid.filters.clearFilters();
		}
	},
	
	restoreGroup: function(groupInfo)
	{
		if (groupInfo) {
			this.groupInfo = groupInfo;

			this.grid.getStore().groupBy(groupInfo.groupBy, false, groupInfo.groupDir, true);
		} else {
			this.grid.getStore().clearGrouping(true);
		}
	},
	
	restoreWidths: function(widths)
	{
        for (var colId in widths) {
        	var colIndex = this.grid.getColumnModel().getIndexById(colId);
        	if (colIndex && colIndex >= 0) {
        		this.grid.getColumnModel().setColumnWidth(colIndex, widths[colId]);
        	}
        }
	},
	
	restoreHidden: function(hidden)
	{
		this.hidden = hidden;
		
		for (var colId in hidden) {
			var colIndex = this.grid.getColumnModel().getIndexById(colId);
			if (colIndex && colIndex >= 0) {				
				this.grid.getColumnModel().setHidden(colIndex, hidden[colId]);
			}
		}
	},
	
	restoreLocked: function(locked)
	{
		this.locked = locked;
		
		for (var colId in locked) {
			var colIndex = this.grid.getColumnModel().getIndexById(colId);
			if (colIndex && colIndex >= 0) {
				this.grid.getColumnModel().setLocked(colIndex, locked[colId]);
			}
		}
	},
	
	restoreMoves: function(colIds)
	{
		if (colIds)
		{
			this.colIds = colIds;
			
			Ext.each(colIds, function(savedColId, index) {
				var initialColIndex = this.grid.getColumnModel().getIndexById(savedColId);
				
				if (initialColIndex && initialColIndex > 0 && initialColIndex != index) {
					this.grid.getColumnModel().moveColumn(initialColIndex, index);
				}
				
			}, this);
		}
	},
	
	restoreSort: function(sortInfo)
	{
		if (sortInfo) {
			this.sortInfo = sortInfo;
			this.grid.getStore().setDefaultSort(sortInfo.field, sortInfo.direction);
		}
	},
	    
	onHiddenChange : function(cm, colIndex, hidden)
	{	
		var colId = cm.getColumnId(colIndex);
		this.state.hidden[colId] = hidden;
		
		this.fireEvent('statechanged');
	},
	    
	onColumnResize : function(colIndex, width)
	{
		var colId = this.grid.getColumnModel().getColumnId(colIndex);
		this.state.widths[colId] = width;
		
		this.fireEvent('statechanged');
	},
	    
	onLockChange : function(cm, colIndex, lockState){
		var colId = cm.getColumnId(colIndex);
		this.state.locked[colId] = lockState;
		
		this.fireEvent('statechanged');
	},
	    
	onColumnMove : function(cm, oldIndex, newIndex)
	{
		var colIds = [];
		for(var i = 0; i < cm.getColumnCount(); i++){
			colIds.push(cm.getColumnId(i));
		} 
		this.state.colIds = colIds;
		
		this.fireEvent('statechanged');
	},
	    
	onSort : function(grid, sortInfo)
	{
		this.state.sortInfo = sortInfo;
		
		this.fireEvent('statechanged');
	}    
});