/*
 * ACCEPT_LANGUAGE was not an acceptable name so we are
 * changing it to just LANGUAGE to be more generic.
 * 
 * Created: Sean Jackson <sean@pnc.com.au>
 * $Id$
 */

ALTER TABLE SCARAB_USER_PREFERENCE RENAME ACCEPT_LANGUAGE TO LANGUAGE;
