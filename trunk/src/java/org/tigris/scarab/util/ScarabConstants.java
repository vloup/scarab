package org.tigris.scarab.util;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 

import java.util.Locale;
import org.apache.fulcrum.localization.Localization;
import org.apache.turbine.Turbine;

/**
 * A place to put public final static strings and other constants.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id$
 */
public interface ScarabConstants
{
    /** 
     * the registration code uses this in order to store an object
     * into the data.getUser().getTemp() hashtable. this is the key 
     * value and is used across several classes.
     */
    final String SESSION_REGISTER = "scarab.newUser";

    /**
     * This is the key value that stores the name of the template to 
     * execute next.
     */
    final String NEXT_TEMPLATE = "nextTemplate";

    /**
     * This is the key value that stores the name of the template to 
     * cancel to.
     */
    final String CANCEL_TEMPLATE = "cancelTemplate";

    /**
     * This is the key value that stores the name of the template to 
     * go back to to (used in wizards)
     */
    final String BACK_TEMPLATE = "backTemplate";

    /**
     * This is the key value that stores the name of the template to 
     * go back to to (used in wizards)
     */
    final String LAST_TEMPLATE = "lastTemplate";

    /**
     * This is the key value that stores the name of the template
     * that is currently being executed.
     */
    final String TEMPLATE = "template";

    /**
     * This is the key value that stores the name of the action
     * that is currently being executed.
     */
    final String ACTION = "action";

    /**
     * This is the key value that stores the name of the template
     * other than the next, or cancel, where a user can go
     * depending on an action.
     */
    final String OTHER_TEMPLATE = "otherTemplate";
    
    /**
     * This is the key value that stores the issue
     * id.
     */
    final String ID = "id";
    
    /**
     * Primary System Objects
     */
    final String SCARAB_REQUEST_TOOL = "scarabR";
    final String SCARAB_USER_TOOL = "scarabU";
    
    /**
     * Name of the scarab Link Tool
     */
    final String SCARAB_LINK_TOOL = "link";    

    /**
     * Collection of useful methods
     */
    final String SCARAB_GLOBAL_TOOL = "scarabG";

    /**
     * The name used for the Intake tool
     */
    final String INTAKE_TOOL = "intake";

    /**
     * The name used for the Security tool
     */
    final String SECURITY_TOOL = "security";

    /**
     * The name used for the Security Admin tool
     */
    final String SECURITY_ADMIN_TOOL = "securityAdmin";

    /**
     * The name used for the Localization tool
     */
    final String LOCALIZATION_TOOL = "l10n";

    /**
     * Key passed around in the query string which tracks the
     * current module.
     */
    final String DEBUG = "debug";
    final String CURRENT_MODULE = "curmodule";
    final String CURRENT_ISSUE_TYPE = "curit";
    final String CURRENT_ADMIN_MENU = "curadminmenu";
    final String REPORTING_ISSUE = "rissue";
    final String CURRENT_REPORT = "curreport";
    final String REMOVE_CURRENT_REPORT = "remcurreport";
    final String HISTORY_SCREEN = "oldscreen";
    final String NEW_MODULE = "newmodule";
    final String NEW_ISSUE_TYPE = "newissuetype";
    final String CURRENT_QUERY = "queryString";
    final String CURRENT_MITLIST_ID = "curmitlistid";
    final String CURRENT_MITLISTITEM = "curmitlistitem";
    final String USER_SELECTED_MODULE = "scarab.user.selected.module";
    /** @deprecated No longer used */
    final String PROJECT_CHANGE_BOX = "project_change_box";

    final String THREAD_QUERY_KEY = "tqk";
    final String REMOVE_CURRENT_MITLIST_QKEY = "remcurmitl";

    /**
     * This name will be used to distinguish specific scarab application
     * from other instances that it may interact with (in the future).
     * It is the prefix to all issue id's created in response to an issue
     * entered against a module in this instance's database.
     */
    final String INSTANCE_ID = "scarab.instance.id";

    /**
     *  This is maximum rating for a word.
     *
     */
    final int MAX_WORD_RATING = 100000;

    /**
     *  The list of issue id's resulting from a search.
     *
     */
    final String ISSUE_ID_LIST = "scarab.issueIdList";

    /**
     *  The message the user sees if they try to perform an action
     *  For which they have no permissions.
     *
     */
    final String NO_PERMISSION_MESSAGE = "YouDoNotHavePermissionToAction";

    final String ATTACHMENTS_REPO_KEY = "scarab.attachments.repository";

    final String ARCHIVE_EMAIL_ADDRESS = "scarab.email.archive.toAddress";
    
    final String IMPORT_ADD_USERS = "scarab.import.addNewUsers";

