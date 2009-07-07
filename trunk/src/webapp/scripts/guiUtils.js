// =============================
// functions for dynamic buttons
// =============================

  function addLoadEvent(func) {
  	var oldonload = window.onload;
  	if (typeof window.onload != 'function') {
  		//alert(func);
  		window.onload = func;
  	}
  	else {
  		//alert("b");
  		window.onload = function() {
  			if (oldonload) {
  			    oldonload();
  			}
  			func();
  		}
  	}
  }

  function getElementsByClass(searchClass,node,tag) {
  	var classElements = new Array();
  	if ( node == null )
  		node = document;
  	if ( tag == null )
  		tag = '*';
  	var els = node.getElementsByTagName(tag);
  	var elsLen = els.length;
  	var pattern = new RegExp("(^|\\s)"+searchClass+"(\\s|$)");
  	for (i = 0, j = 0; i < elsLen; i++) {
  		if ( pattern.test(els[i].className) ) {
  			classElements[j] = els[i];
  			j++;
  		}
  	}
  	return classElements;
  }

  function insertAfter(newElement, targetElement) {
  	var parent = targetElement.parentNode;
  	if (parent.lastChild == targetElement) {
  		parent.appendChild(newElement);
  	}
  	else {
  		parent.insertBefore(newElement, targetElement.nextSibling);
  	}
  }

  function buttonEndings(clazz) {
	//alert("lookup buttons");
  	if (!document.getElementsByTagName) {
  		return false
  	}
  	var buttons = getElementsByClass(clazz);
  	/* loop through all buttons and attach a child div */
  	for (i=0; i < buttons.length; i++) {
  		var div = document.createElement("div");
  		div.className = clazz+"Ending";
  		insertAfter(div, buttons[i]);
  	}
  }
 