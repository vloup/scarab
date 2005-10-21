var preserve = true;

function toggle(elm) {
  if (!elm) return;
  var row = getParent(elm, 'TR');
  if (!row.a) {
   a = getChild(row, 'DIV');
   row.a = a;
  } else {
    a = row.a;
  }
  if (a.className != 'folder') return;  //you can only toggle folders
  var rows = row.parentNode.childNodes;
  if (!rows) return;
  var newDisplay = "none";
  var newImage = baseUrl + "/images/folder-closed.gif";
  var thisId = row.id + "-";
  var a;



  if (row.status == "closed") {
    if (document.all) newDisplay = "block"; //IE4+ specific code
    else newDisplay = "table-row"; //Netscape and Mozilla
    a.style.backgroundImage = "url(" + baseUrl + "/images/folder-open.gif)";
    newImage = baseUrl + "/images/folder-open.gif";
    row.status = "open";
  } else {
    row.status = "closed";
    a.style.backgroundImage = "url(" + baseUrl + "/images/folder-closed.gif)";
    newImage = baseUrl + "/images/folder-closed.gif";
  }

  //find elm in collection
  var i=0;
  var last;
  var s;
  while(rows[i] != row) {
    i++;
  }
  var thisIdLength = thisId.length;
  for (i=i;i<rows.length;i++) {
    s = rows[i];
    if (s.tagName != 'TR') continue;  //could be a white space
    if (s.id == row.id) continue;
    if (!s.a) {
      a = getChild(s, 'DIV');
      s.a = a;
    } else {
      a = s.a;
    }
    if (!preserve&&(s.id.indexOf(thisId) >= 0)) { //dont preserve, set all children
      s.style.display = newDisplay;
      if (a.className != "doc") a.style.backgroundImage = "url("+newImage+")";
      s.status = row.status;
    } else if (s.id.indexOf(thisId) >= 0) {
      if ((a.className == 'folder')&&(((row.status == 'open')&&(s.status == 'closed')))) {
        s.style.display = newDisplay;
        //this is needed so we skip all sub modules for the closed folder
        last = s.id.lastIndexOf('-');
        for (var j=i+1;j<rows.length;j++) {
          t = rows[j];
          if (t && t.id && (t.id.lastIndexOf('-') <= last)) break;
        }
        if (j >= rows.length) break;
        i = j;
        s = t;
        a = getChild(s, 'DIV');
        if (a && a.className == 'folder') {
          i--;
        } else if (a) {
          s.style.display = newDisplay;
        }
      } else {
        s.style.display = newDisplay;
      }

    } else {
      break;
    }
  }
}

function getParent(elm, tagName) {
  if (elm.tagName == tagName) {
    return elm;
  } else if (elm.parentNode) {
      elm = getParent(elm.parentNode, tagName);
  } else {
      return null;
  }
  return elm;
}

function collapseAllRows(tree) {
  var tree = tree || document.getElementById("navbar");
  var row = getChild(tree, 'TR');
  if (!row) return;
  var rows = row.parentNode.childNodes;

  var a;
  for (var j = 0; j < rows.length; j++) {
    var r = rows[j];
    if (r.tagName != 'TR') continue;
    r.status = "closed";
    a = getChild(r, 'DIV');
    if (!a) continue;
    if (a.className == "folder") a.style.backgroundImage = "url(" + baseUrl + "/images/folder-closed.gif)";
    // because we dont display root so 0-x are top level
    if (r.id.indexOf("-") >= 0) r.style.display = "none";
  }
}

function getChild(elm, tagName) {
  var o=null;
  for (var i=0;i<elm.childNodes.length;i++) {
    if (elm.childNodes[i].tagName == tagName) {
      o = elm.childNodes[i];
      break;
    }
    if (elm.childNodes[i].childNodes) {
      o = getChild(elm.childNodes[i], tagName);
      if (o && (o.tagName == tagName)) break;
    }
  }
  return o;
}

function expandTo(elm) {
  if (!elm) return;
  var ids = elm.id.split(/-/);
  var id = "";
  if (ids.length) {
    for (i = 0; i < ids.length; i++) {
      id +=  ids[i];
      if (ids[i] != "") toggle(document.getElementById(id));
      id += "-";
    }
  } else {
    toggle(elm);
  }
}