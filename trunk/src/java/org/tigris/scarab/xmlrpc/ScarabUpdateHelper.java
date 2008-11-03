package org.tigris.scarab.xmlrpc;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionManager;
import org.tigris.scarab.om.AttributeOptionPeer;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypePeer;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ParentChildAttributeOption;
import org.tigris.scarab.om.RIssueTypeAttributePeer;
import org.tigris.scarab.om.RIssueTypeOption;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.om.RModuleOption;

public class ScarabUpdateHelper {
    /**
     * Internal and external state data
     * 
     * @author pti
     * 
     */
    private class SyncState {
        /**
         * Logger for this class
         */
        private final Logger log = Logger.getLogger(SyncState.class);

        /**
         * Logger for this class
         */
        private boolean external = false;

        private boolean internal = false;

        private SyncState() {

        }

        /**
         * @return Returns the external.
         */
        public boolean isExternal() {
            return external;
        }

        /**
         * @return Returns the internal.
         */
        public boolean isInternal() {
            return internal;
        }

        /**
         * @param external
         *            The external to set.
         */
        public void setExternal(boolean external) {
            this.external = external;
        }

        /**
         * @param internal
         *            The internal to set.
         */
        public void setInternal(boolean internal) {
            this.internal = internal;
        }
    }

    /**
     * The SyncStateMap keeps a map which reflects the difference between the
     * internal and external lists.
     * 
     * @author pti
     * 
     */
    private class SyncStateMap {
        /**
         * Logger for this class
         */
        private final Logger log = Logger.getLogger(SyncStateMap.class);

        private Map hash = new HashMap();

        /**
         * Logger for this class
         */
        public SyncState getSyncState(String s) {
            SyncState rslt = (SyncState) hash.get(s);
            if (rslt == null) {
                rslt = new SyncState();
                hash.put(s, rslt);
            }
            return rslt;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#keySet()
         */
        public Set keySet() {
            return hash.keySet();
        }

    }

    /**
     * Logger for this class
     */
    private static final Logger log = Logger
            .getLogger(ScarabUpdateHelper.class);

    /**
     * Add the option with the given name to the global attribute with the given name.
     * 
     * @param attribute
     *            a String with the global attribute name
     * @param optionName
     *            a String with the name of the option
     * @throws Exception
     */
    public void addAttributeOption(String attribute, String optionName)
            throws Exception {
        // get the relevant entities
        AttributeOption option = getAttributeOption(attribute, optionName);
        Attribute attr = getOptionAttribute(attribute);
        setAttributeOptionDeletedFlag(attr, option, false);
    }

    /**
     * Remove option from global attribute
     * 
     * @param attribute
     * @param optionName
     * @throws Exception
     */
    public void removeAttributeOption(String attribute, String optionName)
            throws Exception {
        
        // get the relevant entities
        AttributeOption option = getAttributeOption(attribute, optionName);
        Attribute attr = getOptionAttribute(attribute);

        setAttributeOptionDeletedFlag(attr, option, true);
        option.deleteIssueTypeMappings();
        option.deleteModuleMappings();
       
    }


    /**
     * Finds and returns the Attribute object having the name given as
     * parameter.
     * 
     * @param attribute
     *            a String with the name of the Global Attribute
     * @return
     * @throws ScarabUpdateException
     * @throws TorqueException
     */
    private Attribute getAttribute(String attribute)
            throws ScarabUpdateException, TorqueException {
        // find the corresponding attribute
        Attribute attr;
        Criteria crit = new Criteria();
        crit.add(AttributePeer.ATTRIBUTE_NAME, (Object) attribute,
                Criteria.EQUAL);
        List attrList = AttributePeer.doSelect(crit);
        if (attrList.size() == 1) {
            attr = (Attribute) attrList.get(0);
        } else {
            throw new ScarabUpdateException(
                    "Found " + attrList.size() + " attributes for " + attribute + ".");
        }
        return attr;
    }

