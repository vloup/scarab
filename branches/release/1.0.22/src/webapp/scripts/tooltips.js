// =================================
//   Tooltip support  
// =================================

  currentTooltip=null;
  currentTimer = null;
  //document.onmousemove = updateTooltipPosition;

  // =============================================================
  // a tooltip is defined within a hidden element and
  // placed on the screen relative to a specific visible element.
  // If the cursor moves within the visible element, the tooltip
  // moves along with the cursor. This movement is controlled in
  // this function.
  // =============================================================
  function updateTooltipPosition(e) 
  {
    if (currentTooltip != null) 
    {
        x = (document.all) ? window.event.x + document.body.scrollLeft : e.pageX;
        y = (document.all) ? window.event.y + document.body.scrollTop  : e.pageY;

        // Check that tooltip is fully visible
        var tooltipWidth  = currentTooltip.offsetWidth;
        var documentWidth = document.body.offsetWidth;
        var xOverflow = x + 40 + tooltipWidth - documentWidth;
        if(xOverflow > 0) x = x - xOverflow;

        currentTooltip.style.left = (x + 20) + "px";
        currentTooltip.style.top  = (y + 20) + "px";
    }
  }

  // ==========================================================
  // Use the element with id as content for tooltip.
  // Note:the element should be associated to the
  // css-class "scarab-tooltip" i.e. the element should
  // be initially hidden. The tooltip is shown on mouse over,
  // but with a delay of delayTime, see below.
  // ==========================================================
  function showDelayedTooltip(id, delayTime) 
  {
	currentTooltip = document.getElementById(id);
    currentTimer = setTimeout("displayTooltip()", delayTime);
  }
  
 function showTooltip(id) 
  {
	showDelayedTooltip(id, 1000);
  }

  // ==========================================================
  // display the current Tooltip. Note: Only one tooltip can
  // be displayed at any given time. Tooltips are directly 
  // linked to the mouse cursor. Since there
  // is only one mouse cursor, it is not a problem.
  // ==========================================================
  function displayTooltip() 
  {
	currentTooltip.style.display = "block";
    // Check that tooltip is fully visible
    var tooltipWidth  = currentTooltip.offsetWidth;
    var documentWidth = document.body.offsetWidth;
    var x = currentTooltip.offsetLeft;
    var xOverflow = x + 40 + tooltipWidth - documentWidth;
    if(xOverflow > 0) 
    {
        x = x - xOverflow;
        currentTooltip.style.left = x + "px";
    }
  }

  // ============================================================
  // remove the current visible tooltip from the browser canvas,
  // i.e.make it invisible.
  // Typically used with the onmouseout event.
  // ============================================================
  function hideTooltip() 
  {
    if(currentTimer != null)
    {
      clearTimeout(currentTimer);
      currentTimer = null;
    }
	currentTooltip.style.display = "none";
  }

