/* Copyright 2011 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

T5.define("dom", function() {

	/**
	 * Tree-walks the children of the element; for each dhild, ensure that all
	 * event handlers, listeners and PubSub publishers for the child are
	 * removed.
	 */
	function purgeChildren(element) {
		var children = element.childNodes;

		if (children) {
			var l = children.length, i, child;

			for (i = 0; i < l; i++) {
				var child = children[i];

				/* Just purge element nodes, not text, etc. */
				if (child.nodeType == 1)
					purge(children[i]);
			}
		}
	}

	// Adapted from http://javascript.crockford.com/memory/leak.html
	function purge(element) {
		var attrs = element.attributes;
		if (attrs) {
			var i, name;
			for (i = attrs.length - 1; i >= 0; i--) {
				if (attrs[i]) {
					name = attrs[i].name;
					/* Looking for onclick, etc. */
					if (typeof element[name] == 'function') {
						element[name] = null;
					}
				}
			}
		}

		// Get rid of any Prototype event handlers as well.
		Event.stopObserving(element);

		purgeChildren(element);

		if (element.t5pubsub) {
			// TODO: Execute this deferred?
			T5.pubsub.cleanupRemovedElement(element);
		}
	}

	/**
	 * Removes an element and all of its direct and indirect children. The
	 * element is first purged, to ensure that Internet Explorer doesn't leak
	 * memory if event handlers associated with the element (or its children)
	 * have references back to the element. This also removes all Prototype
	 * event handlers, and uses T5.pubsub.cleanupRemovedElement() to delete and
	 * publishers or subscribers for any removed elements.
	 * 
	 */
	function remove(element) {
		purge(element);

		// Remove the element, and all children, in one go.
		Element.remove(element);
	}

	return {
		remove : remove,
		purgeChildren : purgeChildren
	};
});