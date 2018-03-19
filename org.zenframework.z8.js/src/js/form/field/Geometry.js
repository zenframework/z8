Z8.define('Z8.form.field.Geometry', {
	extend: 'Z8.form.field.Control',

	isGeometry: true,
	instantAutoSave: true,

	minHeight: Ems.unitsToEms(6),

	tag: 'div',

	isValid: function() {
		return true;
	},

	validate: function() {},

	controlMarkup: function() {
		return [{ cls: 'control geometry', tabIndex: 0 }];
	},

	htmlMarkup: function() {
		var label = this.label;
		if(label == null)
			label = this.label = {};

		if(label.tools == null)
			label.tools = new Z8.button.Group({ items: this.createTools() });

		return this.callParent();
	},

	createTools: function() {
		var zoomOut = this.zoomOutButton = new Z8.button.Tool({ icon: 'fa-minus-circle', tooltip: 'Уменьшить', handler: this.onZoomOut, scope: this });
		var zoomIn = this.zoomInButton = new Z8.button.Tool({ icon: 'fa-plus-circle', tooltip: 'Увеличить', handler: this.onZoomIn, scope: this });
		var zoom = new Z8.button.Group({ items: [zoomOut, zoomIn] });

		var select = this.selectButton = new Z8.button.Tool({ icon: 'fa-mouse-pointer', tooltip: 'Выбор объектов', toggled: true });
		select.on('toggle', this.onInteractionToggle, this);

		var move = this.moveButton = new Z8.button.Tool({ icon: 'fa-hand-paper-o', tooltip: 'Переместить объект', toggled: false, enabled: false });
		move.on('toggle', this.onInteractionToggle, this);

		var edit = this.editButton = new Z8.button.Tool({ icon: 'fa-pencil', tooltip: 'Изменить объект', toggled: false, enabled: false });
		edit.on('toggle', this.onInteractionToggle, this);

		var draw = this.drawButton = new Z8.button.Tool({ icon: 'fa-pencil-square-o', tooltip: 'Нарисовать объект', toggled: false, enabled: false });
		draw.on('toggle', this.onInteractionToggle, this);
/*
		var erase = this.eraseButton = new Z8.button.Tool({ icon: 'fa-eraser', tooltip: 'Удалить объект', toggled: false, enabled: false });
		edit.on('toggle', this.onInteractionToggle, this);
*/
		var interactions = new Z8.button.Group({ items: [select, move, edit, draw/*, erase*/], radio: true });

		return [zoom, interactions];
	},

	updateTools: function() {
		if(this.selectButton == null)
			return;

		var enabled = !this.isReadOnly() && this.isEnabled();
		var canMove = this.canMove();
		var canEdit = this.canEdit();
		var canDraw = this.canDraw();

		if(!enabled || !canMove && this.moveButton.toggled || 
				!canEdit && this.editButton.toggled || !canDraw && this.drawButton.toggled)
			this.toggleSelect();

		this.moveButton.setEnabled(enabled && canMove);
		this.editButton.setEnabled(enabled && canEdit);
		this.drawButton.setEnabled(enabled && canDraw);
	},

	completeRender: function() {
		this.callParent();
		this.createMap();
		this.updateTools();
	},

	createMap: function() {
		var imageParams = Z8.apply({ ratio: 1}, Application.geometry);
		var imageSource = new ol.source.ImageWMS(imageParams);
		var imageLayer = new ol.layer.Image({ source: imageSource });

		var vectorSource = this.vectorSource = new ol.source.Vector({ strategy : ol.loadingstrategy.bbox, wrapX: false });
		vectorSource.on('changefeature', this.onFeatureChange, this);

		var me = this;
		var callback = function(feature, resolution) {
			return me.getStyle(feature, me.getZoom(), resolution);
		};
		var vectorLayer = new ol.layer.Vector({ source : vectorSource, style: this.getStyle != null ? callback : undefined });

		var projection = new ol.proj.Projection({ code: 'EPSG:96872', units: 'm', axisOrientation: 'enu', global: false });
		var view = this.view = new ol.View({ center: [0, 0], zoom : 17, minZoom: 11, maxZoom: 21, projection: projection });
		view.on('change:resolution', this.onResolutionChange, this);

		var mapContainer = this.mapContainer = this.selectNode('.control');
		var map = this.map = new ol.Map({ layers: [imageLayer, vectorLayer], target: mapContainer, view: view });
		map.on('moveend', this.onMove, this);

		this.installEdit();

		var snap = this.snap = new ol.interaction.Snap({ source: vectorSource });
		snap.setActive(false);
		map.addInteraction(snap);

		DOM.on(mapContainer, 'keyDown', this.onKeyDown, this);
		DOM.on(mapContainer, 'keyUp', this.onKeyUp, this);
		DOM.on(mapContainer, 'contextMenu', this.onContextMenu, this);
	},

	installEdit: function() {
		var map = this.map;

		var move = this.move = new ol.interaction.Translate({ hitTolerance: 5 });
		move.setActive(false);
		move.on('translatestart', this.onEditStart, this);
		move.on('translateend', this.onEditEnd, this);
		map.addInteraction(move);

		var edit = this.edit = new ol.interaction.Modify({ source: this.vectorSource });
		edit.setActive(false);
		edit.on('modifystart', this.onEditStart, this);
		edit.on('modifyend', this.onEditEnd, this);
		map.addInteraction(edit);

		var draw = this.draw = new ol.interaction.Draw({ source: this.vectorSource, snapTolerance: 0, type: this.getDrawType() });
		draw.setActive(false);
		draw.on('drawstart', this.onEditStart, this);
		draw.on('drawend', this.onEditEnd, this);
		map.addInteraction(draw);
	},

	uninstallEdit: function() {
		var map = this.map;

		var move = this.move;
		move.un('translatestart', this.onEditStart, this);
		move.un('translateend', this.onEditEnd, this);
		map.removeInteraction(move);

		var edit = this.edit;
		edit.un('modifystart', this.onEditStart, this);
		edit.un('modifyend', this.onEditEnd, this);
		map.removeInteraction(edit);

		var draw = this.draw;
		draw.un('drawstart', this.onEditStart, this);
		draw.un('drawend', this.onEditEnd, this);
		map.removeInteraction(draw);

		this.move = this.edit = this.draw = null;
	},

	resetEdit: function() {
		this.uninstallEdit();
		this.installEdit();
	},

	onDestroy: function() {
		var mapContainer = this.mapContainer;

		DOM.un(mapContainer, 'keyUp', this.onKeyUp, this);
		DOM.on(mapContainer, 'keyDown', this.onKeyDown, this);
		DOM.un(mapContainer, 'contextMenu', this.onContextMenu, this);

		if(mapContainer != null) {
			this.map.un('moveend', this.onMove, this);
			this.view.un('change:resolution', this.onResolutionChange, this);
			this.vectorSource.un('changefeature', this.onFeatureChange, this);

			this.uninstallEdit();
		}

		this.mapContainer = this.map = this.vectorSource = this.view = null;

		this.callParent();
	},

	show: function() {
		this.callParent();
		this.map.updateSize();
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
		return this.vectorSource != null && this.vectorSource.getFeatures().length != 0;
	},

	addFeatures: function(feature) {
		if(feature == null || this.vectorSource == null)
			return;

		var features = !Array.isArray(feature) ? [feature] : feature;

		for(var i = 0, length = features.length; i < length; i++) {
			feature = features[i];
			this.saveGeometry(feature);
			this.vectorSource.addFeature(feature);
		}
	},

	removeFeatures: function(feature, delay) {
		if(feature == null || this.vectorSource == null)
			return;

		var source = this.vectorSource;

		var callback = function() {
			var features = !Array.isArray(feature) ? [feature] : feature;
			for(var i = 0, length = features.length; i < length; i++)
				source.removeFeature(features[i]);
		}

		delay != null ? new Z8.util.DelayedTask().delay(delay, callback) : callback();
	},

	clear: function() {
		var source = this.vectorSource;
		if(source != null) {
			this.onMoveLock = true;
			source.clear();
			this.onMoveLock = false;
		}

		this.feature = null;
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

	getMaxZoom: function() {
		return this.view.getMaxZoom();
	},

	onZoomOut: function(button) {
		this.setZoom(this.getZoom() - 1);
	},

	onZoomIn: function(button) {
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
		return this.isEditing && (this.move.getActive() || this.edit.getActive());
	},

	isDrawing: function() {
		return this.isEditing && this.draw.getActive();
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
		if(!this.isEditing)
			return;

		if(this.isModifying())
			this.restoreGeometry(this.editingFeature);

		this.isEditing = false;
		this.editingFeature = null;

		if(toggleSelect !== false)
			this.toggleSelect();

		this.resetEdit();
	},

	finishEdit: function() {
		this.draw.finishDrawing();
	},

	onMove: function(event) {
		if(!this.onMoveLock)
			this.fireEvent('move', this);
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(this.isEditing && key == Event.ESC) {
			this.cancelEdit();
			event.stopEvent();
		}

		if(key == Event.SHIFT) {
			this.snap.setActive(true);
			event.stopEvent();
		}

		if(key == Event.ENTER) {
			this.finishEdit();
			event.stopEvent();
		}
	},

	onKeyUp: function(event, target) {
		if(event.getKey() == Event.SHIFT) {
			this.snap.setActive(false);
			event.stopEvent();
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
		this.zoomInButton.setEnabled(zoom < this.getMaxZoom());
		this.zoomOutButton.setEnabled(zoom > this.getMinZoom());
	},

	toggleSelect: function() {
		this.selectButton.setToggled(true);
	},

	toggleMove: function() {
		this.moveButton.setToggled(true);
	},

	toggleEdit: function() {
		this.editButton.setToggled(true);
	},

	toggleDraw: function() {
		this.drawButton.setToggled(true);
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

	onInteractionToggle: function(button) {
		this.cancelEdit(false);
		this.move.setActive(button == this.moveButton && this.canMove());
		this.edit.setActive(button == this.editButton && this.canEdit());
		this.draw.setActive(button == this.drawButton && this.canDraw());
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

	setCenter: function(feature) {
		if(feature != null) {
			var geometry = feature.getGeometry();
			if(geometry == null)
				return;

			var extent = geometry.getExtent();
			var view = this.view;
			var center = view.getCenter();
			var newCenter = [(extent[0] + extent[2]) / 2, (extent[1] + extent[3]) / 2];
			if(center[0] != newCenter[0] || center[1] != newCenter[1])
				view.setCenter(newCenter);
			else
				this.onMove();
		}
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

	pointToPolygon: function(point, width) {
		var coordinates = point.getCoordinates();
		var x = coordinates[0];
		var y = coordinates[1];
		var width = (width || 1) / 2;
		var points = [[x - width, y - width], [x - width, y + width], [x + width, y + width], [x + width, y - width], [x - width, y - width]];
		var polygon = new ol.geom.Polygon([points]);
		return new ol.geom.GeometryCollection([polygon, point]);
	},

	pointToCircle: function(point, width) {
		var center = point.getCoordinates();
		var circle = new ol.geom.Circle(center, (width || 1) / 2);
		var polygon = ol.geom.Polygon.fromCircle(circle);
		return new ol.geom.GeometryCollection([polygon, point]);
	}
});