    /**
     * An attribute type
     */
    final String DROPDOWN_LIST = "Dropdown list";
    final String DROPDOWN_TREE = "Dropdown tree";

    /**
     * Scarab.properties key for roles to be automatically approved.
     */
    final String AUTO_APPROVED_ROLES = "scarab.automatic.role.approval";

    /** 
     * Scarab.properties key for restricting viewIssue to long form (single screen).
     */
    final String SINGLE_SCREEN_ONLY = "scarab.viewIssue.singleScreenOnly";
    
    /**
     * key used to store session preference for long issue view vs. tabs
     * used in get/setTemp within ScarabUser.
     */
    final String TAB_KEY = "scarab.view.issue.details";

    /**
     * Value of the session parameter to view the issue in long form.
     */
    final String ISSUE_VIEW_ALL = "all";

    /**
     * format for displaying dates
     */
    final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z";
    
    /**
     * The ISO date format that we accept when users enter dates.
     */
    final String ISO_DATE_PATTERN = "yyyy-MM-dd";
    
    /**
     * The ISO date/time format that we accept when users enter
     * dates and times.
     */
    final String ISO_DATETIME_PATTERN = "yyyy-MM-dd HH:mm";

    /**
     * The default base for resolving ResourceBundles.
     */
    final String DEFAULT_BUNDLE_NAME = "ScarabBundle";

    /**
     * Default locale, taken from configuration files.
     */
    Locale DEFAULT_LOCALE =  new Locale(Localization.getDefaultLanguage(), 
                                        Localization.getDefaultCountry());

    /**
     * Scarab.properties key for email encoding property.
     */
    final String DEFAULT_EMAIL_ENCODING_KEY = "scarab.email.encoding";   

    /**
     * Scarab.properties key to enable sending all email with an alternate
     * HTML section (if a Velocity template exists).
     */
    final String EMAIL_IN_HTML_KEY = "system.email.html";

    /**
     * Scarab.properties key to enable debug output from JavaMail when doing
     * SMTP send.
     */
    final String JAVAMAIL_DEBUG_KEY = "system.mail.smtp.debug";
        
    /**
     * If this property is set, the notification URI will be derived from this template.
     * This is used when the online URL differs from the url to be used in the EMail notification.
     * One particular use case is when Scarab runs behind a Secure Access system (e.g. Juniper SA)
     * In that special case, the email_notification_urio would be set to:
     * 
     * email.notification.uri=https://${securehost}/${scarab.context}/issues/id/${issueId},DanaInfo=${scarab.http.domain},Port=${scarab.http.port}
     * 
     * where ${securehost} is the secure access host
     */
    final String EMAIL_NOTIFICATION_URI = 
        Turbine.getConfiguration().getString("email.notification.uri", "");

    final Integer INTEGER_0 = new Integer(0);

    /**
     * The maximum number of issues for batch view of detail.
     */
    final int ISSUE_MAX_VIEW =
        Turbine.getConfiguration().getInt("scarab.issue.max.view", 25);

    /**
     * The maximum number of issues for batch assign.
     */
    final int ISSUE_MAX_ASSIGN =
        Turbine.getConfiguration().getInt("scarab.issue.max.assign", 25);

    /**
     * The maximum number of issues for batch copy.
     */
    final int ISSUE_MAX_COPY =
        Turbine.getConfiguration().getInt("scarab.issue.max.copy", 250);

    /**
     * The maximum number of issues for batch move.
     */
    final int ISSUE_MAX_MOVE =
        Turbine.getConfiguration().getInt("scarab.issue.max.move", 250);

    /**
     * The maximumn number of report headings
     */
    final int REPORT_MAX_CRITERIA = 5;

    /**
     * The default comment rendering engine
     */
    final String COMMENT_RENDER_ENGINE = 
        Turbine.getConfiguration().getString("scarab.issue.comment.renderer", "plaintext");

    // Http parameters
    public static final String HTTP_DOMAIN      = "scarab.http.domain";
    public static final String HTTP_SCHEME      = "scarab.http.scheme";
    public static final String HTTP_SCRIPT_NAME = "scarab.http.scriptname";
    public static final String HTTP_PORT        = "scarab.http.port";
    
    // Condition editor constants
    public static int TRANSITION_OBJECT                = 0;
    public static int GLOBAL_ATTRIBUTE_OBJECT          = 1;
    public static int MODULE_ATTRIBUTE_OBJECT          = 2;
    public static int BLOCKED_MODULE_ISSUE_TYPE_OBJECT = 3;
    public static int NOTIFICATION_ATTRIBUTE_OBJECT    = 4;
    
    public static String IS_BLOCKED = "IsBlocked";
    public static String BLOCKS     = "Blocks";
}    
