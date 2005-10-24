function toggle(elm) {
 var newDisplay = "none";
 elm.style.backgroundImage = 'url(/scarab/images/folder-closed.gif)';
 var e = elm.nextSibling; 
 while (e != null) {
  if (e.tagName == "OL" || e.tagName == "ol") {
   if (e.style.display == "none") {
    newDisplay = "block";
    elm.style.backgroundImage = 'url(/scarab/images/folder-open.gif)';
   }
   break;
  }
  e = e.nextSibling;
 }
 while (e != null) {
  if (e.tagName == "OL" || e.tagName == "ol") e.style.display = newDisplay;
  e = e.nextSibling;
 }
}

function collapseAll(tags) {
 for (i = 0; i < tags.length; i++) {
  var lists = document.getElementsByTagName(tags[i]);
  for (var j = 0; j < lists.length; j++)
  {
   lists[j].style.display = "none";
  }
 }
 var e = document.getElementById("root");
 if(e != null)
 {
  e.style.display = "block";
 }
}

function openBookMark() {
 var h = location.hash;
 if (h == "") h = "default";
 if (h == "#") h = "default";
 var ids = h.split(/[#.]/);
 for (i = 0; i < ids.length; i++) {
  if (ids[i] != "") toggle(document.getElementById(ids[i]));
 }
}
