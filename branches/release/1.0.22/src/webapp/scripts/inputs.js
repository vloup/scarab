// Handling for automatic form submittal via a drop-down value change.
function submitFormTo(form, name) {
  var input = document.createElement('input');
  input.type = 'hidden';
  input.name = name;
  form.appendChild(input);
  form.submit();
}



// Variables for handling preferred order (po) lists
var poListNames = new Array();
var poListArrays = new Array();
var poListLastPosition = new Array();
var poListInitialisors = new Array()
var poUpdating = false;

// initialise the select box (opOption) for this preferredOrder list (poName)
// to its current value (position)
function initialisePreferredOrderList( poName, poOption, position){

    debug(poName+" , "+poOption+" , "+position);

    position = position -1;
    // this function's information
    var fnt = new Array();
    fnt[0] = "spare";
    fnt[1] = poName;
    fnt[2] = poOption;
    fnt[3] = position;
    // store it in the initialisors array
    next = poListInitialisors.length;
    poListInitialisors[next] = fnt;
    // update poListLastPosition
    for(i=0;i<poListNames.length;++i){
        if( poListNames[i] == poName ){
            debug("found poName in poListNames at " + i);
            lastPosition = poListLastPosition[i];
            if( position > lastPosition ){
                poListLastPosition[i] = position;
                debug("updated last position to " + position);
            }
            return;
        }
    }
    // this poName hasn't been stored yet in poListNames.
    debug("adding " + poName + " at index " + i);
    poListNames[i] = poName;
    poListLastPosition[i] = position;
    return;
}

// run the initialisors that were created by initialisePreferredOrderList(..)
function runPreferredOrderListInitialisors(){
    debug("running " + poListInitialisors.length + " initialisors");
    for (i=0; i<poListInitialisors.length; i++) {
        // extract function information
        fnt = poListInitialisors[i];
        spare = fnt[0];
        poName = fnt[1];
        poOption = fnt[2];
        position =fnt[3];
        // create poName's array
        for(j=0;j<poListNames.length;++j){
            if( poListNames[j] == poName ){
                var listArray = poListArrays[j];
                if( listArray == null ){
                    poListArrays[j] = new Array();
                    listArray = poListArrays[j];
                }
                lastPosition = poListLastPosition[j];
                debug("initialisor " + i + " creating listArray of size " + lastPosition);
                //for (k=0; k<= lastPosition; k++) {
                //    listArray[k] = 0;
                //}
                listArray[position] = poOption;
                break;
            }
        }
    }
    poListInitialisors = new Array();
}

// bump up or down the other preferredOrder select boxes to avoid duplicates in the order.
function updatePreferredOrderList(object, poName, poOption){
    if( !poUpdating ){
        poUpdating = true;
        debug("initialisor list size " + poListInitialisors.length);
        if( poListInitialisors.length > 0 ){
            runPreferredOrderListInitialisors();
        }
        position = object.value -1;
        for (i=0; i<poListNames.length; i++) {
            if( poName == poListNames[i] ){
                debug("poName at index " + i);
                var listArray = poListArrays[i];
                // find where poOption was
                var oldPosition = -1;
                debug("looking for " + poOption);
                for (j=0; j <= listArray.length; j++) {
                    var option = listArray[j];
                    debug("comparing to " + option);
                    if( option == poOption ){
                        oldPosition = j;
                        break;
                    }
                }
                debug("old position was at " + oldPosition);
                debug("new position is " + position);
                var bump = 0;
                var loLimit = 0;
                var hiLimit = 0;
                if( oldPosition > position ){
                    bump = 1;
                    loLimit = position;
                    hiLimit = oldPosition;
                    debug("bumping up");
                }else{
                    bump = -1;
                    loLimit = oldPosition;
                    hiLimit = position;
                    debug("bumping down");
                }

                // bumping down
                //j == position --> replaced
                //j < oldPosition --> stay same
                //j >= oldPosition && j < position --> bumping down, update select
                //j > position --> stays same

                // bumping up
                //j == position --> replaced
                //j < position --> stay same
                //j <= oldPosition  --> bumping down, update select
                //j > oldPosition --> stays same

                // bump everything after position up
                var newListArray = new Array();
                for (j=0; j <= listArray.length; j++) {
                    if( j == position ){
                        debug("element "+j+" is replaced");
                        newListArray[j] = listArray[oldPosition];
                    }else if( j < loLimit || j > hiLimit ){
                        debug("element "+j+" stays the same");
                        newListArray[j] = listArray[j];
                    }else{
                        debug("element "+j+" bumps");
                        newListArray[j] = listArray[j-bump];

                        // update select input
                        var v = document.getElementById(newListArray[j]).selectedIndex;
                        document.getElementById(newListArray[j]).selectedIndex = v + bump;

                    }
                }
                poListArrays[i] = newListArray;
                debug("new world order now");
                for (j=0; j <= newListArray.length; j++) {
                    debug(newListArray[j]);
                    
                }
                poUpdating = false;
                return;
            }
        }
    }
    poUpdating = false;
    return;
}
