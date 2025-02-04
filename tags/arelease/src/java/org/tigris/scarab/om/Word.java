package org.tigris.scarab.om;

// JDK classes
import java.util.*;

// Turbine classes
import org.apache.turbine.om.*;
import org.apache.turbine.om.peer.BasePeer;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.util.StringUtils;
import org.apache.turbine.util.ParameterParser;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.db.pool.DBConnection;

//Village classes
import com.workingdogs.village.*;

// Scarab classes
import org.tigris.scarab.util.ScarabConstants;

/** 
  * The skeleton for this class was autogenerated by Torque on:
  *
  * [Mon Apr 09 14:48:06 PDT 2001]
  *
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.

  */
public class Word 
    extends org.tigris.scarab.om.BaseWord
    implements Persistent
{
    private static String query =
        "SELECT sum(" + RIssueWordPeer.OCCURENCES + ") as OCCUR"+
        " FROM " + RIssueWordPeer.TABLE_NAME +
        " WHERE " + RIssueWordPeer.WORD_ID + " = ";
    /**
     *  recalcualtes and saves word's rating
     *
     */
    public void updateRating() throws Exception
    {
        Vector v = BasePeer.executeQuery(query + getWordId());
        Record rec = (Record)v.get(0);
        setRating(
            ScarabConstants.MAX_WORD_RATING /
            rec.getValue("OCCUR").asInt());
        save();
    }
}