    /**
     * Return the Atribute and tests if it is an option attribute. Throws an
     * exception if an attribute was found, but it is not of an option type.
     * 
     * @param attribute
     * @return
     * @throws ScarabUpdateException
     * @throws TorqueException
     */
    private Attribute getOptionAttribute(String attribute)
            throws ScarabUpdateException, TorqueException {
        Attribute attr = getAttribute(attribute);
        if (attr.isOptionAttribute()) {
            // do nothing
        } else {
            throw new ScarabUpdateException(
                    "Found non-optiontype attribute when an option type attribte was expected.");
        }
        return attr;
    }

    /**
     * Return the AttributeOption object with the combination of attrobute and
     * option given as their user visible names.
     * 
     * @param attribute
     *            The name of the global attribute
     * @param option
     *            The name of the option in the global attribute
     * @return the AttributeOption with the above combination
     * @throws TorqueException
     * @throws ScarabUpdateException
     */
    private AttributeOption getAttributeOption(String attribute, String option)
            throws Exception {
        // get all options related to this attribute
        Attribute attr = getOptionAttribute(attribute);        
        return AttributeOptionManager.getInstance(attr, option);
    }

    /**
     * Find the list of possible attributes for an attribute specified by its
     * global name
     * 
     * @param attribute
     * @return A list of strings containing the options available for the
     *         specified attribute
     * @throws TorqueException
     * @throws ScarabUpdateException
     */
    public List getAttributeOptions(String attribute) throws TorqueException,
            ScarabUpdateException {
        Criteria crit = new Criteria();

        // get all non-deleted options for this attribute
        crit.add(AttributePeer.ATTRIBUTE_NAME, (Object) attribute,
                Criteria.EQUAL);
        crit.add(AttributeOptionPeer.DELETED, false);
        crit.addJoin(AttributePeer.ATTRIBUTE_ID,
                AttributeOptionPeer.ATTRIBUTE_ID);
        return AttributeOptionPeer.doSelect(crit);
    }

    /**
     * Find the list of Module/Issuetype combinations using the given attribute
     * 
     * @param attribute
     * @return A list of RModuleAttributes related to this attribute.
     * @throws TorqueException
     * @throws ScarabUpdateException
     */
    private List getModuleAttributes(String attribute) throws TorqueException,
            ScarabUpdateException {
        Criteria crit = new Criteria();

        // get all non-deleted options for this attribute
        crit.add(AttributePeer.ATTRIBUTE_NAME, (Object) attribute,
                Criteria.EQUAL);
        crit.addJoin(AttributePeer.ATTRIBUTE_ID,
                RModuleAttributePeer.ATTRIBUTE_ID);
        List matOptions = RModuleAttributePeer.doSelect(crit);

        return matOptions;
    }

    /**
     * Find the list of Module/Issuetype combinations using the given attribute
     * 
     * @param attribute
     * @return A list of RModuleAttributes related to this attribute.
     * @throws TorqueException
     * @throws ScarabUpdateException
     */
    private List getIssueTypes(String attribute) throws TorqueException,
            ScarabUpdateException {
        Criteria crit = new Criteria();

        // get all non-deleted options for this attribute
        crit.add(AttributePeer.ATTRIBUTE_NAME, (Object) attribute,
                Criteria.EQUAL);
        crit.addJoin(AttributePeer.ATTRIBUTE_ID,
                RIssueTypeAttributePeer.ATTRIBUTE_ID);
        crit.addJoin(IssueTypePeer.ISSUE_TYPE_ID,
                RIssueTypeAttributePeer.ISSUE_TYPE_ID);
        List itOptions = IssueTypePeer.doSelect(crit);

        return itOptions;
    }

