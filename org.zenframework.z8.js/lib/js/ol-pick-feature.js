(function() {

	var PickFeatureEvent = class {
		constructor (type, coordinate, feature) {
			this.propagationStopped_ = false;
			this.type_ = type;
			this.coordinate_ = coordinate;
			this.feature_ = feature;
		}

		get propagationStopped () { return this.propagationStopped_ }
		get type () { return this.type_ }
		get coordintates () { return this.coordintates_ }
		get feature() { return this.feature_ }

		preventDefault () { this.propagationStopped_ = true }

		stopPropagation () { this.propagationStopped_ = true }
	};

	var Pick = ol.interaction.Pick = function(config) {
		ol.interaction.Pointer.call(this, {
			handleEvent : Pick.prototype.handleEvent,
		});

		this.source = config.source;
		this.picker = config.picker;
		this.scope = config.scope;
	};
	ol.inherits(Pick, ol.interaction.Interaction);
	
	Pick.prototype.defaultSetActive = Pick.prototype.setActive;
	Pick.prototype.setActive = function(active) {
		Pick.prototype.defaultSetActive.call(this, active);
		this.getMap().getTargetElement().style.cursor = active ? this.pickerUrl + ', auto' : 'default';
	};
	
	// load fa-hand-lizard-o as image
	setTimeout(function() {
		var canvas = document.createElement('canvas');
		canvas.width = 24;
		canvas.height = 24;
		var ctx = canvas.getContext('2d');
		ctx.fillStyle = '#000000';
		ctx.font = '20px FontAwesome';
		ctx.textAlign = 'center';
		ctx.textBaseline = 'middle';
		ctx.fillText('\uf258', 12, 12);
		Pick.prototype.pickerUrl = 'url(' + canvas.toDataURL('image/png') + ')';
	}, 2000);

	Pick.prototype.handleEvent = function(evt) {
		var type = evt.type;
		var map = evt.map;
		var coordinate = evt.coordinate;

		if (type == 'pointerdown') {
			var feature = this.feature = new ol.Feature();
			this.dispatchEvent(new PickFeatureEvent('pickstart', coordinate, feature));
			return false;
		} else if (type == 'pointerup') {
			var me = this;
			var callback = function(geometry) {
				me.feature.setGeometry(geometry);
				me.dispatchEvent(new PickFeatureEvent('pickend', coordinate, feature));
			};
			this.picker.call(this.scope, coordinate, callback);
			return false;
		}

		return true;
	};
}).call();