package org.tigris.scarab.workflow.validations;
/**
 * @author akuklewicz
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ValidationResult
{

    private String result = "";

    public ValidationResult(String aResult)
    {
        setResult(aResult);
    }

    /**
     * Constructor ValidationResult.
     */
    public ValidationResult()
    {
    }

    
    public String getResult()
    {
        return result;
    }
    
    public void setResult(String aResult)
    {
        result = aResult;
    }

    public String toString()
    {
        return getResult();
    }        

}
