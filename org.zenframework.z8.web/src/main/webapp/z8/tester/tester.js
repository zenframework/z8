/*global setTimeout: false, console: false */(function(){var a={},b=this,c=b.async;typeof module!="undefined"&&module.exports?module.exports=a:b.async=a,a.noConflict=function(){return b.async=c,a};var d=function(a,b){if(a.forEach)return a.forEach(b);for(var c=0;c<a.length;c+=1)b(a[c],c,a)},e=function(a,b){if(a.map)return a.map(b);var c=[];return d(a,function(a,d,e){c.push(b(a,d,e))}),c},f=function(a,b,c){return a.reduce?a.reduce(b,c):(d(a,function(a,d,e){c=b(c,a,d,e)}),c)},g=function(a){if(Object.keys)return Object.keys(a);var b=[];for(var c in a)a.hasOwnProperty(c)&&b.push(c);return b};typeof process=="undefined"||!process.nextTick?a.nextTick=function(a){setTimeout(a,0)}:a.nextTick=process.nextTick,a.forEach=function(a,b,c){c=c||function(){};if(!a.length)return c();var e=0;d(a,function(d){b(d,function(b){b?(c(b),c=function(){}):(e+=1,e===a.length&&c(null))})})},a.forEachSeries=function(a,b,c){c=c||function(){};if(!a.length)return c();var d=0,e=function(){b(a[d],function(b){b?(c(b),c=function(){}):(d+=1,d===a.length?c(null):e())})};e()},a.forEachLimit=function(a,b,c,d){d=d||function(){};if(!a.length||b<=0)return d();var e=0,f=0,g=0;(function h(){if(e===a.length)return d();while(g<b&&f<a.length)f+=1,g+=1,c(a[f-1],function(b){b?(d(b),d=function(){}):(e+=1,g-=1,e===a.length?d():h())})})()};var h=function(b){return function(){var c=Array.prototype.slice.call(arguments);return b.apply(null,[a.forEach].concat(c))}},i=function(b){return function(){var c=Array.prototype.slice.call(arguments);return b.apply(null,[a.forEachSeries].concat(c))}},j=function(a,b,c,d){var f=[];b=e(b,function(a,b){return{index:b,value:a}}),a(b,function(a,b){c(a.value,function(c,d){f[a.index]=d,b(c)})},function(a){d(a,f)})};a.map=h(j),a.mapSeries=i(j),a.reduce=function(b,c,d,e){a.forEachSeries(b,function(a,b){d(c,a,function(a,d){c=d,b(a)})},function(a){e(a,c)})},a.inject=a.reduce,a.foldl=a.reduce,a.reduceRight=function(b,c,d,f){var g=e(b,function(a){return a}).reverse();a.reduce(g,c,d,f)},a.foldr=a.reduceRight;var k=function(a,b,c,d){var f=[];b=e(b,function(a,b){return{index:b,value:a}}),a(b,function(a,b){c(a.value,function(c){c&&f.push(a),b()})},function(a){d(e(f.sort(function(a,b){return a.index-b.index}),function(a){return a.value}))})};a.filter=h(k),a.filterSeries=i(k),a.select=a.filter,a.selectSeries=a.filterSeries;var l=function(a,b,c,d){var f=[];b=e(b,function(a,b){return{index:b,value:a}}),a(b,function(a,b){c(a.value,function(c){c||f.push(a),b()})},function(a){d(e(f.sort(function(a,b){return a.index-b.index}),function(a){return a.value}))})};a.reject=h(l),a.rejectSeries=i(l);var m=function(a,b,c,d){a(b,function(a,b){c(a,function(c){c?(d(a),d=function(){}):b()})},function(a){d()})};a.detect=h(m),a.detectSeries=i(m),a.some=function(b,c,d){a.forEach(b,function(a,b){c(a,function(a){a&&(d(!0),d=function(){}),b()})},function(a){d(!1)})},a.any=a.some,a.every=function(b,c,d){a.forEach(b,function(a,b){c(a,function(a){a||(d(!1),d=function(){}),b()})},function(a){d(!0)})},a.all=a.every,a.sortBy=function(b,c,d){a.map(b,function(a,b){c(a,function(c,d){c?b(c):b(null,{value:a,criteria:d})})},function(a,b){if(a)return d(a);var c=function(a,b){var c=a.criteria,d=b.criteria;return c<d?-1:c>d?1:0};d(null,e(b.sort(c),function(a){return a.value}))})},a.auto=function(a,b){b=b||function(){};var c=g(a);if(!c.length)return b(null);var e={},h=[],i=function(a){h.unshift(a)},j=function(a){for(var b=0;b<h.length;b+=1)if(h[b]===a){h.splice(b,1);return}},k=function(){d(h.slice(0),function(a){a()})};i(function(){g(e).length===c.length&&(b(null,e),b=function(){})}),d(c,function(c){var d=a[c]instanceof Function?[a[c]]:a[c],g=function(a){if(a)b(a),b=function(){};else{var d=Array.prototype.slice.call(arguments,1);d.length<=1&&(d=d[0]),e[c]=d,k()}},h=d.slice(0,Math.abs(d.length-1))||[],l=function(){return f(h,function(a,b){return a&&e.hasOwnProperty(b)},!0)&&!e.hasOwnProperty(c)};if(l())d[d.length-1](g,e);else{var m=function(){l()&&(j(m),d[d.length-1](g,e))};i(m)}})},a.waterfall=function(b,c){c=c||function(){};if(!b.length)return c();var d=function(b){return function(e){if(e)c(e),c=function(){};else{var f=Array.prototype.slice.call(arguments,1),g=b.next();g?f.push(d(g)):f.push(c),a.nextTick(function(){b.apply(null,f)})}}};d(a.iterator(b))()},a.parallel=function(b,c){c=c||function(){};if(b.constructor===Array)a.map(b,function(a,b){a&&a(function(a){var c=Array.prototype.slice.call(arguments,1);c.length<=1&&(c=c[0]),b.call(null,a,c)})},c);else{var d={};a.forEach(g(b),function(a,c){b[a](function(b){var e=Array.prototype.slice.call(arguments,1);e.length<=1&&(e=e[0]),d[a]=e,c(b)})},function(a){c(a,d)})}},a.series=function(b,c){c=c||function(){};if(b.constructor===Array)a.mapSeries(b,function(a,b){a&&a(function(a){var c=Array.prototype.slice.call(arguments,1);c.length<=1&&(c=c[0]),b.call(null,a,c)})},c);else{var d={};a.forEachSeries(g(b),function(a,c){b[a](function(b){var e=Array.prototype.slice.call(arguments,1);e.length<=1&&(e=e[0]),d[a]=e,c(b)})},function(a){c(a,d)})}},a.iterator=function(a){var b=function(c){var d=function(){return a.length&&a[c].apply(null,arguments),d.next()};return d.next=function(){return c<a.length-1?b(c+1):null},d};return b(0)},a.apply=function(a){var b=Array.prototype.slice.call(arguments,1);return function(){return a.apply(null,b.concat(Array.prototype.slice.call(arguments)))}};var n=function(a,b,c,d){var e=[];a(b,function(a,b){c(a,function(a,c){e=e.concat(c||[]),b(a)})},function(a){d(a,e)})};a.concat=h(n),a.concatSeries=i(n),a.whilst=function(b,c,d){b()?c(function(e){if(e)return d(e);a.whilst(b,c,d)}):d()},a.until=function(b,c,d){b()?d():c(function(e){if(e)return d(e);a.until(b,c,d)})},a.queue=function(b,c){var e=0,f={tasks:[],concurrency:c,saturated:null,empty:null,drain:null,push:function(b,e){b.constructor!==Array&&(b=[b]),d(b,function(b){f.tasks.push({data:b,callback:typeof e=="function"?e:null}),f.saturated&&f.tasks.length==c&&f.saturated(),a.nextTick(f.process)})},process:function(){if(e<f.concurrency&&f.tasks.length){var a=f.tasks.shift();f.empty&&f.tasks.length==0&&f.empty(),e+=1,b(a.data,function(){e-=1,a.callback&&a.callback.apply(a,arguments),f.drain&&f.tasks.length+e==0&&f.drain(),f.process()})}},length:function(){return f.tasks.length},running:function(){return e}};return f};var o=function(a){return function(b){var c=Array.prototype.slice.call(arguments,1);b.apply(null,c.concat([function(b){var c=Array.prototype.slice.call(arguments,1);typeof console!="undefined"&&(b?console.error&&console.error(b):console[a]&&d(c,function(b){console[a](b)}))}]))}};a.log=o("log"),a.dir=o("dir"),a.memoize=function(a,b){var c={},d={};b=b||function(a){return a};var e=function(){var e=Array.prototype.slice.call(arguments),f=e.pop(),g=b.apply(null,e);g in c?f.apply(null,c[g]):g in d?d[g].push(f):(d[g]=[f],a.apply(null,e.concat([function(){c[g]=arguments;var a=d[g];delete d[g];for(var b=0,e=a.length;b<e;b++)a[b].apply(null,arguments)}])))};return e.unmemoized=a,e},a.unmemoize=function(a){return function(){return(a.unmemoized||a).apply(null,arguments)}}})();

