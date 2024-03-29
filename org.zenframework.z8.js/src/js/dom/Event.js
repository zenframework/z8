Z8.apply(Event.prototype, {
	stopEvent: function() {
		this.stopPropagation();
		this.preventDefault();
		this.cancelled = true;
	},

	getCharCode: function() {
		return this.charCode || this.keyCode;
	},

	getKey: function() {
		return this.keyCode || this.charCode;
	}
});

Z8.apply(Event, {
	BACKSPACE: 8, TAB: 9, NUM_CENTER: 12, ENTER: 13, RETURN: 13, SHIFT: 16, CTRL: 17, ALT: 18, PAUSE: 19, CAPS_LOCK: 20, ESC: 27, SPACE: 32,
	PAGE_UP: 33, PAGE_DOWN: 34, END: 35, HOME: 36, LEFT: 37, UP: 38, RIGHT: 39, DOWN: 40,
	PRINT_SCREEN: 44, INSERT: 45, DELETE: 46, 
	ZERO: 48, ONE: 49, TWO: 50, THREE: 51, FOUR: 52, FIVE: 53, SIX: 54, SEVEN: 55, EIGHT: 56, NINE: 57,
	A: 65, B: 66, C: 67, D: 68, E: 69, F: 70, G: 71, H: 72, I: 73, J: 74, K: 75, L: 76, M: 77, N: 78, O: 79, P: 80, Q: 81, R: 82, S: 83, T: 84, U: 85, V: 86, W: 87, X: 88, Y: 89, Z: 90,
	CONTEXT_MENU: 93,
	NUM_ZERO: 96, NUM_ONE: 97, NUM_TWO: 98, NUM_THREE: 99, NUM_FOUR: 100, NUM_FIVE: 101, NUM_SIX: 102, NUM_SEVEN: 103, NUM_EIGHT: 104, NUM_NINE: 105, NUM_MULTIPLY: 106, NUM_PLUS: 107, NUM_MINUS: 109, NUM_PERIOD: 110, NUM_DIVISION: 111,
	F1: 112, F2: 113, F3: 114, F4: 115, F5: 116, F6: 117, F7: 118, F8: 119, F9: 120, F10: 121, F11: 122, F12: 123,
	PLUS: 187, EQUALS: 187, MINUS: 189, UNDERSCORE: 189
});