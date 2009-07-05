var BANNER_ID = "scb_banner";

function setCookieScarableftNav(name, value) {
  var Mycookie = name + "=" + escape(value) + "; path=" + "/";
  document.cookie = Mycookie;
}
function getCookieScarableftNav() {
  var cookie = document.cookie;
  tag = cookie.indexOf("ScarableftNav=");
  return cookie.substring(tag + 14, tag + 15)
}

function setLeftcol() {
    if(getCookieScarableftNav()==0) 
    {
        hideScarableftNav()
    }
    else
    {
        showScarableftNav()
    }
}

function setCookieBanner(name, value) {
  var Mycookie = name + "=" + escape(value) + "; path=" + "/";
  document.cookie = Mycookie;
}
function getCookieBanner() {
  var cookie = document.cookie;
  tag = cookie.indexOf("Banner=");
  return cookie.substring(tag + 7, tag + 8)
}
function hideBanner() {
    document.getElementById(BANNER_ID).style.display="none"
    setCookieBanner("Banner","0")
}
function showBanner() {
    document.getElementById(BANNER_ID).style.display=""
    setCookieBanner("Banner","1")
}

function setBanner() {
    if(getCookieBanner()==0) 
    {
        hideBanner()
    }
    else
    {
        showBanner()
    }
}

function setLeftcolAndBanner() {
    setLeftcol()
    setBanner()
}


function onTextArea(area) {
    setRealNumberOfRows(area,"0")
}

//We needed a function that could be called using object name
//to be called in the setTimeout function 
function setRealNumberOfRowsByAreaName(areaname, ini) {
	area=document.getElementsByName(areaname)[0]; 
	setRealNumberOfRows(area,ini) 
}

function outTextArea(area) {
    //introduced a little delay to avoid SCB1461
    setTimeout('setRealNumberOfRowsByAreaName("'+area.name+'","1")',200);
}

function numberOfRows(area) {
  lines=area.value.split('\n');
  //The code above does not work. A solution to find a better approximation 
  //of the number of lines should be found.
  //  realNumberOfLines=0;
  //  for(i=0; 1<lines.length; i++) {
  //    realNumberOfLines = realNumberOfLines + lines[i].length/area.cols;
  //  }
  //  return realNumberOfLines;
  return lines.length;
}

function setRealNumberOfRows(area,ini) {
  realRows=numberOfRows(area);
  if(ini=="1")
    {
      if(realRows>10) 
	{
	  area.rows="10";
	} 
      else
	{
	  area.rows=realRows;
	}
    }
  else
    {
      if(realRows<10) 
	{
	  area.rows="10";
	} 
      else
	{
	  area.rows=realRows;
	}
    }
}

function initialRows(area) {
    allTextAreas=document.getElementsByTagName("textarea");
    for(i=0;i<allTextAreas.length;i++)
    {
        setRealNumberOfRows(allTextAreas[i],"1")
    }
}

function initializeTreeview() {
  collapseAll(["ol"]);
  //openBookMark();
  buttonEndings();
}


  // Used for autoscrolling when an issue topic is expanded
  // See toggleVisibility()
  function getElementOffset(obj)
  {
    var offset = 0;
    if (obj.offsetParent)
    {
      while (obj.offsetParent)
      {
        offset += obj.offsetTop
        obj = obj.offsetParent;
      }
    }
    else if (obj.y)
    {
      offset += obj.y;
    }
    return offset;
  }

  function stickyNavigationBar()
  {

    // Get the top scroll position :
    if (window.innerHeight)
	{
        pos = window.pageYOffset
	}
	else if (document.documentElement && document.documentElement.scrollTop)
	{
		pos = document.documentElement.scrollTop
	}
	else if (document.body)
	{
	    pos = document.body.scrollTop
	}   
	
	if (currentTop == null)
	{
	    currentTop = pos;
	}
	else
	{
	    if(currentTop != pos)
	    {
	        alert("Change scroll position from ["+currentTop + "] to [" + pos + "]");
	        currentTop = pos;
	    }
	}
   
    var obj = document.getElementById("navcolumn");
    elementOffset = getElementOffset(obj);
    
    if(currentTop > elementOffset)
    {
      alert("Move object from ["+elementOffset+"] to [" + (elementOffset + currentTop) +"]");
      elementOffset += currentTop;
      obj.style.top = elementOffset;
    }
    
    setTimeout("stickyNavigationBar()",500);
  }