Z8.TesterLogsGrid = Ext.extend(Ext.grid.GridPanel, {
	
	autoHeight: true,
	data: [],
	
	initComponent: function() {

		var config = {};
		this.buildConfig(config);
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		Z8.TesterLogsGrid.superclass.initComponent.call(this);
	},
	
	buildConfig:function(config) {
	      this.buildStore(config);
	      this.buildColumns(config);
	      this.buildView(config);
	},
  
	buildStore: function(config) {	
		config.store =  new Ext.data.ArrayStore({
			data: this.data,
			fields: [{name: 'message'}, {name: 'status'}]
		}, this);
	},
  
	buildColumns: function(config) {
		config.columns = [{
			header: 'Сообщение', width: 200, sortable: true, dataIndex: 'message'
		}/*,{
			header: 'Статус', width: 100, sortable: true, dataIndex: 'status'
		}*/];
	},
  
	buildView: function(config) {
		config.view = new Ext.grid.GridView({forceFit: true});
	},
	
	addRecord: function(value)
	{
		this.store.add(new this.store.recordType({
			message: value.message,
			status: value.status
		}));
	}
});

Z8.TesterErrorsGrid = Ext.extend(Ext.grid.GridPanel, {
	
	autoHeight: true,
	data: [],
	
	initComponent: function() {

		var config = {};
		this.buildConfig(config);
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		Z8.TesterErrorsGrid.superclass.initComponent.call(this);
	},
	
	buildConfig:function(config) {
	      this.buildStore(config);
	      this.buildColumns(config);
	      this.buildView(config);
	},
  
	buildStore: function(config) {	
		config.store =  new Ext.data.ArrayStore({
			data: this.data,
			fields: [{name: 'message'}, {name: 'error'}]
		}, this);
	},
  
	buildColumns: function(config) {
		config.columns = [{
			header: 'Сообщение', width: 200, sortable: true, dataIndex: 'message'
		},{
			header: 'Ошибка', width: 200, sortable: true, dataIndex: 'error'
		}];
	},
  
	buildView: function(config) {
		config.view = new Ext.grid.GridView({forceFit: true});
	},
	
	addRecord: function(value)
	{
		var errorMsg = '';
		
		if(value.status == 'error')
		{
			if (Ext.isObject(value.error))
				errorMsg = value.error.message;
			else
				errorMsg = value.error;
		}
		
		if(value.status == 'exception')
		{
			Ext.each(value.error.messages, function(message){
				errorMsg += ' ' + message;
			});
		}
		
		this.store.add(new this.store.recordType({
			message: value.message,
			error: errorMsg
		}));
	}
});

