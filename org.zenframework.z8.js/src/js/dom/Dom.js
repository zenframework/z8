Z8.define('Z8.dom.Dom', {
	shortClassName: 'DOM',

	statics: {
		Element: 1,
		Text: 3,

		emptyTags: {
			br: true,
			frame: true,
			hr: true,
			img: true,
			input: true,
			link: true,
			meta: true,
			col: true
		},

		readyListeners: [],

		isDom: function(dom) {
			return dom != null && dom.addEventListener != null;
		},

		get: function(dom) {
			if(dom == null)
				return null;

			if(dom.isComponent || dom.dom != null)
				return dom.dom;

			if(dom.getDom != null)
				return dom.getDom();

			return DOM.isDom(dom) ? dom : null;
		},

		isParentOf: function(parents, child) {
			if(parents == null || child == null)
				return false;

			parents = Array.isArray(parents) ? parents.slice(0) : [parents];

			for(var i = 0, count = 0, length = parents.length; i < length; i++) {
				var dom = parents[i] = DOM.get(parents[i]);
				if(dom != null)
					count++;
			}

			if(count == 0)
				return false;

			var topmost = document.body;

			while(child != null && child !== topmost) {
				for(var i = 0, length = parents.length; i < length; i++) {
					if(child == parents[i])
						return true;
				}

				child = child.parentNode;
			}

			return false;
		},

		getCookie: function(name) {
			var matches = document.cookie.match(name + '=([^;|*]*);*');
			return matches != null ? decodeURIComponent(matches[1] || '') : '';
		},

		setCookie: function(name, value, days) {
			document.cookie = name + '=' + encodeURIComponent(value) + ';expires=' + new Date().addDay(days || 2).toUTCString() + ';path=/';
		},

		removeCookie: function(name) {
			document.cookie = name + '=;Path=/;';
		},

		selectNode: function(dom, selector) {
			if(arguments.length == 1) {
				selector = dom;
				dom = document.body;
			}
			if((dom = DOM.get(dom)) == null)
				return null;
			return dom.querySelector(selector);
		},

		query: function(dom, selector) {
			if(arguments.length == 1) {
				selector = dom;
				dom = document.body;
			}
			if((dom = DOM.get(dom)) == null)
				return null;
			return dom.querySelectorAll(selector);
		},

		markup: function(markup) {
			var buffer = '';

			if(String.isString(markup) || Number.isNumber(markup)) {
				buffer += markup;
				return buffer;
			}

			if(Array.isArray(markup)) {
				for(var i = 0, length = markup.length; i < length; i++) {
					var item = markup[i];
					if(item != null)
						buffer += DOM.markup(item);
				}
				return buffer;
			}

			var tag = markup.tag || 'div';
			buffer += '<' + tag;

			var value;

			for(var attribute in markup) {
				value = markup[attribute];
				if(attribute != 'tag' && attribute != 'children' && attribute != 'cn' && attribute != 'html' && attribute != 'container')
					buffer += ' ' + (attribute == 'cls' ? 'class' : attribute) + (value != null ? '="' + value + '"' :'');
			}

			if(DOM.emptyTags[tag] == null) {
				buffer += '>';

				if((value = markup.html) != null)
					buffer += value;

				if((value = markup.cn || markup.children) != null)
					buffer += DOM.markup(value);

				buffer += '</' + tag + '>';
			} else
				buffer += '/>';

			return buffer;
		},

		create: function(tag, cls, style, content) {
			var dom = document.createElement(tag);
			if(cls)
				dom.setAttribute('class', cls);
			if(style)
				dom.setAttribute('style', style);
			if(content)
				dom.textContent = content;
			return dom;
		},

		append: function(container, child) {
			if((container = DOM.get(container || document.body)) == null || child == null)
				return null;

			var childDom = DOM.get(child);

			if(childDom != null)
				return container.appendChild(childDom);

			container.insertAdjacentHTML('beforeend', DOM.markup(child));
			return container.lastChild;
		},

		prepend: function(container, child) {
			if((container = DOM.get(container || document.body)) == null || child == null)
				return null;

			var childDom = DOM.get(child);

			if(childDom != null)
				return container.insertAdjacentElement('afterbegin', childDom);

			container.insertAdjacentHTML('afterbegin', DOM.markup(child));
			return container.lastChild;
		},

		insertBefore: function(before, element) {
			if((before = DOM.get(before)) == null || element == null)
				return null;

			var elementDom = DOM.get(element);

			if(elementDom != null)
				return before.insertAdjacentElement('beforebegin', elementDom);

			before.insertAdjacentHTML('beforebegin', DOM.markup(element));
			return before.previousSibling;
		},

		insertAfter: function(after, element) {
			if((after = DOM.get(after)) == null || element == null)
				return null;

			var elementDom = DOM.get(element);

			if(elementDom != null)
				return after.insertAdjacentElement('afterend', elementDom);

			after.insertAdjacentHTML('afterend', DOM.markup(element));
			return after.nextSibling;
		},

		remove: function(dom, delay) {
			if((dom = DOM.get(dom)) == null)
				return;

			if(dom.parentElement == null)
				return;

			if(delay != null) {
				var remove = function(dom) {
					dom.parentElement.removeChild(dom);
				};
				new Z8.util.DelayedTask().delay(delay, remove, this, dom);
			} else
				dom.parentElement.removeChild(dom);
		},

		removeChildren: function(dom, delay) {
			if((dom = DOM.get(dom)) == null)
				return;

			var childNodes = dom.childNodes;
			for(var i = childNodes.length - 1;  i >= 0; i--)
				dom.removeChild(childNodes[i]);
		},

		getProperty: function(dom, property) {
			dom = DOM.get(dom);
			return dom != null ? dom[property] : null;
		},

		setProperty: function(dom, property, value, delay) {
			if((dom = DOM.get(dom)) == null)
				return;

			if(delay != null) {
				var setValue = function(dom, property, value) {
					dom[property] = value;
				};
				new Z8.util.DelayedTask().delay(delay, setValue, this, dom, property, value);
			} else
				dom[property] = value;
		},

		getStyle: function(dom, property) {
			dom = DOM.get(dom);
			return dom != null ? dom.style[property] : null;
		},

		setStyle: function(dom, property, value, delay) {
			if(value == null || (dom = DOM.get(dom)) == null)
				return;

			if(delay != null) {
				var setValue = function(dom, property, value) {
					dom.style[property] = value;
				};
				new Z8.util.DelayedTask().delay(delay, setValue, this, dom, property, value);
			} else
				dom.style[property] = value;
		},

		getParent: function(dom) {
			return DOM.getProperty(dom, 'parentNode');
		},

		getChildren: function(dom) {
			return DOM.getProperty(dom, 'childNodes') || [];
		},

		getChildAt: function(dom, index) {
			return DOM.getChildren(dom)[index];
		},

		getComputedStyle: function(dom) {
			dom = DOM.get(dom);
			return dom != null ? window.getComputedStyle(dom) : null;
		},

		screenToClientX: function(dom, screenX, screen) {
			dom = DOM.get(dom);
			screen = DOM.get(screen);

			while(dom != null && dom != screen) {
				screenX -= (dom.offsetLeft + dom.clientLeft - dom.scrollLeft);
				dom = dom.offsetParent;
			}
			return screenX;
		},

		screenToClientY: function(dom, screenY, screen) {
			dom = DOM.get(dom);
			screen = DOM.get(screen);

			while(dom != null && dom != screen) {
				screenY -= (dom.offsetTop + dom.clientTop - dom.scrollTop);
				dom = dom.offsetParent;
			}
			return screenY;
		},

		clientToScreenX: function(dom, clientX, screen) {
			dom = DOM.get(dom);
			screen = DOM.get(screen);

			while(dom != null && dom != screen) {
				clientX += dom.offsetLeft + dom.clientLeft - dom.scrollLeft;
				dom = dom.offsetParent;
			}
			return clientX;
		},

		clientToScreenY: function(dom, clientY, screen) {
			dom = DOM.get(dom);
			screen = DOM.get(screen);

			while(dom != null && dom != screen) {
				clientY += dom.offsetTop + dom.clientTop - dom.scrollTop;
				dom = dom.offsetParent;
			}
			return clientY;
		},

		getClipping: function(dom) {
			if((dom = DOM.get(dom)) == null)
				return null;

			var topmost = document.body;
			dom = dom.parentNode;

			while(dom != null && dom.nodeType == 1) {
				if(dom == topmost)
					return topmost;

				var style = window.getComputedStyle(dom);
				var overflowY = style.overflowY;

				if(overflowY == 'hidden' || overflowY == 'auto')
					return dom;

				dom = dom.parentNode;
			}

			return null;
		},

		getAttribute: function(dom, attribute) {
			dom = DOM.get(dom);
			return dom != null ? dom.getAttribute(attribute) : null;
		},

		getIntAttribute: function(dom, attribute) {
			var value = DOM.getAttribute(dom, attribute);
			return value != null ? Parser.integer(value) : null;
		},

		setAttribute: function(dom, attribute, value) {
			if((dom = DOM.get(dom)) != null)
				dom.setAttribute(attribute, value);
		},

		removeAttribute: function(dom, attribute) {
			if((dom = DOM.get(dom)) != null)
				dom.removeAttribute(attribute);
		},

		focus: function(dom, select, preventScroll) {
			if((dom = DOM.get(dom)) == null || dom.focus == null)
				return false;

			dom.focus({ preventScroll: preventScroll });

			if(select && dom.select != null)
				dom.select();

			return true;
		},

		select: function(dom) {
			if((dom = DOM.get(dom)) != null && dom.select != null)
				dom.select();
		},

		deselect: function(dom) {
			if((dom = DOM.get(dom)) != null && dom.setSelectionRange != null)
				dom.setSelectionRange(1000, 1000);
		},

		scroll: function(dom, left, top) {
			if((dom = DOM.get(dom)) == null || dom.focus == null)
				return;

			dom.scrollLeft = left;
			dom.scrollTop = top;
		},

		scrollIntoView: function(dom, alignToTop) {
			if((dom = DOM.get(dom)) == null || dom.focus == null)
				return;
			dom.scrollIntoView(alignToTop);
		},

		parseCls: function(cls) {
			return String.isString(cls) ? cls.toLowerCase().split(' ') : (cls != null ? cls.slice(0) : []);
		},

		getCls: function(dom) {
			return DOM.getProperty(dom, 'className') || '';
		},

		setCls: function(dom, cls, delay) {
			DOM.setProperty(dom, 'className', Array.isArray(cls) ? cls.join(' ') : cls, delay);
		},

		hasCls: function(dom, cls) {
			if((dom = DOM.get(dom)) == null)
				return false;
			return DOM.parseCls(dom.className).indexOf(cls) != -1;
		},

		removeCls: function(dom, cls, delay) {
			if((dom = DOM.get(dom)) == null)
				return;

			var classes = DOM.parseCls(dom.className);
			var index = classes.indexOf(cls);
			if(index == -1)
				return;

			classes.splice(index, 1);
			DOM.setCls(dom, classes);
		},

		addCls: function(dom, cls, delay) {
			if((dom = DOM.get(dom)) == null)
				return;

			var classes = DOM.parseCls(dom.className);
			if(classes.indexOf(cls) != -1)
				return;

			classes.push(cls);
			DOM.setCls(dom, classes, delay);
		},

		swapCls: function(dom, condition, trueCls, falseCls, delay) {
			if((dom = DOM.get(dom)) == null)
				return;

			var index = -1;
			var classes = DOM.parseCls(dom.className);

			var clsToAdd = condition ? trueCls : falseCls;
			var clsToRemove = condition ? falseCls : trueCls;

			if(clsToAdd != null) {
				if(classes.indexOf(clsToAdd) == -1)
					classes.push(clsToAdd);
				else
					clsToAdd = null;
			}

			if(clsToRemove != null) {
				index = classes.indexOf(clsToRemove);
				if(index != -1 )
					classes.splice(index, 1);
				else
					clsToRemove = null;
			}

			if(clsToAdd != null || clsToRemove != null)
				DOM.setCls(dom, classes, delay);

			return classes;
		},

		isInput: function(dom) {
			if((dom = DOM.get(dom)) == null)
				return false;
			var tag = dom.tagName;
			return tag == 'INPUT' || tag == 'TEXTAREA';
		},

		isReadOnly: function(dom) {
			return DOM.getAttribute(dom, 'readonly') != null;
		},

		getValue: function(dom) {
			if((dom = DOM.get(dom)) == null)
				return null;
			var tag = dom.tagName;
			return DOM.isInput(dom) ? dom.value : dom.textContent;
		},

		setValue: function(dom, value, delay) {
			if((dom = DOM.get(dom)) != null) {
				var property = DOM.isInput(dom) ? 'value' : 'textContent';
				DOM.setProperty(dom, property, value || '', delay);
			}
		},

		setTitle: function(dom, title, delay) {
			DOM.setProperty(dom, 'title', title, delay);
		},

		leftTag: function(node) {
			var html = DOM.getOuterHtml(node);
			return html.substring(0, html.indexOf('>') + 1);
		},

		rightTag: function(node) {
			return '</' + node.localName || node.tagName + '>';
		},

		getOuterHtml: function(dom) {
			return DOM.getProperty(dom, 'outerHTML');
		},

		getInnerHtml: function(dom) {
			return DOM.getProperty(dom, 'innerHTML');
		},

		setInnerHtml: function(dom, innerHTML, delay) {
			DOM.setProperty(dom, 'innerHTML', innerHTML, delay);
		},

		setReadOnly: function(dom, readOnly, delay) {
			DOM.setProperty(dom, 'readOnly', readOnly, delay);
		},

		setDisabled: function(dom, disabled, delay) {
			DOM.setProperty(dom, 'disabled', disabled, delay);
		},

		setTabIndex: function(dom, tabIndex, delay) {
			DOM.setProperty(dom, 'tabIndex', tabIndex, delay);
		},

		setDisplay: function(dom, display, delay) {
			DOM.setStyle(dom, 'display', display, delay);
		},

		setHidden: function(dom, hidden, delay) {
			DOM.setStyle(dom, 'visibility', hidden ? 'hidden' : 'visible', delay);
		},

		getOffsetTop: function(dom) {
			return Ems.pixelsToEms(DOM.getProperty(dom, 'offsetTop'));
		},

		getOffsetLeft: function(dom) {
			return Ems.pixelsToEms(DOM.getProperty(dom, 'offsetLeft'));
		},

		getOffsetWidth: function(dom) {
			return Ems.pixelsToEms(DOM.getProperty(dom, 'offsetWidth'));
		},

		getOffsetHeight: function(dom) {
			return Ems.pixelsToEms(DOM.getProperty(dom, 'offsetHeight'));
		},

		getClientWidth: function(dom) {
			return Ems.pixelsToEms(DOM.getProperty(dom, 'clientWidth'));
		},

		getClientHeight: function(dom) {
			return Ems.pixelsToEms(DOM.getProperty(dom, 'clientHeight'));
		},

		getPoint: function(dom, point) {
			var point = DOM.getStyle(dom, point);
			return point != null ? parseFloat(point) : null;
		},

		setPoint: function(dom, point, value, delay) {
			if(value != null)
				DOM.setStyle(dom, point, String.isString(value) ? value : (value + 'em'), delay);
		},

		getLeft: function(dom) {
			return DOM.getPoint(dom, 'left');
		},

		setLeft: function(dom, left, delay) {
			DOM.setPoint(dom, 'left', left, delay);
		},

		getRight: function(dom) {
			return DOM.getPoint(dom, 'right');
		},

		setRight: function(dom, right, delay) {
			DOM.setPoint(dom, 'right', right, delay);
		},

		getTop: function(dom) {
			return DOM.getPoint(dom, 'top');
		},

		setTop: function(dom, top, delay) {
			DOM.setPoint(dom, 'top', top, delay);
		},

		getBottom: function(dom) {
			return DOM.getPoint(dom, 'bottom');
		},

		setBottom: function(dom, bottom, delay) {
			DOM.setPoint(dom, 'bottom', bottom, delay);
		},

		getWidth: function(dom) {
			return DOM.getPoint(dom, 'width');
		},

		setWidth: function(dom, width, delay) {
			DOM.setPoint(dom, 'width', width, delay);
		},

		getHeight: function(dom) {
			return DOM.getPoint(dom, 'height');
		},

		setHeight: function(dom, height, delay) {
			DOM.setPoint(dom, 'height', height, delay);
		},

		setCssText: function(dom, cssText, delay) {
			DOM.setStyle(dom, 'cssText', cssText, delay);
		},

		rotate: function(dom, degree) {
			if(dom == null)
				return;

			var cls = 'fa-rotate-' + degree;

			if(degree != 0) {
				DOM.addCls(dom, 'fa-transform-transition');
				DOM.addCls(dom, cls);
				dom.rotationCls = cls;
			} else {
				DOM.removeCls(dom, dom.rotationCls);
				delete dom.rotationCls;
			}
		},

		substitutes: {
			'click': 'touchend',
			'mousedown': 'touchstart'
		},

		on: function(dom, event, fn, scope, capture) {
			if((dom = DOM.get(dom)) == null)
				return;

			event = event.toLowerCase();

			var eventsData = dom.eventsData;

			if(eventsData == null)
				eventsData = dom.eventsData = eventsData = {};

			var listeners = eventsData[event] || [];

			var callback = function(event) {
				var target = event.currentTarget;
				var eventsData = target.eventsData;
				if(eventsData == null)
					return;
				var listeners = eventsData[event.type];
				if(listeners != null) {
					listeners = [].concat(listeners); // original listeners array can be modified inside the loop
					for(var i = 0, length = listeners.length; i < length; i++) {
						Z8.callback(listeners[i], event, event.target);
						if(event.cancelled)
							return;
					}
				}
			};

			if(fn == null)
				throw 'DOM.on: fn is null';

			var listener = { fn: fn, scope: scope, callback: callback, capture: capture };
			listeners.push(listener);
			eventsData[event] = listeners;

			if(listeners.length == 1)
				dom.addEventListener(event, callback, capture);
		},

		un: function(dom, event, fn, scope) {
			if((dom = DOM.get(dom)) == null)
				return;

			event = event.toLowerCase();

			var eventsData = dom.eventsData;
			if(eventsData == null)
				return;

			var listeners = eventsData[event];

			if(listeners == null)
				return;

			for(var i = 0, length = listeners.length; i < length; i++) {
				var listener = listeners[i];
				if(listener.fn == fn && listener.scope == scope) {
					listeners.removeAt(i);
					if(listeners.length == 0) {
						dom.removeEventListener(event, listener.callback, listener.capture);
						delete eventsData[event];
					}
					break;
				}
			}

			if(Object.keys(eventsData).length == 0)
				delete dom.eventsData;
		},

		goToUrl: function(url) {
			var anchor = DOM.append(document.body, { tag: 'a', cls: 'display-none', href: url, target: '_blank' });
			anchor.click();
			new Z8.util.DelayedTask().delay(5000, DOM.remove, DOM, anchor);
		},

		saveFile: function(file) {
			var url = URL.createObjectURL(file);
			var anchor = DOM.append(document.body, { tag: 'a', cls: 'display-none', href: url, download: Format.htmlEncode(file.name) });
			anchor.click();
			URL.revokeObjectURL(url);

			new Z8.util.DelayedTask().delay(5000, DOM.remove, DOM, anchor);
		},

		download: function(url, id, serverId, callback, noCache) {
			var config = { tag: 'iframe', html: '<head><meta http-equiv="Content-Type" content="text/html; charset=utf-8"></head>', src: '', hidden: true };
			var frame = DOM.append(document.body, config);

			frame.src = encodeURI((window._DEBUG_ ? '/' : '') + url.replace(/\\/g, '/')) + '?&session=' + Application.session +
				(id != null ? '&id=' + id : '') + (serverId != null ? '&serverId=' + serverId : '') + (noCache ? '&noCache' : '');

			new Z8.util.DelayedTask().delay(500, DOM.downloadCallback, this, url, id, serverId, frame, callback);
		},

		downloadCallback: function(url, id, serverId, frame, callback) {
			var document = frame.contentDocument;
			var readyState = document.readyState;

			if(readyState == 'complete') {
				var response = document.body.innerHTML;
				var success = Z8.isEmpty(response);
				if(!success) {
					response = response.charAt(0) == '{' ? JSON.decode(response) : { info: { messages: [{ text: '\'' + url + Z8.$('DOM.fileNotFound'), type: 'error' }] }};

					if(response.status == HttpRequest.status.AccessDenied) {
						var loginCallback = function() {
							DOM.download(url, id, serverId, callback);
						};

						Application.login({ fn: loginCallback, scope: this });
						DOM.remove(frame);
						return;
					}

					Application.message(response.info.messages);
				}
				Z8.callback(callback, success);
				DOM.remove(frame, 10000);
			} else
				new Z8.util.DelayedTask().delay(500, DOM.downloadCallback, this, url, frame, callback);
		},

		onReady: function(callback, scope) {
			DOM.readyListeners.push({ fn: callback, scope: scope });
		},

		onContextMenu: function(event, target) {
			if(target.tagName != 'INPUT' && target.tagName != 'PRE' && target.tagName != 'TEXTAREA')
				event.stopEvent();
		},

		onReadyEvent: function(callback, scope) {
			var listeners = DOM.readyListeners;
			for(var i = 0, length = listeners.length; i < length; i++)
				Z8.callback(listeners[i]);

			delete DOM.readyListeners;
			DOM.un(window, 'load', DOM.onReadyEvent);

			if(DOM.onContextMenu != null)
				DOM.on(window, 'contextmenu', DOM.onContextMenu);
		},

		callWindowResize: function() {
			window.dispatchEvent(new Event('resize'));
		},

		requestAnimationFrame: function(callback) {
			var callbackFn = function() {
				Z8.callback(callback);
			};
			return window.requestAnimationFrame(callbackFn);
		},

		cancelAnimationFrame: function(requestId) {
			window.cancelAnimationFrame(requestId);
		},

		getImageSizeFromBlob: function(blob, callback) {
			var image = DOM.create('img');

			var onImageLoad = function() {
				DOM.un(image, 'load', onImageLoad, this);
				DOM.un(image, 'error', onImageLoad, this);
				URL.revokeObjectURL(image.src);
				Z8.callback(callback, image.naturalWidth, image.naturalHeight);
			};

			DOM.on(image, 'load', onImageLoad, this);
			DOM.on(image, 'error', onImageLoad, this);

			image.src = URL.createObjectURL(blob);
		}
	}
});

DOM.on(window, 'load', DOM.onReadyEvent);
