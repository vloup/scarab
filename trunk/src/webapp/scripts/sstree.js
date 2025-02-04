function toggle(elm, imgpath) 
{
 var closedIconPath = "url(" + imgpath + "/folder-closed.gif" +")";
 var openIconPath   = "url(" + imgpath + "/folder-open.gif" +")";
 var newDisplay = "none";

 elm.style.backgroundImage = closedIconPath;
 var e = elm.nextSibling; 
 while (e != null) {
  if (e.tagName == "OL" || e.tagName == "ol") 
  {
   if (e.style.display == "none") 
   {
    newDisplay = "block";
    elm.style.backgroundImage = openIconPath;
   }
   break;
  }
  e = e.nextSibling;
 }
 while (e != null) {
  if (e.tagName == "OL" || e.tagName == "ol") 
  {
   e.style.display = newDisplay;
   break;
  }
  e = e.nextSibling;
 }
}

function collapseAll(tags) 
{
 var i=0;
 for (i = 0; i < tags.length; i++) {
  var lists = getElementsByClassName(document, tags[i], "treeview");
  for (var j = 0; j < lists.length; j++)
  {
   if (lists[j].className.indexOf('root') === -1)
   {
	   lists[j].style.display = "none";
   }
  }
 }
 var e = document.getElementById("root");
 if(e != null)
 {
  e.style.display = "block";
 }
}

function openBookMark(imgpath) {
 var h = location.hash;
 if (h == "") h = "default";
 if (h == "#") h = "default";
 var ids = h.split(/[#.]/);
 for (i = 0; i < ids.length; i++) {
  if (ids[i] != "") toggle(document.getElementById(ids[i]), imgpath);
 }
}

function getElementsByClassName(oElm, strTagName, strClassName){
	var arrElements = (strTagName == "*" && oElm.all)? oElm.all : oElm.getElementsByTagName(strTagName);
	var arrReturnElements = new Array();
	strClassName = strClassName.replace(/\-/g, "\\-");
	var oRegExp = new RegExp("(^|\\s)" + strClassName + "(\\s|$)");
	var oElement;
	var i=0;
	for(var i=0; i<arrElements.length; i++){
		oElement = arrElements[i];
		if(oRegExp.test(oElement.className)){
			arrReturnElements.push(oElement);
		}
	}
	return (arrReturnElements)
}


function renderJSONTreePopup(key, value, root, imgpath) {
	document.writeln('<div id="' + key + ':Popup" class="tree_popup"><ol class="treeview root">');		
	for (var i = 0; i < root._children.length; i++) {			
		renderJSONTree(root.name, key, value, root._children[i], imgpath);
	}
	document.writeln('</ol></div>');
}

var treeviewInit = [];

function renderJSONTree(attributeId, key, value, root, imgpath) 
{
	if (root.optionId == value) 
	{
		document.getElementById(key + ':Display').value = root.displayValue;
		var data = function(){clickTree(attributeId,key,value,root.displayValue)};
		treeviewInit.push(data);
	}
	
	if (root._children.length > 0) {
		document.writeln('<li>');
		document.writeln('<a class="folder" onclick="toggle(this,\''+imgpath+'\');"></a><a href="javascript:clickTree(\'' + attributeId + '\', \'' + key + '\', \'' + root.optionId + '\', \'' + root.displayValue + '\');">' + root.displayValue + '</a>');
		document.writeln('<ol class="treeview">');			
		for (var i = 0; i < root._children.length; i++) {			
			renderJSONTree(attributeId, key, value, root._children[i],imgpath);						
		}
		document.writeln('</ol>');	
	}
	else {
		document.writeln('<li class="leaf">');
		document.writeln('<a href="javascript:clickTree(\'' + attributeId + '\', \'' + key + '\', \'' + root.optionId + '\', \'' + root.displayValue + '\');">' + root.displayValue + '</a>');
	}
	
	document.writeln('</li>');
}


function toggleTreePopup(key) {
	if (document.getElementById(key + ':Popup').style.display !== 'inline-block') {
		var elem = document.getElementById(key + ':Popup');
		
		var pos = getAnchorPosition(key + ':Anchor');

		elem.style.display = 'inline-block';
		elem.style.left = pos.x + 'px';
		elem.style.top  = pos.y + 'px';
		elem.oldMouseup = document.onmouseup;
		
		document.onmouseup = function(event) {
			if (!event) event = window.event;
			 
			var pos = getAnchorPosition(key + ':Popup');
			var x1 = pos.x;
			var x2 = pos.x + elem.offsetWidth;

			var y1 = pos.y;
			var y2 = pos.y + elem.offsetHeight;
			
			if (!((event.clientX >= x1) && (event.clientX <= x2) && (event.clientY >= y1) && (event.clientY <= y2))) {
				elem.style.display = 'none';				
				document.onmouseup = elem.oldMouseup;
			}
		};

	}
	else {
		document.getElementById(key + ':Popup').style.display = 'none';
	}
}

//============================================================================================

Array.prototype.forEach = function(fn, thisObj) {
    var scope = thisObj || window;
    for ( var i=0, j=this.length; i < j; ++i ) {
        fn.call(scope, this[i], i, this);
    }
};

Array.prototype.filter = function(fn, thisObj) {
    var scope = thisObj || window;
    var a = [];
    for ( var i=0, j=this.length; i < j; ++i ) {
        if ( !fn.call(scope, this[i], i, this) ) {
            continue;
        }
        a.push(this[i]);
    }
    return a;
};

function Observer() {
    this.fns = [];
}
Observer.prototype = {
    subscribe : function(fn) {
        this.fns.push(fn);
    },
    unsubscribe : function(fn) {
        this.fns = this.fns.filter(
            function(el) {
                if ( el !== fn ) {
                    return el;
                }
            }
        );
    },
    fire : function(o, thisObj) {
        var scope = thisObj || window;
        this.fns.forEach(
            function(el) {
                el.call(scope, o);
            }
        );
    }
};  

var observer = new Observer;

function clickTree(attributeId, key, value, display) {
	document.getElementById(key).value = value;
	document.getElementById(key + ':Display').value = display;
	var elem = document.getElementById(key + ':Popup');
	elem.style.display = 'none';				
	if(elem.oldMouseup != undefined)
	{
      document.onmouseup = elem.oldMouseup;
	}
	var scope = ["treeview", attributeId, key, value, display];
	observer.fire(scope);
}

