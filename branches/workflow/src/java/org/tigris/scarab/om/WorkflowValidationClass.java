
package org.tigris.scarab.om;


import org.apache.torque.om.Persistent;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.workflow.validations.WorkflowValidation;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class WorkflowValidationClass 
    extends org.tigris.scarab.om.BaseWorkflowValidationClass
    implements Persistent
{

    public WorkflowValidation getWorkflowValidation()
        throws ScarabException
    {
        String className = this.getJavaClassName();
        Class wvClass = null;
        WorkflowValidation aValidation = null;

        try
        {
            wvClass = Class.forName(className);
            aValidation = (WorkflowValidation) wvClass.newInstance();
        }
        catch (Exception e)
        {
            throw new ScarabException("WorkflowValidationClass.getWorkflowValidation could not instantiate validation class: " + className, e);
        }
        
        return aValidation;
    }
}
