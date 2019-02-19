Z8.define('Z8.form.field.Geometry', {
	extend: 'Z8.form.field.Control',

	isGeometry: true,
	instantAutoSave: true,

	minHeight: Ems.unitsToEms(4),

	zoom: 17,
	minZoom: 11,
	maxZoom: 21,
	zoomFactor: 2,

	tag: 'div',

	gridStep: 20,
	updateSizePending: true,

	isValid: function() {
		return true;
	},

	validate: function() {},

	controlMarkup: function() {
		return [{ cls: 'control geometry', tabIndex: 0 }];
	},

	htmlMarkup: function() {
		var label = this.label;
		if(label !== false) {
			if(label == null)
				label = this.label = {};
			if(label.tools == null)
				label.tools = new Z8.button.Group({ items: this.createTools() });
		}
		return this.callParent();
	},

	createTools: function() {
		var zoomOut = new Z8.button.Tool({ icon: 'fa-minus-circle', tooltip: 'Уменьшить' });
		this.setZoomOutTool(zoomOut);

		var zoomIn = new Z8.button.Tool({ icon: 'fa-plus-circle', tooltip: 'Увеличить' });
		this.setZoomInTool(zoomIn);

		var zoom = new Z8.button.Group({ items: [zoomOut, zoomIn] });

		var tools = this.createGeometryTools();
		if (tools != null)
			this.setGeometryTools(tools);

		return [zoom].add(tools || []);
	},

	createGeometryTools: function() {
		return new Z8.button.GeometryTools();
	},

	setGeometryTools: function(tools) {
		tools.on('select', this.onToolSelect, this);
		this.geometryTools = tools;
	},

	setZoomOutTool: function(tool) {
		tool.on('click', this.onZoomOut, this);
		this.zoomOutTool = tool;
	},

	setZoomInTool: function(tool) {
		tool.on('click', this.onZoomIn, this);
		this.zoomInTool = tool;
	},

	setUndoTool: function(tool) {
		tool.on('click', this.onUndo, this);
		this.undoTool = tool;
	},

	setRedoTool: function(tool) {
		tool.on('click', this.onRedo, this);
		this.redoTool = tool;
	},

	enableTool: function(tool, enable) {
		return tool != null && tool.setEnabled(enable);
	},

	updateTools: function() {
		if(this.getDom() == null)
			return;

		var enabled = !this.isReadOnly() && this.isEnabled();
		var canMove = this.canMove();
		var canEdit = this.canEdit();
		var canDraw = this.canDraw();
		var canErase = this.canErase();
		var canRotate = this.canRotate();

		var tools = this.geometryTools;

		if(tools == null)
			return;

		if(!enabled || !canMove && tools.isMoveActive() || !canEdit && tools.isEditActive() || !canDraw && tools.isDrawActive()
				 || !canErase && tools.isEraseActive() || !canRotate && tools.isRotateActive())
			tools.activateSelect();

		tools.enableMove(enabled && canMove);
		tools.enableEdit(enabled && canEdit);
		tools.enableDraw(enabled && canDraw);
		tools.enableErase(enabled && canErase);
		tools.enableRotate(enabled && canRotate);
	},

	completeRender: function() {
		this.callParent();
		this.createMap();
		this.updateTools();
	},

	getVectorLayer: function() {
		return this.vectorLayer;
	},

	getGridLayer: function() {
		return this.gridLayer;
	},

	getRulerLayer: function() {
		return this.rulerLayer;
	},

	getSource: function(layer) {
		return layer != null ? layer.getSource() : null;
	},

	getVectorSource: function() {
		return this.getSource(this.getVectorLayer());
	},

	getRulerSource: function() {
		return this.getSource(this.getRulerLayer());
	},

	getGridSource: function() {
		return this.getSource(this.getGridLayer());
	},

	clearLayer: function(layer) {
		if(layer != null)
			layer.getSource().clear();
	},

	clearVector: function() {
		this.clearLayer(this.getVectorLayer());
	},

	clearRuler: function() {
		this.clearLayer(this.getRulerLayer());
	},

	clearGrid: function() {
		this.clearLayer(this.getGridLayer());
	},

	clear: function() {
		this.clearVector();
		this.clearRuler();
		this.clearGrid();
		this.feature = null;
	},

	createLayer: function(config) {
		var source = new ol.source[config.sourceCls](config);
		var layer = new ol.layer[config.cls]({ source: source });
		layer.name = config.name;
		return layer;
	},

	addLayer: function(config) {
		this.map.getLayers().insertAt(1, this.createLayer(config));
	},

	getLayerByName: function(name) {
		var layers = this.map.getLayers().getArray();
		for(var i = 0, length = layers.length; i < length; i++) {
			var layer = layers[i];
			if(layer.name == name)
				return layer;
		}
		return null;
	},

	removeLayer: function(config) {
		var layer = this.getLayerByName(config.name);
		if(layer != null)
			this.map.getLayers().remove(layer);
	},

	replaceLayer: function(index, config) {
		this.map.getLayers().setAt(index, this.createLayer(config));
	},

	createMap: function() {
		var geometry = Application.geometry;

		var layers = [];

		if(this.hasTiles !== false && !Z8.isEmpty(geometry.layers)) {
			var layer = this.layer = geometry.layers[0]
			layer = this.createLayer(layer);
			layers.add(layer);
		}

		var me = this;

		var getRulerStyle = function(feature, resolution) {
			return me.getRulerStyle(feature, me.getZoom(), resolution);
		};

		var getGridStyle = function(feature, resolution) {
			return me.getGridStyle(feature, me.getZoom(), resolution);
		};

		var getStyle = function(feature, resolution) {
			return me.getStyle(feature, me.getZoom(), resolution);
		};

		var source = new ol.source.Vector({ wrapX: false });
		source.on('changefeature', this.onFeatureChange, this);
		var layer = this.vectorLayer = new ol.layer.Vector({ source: source, style: this.getStyle != null ? getStyle : undefined });
		layers.add(layer);

		source = new ol.source.Vector({ wrapX: false });
		layer = this.gridLayer = new ol.layer.Vector({ source: source, style: this.getGridStyle != null ? getGridStyle : undefined });
		layers.add(layer);

		source = new ol.source.Vector({ wrapX: false });
		layer = this.rulerLayer = new ol.layer.Vector({ source: source, style: this.getRulerStyle != null ? getRulerStyle : undefined });
		layers.add(layer);

		var projection = new ol.proj.Projection({ code: geometry.projection, units: 'm', axisOrientation: 'enu' });

		var view = this.view = new ol.View({ center: [0, 0], zoom : this.zoom, minZoom: this.minZoom, maxZoom: this.maxZoom, zoomFactor: this.zoomFactor, projection: projection });
		view.on('change:resolution', this.onResolutionChange, this);

		var mapContainer = this.mapContainer = this.selectNode('.control.geometry');
		var map = this.map = new ol.Map({ layers: layers, target: mapContainer, view: view });
		map.on('moveend', this.onMove, this);

		this.installEdit();

		DOM.on(mapContainer, 'keyDown', this.onKeyDown, this);
		DOM.on(mapContainer, 'contextMenu', this.onContextMenu, this);
		DOM.on(window, 'resize', this.onResize, this);

		this.map.once('postcompose', function(event) {
			this.canvas = event.context.canvas;
		}, this);
	},

	createBox: function() {
		var style = new ol.style.Style({
			stroke: new ol.style.Stroke({ color: 'blue',  width: 2 })
		});

		var me = this;
		var box = new ol.interaction.Extent({ boxStyle: style });

		var handleEvent = box.handleEvent;
		box.handleEvent = function(event) {
			handleEvent.call(box, event);
			if(event.type == 'pointerup')
				me.fireEvent('boxSelect', me, box.getExtent());
		};

		return box;
	},

	createRuler: function() {
		var me = this;

		var getRulerStyle = function(feature, resolution) {
			return me.getRulerStyle(feature, me.getZoom(), resolution, true);
		};

		return new ol.interaction.Draw({ source: this.getRulerSource(), type: 'LineString', style: this.getRulerStyle != null ? getRulerStyle : undefined });
	},

	createMove: function() {
		return new ol.interaction.Translate({ hitTolerance: 5 });
	},

	createEdit: function() {
		var altOrCtrl = function(mapBrowserEvent) {
			var e = mapBrowserEvent.originalEvent;
			return (e.altKey || e.ctrlKey) && !(e.metaKey || e.shiftKey);
		};
		return new ol.interaction.Modify({ source: this.getVectorSource(), deleteCondition: altOrCtrl });
	},

	createDraw: function() {
		return new ol.interaction.Draw({ source: this.getVectorSource(), snapTolerance: 0, freehandCondition: ol.events.condition.never, type: this.getDrawType() });
	},

	createErase: function() {
		return new ol.interaction.Erase({ features: this.getVectorSource().getFeatures() });
	},

	createRotate: function() {
		return new ol.interaction.Rotate({ features: this.getVectorSource().getFeatures() });
	},

	installEdit: function() {
		var map = this.map;
		var tools = this.geometryTools;

		if(map == null || tools == null)
			return;


		if(tools.box !== false) {
			var box = this.box = this.createBox();
			box.setActive(false);
			map.addInteraction(box);
		}

		if(tools.ruler !== false) {
			var ruler = this.ruler = this.createRuler();
			ruler.setActive(false);
			ruler.on('drawstart', this.onRulerStart, this);
			ruler.on('drawend', this.onRulerEnd, this);
			map.addInteraction(ruler);
		}

		if(tools.move !== false) {
			var move = this.move = this.createMove();
			move.setActive(false);
			move.on('translatestart', this.onEditStart, this);
			move.on('translateend', this.onEditEnd, this);
			map.addInteraction(move);
		}

		if(tools.edit !== false) {
			var edit = this.edit = this.createEdit();
			edit.setActive(false);
			edit.on('modifystart', this.onEditStart, this);
			edit.on('modifyend', this.onEditEnd, this);
			map.addInteraction(edit);
		}

		if(tools.draw !== false) {
			var draw = this.draw = this.createDraw();
			draw.setActive(false);
			draw.on('drawstart', this.onEditStart, this);
			draw.on('drawend', this.onEditEnd, this);
			map.addInteraction(draw);
		}

		if(tools.erase !== false) {
			var erase = this.erase = this.createErase();
			erase.setActive(false);
			erase.on('erasestart', this.onEditStart, this);
			erase.on('eraseend', this.onEditEnd, this);
			map.addInteraction(erase);
		}

		if(tools.rotate !== false) {
			var rotate = this.rotate = this.createRotate();
			rotate.setActive(false);
			rotate.on('rotatestart', this.onEditStart, this);
			rotate.on('rotateend', this.onEditEnd, this);
			map.addInteraction(rotate);
		}
	},

	uninstallEdit: function() {
		var map = this.map;
		var tools = this.geometryTools;

		if(map == null || tools == null)
			return;

		var box = this.box;
		if(box != null) {
			map.removeInteraction(box);
			this.box = null;
		}

		var ruler = this.ruler;
		if(ruler != null) {
			ruler.un('drawstart', this.onRulerStart, this);
			ruler.un('drawend', this.onRulerEnd, this);
			map.removeInteraction(ruler);
			this.ruler = null;
		}

		var move = this.move;
		if(move != null) {
			move.un('translatestart', this.onEditStart, this);
			move.un('translateend', this.onEditEnd, this);
			map.removeInteraction(move);
			this.move = null;
		}

		var edit = this.edit;
		if(edit != null) {
			edit.un('modifystart', this.onEditStart, this);
			edit.un('modifyend', this.onEditEnd, this);
			map.removeInteraction(edit);
			this.edit = null;
		}

		var draw = this.draw;
		if(draw != null) {
			draw.un('drawstart', this.onEditStart, this);
			draw.un('drawend', this.onEditEnd, this);
			map.removeInteraction(draw);
			this.draw = null;
		}

		var erase = this.erase;
		if(erase != null) {
			erase.un('erasestart', this.onEditStart, this);
			erase.un('eraseend', this.onEditEnd, this);
			map.removeInteraction(erase);
			this.erase = null;
		}

		var rotate = this.rotate;
		if(rotate != null) {
			rotate.un('rotatestart', this.onEditStart, this);
			rotate.un('rotateend', this.onEditEnd, this);
			map.removeInteraction(rotate);
			this.rotate = null;
		}
	},

	resetEdit: function() {
		this.uninstallEdit();
		this.installEdit();
	},

	onDestroy: function() {
		this.clear();

		var mapContainer = this.mapContainer;

		DOM.un(mapContainer, 'keyDown', this.onKeyDown, this);
		DOM.un(mapContainer, 'contextMenu', this.onContextMenu, this);
		DOM.un(window, 'resize', this.onResize, this);

		if(mapContainer != null) {
			this.map.un('moveend', this.onMove, this);
			this.view.un('change:resolution', this.onResolutionChange, this);
			this.getVectorSource().un('changefeature', this.onFeatureChange, this);

			this.uninstallEdit();
		}

		this.mapContainer = this.map = this.canvas = this.vectorLayer = this.gridLayer = this.rulerLayer = this.view = null;

		this.callParent();
	},

	updateMapSize: function() {
		this.map.updateSize();
	},

	setActive: function(active) {
		if(this.isActive() == active)
			return;

		this.callParent(active);

		if(active && this.updateSizePending) {
			this.updateMapSize();
			this.updateSizePending = false;
		}
	},

	onResize: function(event, target) {
		if(this.isActive())
			this.updateMapSize();
		else
			this.updateSizePending = true;
	},

	valueToRaw: function(value) {
		return this.readFeature(value);
	},

	rawToValue: function(value) {
		return this.writeFeature(value);
	},

	getRawValue: function(value) {
		return this.feature;
	},

	setRawValue: function(value) {
		this.removeFeatures(this.feature);
		this.feature = value;
		this.addFeatures(value);
	},

	setRecord: function(record) {
		if(record != null) {
			if(record != this.getRecord() && !this.isInsideView(this.feature))
				this.setCenter(this.feature);
		} else
			this.clear();

		this.callParent(record);
	},

	hasFeatures: function() {
		return this.getFeatures().length != 0;
	},

	getFeatures: function() {
		var source = this.getVectorSource();
		return source != null ? source.getFeatures() : [];
	},

	cloneFeatures: function() {
		var result = [];
		var features = this.getFeatures();
		for(var i = 0, length = features.length; i < length; i++)
			result.add(features[i].clone());
		return result;
	},

	getFeatureById: function(id) {
		var source = this.getVectorSource();
		return source != null && source.getFeatureById(id);
	},

	addFeatures: function(feature) {
		if(feature == null)
			return;

		var source = this.getVectorSource();
		if(source == null)
			return null;

		var features = !Array.isArray(feature) ? [feature] : feature;

		for(var i = 0, length = features.length; i < length; i++) {
			feature = features[i];
			this.saveGeometry(feature);
			source.addFeature(feature);
		}
	},

	removeFeatures: function(feature, delay) {
		if(feature == null)
			return;

		var source = this.getVectorSource();

		if(source == null)
			return;

		var callback = function() {
			var features = !Array.isArray(feature) ? [feature] : feature;
			for(var i = 0, length = features.length; i < length; i++)
				source.removeFeature(features[i]);
		}

		delay != null ? new Z8.util.DelayedTask().delay(delay, callback) : callback();
	},

	getResolution: function() {
		return this.view.getResolution();
	},

	getZoom: function() {
		return this.view.getZoom();
	},

	getMinZoom: function() {
		return this.view.getMinZoom();
	},

	setMinZoom: function(minZoom) {
		this.view.setMinZoom(minZoom);
		this.updateZoomTools();
	},

	getMaxZoom: function() {
		return this.view.getMaxZoom();
	},

	setMaxZoom: function(maxZoom) {
		this.view.setMaxZoom(maxZoom);
		this.updateZoomTools();
	},

	onZoomOut: function(tool) {
		this.setZoom(this.getZoom() - 1);
	},

	onZoomIn: function(tool) {
		this.setZoom(this.getZoom() + 1);
	},

	onUndo: function(tool) {
	},

	onRedo: function(tool) {
	},

	onResolutionChange: function() {
		this.updateZoomTools(this.getZoom());
	},

	saveGeometry: function(feature, force) {
		if(feature.cachedGeometry == null || force) {
			var geometry = feature.getGeometry();
			if(geometry != null)
				feature.cachedGeometry = feature.getGeometry().clone();
		}
	},

	restoreGeometry: function(feature) {
		feature.setGeometry(feature.cachedGeometry != null ? feature.cachedGeometry.clone() : null);
	},

	isModifying: function() {
		var tools = this.geometryTools;
		return this.isEditing && tools != null && (tools.isMoveActive() || tools.isEditActive());
	},

	isDrawing: function() {
		var tools = this.geometryTools;
		return this.isEditing && tools != null && tools.isDrawActive();
	},

	onFeatureChange: function(event) {
		if(this.isModifying())
			this.editingFeature = this.getEventFeature(event);
	},

	onEditStart: function(event) {
		this.isEditing = true;
		this.editingFeature = this.getEventFeature(event);
	},

	onEditEnd: function() {
		if(!this.isEditing)
			return;

		var workingFeature = this.editingFeature;

		if(workingFeature == null) { // edit:end before any feature:change
			this.isEditing = false;
			return;
		}

		var isDrawing = this.isDrawing();
		var feature = isDrawing ? this.getDrawingFeature() : workingFeature;
		var record = feature.record;

		if(isDrawing) {
			var geometry = this.convertDrawGeometry(workingFeature.getGeometry());
			if(geometry == null) {
				this.cancelEdit();
				return;
			}
			feature.setGeometry(geometry);
			this.removeFeatures(workingFeature, 1);
			this.toggleSelect();
		}

		this.isEditing = false;
		this.editingFeature = null;

		this.setValue(this.rawToValue(feature));
	},

	getEventFeature: function(event) {
		var feature = event.feature;
		if (feature != null)
			return feature;

		var features = event.features;
		if (Array.isArray(features))
			return features[0];
		if (features instanceof ol.Collection)
			return features.getArray()[0];
		return null;
	},

	cancelEdit: function(toggleSelect) {
		var wasModifying = this.isModifying();

		this.resetEdit();

		if(!this.isEditing)
			return;

		if(wasModifying)
			this.restoreGeometry(this.editingFeature);

		this.isEditing = false;
		this.editingFeature = null;

		if(toggleSelect !== false)
			this.toggleSelect();
	},

	finishEdit: function() {
		this.draw.finishDrawing();
	},

	onRulerStart: function(event) {
		if(this.rulerFeature != null)
			this.getRulerSource().removeFeature(this.rulerFeature);

		this.rulerFeature = this.getEventFeature(event);
	},

	onRulerEnd: function(event) {
		this.rulerFeature.getGeometry().un('change', this.onRulerChange, this);
	},

	onMove: function(event) {
		this.updateGrid();
		this.fireEvent('move', this);
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ESC) {
			if(this.isEditing)
				this.cancelEdit();
			else
				this.toggleSelect();
			event.stopEvent();
		}

		if(this.isDrawing() && key == Event.ENTER) {
			this.finishEdit();
			event.stopEvent();
		}

		var tools = this.geometryTools;

		if(key == Event.L) {
			if(event.ctrlKey && tools != null && tools.isRulerEnabled()) {
				tools.activateRuler();
				event.stopEvent();
			}
		}

		if(key == Event.G) {
			if(event.ctrlKey && tools != null && tools.isRotateEnabled()) {
				tools.activateRotate();
				event.stopEvent();
			}
		}

		if(key == Event.M) {
			if(event.ctrlKey && tools != null && tools.isMoveEnabled()) {
				tools.activateMove();
				event.stopEvent();
			}
		}

		if(key == Event.E) {
			if(event.ctrlKey && tools != null && tools.isEditEnabled()) {
				tools.activateEdit();
				event.stopEvent();
			}
		}

		if(key == Event.D) {
			if(event.ctrlKey && tools != null && tools.isDrawEnabled()) {
				tools.activateDraw();
				event.stopEvent();
			}
		}

		if(key == Event.Z) {
			var undo = this.undoTool;
			if(event.ctrlKey && undo != null && undo.isEnabled()) {
				this.onUndo(undo);
				event.stopEvent();
			}
		}

		if(key == Event.Y) {
			var redo = this.redoTool;
			if(event.ctrlKey && redo != null && redo.isEnabled()) {
				event.stopEvent();
				this.onRedo(redo);
			}
		}
	},

	onContextMenu: function(event, target) {
		if(this.isDrawing()) {
			this.finishEdit();
			event.stopEvent();
		}
	},

	setZoom: function(zoom) {
		zoom = Math.min(Math.max(zoom, this.getMinZoom()), this.getMaxZoom());
		this.view.animate({ zoom: zoom });
		this.updateZoomTools(zoom);
	},

	updateZoomTools: function(zoom) {
		zoom = zoom || this.getZoom();
		if(this.zoomInTool != null)
			this.zoomInTool.setEnabled(zoom < this.getMaxZoom());
		if(this.zoomOutTool != null)
			this.zoomOutTool.setEnabled(zoom > this.getMinZoom());
		this.updateGrid();
	},

	toggleSelect: function() {
		var tools = this.geometryTools;
		if(tools != null)
			tools.activateSelect();
	},

	canSelect: function() {
		return true;
	},

	canMove: function() {
		return this.hasFeatures();
	},

	canEdit: function() {
		return this.hasFeatures();
	},

	getDrawType: function() {
		return 'LineString';
	},

	canDraw: function() {
		return this.record != null && (this.feature == null || this.feature.getGeometry() == null);
	},

	canErase: function() {
		return this.hasFeatures();
	},

	canRotate: function() {
		return this.hasFeatures();
	},

	getDrawingFeature: function() {
		return this.editingFeature;
	},

	convertDrawGeometry: function(geometry) {
		return geometry;
	},

	activateInteraction: function(interaction, activate) {
		if(interaction != null)
			interaction.setActive(activate);
	},

	onToolSelect: function(tools, tool) {
		if(tool.isTool) {
			this.cancelEdit(false);
			this.activateInteraction(this.box, tool.isBox);
			this.activateInteraction(this.ruler, tool.isRuler);
			this.activateInteraction(this.move, tool.isMove && this.canMove());
			this.activateInteraction(this.edit, tool.isEdit && this.canEdit());
			this.activateInteraction(this.draw, tool.isDraw && this.canDraw());
			this.activateInteraction(this.erase, tool.isErase && this.canErase());
			this.activateInteraction(this.rotate, tool.isRotate && this.canRotate());
		} else if(tool.isLocation)
			this.goToCurrentLocation();
		else if(tool.isYandex)
			this.openYandexMaps();
	},

	goToCurrentLocation: function() {
		var me = this;

		var callback = function(position) {
			var lon = position.coords.longitude;
			var lat = position.coords.latitude;
			var projection = me.view.getProjection();
			var coordinate = ol.proj.fromLonLat([lon, lat], projection);
			me.setCenter(coordinate[0], coordinate[1]);
		};

		navigator.geolocation.getCurrentPosition(callback);
	},

	openYandexMaps: function() {
		var center = ol.proj.toLonLat(this.getCenter(), this.view.getProjection());
		window.open('https://yandex.ru/maps/213/moscow/?z=' + this.getZoom() + '&ll=' + center[0] + ',' + center[1]);
	},

	getCenter: function() {
		return this.view.getCenter();
	},

	getExtent: function() {
		var view = this.view;
		return view != null ? view.calculateExtent() : null;
	},

	isInsideView: function(feature) {
		if(feature == null)
			return false;

		var view = this.getExtent();
		var geometry = (feature instanceof ol.geom.Geometry) ? feature : feature.getGeometry();
		return geometry != null ? ol.extent.containsExtent(view, geometry.getExtent()) : true;
	},

	setCenter: function(x, y) {
		var newCenter = null; 

		if(x instanceof ol.Feature) {
			var feature = x;
			var geometry = feature.getGeometry();
			if(geometry == null)
				return;

			var extent = geometry.getExtent();
			newCenter = [(extent[0] + extent[2]) / 2, (extent[1] + extent[3]) / 2];
		} else if(x instanceof ol.geom.Geometry) {
			var geometry = x;
			var extent = geometry.getExtent();
			newCenter = [(extent[0] + extent[2]) / 2, (extent[1] + extent[3]) / 2];
		} else if(Number.isNumber(x) && Number.isNumber(y))
			newCenter = [x, y];

		if(newCenter == null)
			return;

		var center = this.getCenter();
		if(center[0] != newCenter[0] || center[1] != newCenter[1])
			this.view.setCenter(newCenter);
	},

	readFeature: function(value) {
		if(Z8.isEmpty(value))
			return null;

		if(String.isString(value)) {
			var parser = new ol.format.GeoJSON();
			return parser.readFeature(value);
		}

		return value;
	},

	writeFeature: function(value) {
		return value != null && value.getGeometry() != null ? new ol.format.GeoJSON().writeFeature(value) : null;
	},

	writeGeometry: function(value) {
		return value != null ? new ol.format.GeoJSON().writeGeometry(value) : null;
	},

/*
 *  Default styling, should be defined as function(feature, zoom, resolution) to use customized styles 
*/
	getStyle: null,

	lineToPolygon: function(line, width) {
		var coordinates = line.getCoordinates();

		if(coordinates.length < 2)
			return null;

		var left = [];
		var right = [];
		width = (width || 2) / 2;

		for(var i = 1, length = coordinates.length; i < length; i += 1) {
			var start = coordinates[i - 1];
			var end = coordinates[i];
			var x1 = start[0];
			var x2 = end[0];
			var y1 = start[1];
			var y2 = end[1];

			if(x1 == x2) {
				left.push([[x1 - width, y1], [x2 - width, y2]]);
				right.push([[x1 + width, y1], [x2 + width, y2]]);
			} else if(y1 == y2) {
				left.push([[x1, y1 - width], [x2, y2 - width]]);
				right.push([[x1, y1 + width], [x2, y2 + width]]);
			} else {
				var dx = x1 - x2, dy = y1 - y2;
				var z = Math.sqrt(dx * dx + dy * dy);
				dx /= z; dy /= z;
				var xOffset = width * dy;
				var yOffset = width * dx;
				left.push([[x1 - xOffset, y1 + yOffset], [x2 - xOffset, y2 + yOffset]]);
				right.push([[x1 + xOffset, y1 - yOffset], [x2 + xOffset, y2 - yOffset]]);
			}
		}

		this.intersect(left);
		this.intersect(right);

		var points = [];
		var length = left.length;

		for(var i = 0; i < length; i++)
			points.push(left[i][0]);
		points.push(left[length - 1][1]);

		for(var i = right.length - 1; i >= 0; i--)
			points.push(right[i][1]);
		points.push(right[0][0]);

		points.push(left[0][0]);

		var polygon = new ol.geom.Polygon([points]);
		return new ol.geom.GeometryCollection([polygon, line]);
	},

	intersect: function(segments) {
		for(var i = 1, length = segments.length; i < length; i++) {
			var first = segments[i - 1];
			var second = segments[i];
			var x11 = first[0][0];
			var x12 = first[1][0];
			var y11 = first[0][1];
			var y12 = first[1][1];

			var x21 = second[0][0];
			var x22 = second[1][0];
			var y21 = second[0][1];
			var y22 = second[1][1];

			if(x11 == x12 && x21 == x22)
				throw 'Incorrect segment [' + x11 + ', ' + y11 + '] - [' + x21 + ', ' + y21 + ']';

			if(x11 != x12 && x21 != x22) {
				var a1 = (y11 - y12) / (x11 - x12);
				var a2 = (y21 - y22) / (x21 - x22);
				var b1 = (x11 * y12 - x12 * y11) / (x11 - x12);
				var b2 = (x21 * y22 - x22 * y21) / (x21 - x22);

				if(a1 == a2)
					throw 'Incorrect segment [' + x11 + ', ' + y11 + '] - [' + x21 + ', ' + y21 + ']';

				var x = (b2 - b1) / (a1 - a2);
			} else if(x11 == x12) {
				var x = x11;
				var a2 = (y21 - y22) / (x21 - x22);
				var b2 = (x21 * y22 - x22 * y21) / (x21 - x22);
				var y = a2 * x + b2;
			} else if(x21 == x22) {
				var x = x21;
				var a2 = (y11 - y12) / (x11 - x12);
				var b2 = (x11 * y12 - x12 * y11) / (x11 - x12);
				var y = a2 * x + b2;
			}

			var y = a2 * x + b2;

			first[1][0] = second[0][0] = x;
			first[1][1] = second[0][1] = y;
		}
	},

	getGridStep: function() {
		return this.gridStep || null;
	},

	setGridStep: function(step) {
		this.gridStep = step;
		this.updateGrid();
	},

	updateGrid: function() {
		this.clearGrid();

		var source = this.getGridSource();
		var step = this.getGridStep();

		if(step == null)
			return;

		var extent = this.getExtent();
		var width = ol.extent.getWidth(extent);
		var height = ol.extent.getHeight(extent);

		var count = (width / step).round();

		var inc = Math.max(count > 20 ? (count / 20).round() : 1, 1);

		var left = extent[0];
		var bottom = extent[1];
		var right = extent[2];
		var top = extent[3];

		var x = (left / step).round() * step;
		while(x < right) {
			if(x >= left) {
				var feature = new ol.Feature({ geometry: new ol.geom.LineString([[x, bottom], [x, top]]) });
				feature.vertical = true;
				feature.position = x;
				source.addFeature(feature);
			}
			x += inc * step;
		}

		var y = (bottom / step).round() * step;
		while(y < top) {
			if(y >= bottom) {
				var feature = new ol.Feature({ geometry: new ol.geom.LineString([[left, y], [right, y]]) });
				feature.vertical = false;
				feature.position = y;
				source.addFeature(feature);
			}
			y += inc * step;
		}
	},

	getRulerStyle: function(feature, zoom, resolution, drawing) {
		var styles = [
			new ol.style.Style({
				stroke: new ol.style.Stroke({ 
					color: drawing ? [128, 128, 0] : [128, 128, 128],
					lineDash: [5, 2],
					width: 1
				}),
				image: new ol.style.Circle({
					stroke: new ol.style.Stroke({ color: drawing ? [128, 128, 0] : [0, 0, 0, .3], width: 1 }),
					fill: new ol.style.Fill({ color: [255, 255, 255, .8] }),
					radius: 3
				})
			})
		];

		var coordinates = feature.getGeometry().getCoordinates();

		var distance = 0;
		var point1 = null;

		for(var i = 0, length = coordinates.length; i < length; i++) {
			var point2 = coordinates[i];

			if(point1 != null)
				distance += Math.sqrt(Math.pow(point1[0] - point2[0], 2) + Math.pow(point1[1] - point2[1], 2));

			point1 = point2;

			styles.push(new ol.style.Style({
				geometry: new ol.geom.Point(point2),
				image: new ol.style.Circle({
					stroke: new ol.style.Stroke({ color: drawing ? [128, 128, 0] : [0, 0, 0, .3], width: 1 }),
					fill: new ol.style.Fill({ color: [255, 255, 255, .8] }),
					radius: 3
				}),
				text: new ol.style.Text({
					text: distance < 1000 ? Format.integer(distance.round()) + ' м' : (Format.float((distance / 1000).round(2)) + ' км'),
					placement: 'point',
					overflow: true,
					font: 'normal ' + Ems.emsToPixels(.78571429) + 'px Roboto ',
					offsetY: -10,
					textAlign: 'center',
					textBaseline: 'middle',
					fill: new ol.style.Fill({ color: [0, 0, 0] }),
					backgroundFill: new ol.style.Fill({ color: [255, 255, 255] })
				})
			}));
		}

		return styles;
	},

	getGridStyle: function(feature, zoom, resolution) {
		if(this.font == null)
			this.font = DOM.getComputedStyle(this.mapContainer).font;

		var extent = feature.getGeometry().getExtent();
		var width = ol.extent.getWidth(extent);
		var height = ol.extent.getHeight(extent);
		var x = extent[0];
		var y = extent[1];
		var vertical = feature.vertical;
		var position = feature.position;

		return new ol.style.Style({
			stroke: this.gridStroke || new ol.style.Stroke({ color: [0, 0, 0, .1], width: 1 }),
			text: new ol.style.Text({
				text: Format.integer(position),
				placement: 'point',
				overflow: true,
				font: 'normal ' + Ems.emsToPixels(.64285714 /* 9px */ /*.78571429*/ /* 11px */) + 'px Roboto ',
				offsetX: vertical ? 0 : (-width / (2 * resolution) + 5),
				offsetY: vertical ? (height / (2 * resolution) - 5) : 0,
				textAlign: vertical ? 'center' : 'end',
				textBaseline: 'middle',
				fill: new ol.style.Fill({ color: [0, 0, 0] }),
				backgroundFill: new ol.style.Fill({ color: [255, 255, 255] })
			})
		});
	}

});