    /**
     * Map an option to all Module/Issuetypes which have this attribute
     * configured. In case the option is deleted, all mappings are removed.
     * 
     * @throws Exception
     * 
     */
    public void mapAttributeOptionToAllModuleIssueTypes(String attribute,
            String option) throws Exception {
        Iterator iter;
        AttributeOption ao = getAttributeOption(attribute, option);
        if (ao.getDeleted()) {
            ao.deleteIssueTypeMappings();
            ao.deleteModuleMappings();
        } else {
            // add module mappings
            iter = getModuleAttributes(attribute).iterator();
            while (iter.hasNext()) {
                RModuleAttribute rat = (RModuleAttribute) iter.next();
                IssueType it = rat.getIssueType();
                Module mod = rat.getModule();

                // skip template IssueTypes
                if (!isIssueTypeTemplate(it) &&
                   (mod.getRModuleOption(ao,it) == null)) {
                    if (log.isDebugEnabled()) {
                        log.debug("mapAttributeOptionToAllModuleIssueTypes(String, String) - Adding attribute to module/issuetype : ao=" + ao + ", rat=" + rat + ", it=" + it + ", mod=" + mod); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    }
                    mod.addAttributeOption(it,ao);
                }                    
            }
            
            // add issuetype mappings
            iter = getIssueTypes(attribute).iterator();
            while (iter.hasNext()) {
                IssueType it = (IssueType) iter.next();
                try { 
                    it.addRIssueTypeOption(ao);
                } catch (TorqueException e) {
                    if (log.isEnabledFor(org.apache.log4j.Priority.WARN)) {
                        log
                                .warn(
                                        "mapAttributeOptionToAllModuleIssueTypes(String, String) - IssueTypeMApping alreadyt exists for this combination : attribute=" + attribute + ", option=" + option + ", it=" + it, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
            }

        }

    }

    /**
     * Sort options in all Module/Issuetypes which have this attribute
     * configured. In case the option is deleted, all mappings are removed.
     * 
     * @throws Exception
     * 
     */
    public void sortAttributeOptions(String attribute) throws Exception {
        Iterator iter;
        
        /**
         * Compare the order of  2 RModuleOptions based on their display value
         * 
         * @author pti
         *
         */
        final class RMOComparator implements Comparator {
            /**
             * Logger for this class
             */
            private final Logger log = Logger.getLogger(RMOComparator.class);

            public int compare(Object arg0, Object arg1) {
                // TODO Auto-generated method stub
                String s1 = ((RModuleOption)arg0).getDisplayValue();
                String s2 = ((RModuleOption)arg1).getDisplayValue();
                
                return s1.compareTo(s2);
            }
            
        };
        Comparator rmocomp = new RMOComparator();
        
        
        /**
         * Compare the order of  2 RIssueTypeOptions based on their display value
         * 
         * @author pti
         *
         */
        final class RIOComparator implements Comparator {
            /**
             * Logger for this class
             */
            private final Logger log = Logger.getLogger(RIOComparator.class);

            public int compare(Object arg0, Object arg1) {
                Integer oid1 = ((RIssueTypeOption)arg0).getOptionId();
                Integer oid2 = ((RIssueTypeOption)arg1).getOptionId();
                AttributeOption ao1=null;
                AttributeOption ao2=null;
                try {
                    ao1 = AttributeOptionPeer.retrieveByPK(oid1);
                    ao2 = AttributeOptionPeer.retrieveByPK(oid2);
                } catch (NoRowsException e) {
                    log.error("No Rows exception when sorting IssueTypeOptions.",e);
                } catch (TooManyRowsException e) {
                    log.error("Too many rows when sorting IssueTypeOptions.",e);
                } catch (TorqueException e) {
                    log.error("Torque exception when sorting IssueTypeOptions.",e);
                }
                
                return ao1.getName().compareTo(ao2.getName());
            }
            
        };
        Comparator riocomp = new RIOComparator();
        
        /**
         * Compare the order of  2 ParentChildAttributeOptions based on their display value
         * 
         * @author pti
         *
         */
        final class PCAOComparator implements Comparator {
            /**
             * Logger for this class
             */
            private final Logger log = Logger.getLogger(PCAOComparator.class);

            public int compare(Object arg0, Object arg1) {
                // TODO Auto-generated method stub
                String s1 = ((ParentChildAttributeOption)arg0).getName();
                String s2 = ((ParentChildAttributeOption)arg1).getName();
                
                return s1.compareTo(s2);
            }
            
        };
        Comparator pcaocomp = new PCAOComparator();
        
        
        Attribute attr = getAttribute(attribute);
        
        // sort module mappings
        iter = getModuleAttributes(attribute).iterator();
        while (iter.hasNext()) {
            RModuleAttribute rat = (RModuleAttribute) iter.next();
            IssueType it = rat.getIssueType();
            Module mod = rat.getModule();

            // skip template IssueTypes
            if (!isIssueTypeTemplate(it)) {
                List options = mod.getRModuleOptions(attr,it);
                Collections.sort(options, rmocomp);
                for (int i = 0; i < options.size(); i++) {
                    RModuleOption opt = (RModuleOption)options.get(i);
                    opt.setOrder(i + 1);
                    opt.save();
                }
                    
            }                    
        }
        
        // add issuetype mappings
        iter = getIssueTypes(attribute).iterator();
        while (iter.hasNext()) {
            IssueType it = (IssueType) iter.next();
            List options = it.getRIssueTypeOptions(attr);
            Collections.sort(options, riocomp);
            for (int i = 0; i < options.size(); i++) {
                RIssueTypeOption opt = (RIssueTypeOption)options.get(i);
                opt.setOrder(i + 1);
                opt.save();
            }
            
        }
        
        // sort pcao mappings
        List pcaos = attr.getParentChildAttributeOptions();
        Collections.sort(pcaos, pcaocomp);
        for (int i = 0; i < pcaos.size(); i++) {
            ParentChildAttributeOption opt = (ParentChildAttributeOption)pcaos.get(i);
            opt.setPreferredOrder(i + 1);
            opt.save();
        }

    }

    private boolean isIssueTypeTemplate(IssueType it) {
        return it.getParentId().intValue() != 0;
    }

    /**
     * Handle the the setting, clearing of the deleted flag. If the record does
     * not exist, it is created.
     * 
     * @param attribute
     * @param optionName
     * @param deleted
     * @throws Exception
     */
    private void setAttributeOptionDeletedFlag(Attribute attr,
            AttributeOption option, boolean deleted) throws Exception {
        ParentChildAttributeOption pcao = ParentChildAttributeOption
                .getInstance();

        // claculate some sensible defaults
        int order = attr.getAttributeOptions().size() + 1;
        int weight = order;

        // populate the object and save it to db
        pcao.setAttributeId(attr.getAttributeId());
        pcao.setOptionId(option != null ? option.getOptionId() : null);
        pcao.setParentId(new Integer(0));
        pcao.setName(option.getName());
        pcao.setPreferredOrder(order);
        pcao.setWeight(weight);
        pcao.setDeleted(deleted);
        pcao.save();

    }

    /**
     * Set the option list for the specified global attribute to correspond to
     * the list of strings given. In order to preserve setting changes throught
     * the user interface, only minimal changes are done.
     * 
     * @param attribute
     *            The name of the global attribute
     * @param options
     *            The list of strings of the new attributes.
     * @throws Exception
     */
    public void updateAttributeOptions(String attribute, List options)
            throws Exception {
        SyncStateMap ssm = new SyncStateMap();
        Iterator iter = null;

        // Retrieve attribute to be updated
        iter = getAttributeOptions(attribute).iterator();
        while (iter.hasNext()) {
            
            ssm.getSyncState(((AttributeOption)iter.next()).getName()).setInternal(true);
        }

        // create SyncState info based on given external list.
        iter = options.iterator();
        while (iter.hasNext()) {
            String option = (String) iter.next();
            ssm.getSyncState(option).setExternal(true);
        }

        // iterate over the syncstates
        iter = options.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            SyncState ss = ssm.getSyncState(key);
            if (ss.isExternal() && !ss.isInternal()) {
                // new in external data : add to attribute options
                addAttributeOption(attribute, key);
                mapAttributeOptionToAllModuleIssueTypes(attribute, key);
            } else if (!ss.isExternal() && ss.isInternal()) {
                // no longer in external data : remove from option list
                removeAttributeOption(attribute, key);
                mapAttributeOptionToAllModuleIssueTypes(attribute, key);
            }
        }
    }

}
