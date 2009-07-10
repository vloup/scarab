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

function collapseAll(tags) {
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
	for(var i=0; i<arrElements.length; i++){
		oElement = arrElements[i];
		if(oRegExp.test(oElement.className)){
			arrReturnElements.push(oElement);
		}
	}
	return (arrReturnElements)
}


function renderJSONTreePopup(key, value, root) {
	document.writeln('<div id="' + key + ':Popup" class="tree_popup"><ol class="treeview root">');		
	for (var i = 0; i < root._children.length; i++) {			
		renderJSONTree(key, value, root._children[i]);						
	}
	document.writeln('</ol></div>');	
}

function renderJSONTree(key, value, root) {
	if (root.optionId == value) {
		document.getElementById(key + ':Display').value = root.displayValue;	
	}	
	
	document.writeln('<li>');

	if (root._children.length > 0) {
		document.writeln('<a class="folder" onclick="toggle(this,\'/scarab/images\');"></a><a href="javascript:clickTree(\'' + key + '\', \'' + root.optionId + '\', \'' + root.displayValue + '\');">' + root.displayValue + '</a>');
		document.writeln('<ol class="treeview">');			
		for (var i = 0; i < root._children.length; i++) {			
			renderJSONTree(key, value, root._children[i]);						
		}
		document.writeln('</ol>');	
	}
	else {
		document.writeln('<a href="javascript:clickTree(\'' + key + '\', \'' + root.optionId + '\', \'' + root.displayValue + '\');">' + root.displayValue + '</a>');
	}
	
	document.writeln('</li>');
}

function toggleTreePopup(key, caller) {
	if (document.getElementById(key + ':Popup').style.display !== 'inline-block') {
		var pos = getAnchorPosition(key + ':Anchor');

		document.getElementById(key + ':Popup').style.display = 'inline-block';
		document.getElementById(key + ':Popup').style.left = pos.x + 'px';
		document.getElementById(key + ':Popup').style.top = pos.y + 'px';
		
		if (document.layers) {
			document.captureEvents(Event.MOUSEUP);
		}
		document.onmouseup = function() { document.getElementById(key + ':Popup').style.display = 'none' };
	}
	else {
		document.getElementById(key + ':Popup').style.display = 'none';
	}
}

function clickTree(key, value, display) {
	document.getElementById(key).value = value;
	document.getElementById(key + ':Display').value = display;
}