Z8.TesterWindow = Ext.extend(Z8.Window, {
	
	width:800,
	height:600,
	title: 'Тестирование Z8',
	layout:'fit',
	
	initComponent: function()
	{
		this.logGrid = new Z8.TesterLogsGrid();
		this.errorGrid = new Z8.TesterErrorsGrid();
		
		var config = {
			items:[{
				 xtype:'tabpanel',
				 border:false,
				 activeItem:0,
				 items:[{
					title:'Общий лог',
					items: this.logGrid,
					autoScroll: true
				 },{
					title:'Ошибки',
					items: this.errorGrid,
					autoScroll: true
				}]
			}]
		};
	
		// apply config
	    Ext.apply(this, Ext.apply(this.initialConfig, config));
    
		Z8.TesterWindow.superclass.initComponent.call(this);
		
		this.tester.on('message', function(message){
			this.logGrid.addRecord(message);
		}, this);
		
		this.tester.on('exception', function(message){
			this.errorGrid.addRecord(message);
		}, this);
		
		this.tester.on('error', function(message){
			this.errorGrid.addRecord(message);
		}, this);
	},
	
	onClose: function()
	{
		this.hide();
	}
});

Z8.Tester = Ext.extend(Ext.util.Observable, {
	
	constructor : function(config)
	{
		Ext.apply(this, config);
		Z8.Tester.superclass.constructor.call(this);
		
		this.addEvents('message', 'error');
	},
	
	init: function()
	{
		if(!this.window){ this.window = new Z8.TesterWindow({tester: this}); }
		
		this.window.on('show', this.testMenus, this);
		
		this.window.show();
	},
	
	testMenus: function(components)
	{
		var components = Z8.viewport.loginInfo.user.components;
		var that = this;
		var successRes = [];
		
		async.mapSeries(components, function(component, callback){
			that.request({
				menu:		component.id,
				requestId:	'Z8.desktop'
			}, callback, component);

		}, function(err, results){
			
			that.fireEvent('message', {message: 'Тест меню начат', status: 'ok'});
			Ext.each(results, function(result){
				if(result.data){
					successRes.push(result.data);
					that.fireEvent('message', {message: 'Меню "' + result.object.text + '" было успешно загружено', status: 'ok'});
				}
				else if(result.exception){
					that.fireEvent('exception', {message: 'Меню "' + result.object.text + '" не загрузилось с exception', status: 'exception', error: result.exception});
				}
				else if(result.error){
					that.fireEvent('error', {message: 'Меню "' + result.object.text + '" не загрузилось с ошибкой', status: 'error', error: result.error});
				}
			});
			
			that.fireEvent('message', {message: 'Тест меню завершен', status: 'ok'});
			
			that.testForms(successRes);
		});
	},
	
	testForms: function(menus)
	{
		var menuitems = this.createForms(menus);
		var that = this;
		var successRes = [];
		
		async.mapSeries(menuitems, function(menuitem, callback){	
			that.request({
				requestId:	menuitem.id
			}, callback, menuitem);
		}, function(err, results){
			
			that.fireEvent('message', {message: 'Тест форм и процедур начат', status: 'ok'});
			Ext.each(results, function(result){
				if(result.data){
					successRes.push(result.data);
					if (result.object.isJob)
					{
						that.fireEvent('message', {message: 'Процедура "' + result.object.text + '" была запущена', status: 'ok'});
					} else
						that.fireEvent('message', {message: 'Форма "' + result.object.text + '" была успешно загружена', status: 'ok'});
				}
				else if(result.exception){
					if (result.object.isJob)
					{
						that.fireEvent('exception', {message: 'Процедура "' + result.object.text + '" не запустилась с exception', status: 'exception', error: result.exception});
					}
					else
						that.fireEvent('exception', {message: 'Форма "' + result.object.text + '" не загрузилась с exception', status: 'exception', error: result.exception});
				}
				else if(result.error){
					that.fireEvent('error', {message: 'Форма "' + result.object.text + '" не загрузилась с ошибкой', status: 'error', error: result.error});
				}
			});
			
			that.fireEvent('message', {message: 'Тест форм и процедур завершен', status: 'ok'});
			
			that.testBackwards(successRes);
			that.testCommands(successRes);
			//that.testCrud(successRes);
		});
	},
	
	testBackwards: function(forms)
	{
		var backwards = this.createBackwards(forms);
		var that = this;
		
		async.mapSeries(backwards, function(backward, callback){	
			that.request({
				queryId:	backward.queryId,
				recordId:	backward.recordId,
				requestId:	backward.requestId
			}, callback, backward);
		}, function(err, results){
			
			that.fireEvent('message', {message: 'Тест подчиненных форм начат', status: 'ok'});
			Ext.each(results, function(result){
				if(result.data){
					that.fireEvent('message', {message: 'Подчиненная форма "' + result.data.text + '" из формы "' + result.object.fromText + '" была успешно загружена', status: 'ok'});
				}
				else if(result.exception){
					that.fireEvent('exception', {message: 'Подчиненная форма "' + result.exception.text + '" не загрузилась с exception', status: 'exception', error: result.exception});
				}
				else if(result.error){
					that.fireEvent('exception', {message: 'Подчиненная форма "' + result.error.text + '" не загрузилась с error', status: 'error', error: result.error});
				}
			});
			
			that.fireEvent('message', {message: 'Тест подчиненных форм завершен', status: 'ok'});
			
		});
	},
	
	testCommands: function(forms)
	{
		var commands = this.createCommands(forms);
		var that = this;
		
		async.mapSeries(commands, function(command, callback){
			that.request({
				command:	command.id,
				data:		command.data,
				requestId:	command.requestId,
				title:		command.title,
				xaction:	'command'
			}, callback, command);
		}, function(err, results){
			
			that.fireEvent('message', {message: 'Тест команд начат', status: 'ok'});
			Ext.each(results, function(result){
				if(result.data){
					var messages = '';
					Ext.each(result.data.messages, function(message){
						messages += ' ' + message;
					});
					
					that.fireEvent('message', {message: 'Команда "' + result.object.command + '" из формы "' + result.object.title + '" из формы была успешно выполнена. Результат: "' + messages + '"', status: 'ok'});
				}
				else if(result.exception){
					that.fireEvent('exception', {message: 'Команда "' + result.object.command + '" из формы "' + result.object.title + '" не загрузилась с exception', status: 'exception', error: result.exception});
				}
				else if(result.error){
					that.fireEvent('error', {message: 'Команда "' + result.object.command + '" из формы "' + result.object.title + '" не загрузилась с ошибкой', status: 'error', error: result.error});
				}
			});
			
			that.fireEvent('message', {message: 'Тест команд завершен', status: 'ok'});

			
			/*Ext.each(results, function(result){
				if(result.data){
					var messages = '';
					Ext.each(result.data.messages, function(message){
						messages += ' ' + message;
					});
					
					that.fireEvent('message', {message: 'Команда "' + result.object.command + '" из формы "' + result.object.title + '" из формы была успешно выполнена. Результат: "' + messages + '"', status: 'ok'});
				}
				else if(result.exception){
					console.log(result.exception);
					//that.fireEvent('exception', {message: 'Подчиненная форма "' + result.data.text + '" из формы "' + result.object.fromText + '" не загрузилась с exception', status: 'exception', error: 'exception'});
				}
				
			});*/
			
		});
	},
	
	createCommands: function(forms)
	{
		var items = [];
		
		Ext.each(forms, function(form, index){
			if (form.data && form.data.length > 0)
			{
				Ext.each(form.commands, function(command, i){
					var recordId = form.data[0][form.primaryKey];
					var data = Ext.encode([recordId]);
					items.push({ requestId: form.requestId, id: command.id, data: data, title: form.text, command: command.text });
				});
			}
		});
		
		return items;
	},
	
	createBackwards: function(forms)
	{
		var items = [];
		
		Ext.each(forms, function(form, index){
			if (form.data && form.data.length > 0)
			{
				Ext.each(form.backwards, function(backward){
					var recordId = form.data[0][form.primaryKey];
					items.push({ requestId: form.requestId, queryId: backward.queryId, recordId: recordId, fromText: form.text });
				});
			}
		});
		
		return items;
	},
	
	
	createForms: function(menus)
	{
		var items = [];
		
		Ext.each(menus, function(menu, index)
		{
			Ext.iterate(menu.data, function(block, menuitems){
				Ext.each(menuitems, function(menuitem, index){
					items.push(menuitem);
				});
			});
		});
		
		return items;
	},
	
	testCrud: function(forms)
	{
		Ext.each(forms, function(form, index){
			this.createNewFormRecord(form);
		}, this);
	},
	
	createNewFormRecord: function(form)
	{
		var that = this;
		var successRes = [];
		
		async.map([form], function(form, callback){
			that.request({
				requestId: form.requestId,
				title: form.text,
				xaction: 'new'
			}, callback, form);
		}, function(err, results){
			
			Ext.each(results, function(result){
				if(result.data){					
					that.fireEvent('message', {message: 'Новая запись для формы "' + result.object.text + '" подготовлена', status: 'ok'});
				}
			});
			//that.prepareFormRecord(form, results);
		});
	},
	
	prepareFormRecord: function(form, results)
	{
		var that = this;
		
		var formCombos = this.createComboLinks(form);
		
		async.map(formCombos, function(combo, callback){	
			that.request({
				requestId: combo.requestId,
				fieldId: combo.fieldId,
				title: combo.title
			}, callback);
		}, function(err, comboresults){
			
			var comboResults = {};
			
			Ext.each(comboresults, function(result){
				comboResults[result.fieldId] = result;
			});
			
			that.createFormRecord(form, results, comboResults);
		});
	},
	
	createFormRecord: function(form, results, comboResults)
	{
		var that = this;
		
		Ext.each(results, function(result){
			var data = result.data[0];
			
			Ext.each(form.fields, function(field){
				if (field.required)
				{
					if (field.linked)
					{
						var linkValue = this.getComboLinkData(comboResults[field.id], field);
						data[field.id] = linkValue.value;
					}
					else
					{
						switch (field.serverType)
						{
							case 'string': data[field.id] = this.randomString(field.length); break;
							case 'int': data[field.id] = this.randomInt(); break;
							case 'float': data[field.id] = this.randomFloat(); break;
							case 'date': break;
						}
					}
				}			
			}, this);
			
			async.map([form], function(form, callback){	
				that.request({
					data: Ext.encode(data),
					requestId: form.requestId,
					xaction: 'create'
				}, callback);
			}, function(err, createresults){
				that.updateFormRecord(form, createresults, comboResults);
			});
			
		}, this);
	},
	
	updateFormRecord: function(form, createresults, comboResults)
	{
		var that = this;
		
		Ext.each(createresults, function(createresult){
			
			var data = createresult.data[0];
			
			async.map([createresult], function(createresult, callback){	
				that.request({
					data: Ext.encode(data),
					requestId: createresult.requestId,
					xaction: 'update'
				}, callback);
			}, function(err, updateresults){
				that.deleteFormRecord(form, createresult, comboResults);
			});
		});
	},
	
	deleteFormRecord: function(form, createresult, comboResults)
	{
		var that = this;
		
		var data = createresult.data[0];
		
		async.map([createresult], function(createresult, callback){	
			that.request({
				data: data[form.primaryKey],
				requestId: createresult.requestId,
				xaction: 'destroy'
			}, callback);
		}, function(err, deleteresults){

		});
	},
	
	getComboLinkData: function(comboResult, field)
	{
		return {recordId: comboResult.data[0][comboResult.primaryKey], value: comboResult.data[0][field.id]};           
	},
	
	createComboLinks: function(form)
	{
		var items = [];
		
		Ext.each(form.fields, function(field){
			if(field.linked)
			{
				items.push({ requestId: form.requestId, fieldId: field.id, title: form.text });
			}
		});
		
		return items;
	},
	
	request: function(params, callback, object)
	{
		params = Ext.apply(params, {
			sessionId:	Z8.sessionId,
			parameters:	Ext.encode({})
		});
		
		Ext.Ajax.request({
			url: Z8.request.url,
			params: params,
			scope: this,
			success: function(response, request){ this.onSuccess(response, request, callback, object); },
			failure: function(response, request){ this.onFailure(response, request, callback, object); }
		}, this);
		
		
	},
	
	onSuccess: function(response, request, callback, object)
	{
		try {
			var result = Ext.decode(response.responseText);
		} catch(exception) {
			callback(null, {exception: exception, object: object});
		}
		
		if(result.success){
			callback(null, {data: result, object: object});
		}else{
			callback(null, {error: result, object: object});
		}
	},
	
	onFailure: function(response, request, callback, object)
	{
		var message = response.statusText ? response.statusText : 'Undefined server message.';
		callback(null, {error: message, object: object});
	},
	
	randomInt: function(minValue, maxValue)
	{
		if(typeof(minValue) == 'undefined'){minValue = 0;}
		if(typeof(maxValue) == 'undefined'){maxValue = 100;}
		
		return minValue + Math.floor(Math.random() * (maxValue - minValue + 1));
	},

	randomFloat: function(minValue, maxValue, precision)
	{
		if(typeof(minValue) == 'undefined'){minValue = 0;}
		if(typeof(maxValue) == 'undefined'){maxValue = 100;}
		if(typeof(precision) == 'undefined'){precision = 2;}
	    
	    return parseFloat(Math.min(minValue + (Math.random() * (maxValue - minValue)),maxValue).toFixed(precision));
	},
	
	randomString: function(string_length)
	{
		if(typeof(string_length) == 'undefined'){string_length = 8;}
		
		var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
		var randomstring = '';
		
		for (var i=0; i<string_length; i++)
		{
			var rnum = Math.floor(Math.random() * chars.length);
			randomstring += chars.substring(rnum,rnum+1);
		}
		
		return randomstring;
	}
});