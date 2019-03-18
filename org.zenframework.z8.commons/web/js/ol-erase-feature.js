(function() {

	//var EraseFeatureEvent = function(type, features) {
	//	this.type = type;
	//	this.features = features;
	//};
	
	var EraseFeatureEvent = class {
		constructor (type, feature) {
			this.propagationStopped_ = false;
			this.type_ = type;
			this.feature_ = feature;
		}

		get propagationStopped () { return this.propagationStopped_ }
		get type () { return this.type_ }
		get feature () { return this.feature_ }

		preventDefault () { this.propagationStopped_ = true }

		stopPropagation () { this.propagationStopped_ = true }
	};

	var Erase = ol.interaction.Erase = function() {
		ol.interaction.Pointer.call(this, {
			handleEvent : Erase.prototype.handleEvent,
		});

		this.overFeature = false;
	};
	ol.inherits(Erase, ol.interaction.Interaction);

	// load fa-eraser as image
	setTimeout(function() {
		var canvas = document.createElement('canvas');
		canvas.width = 24;
		canvas.height = 24;
		var ctx = canvas.getContext('2d');
		ctx.fillStyle = '#000000';
		ctx.font = '20px FontAwesome';
		ctx.textAlign = 'center';
		ctx.textBaseline = 'middle';
		ctx.fillText('\uf12d', 12, 12);
		Erase.prototype.eraserUrl = 'url(' + canvas.toDataURL('image/png') + ')';
	}, 2000);

	Erase.prototype.handleEvent = function(evt) {
		var map = evt.map;
		var type = evt.type;

		var feature = map.forEachFeatureAtPixel(evt.pixel, function(feature) {
			return feature;
		});

		if (type == 'pointermove') {
			// switch cursor
			var wasOverFeature = this.overFeature;
			var overFeature = (feature != null);
			if (overFeature != wasOverFeature) {
				var element = evt.map.getTargetElement();
				element.style.cursor = (overFeature ? this.eraserUrl + ', auto'
						: 'default');
				this.overFeature = overFeature;
			}
		} else if (type == 'pointerdown' && feature != null) {
			// notify erase started
			this.dispatchEvent(new EraseFeatureEvent('erasestart', feature));
		} else if (type == 'pointerup' && feature != null) {
			// delete feature geometry
			feature.setGeometry(null);
			// notify erase finished
			this.dispatchEvent(new EraseFeatureEvent('eraseend', feature));
		}

		return false;
	};

	/*
	 * Erase.prototype.handleDragEvent = function(evt) { var deltaX =
	 * evt.coordinate[0] - this.coordinate_[0]; var deltaY = evt.coordinate[1] -
	 * this.coordinate_[1];
	 * 
	 * var geometry = this.feature_.getGeometry(); geometry.translate(deltaX,
	 * deltaY);
	 * 
	 * this.coordinate_[0] = evt.coordinate[0]; this.coordinate_[1] =
	 * evt.coordinate[1]; };
	 * 
	 * Erase.prototype.handleMoveEvent = function(evt) { if (this.cursor_) { var
	 * map = evt.map; var feature = map.forEachFeatureAtPixel(evt.pixel,
	 * function(feature) { return feature; }); var element =
	 * evt.map.getTargetElement(); if (feature) { if (element.style.cursor !=
	 * this.cursor_) { this.previousCursor_ = element.style.cursor;
	 * element.style.cursor = this.cursor_; } } else if (this.previousCursor_
	 * !== undefined) { element.style.cursor = this.previousCursor_;
	 * this.previousCursor_ = undefined; } } };
	 * 
	 * Erase.prototype.handleUpEvent = function() { this.coordinate_ = null;
	 * this.feature_ = null; return false; };
	 */
}).call();