Z8.define('Z8.util.Base64', {
	shortClassName: 'Base64',

	statics: {
		keyStr: 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=',

		encode: function(input) {
			var output = '';
			var keyStr = Base64.keyStr;

			input = Base64.encodeUTF8(input);

			var index = 0, length = input.length;

			while(index < length) {
				var chr1 = input.charCodeAt(index++);
				var chr2 = input.charCodeAt(index++);
				var chr3 = input.charCodeAt(index++);
				var enc1 = chr1 >> 2;
				var enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
				var enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
				var enc4 = chr3 & 63;

				if(isNaN(chr2))
					enc3 = enc4 = 64;
				else if(isNaN(chr3))
					enc4 = 64;

				output += keyStr.charAt(enc1) + keyStr.charAt(enc2) + keyStr.charAt(enc3) + keyStr.charAt(enc4);
			}

			return output;
		},

		decode: function(input) {
			var output = '';
			var keyStr = Base64.keyStr;

			input = input.replace(/[^A-Za-z0-9\+\/\=]/g, '');

			var index = 0, length = input.length;

			while(index < length) {
				var enc1 = keyStr.indexOf(input.charAt(index++));
				var enc2 = keyStr.indexOf(input.charAt(index++));
				var enc3 = keyStr.indexOf(input.charAt(index++));
				var enc4 = keyStr.indexOf(input.charAt(index++));
				var chr1 = (enc1 << 2) | (enc2 >> 4);
				var chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
				var chr3 = ((enc3 & 3) << 6) | enc4;

				output += String.fromCharCode(chr1);
				if(enc3 != 64)
					output += String.fromCharCode(chr2);
				if(enc4 != 64)
					output += String.fromCharCode(chr3);
			}

			return Base64.decodeUTF8(output);
		},

		encodeUTF8: function(string) {
			string = string.replace(/\r\n/g, '\n');

			var result = '';

			for(var index = 0, length = string.length; index < length; index++) {
				var c = string.charCodeAt(index);
				if (c < 128) {
					result += String.fromCharCode(c);
				} else if((c > 127) && (c < 2048)) {
					result += String.fromCharCode((c >> 6) | 192);
					result += String.fromCharCode((c & 63) | 128);
				} else {
					result += String.fromCharCode((c >> 12) | 224);
					result += String.fromCharCode(((c >> 6) & 63) | 128);
					result += String.fromCharCode((c & 63) | 128);
				}
			}

			return result;
		},

		decodeUTF8: function(utf) {
			var result = '';
			var index = 0, length = utf.length;
			var c = 0, c1 = 0, c2 = 0;

			while(index < length) {
				c = utf.charCodeAt(index);

				if(c < 128) {
					result += String.fromCharCode(c);
					index++;
				} else if((c > 191) && (c < 224)) {
					c2 = utf.charCodeAt(index + 1);
					result += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
					index += 2;
				} else {
					c2 = utf.charCodeAt(index + 1);
					c3 = utf.charCodeAt(index + 2);
					result += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
					index += 3;
				}
			}

			return result;
		}
	}
});
