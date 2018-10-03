Z8.define('Z8.form.field.Geometry', {
	extend: 'Z8.form.field.Control',

	isGeometry: true,
	instantAutoSave: true,

	minHeight: Ems.unitsToEms(4),
	zoom: 17,
	minZoom: 11,
	maxZoom: 21,

	tag: 'div',

	gridStep: 20,

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

		return [zoom].add(this.createInteractionTools());
	},

	createInteractionTools: function() {
		var interactions = [];

		if(this.selectTool !== false) {
			var select = new Z8.button.Tool({ icon: 'fa-mouse-pointer', tooltip: 'Выбор объектов', toggled: true });
			this.setSelectTool(select);
			interactions.add(select);
		} else
			this.selectTool = null;

		if(this.rulerTool !== false) {
			var ruler = new Z8.button.Tool({ icon: 'fa-ruler-combined', tooltip: 'Линейка', toggled: false });
			this.setRulerTool(ruler);
			interactions.add(ruler);
		} else
			this.selectTool = null;

		if(this.moveTool !== false) {
			var move = new Z8.button.Tool({ icon: 'fa-hand-paper-o', tooltip: 'Переместить объект', toggled: false, enabled: false });
			this.setMoveTool(move);
			interactions.add(move);
		} else
			this.moveTool = null;

		if(this.editTool !== false) {
			var edit = new Z8.button.Tool({ icon: 'fa-pencil', tooltip: 'Изменить объект', toggled: false, enabled: false });
			this.setEditTool(edit);
			interactions.add(edit);
		} else
			this.editTool = null;

		if(this.drawTool !== false) {
			var draw = new Z8.button.Tool({ icon: 'fa-pencil-square-o', tooltip: 'Нарисовать объект', toggled: false, enabled: false });
			this.setDrawTool(draw);
			interactions.add(draw);
		} else
			this.drawTool = null;
/*
		var erase = this.eraseButton = new Z8.button.Tool({ icon: 'fa-eraser', tooltip: 'Удалить объект', toggled: false, enabled: false });
		edit.on('toggle', this.onInteractionToggle, this);
*/
		return [new Z8.button.Group({ items: interactions, radio: true })];
	},

	setZoomOutTool: function(tool) {
		tool.on('click', this.onZoomOut, this);
		this.zoomOutTool = tool;
	},

	setZoomInTool: function(tool) {
		tool.on('click', this.onZoomIn, this);
		this.zoomInTool = tool;
	},

	setSelectTool: function(tool) {
		tool.on('toggle', this.onInteractionToggle, this);
		this.selectTool = tool;
	},

	setRulerTool: function(tool) {
		tool.on('toggle', this.onInteractionToggle, this);
		this.rulerTool = tool;
	},

	setMoveTool: function(tool) {
		tool.on('toggle', this.onInteractionToggle, this);
		this.moveTool = tool;
	},

	setEditTool: function(tool) {
		tool.on('toggle', this.onInteractionToggle, this);
		this.editTool = tool;
	},

	setDrawTool: function(tool) {
		tool.on('toggle', this.onInteractionToggle, this);
		this.drawTool = tool;
	},

	isToolToggled: function(tool) {
		return tool != null && tool.toggled;
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

		if(!enabled || !canMove && this.isToolToggled(this.move) || !canEdit && this.isToolToggled(this.edit) || !canDraw && this.isToolToggled(this.draw))
			this.toggleSelect();

		this.enableTool(this.moveTool, enabled && canMove);
		this.enableTool(this.editTool, enabled && canEdit);
		this.enableTool(this.drawTool, enabled && canDraw);
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

	replaceLayer: function(index, config) {
		this.map.getLayers().setAt(index, this.createLayer(config));
	},

	createMap: function() {
		var geometry = Application.geometry;

		var layers = [];

		if(this.hasTiles !== false && !Z8.isEmpty(geometry.layers)) {
			var layer = this.createLayer(geometry.layers[0]);
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

		var view = this.view = new ol.View({ center: [0, 0], zoom : this.zoom, minZoom: this.minZoom, maxZoom: this.maxZoom, projection: projection });
		view.on('change:resolution', this.onResolutionChange, this);

		var mapContainer = this.mapContainer = this.selectNode('.control.geometry');
		var map = this.map = new ol.Map({ layers: layers, target: mapContainer, view: view });
		map.on('moveend', this.onMove, this);

		this.installEdit();

		var snap = this.snap = new ol.interaction.Snap({ source: this.getVectorSource() });
		snap.setActive(false);
		map.addInteraction(snap);

		DOM.on(mapContainer, 'keyDown', this.onKeyDown, this);
		DOM.on(mapContainer, 'keyUp', this.onKeyUp, this);
		DOM.on(mapContainer, 'contextMenu', this.onContextMenu, this);
		DOM.on(window, 'resize', this.onResize, this);

		this.map.once('postcompose', function(event) {
			this.canvas = event.context.canvas;
		}, this);
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
		return new ol.interaction.Modify({ source: this.getVectorSource() });
	},

	createDraw: function() {
		return new ol.interaction.Draw({ source: this.getVectorSource(), snapTolerance: 0, freehandCondition: ol.events.condition.never, type: this.getDrawType() });
	},

	installEdit: function() {
		var map = this.map;
		if(map == null)
			return;

		if(this.rulerTool != null) {
			var ruler = this.ruler = this.createRuler();
			ruler.setActive(false);
			ruler.on('drawstart', this.onRulerStart, this);
			ruler.on('drawend', this.onRulerEnd, this);
			map.addInteraction(ruler);
		}

		if(this.moveTool != null) {
			var move = this.move = this.createMove();
			move.setActive(false);
			move.on('translatestart', this.onEditStart, this);
			move.on('translateend', this.onEditEnd, this);
			map.addInteraction(move);
		}

		if(this.editTool != null) {
			var edit = this.edit = this.createEdit();
			edit.setActive(false);
			edit.on('modifystart', this.onEditStart, this);
			edit.on('modifyend', this.onEditEnd, this);
			map.addInteraction(edit);
		}

		if(this.drawTool != null) {
			var draw = this.draw = this.createDraw();
			draw.setActive(false);
			draw.on('drawstart', this.onEditStart, this);
			draw.on('drawend', this.onEditEnd, this);
			map.addInteraction(draw);
		}
	},

	uninstallEdit: function() {
		var map = this.map;
		if(map == null)
			return;

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
	},

	resetEdit: function() {
		this.uninstallEdit();
		this.installEdit();
	},

	onDestroy: function() {
		this.clear();

		var mapContainer = this.mapContainer;

		DOM.un(mapContainer, 'keyUp', this.onKeyUp, this);
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

	setValue: function(value, displayValue) {
		this.cancelEdit(false);
		this.callParent(value, displayValue);
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
		return this.isEditing && (this.isActiveInteraction(this.move) || this.isActiveInteraction(this.edit));
	},

	isDrawing: function() {
		return this.isEditing && this.isActiveInteraction(this.draw);
	},

	onFeatureChange: function(event) {
		if(this.isModifying())
			this.editingFeature = event.feature;
	},

	onEditStart: function(event) {
		this.isEditing = true;
		this.editingFeature = event.feature;
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

		this.rulerFeature = event.feature;
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

		if(this.isEditing && key == Event.ESC) {
			this.cancelEdit();
			event.stopEvent();
		}

		if(key == Event.CTRL) {
			var snap = this.snap;
			if(!snap.getActive()) {
				snap.setActive(true);
				event.stopEvent();
			}
		}

		if(this.isEditing && key == Event.ENTER) {
			this.finishEdit();
			event.stopEvent();
		}
	},

	onKeyUp: function(event, target) {
		if(event.getKey() == Event.CTRL) {
			var snap = this.snap;
			if(snap.getActive()) {
				snap.setActive(false);
				event.stopEvent();
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

	toggleTool: function(tool, toggled) {
		if(tool != null)
			tool.setToggled(toggled);
	},

	toggleSelect: function() {
		this.toggleTool(this.selectTool, true);
	},

	toggleMove: function() {
		this.toggleTool(this.moveTool, true);
	},

	toggleEdit: function() {
		this.toggleTool(this.editTool, true);
	},

	toggleDraw: function() {
		this.toggleTool(this.drawTool, true);
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
		return this.record != null && this.feature == null;
	},

	getDrawingFeature: function() {
		return this.editingFeature;
	},

	convertDrawGeometry: function(geometry) {
		return geometry;
	},

	isActiveInteraction: function(interaction) {
		return interaction != null ? interaction.getActive() : false;
	},

	activateInteraction: function(interaction, activate) {
		if(interaction != null)
			interaction.setActive(activate);
	},

	onInteractionToggle: function(tool) {
		this.cancelEdit(false);
		this.activateInteraction(this.ruler, tool == this.rulerTool);
		this.activateInteraction(this.move, tool == this.moveTool && this.canMove());
		this.activateInteraction(this.edit, tool == this.editTool && this.canEdit());
		this.activateInteraction(this.draw, tool == this.drawTool && this.canDraw());
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
		var geometry = feature.getGeometry();
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
