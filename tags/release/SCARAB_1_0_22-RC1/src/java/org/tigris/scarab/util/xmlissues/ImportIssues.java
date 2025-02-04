package org.tigris.scarab.util.xmlissues;

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


import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.betwixt.strategy.HyphenatedNameMapper;
import org.apache.commons.betwixt.strategy.NameMapper;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.fileupload.FileItem;
import org.apache.fulcrum.localization.Localization;
import org.apache.log4j.Logger;
import org.apache.torque.TorqueException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.transform.JDOMSource;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.TurbineInitialization;
import org.tigris.scarab.workflow.WorkflowFactory;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;


/**
 * This is a bean'ish object which allows one to set values for importing 
 * issues, and then run the actual import. 
 *
 * Amenable to the ant task wrapper or you can pass an explicit file for 
 * explicit import if turbine is already up and running.
 * 
 * <p>The way the ant task wrapper works is simple: call all the appropriate
 * set methods to define the properties. Then you will need to call the init()
 * and execute methods to start running things. Note: If Turbine is already
 * initialized, there is no need to call the init() method.</p>
 *
 * <p>Instances of this class are not thread-safe.</p>
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @version $Id$
 * @since Scarab beta 14
 */
public class ImportIssues
    implements ErrorHandler
{
    private static final Logger LOG = Logger.getLogger(ImportIssues.class);
    private final transient TransformerFactory transformerFactory 
            = TransformerFactory.newInstance();
//    private final transient DocumentBuilderFactory documentBuilderFactory 
//            = DocumentBuilderFactory.newInstance();
    
    private static final String JIRA_XSL = "/org/tigris/scarab/util/xmlissues/xsls/jira.xsl";
    private static final String BUGZILLA_XSL = "/org/tigris/scarab/util/xmlissues/xsls/bugzilla.xsl";
        
    private static final ImportType SCARAB = ImportType.valueOf("scarab");
    private static final ImportType BUGZILLA = ImportType.valueOf("bugzilla");
    private static final ImportType JIRA = ImportType.valueOf("jira");    

    /*
     * The virtual URL to the document type definition (DTD) used with
     * this version of Scarab.  Though this file doesn't actually
     * exist, it's what can be used as a friendly way to refer to
     * Scarab's DTD in an XML file's <code>DOCTYPE</code> declaration.
     */
    public static final String SYSTEM_DTD_URI =
        "http://scarab.tigris.org/dtd/scarab-0.21.0.dtd";

    /**
     * The absolute URL to the document type definition (DTD) used
     * with this version of Scarab.
     */
    private static final String INTERNAL_DTD_URI =
        "http://scarab.tigris.org/source/browse/*checkout*/scarab/trunk/www/dtd/scarab-0.21.0.dtd?content-type=text%2Fxml";

    /**
     * The resource location of the DTD in the classpath.
     */
    private static final String DTD_RESOURCE = "/org/tigris/scarab/scarab.dtd";

    /** 
     * Name of the properties file.
     */
    private String trProps = "/WEB-INF/conf/TurbineResourcesTest.properties";

    /** 
     * Name of the xmlimport.properties file used for configuration of log4j.
     */
    private String configProps = "/WEB-INF/conf/xmlimport.properties";

    private File configDir = null;
    private boolean sendEmail = false;
    private File xmlFile = null;
    private boolean allowGlobalImports;
    private String resourceDirectory;

    /**
     * Current file attachment handling code contains a security hole
     * which can allow a user to see any file on the host that is
     * readable by Scarab.  It is not easy to exploit this hole (you
     * have to know about file paths on a host you likely don't have
     * access to), and there are cases where we want to use the
     * functionality and can be sure the hole is not being exploited.
     * So adding a flag to disallow file attachments when importing
     * through the UI.
     */    
    private boolean allowFileAttachments;

    /**
     * Whether we're in validation mode, or some phase after that.
     */
    private boolean validationMode;

    /**
     * A list of any errors encountered during the import, likely
     * added during the validation phase.
     */
    private ImportErrors importErrors;

    public ImportIssues()
    {
        this(true, false, null);
    }

    public ImportIssues(
            final boolean allowFileAttachments, 
            final boolean allowGlobalImports,
            final String resourceDirectory) 
    {
        this.allowFileAttachments = allowFileAttachments;
        this.allowGlobalImports = allowGlobalImports;
        this.resourceDirectory = resourceDirectory;
        this.importErrors = new ImportErrors();
    }

    /**
     * Instance of scarabissues we ran the actual insert with.
     *
     * Make it available post import so importer can get at info about
     * what has just been imported.
     */
    private ScarabIssues si = null;


    public boolean getSendEmail()
    {
        return this.sendEmail;
    }

    public void setSendEmail(final boolean state)
    {
        this.sendEmail = state;
    }

    public File getXmlFile()
    {
        return this.xmlFile;
    }

    public void setXmlFile(final File xmlFile)
    {
        this.xmlFile = xmlFile;
    }

    public File getConfigDir()
    {
        return this.configDir;
    }

    public void setConfigDir(final File configDir)
    {
        this.configDir = configDir;
    }

    public String getConfigFile()
    {
        return this.configProps;
    }

    public void setConfigFile(final String configProps)
    {
        this.configProps = configProps;
    }

    public String getTurbineResources()
    {
        return this.trProps;
    }

    public void setTurbineResources(final String trProps)
    {
        this.trProps = trProps;
    }

    public void init()
        throws ScarabException,MalformedURLException,IOException
    {
        TurbineInitialization.setTurbineResources(getTurbineResources());
        TurbineInitialization.setUp(getConfigDir().getAbsolutePath(), 
            getConfigFile());
    }

    /**
     * Hook method called by <a
     * href="http://ant.apache.org/">Ant's</a> Task wrapper.
     */
    public void execute() 
        throws ScarabException
    {
        runImport(getXmlFile());
    }

    /**
     * Run an import.
     *
     * Assumes we're up and running inside of turbine.
     *
     * @param importFile File to import.
     *
     * @return List of errors if any.
     *
     * @exception Exception
     */
    public Collection runImport(final File importFile)
        throws ScarabException
    {
        return runImport(importFile, (Module) null);
    }

    /**
     * Run an import.
     *
     * Assumes we're up and running inside of turbine.
     *
     * @param importFile File to import.
     * @param currentModule If non-null, run check that import is going 
     * against this module.
     *
     * @return List of errors if any.
     *
     * @exception Exception
     */
    public Collection runImport(final File importFile, final Module currentModule)
        throws ScarabException
    {
        return runImport(importFile.getAbsolutePath(), importFile,
                         currentModule, SCARAB);
    }

    /**
     * Run an import.
     *
     * Assumes we're up and running inside of turbine.  Awkwardly duplicates 
     * {@link #runImport(File) import} but duplication is so we can do the reget of
     * the input stream; FileInput "manages" backing up the Upload for us on the
     * second get of the input stream (It creates new ByteArrayInputStream 
     * w/ the src being a byte array of the file its kept in memory or in 
     * temporary storage on disk).  
     *
     * @param importFile FileItem reference to use importing.
     *
     * @return List of errors if any.
     *
     * @exception Exception
     */
    public Collection runImport(final FileItem importFile)
        throws ScarabException
    {
        return runImport(importFile, (Module) null, SCARAB);
    }

    /**
     * Run an import.
     *
     * Assumes we're up and running inside of turbine.  Awkwardly duplicates 
     * {@link #runImport(File) import} but duplication is so we can do the reget of
     * the input stream; FileInput "manages" backing up the Upload for us on the
     * second get of the input stream (It creates new ByteArrayInputStream 
     * w/ the src being a byte array of the file its kept in memory or in 
     * temporary storage on disk).  
     *
     * @param importFile FileItem reference to use importing.
     * @param currentModule If non-null, run check that import is going 
     * against this module.
     *
     * @return List of errors if any.
     *
     * @exception Exception
     */
    public Collection runImport(final FileItem importFile, 
            final Module currentModule,
            final ImportType type)
        throws ScarabException
    {
        return runImport(importFile.getName(), importFile, currentModule, type);
    }

    /**
     * @param input A <code>File</code> or <code>FileItem</code>.
     */
    protected Collection runImport(final String filePath, 
            final Object input,
            final Module currentModule,
            final ImportType type)
        throws ScarabException
    {
        
        final String msg = "Importing issues from XML '" + filePath + '\'';
        LOG.debug(msg);
        try
        {
            // Disable workflow and set file attachment flag
            WorkflowFactory.setForceUseDefault(true);
            
            final Reader is = getScarabFormattedReader(input,type,currentModule);
            
            final BeanReader reader = createScarabIssuesBeanReader();
            validate(filePath, is, reader, currentModule);

            if (importErrors == null || importErrors.isEmpty())
            {
                // Reget the input stream.
                si = insert(filePath, getScarabFormattedReader(input,type,currentModule), reader);
            }
        }
        catch (ParserConfigurationException e)
        {
            LOG.error(msg, e);
            throw new ScarabException(L10NKeySet.ExceptionGeneral,e); //EXCEPTION
        }
        catch (TransformerException e)
        {
            LOG.error(msg, e);
            throw new ScarabException(L10NKeySet.ExceptionGeneral,e); //EXCEPTION
        }
        catch (IOException e)
        {
            LOG.error(msg, e);
            throw new ScarabException(L10NKeySet.ExceptionGeneral,e); //EXCEPTION
        }
        catch (IntrospectionException e)
        {
            LOG.error(msg, e);
            throw new ScarabException(L10NKeySet.ExceptionGeneral,e); //EXCEPTION
        }
        catch (SAXException e)
        {
            LOG.error(msg, e);
            throw new ScarabException(L10NKeySet.ExceptionGeneral,e); //EXCEPTION
        }
        catch (TorqueException e)
        {
            LOG.error(msg, e);
            throw new ScarabException(L10NKeySet.ExceptionGeneral,e); //EXCEPTION
        }
        catch (JDOMException e)
        {
            LOG.error(msg, e);
            throw new ScarabException(L10NKeySet.ExceptionGeneral,e); //EXCEPTION
        }
        catch (ScarabException e)
        {
            LOG.error(msg, e);
            throw e; //EXCEPTION
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            LOG.error(msg, e);
            throw e; //EXCEPTION
        }
        finally
        {
            // Re-enable workflow.
            WorkflowFactory.setForceUseDefault(false);
        }

        return importErrors;
    }

    /**
     * Coerces a new <code>Reader</code> from <code>input</code>.
     * Necessary because the stream is read twice by
     * <code>runImport()</code>, so the source of the stream must be
     * passed into that method.
     *
     * @throws IllegalArgumentException If <code>input</code> is
     * unrecognized.
     */
    private Reader readerFor(Object input)
        throws IOException
    {
        if (input instanceof FileItem)
        {
            return new InputStreamReader(((FileItem) input).getInputStream());
        }
        else if (input instanceof File)
        {
            return new BufferedReader(new FileReader((File) input));
        }
        else
        {
            throw new IllegalArgumentException(); //EXCEPTION
        }
    }

    /**
     * Run validation phase.  Starts by performing XML-well
     * formed-ness and DTD validation (if present), then checks the
     * content.
     *
     * @param name Filename to output in log message.  May be null.
     * @param is Input stream to read.
     * @param reader ScarabIssues bean reader instance.
     * @param currentModule If not <code>null</code>, check whether
     * import is going against this module.
     *
     * @return Any errors encountered during XML or content
     * validation.
     *
     * @exception Exception
     */
    protected void validate(final String name, 
            final Reader is,
            final BeanReader reader, 
            final Module currentModule)
        throws ParserConfigurationException,SAXException,IOException
    {
        // While parsing the XML, we perform well formed-ness and DTD
        // validation (if present, see Xerces dynamic feature).
        setValidationMode(reader, true);
        ScarabIssues si = null;
        try
        {
            si = (ScarabIssues) reader.parse(is);
        }
        catch (SAXParseException e)
        {
            // TODO: L10N this error message from Xerces (somehow),
            // and provide a prefix that describes that a XML parse
            // error was encountered.
            String message = new String("XML parse error at line " + e.getLineNumber() +
                             " column " + e.getColumnNumber() + ": " +
                             e.getMessage());
            LOG.error(message);
            importErrors.add(message);
        }

        // If the XML is okay, validate the actual content.
        if (si != null)
        {
            // ASSUMPTION: Parse errors prevent entry into this block.
            validateContent(si, currentModule);

            // Log any errors encountered during import.
            if (importErrors != null)
            {
                final int nbrErrors = importErrors.size();
                LOG.error("Found " + nbrErrors + " error" +
                          (nbrErrors == 1 ? "" : "s") + " importing '" +
                          name + "':");
                for (Iterator itr = importErrors.iterator(); itr.hasNext(); )
                {
                    LOG.error(itr.next());
                }
            }
        }
    }

    /**
     * Helper method for validate() which invokes validation routines
     * supplied by <code>ScarabIssues</code> plus (conditionally)
     * additional module validation.
     */
    private void validateContent(final ScarabIssues si, final Module currentModule)
    {
        if (currentModule != null)
        {
            // Make sure the XML module corresponds to the current
            // module.  This is later than we'd like to perform this
            // check, since we've already parsed the XML.  On the
            // upside, si.getModule() should not return null.
            final XmlModule xmlModule = si.getModule();

            // HELP: Check domain also?

            final String xmlModuleName = xmlModule.getName();
            final String curModuleName = currentModule.getRealName();
            if (!curModuleName.equals(xmlModuleName))
            {
                Object[] args = { xmlModuleName, curModuleName };
                String error = Localization.format
                    (ScarabConstants.DEFAULT_BUNDLE_NAME, getLocale(),
                     "XMLAndCurrentModuleMismatch", args);
                importErrors.add(error);
            }

            final String xmlCode = xmlModule.getCode();
            if (xmlCode == null ||
                !currentModule.getCode().equals(xmlCode))
            {
                final Object[] args = { xmlCode, currentModule.getCode() };
                final String error = Localization.format
                    (ScarabConstants.DEFAULT_BUNDLE_NAME, getLocale(),
                     "XMLAndCurrentCodeMismatch", args);
                importErrors.add(error);
            }
        }

        si.doValidateDependencies();
        si.doValidateUsers();
    }

    /**
     * Do actual issue insert.
     *
     * Assumes issues passed have already been validated.  If they haven't
     * been, could damage scarab.
     *
     * @param name Name to use in log messages (E.g. filename).  May be null.
     * @param is Input stream of xml to insert.
     * @param reader ScarabIssues bean reader instance.
     * 
     * @return The instance of scarabissues we inserted in case you need to 
     * display info about the issues inserted.
     */
    protected ScarabIssues insert(
            final String name, 
            final Reader is, 
            final BeanReader reader)
        throws ParserConfigurationException,SAXException,IOException,ScarabException, TorqueException
    {
        setValidationMode(reader, false);
        final ScarabIssues si = (ScarabIssues)reader.parse(is);
        si.doHandleDependencies();
        LOG.debug("Successfully imported " + name + '!');
        return si;
    }

    /**
     * Sets the validation mode for both this instance and the
     * specified <code>Digester</code>.
     *
     * @param reader The XML parser to set the validation mode for.
     * @param state The validation mode.
     * @see <a href="http://xml.apache.org/xerces-j/faq-general.html#valid">Xerces validation FAQ</a>
     * @see <a href="http://xml.apache.org/xerces-j/features.html">Xerces SAX2 feature list</a>
     */
    private void setValidationMode(Digester reader, boolean state)
        throws ParserConfigurationException, SAXException
    {
        this.validationMode = state;

        // Setup the XML parser SAX2 features.

        // Turn on DTD validation (these are functionally equivalent
        // with Xerces 1.4.4 and likely most other SAX2 impls).
        reader.setValidating(state);
        reader.setFeature("http://xml.org/sax/features/validation", state);

        // Validate the document only if a grammar is specified
        // (http://xml.org/sax/features/validation must be state).
        reader.setFeature("http://apache.org/xml/features/validation/dynamic",
                          state);
    }

    /**
     * Get instance of the ScarabIssues used importing.
     *
     * You'd use this method to get at the instance of scarab issues used 
     * importing for case where you want to print out info on the import thats
     * just happened. Call after a successful import. Calling before will give
     * undefined results.
     *
     * @return Instance of ScarabIssues we ran the import with.
     */
    public ScarabIssues getScarabIssuesBeanReader()
    {
        return this.si;
    }

    /**
     * A URI Resolver for use with the XSL transforms
     *
     * This resolver will map a URI in the XSL transform to an absolute
     * file path.
     */
    class TransformResolver implements URIResolver {

        File resources_path; // Absolute path to the resources

        public TransformResolver(String resources_path) {
            this.resources_path = new File(resources_path);
        }

        public Source resolve(String href, String base) {

            File resource = new File(resources_path, href);
            
            return (resource.exists()) ? new StreamSource(resource) : null;
        }
    }
    
    /**
     * Return a bean reader for ScarabIssue.
     *
     * @return A bean reader.
     */
    protected BeanReader createScarabIssuesBeanReader()
        throws ParserConfigurationException,IntrospectionException,SAXNotRecognizedException,
            SAXNotSupportedException
    {
        BeanReader reader = new BeanReader()
            {
                public InputSource resolveEntity(String publicId,
                                                 String systemId)
                    throws SAXException
                {
                    return ImportIssues.this.resolveEntity(this, publicId, systemId);
                }
            };

        // Connecting Digster's logger to ours logs too verbosely.
        //reader.setLogger(LOG);
        reader.register(SYSTEM_DTD_URI, INTERNAL_DTD_URI);
        // Be forgiving about the encodings we accept.
        reader.setFeature("http://apache.org/xml/features/allow-java-encodings",
                          true);
        reader.setXMLIntrospector(createXMLIntrospector());
        reader.registerBeanClass(ScarabIssues.class);
        NameMapper nm = reader.getXMLIntrospector().getNameMapper();
        reader.addRule(nm.mapTypeToElementName
                       (new BeanDescriptor(ScarabIssues.class).getName()),
                       new ScarabIssuesSetupRule());
        reader.setErrorHandler(this);
        return reader;
    }

    protected XMLIntrospector createXMLIntrospector()
    {
        XMLIntrospector introspector = new XMLIntrospector();

        // set elements for attributes to true
        introspector.setAttributesForPrimitives(false);

        // wrap collections in an XML element
        //introspector.setWrapCollectionsInElement(true);

        // turn bean elements into lower case
        introspector.setElementNameMapper(new HyphenatedNameMapper());

        return introspector;
    }

    /**
     * A rule to perform setup of the a ScarabIssues instance.
     */
    class ScarabIssuesSetupRule extends Rule
    {
        public void begin(String namespace, String name, Attributes attributes)
        {
            ScarabIssues si = (ScarabIssues) getDigester().peek();
            si.allowFileAttachments(allowFileAttachments);
            si.allowGlobalImports(allowGlobalImports);
            si.inValidationMode(validationMode);
            si.importErrors = importErrors;
        }
    }

    /**
     * Method to output the bean object as XML. 
     * 
     * Not used right now.
     */
    protected void write(final Object bean, final Writer out)
        throws IOException,SAXException,IntrospectionException
    {
        final BeanWriter writer = new BeanWriter(out);
        writer.setXMLIntrospector(createXMLIntrospector());
        writer.enablePrettyPrint();
        writer.setWriteIDs(false);
        writer.write(bean);
    }

    private Locale getLocale()
    {
        return ScarabConstants.DEFAULT_LOCALE;
    }


    // ---- org.xml.sax.ErrorHandler implementation ------------------------

    /** Receive notification of a recoverable error. */
    public void error(SAXParseException e)
        throws SAXParseException
    {
        LOG.error("Parse Error at line " + e.getLineNumber() +
                  " column " + e.getColumnNumber() + ": " + e.getMessage(), e);
        throw e; //EXCEPTION
    }

    /** Receive notification of a non-recoverable error. */
    public void fatalError(SAXParseException e)
        throws SAXParseException
    {
        LOG.error("Parse Fatal Error at line " + e.getLineNumber() +
                  " column " + e.getColumnNumber() + ": " + e.getMessage(), e);
        throw e; //EXCEPTION
    }

    /** Receive notification of a warning. */
    public void warning(SAXParseException e)
    {
        // Warnings are non-fatal.  At some point we should report
        // these back to the end user.
        LOG.debug("Parse Warning at line " + e.getLineNumber() +
                  " column " + e.getColumnNumber() + ": " + e.getMessage());
    }

    /** Handles transforming other xml (bugzilla or jira) formats into
     * the scarab xml format
     **/
    private Reader getScarabFormattedReader(
            final Object input,
            final ImportType type,
            final Module currModule) throws IOException, TransformerException, TorqueException, JDOMException 
    {
        Reader returnValue = null;
        
        if( SCARAB == type )
        {
            // is in correct format already, just return the input stream
            returnValue = readerFor(input);
        }
        else if( BUGZILLA == type )
        {
            // Location of the extensions directory for Bugzilla
            // Transform configuration, mappings and attachments are here
            // TODO move onto resourceDirectory derivative
            final String extensions = System.getProperty("catalina.home") + "/../extensions/bugzilla";
            
            // Locate the Bugzilla to Scarab XSL transform
            final InputStream xsl = getClass().getResourceAsStream(BUGZILLA_XSL);
            
            // Transform Bugzilla xml to scarab format
            // (Trailing '/' to resources is deliberate)
            final Reader result = transformXML(
                    new StreamSource(readerFor(input)), xsl, currModule, extensions);
            
            // insert missing information (module)
            returnValue = insertModuleNode(result, currModule);
        }
        else if( JIRA == type )
        {
                    
            // transform xml to scarab format
            final InputStream xsl = getClass().getResourceAsStream(JIRA_XSL);
            final Reader result = transformXML(
                    new StreamSource(readerFor(input)), xsl, currModule, resourceDirectory);
            // insert missing information (module)
            returnValue = insertModuleNode(result, currModule);
        }
        return returnValue;
    }
    
    private Reader transformXML(final Source xmlSource, 
            final InputStream xsl,
            final Module currModule,
            final String resources)
        throws TransformerException
    {
            final StringWriter writer = new StringWriter();
            final StreamResult result = new StreamResult(writer);            

            // The resolver will help find the transform's resources
            if (resources != null) {
                transformerFactory.setURIResolver(new TransformResolver(resources));
            }
            
            final Transformer transformer = (xsl != null)
                    ? transformerFactory.newTransformer(new StreamSource(xsl))
                    : transformerFactory.newTransformer();
            
            transformer.setOutputProperty(OutputKeys.INDENT, "yes" );
            
            // Pass this parameter to the transform to locate resources,
            // particularly attachments. For attachments, the
            // Scarab instance must be able to see this absolute path
            transformer.setParameter("resources_path", resources);

            // Tell the transformer the module_code
            transformer.setParameter("module_code", currModule.getCode());
            
            if( xsl == null )
            {
                // plain outputting (used on a DomSource)
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");          
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");                
            }
            transformer.transform(xmlSource, result);   
            //System.out.println(writer.toString());
            return new StringReader(writer.toString());
    }

    private Reader insertModuleNode(final Reader result, 
            final Module currModule) 
        throws TorqueException, JDOMException, IOException, TransformerException 
    {
        
        final ScarabUser user = ScarabUserManager.getInstance(currModule.getOwnerId());        
        
        // Core Java: org.w3c.dom version (jdk1.4+ compatible)
//        final DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
//        final Document doc = docBuilder.parse( new InputSource(result) );
//        // insert module
//        final Element root = doc.getDocumentElement();
//        final Element moduleNode = doc.createElement("module");
//        final Element idNode = doc.createElement("id");
//        final Element parentIdNode = doc.createElement("parent-id");
//        final Element nameNode = doc.createElement("name");
//        final Element ownerNode = doc.createElement("owner");
//        final Element descriptionNode = doc.createElement("description");
//        final Element urlNode = doc.createElement("url");
//        final Element domainNode = doc.createElement("domain");
//        final Element codeNode = doc.createElement("code");
//        
//        idNode.appendChild(doc.createTextNode(String.valueOf(currModule.getModuleId())));
//        parentIdNode.appendChild(doc.createTextNode(String.valueOf(currModule.getParentId())));
//        nameNode.appendChild(doc.createTextNode(currModule.getName()));
//        ownerNode.appendChild(doc.createTextNode(user.getUserName()));
//        descriptionNode.appendChild(doc.createTextNode(currModule.getDescription()));
//        urlNode.appendChild(doc.createTextNode(currModule.getUrl()));
//        domainNode.appendChild(doc.createTextNode(currModule.getHttpDomain()));
//        codeNode.appendChild(doc.createTextNode(currModule.getCode()));
//        
//        moduleNode.appendChild(idNode);
//        moduleNode.appendChild(parentIdNode);
//        moduleNode.appendChild(nameNode);
//        moduleNode.appendChild(ownerNode);
//        moduleNode.appendChild(descriptionNode);
//        moduleNode.appendChild(urlNode);
//        moduleNode.appendChild(domainNode);
//        moduleNode.appendChild(codeNode);
//        
//        root.appendChild(moduleNode);
        
        // JDom version (jdk1.3 compatible)
        
        final SAXBuilder builder = new SAXBuilder();
        builder.setEntityResolver(new EntityResolver()
        {
                public InputSource resolveEntity(String publicId,
                                                 String systemId)
                    throws SAXException
                {
                    return ImportIssues.this.resolveEntity(this, publicId, systemId);
                }   
        });
        final Document doc = builder.build(result);        
        final Element root = doc.getRootElement();

        final Element moduleNode = new Element("module");
        final Element idNode = new Element("id");
        final Element parentIdNode = new Element("parent-id");
        final Element nameNode = new Element("name");
        final Element ownerNode = new Element("owner");
        final Element descriptionNode = new Element("description");
        final Element urlNode = new Element("url");
        final Element domainNode = new Element("domain");
        final Element codeNode = new Element("code");    
        
        idNode.setText(String.valueOf(currModule.getModuleId()));
        parentIdNode.setText(String.valueOf(currModule.getParentId()));
        nameNode.setText(currModule.getRealName());
        ownerNode.setText(user.getUserName());
        descriptionNode.setText(currModule.getDescription());
        urlNode.setText(currModule.getUrl());
        domainNode.setText(currModule.getHttpDomain());
        codeNode.setText(currModule.getCode());
        
        moduleNode.addContent(idNode)
                .addContent(parentIdNode)
                .addContent(nameNode)
                .addContent(ownerNode)
                .addContent(descriptionNode)
                /*
                 * These are excluded for now, since your database domain may 
                 * not correspond to currModule.getHttpDomain().
                .addContent(urlNode)
                .addContent(domainNode)
                 */
                .addContent(codeNode);
        root.addContent(2,moduleNode);
        
        return transformXML(new JDOMSource(doc), null, currModule, null);
    }
        
    public final static class ImportType 
    {
        
        private final String type;
        private static final Map instances = new Hashtable();
        
        private ImportType(final String type)
        {
            if( type == null )
            {
                throw new IllegalArgumentException("Not allowed null type");
            }
            this.type = type;
            instances.put(type,this);
        }
        
        public static ImportType valueOf(final String type)
        {
            ImportType instance = (ImportType)instances.get(type);
            if( instance == null)
            {
                instance = new ImportType(type);
            }
            return instance;
        }
    }



    private InputSource resolveEntity(
            final EntityResolver reader,
            final String publicId,
            final String systemId)
        throws SAXException
    {
        InputSource input = null;
        if (publicId == null && systemId != null)
        {
            // Resolve SYSTEM DOCTYPE.
            if (SYSTEM_DTD_URI.equalsIgnoreCase(systemId) ||
                INTERNAL_DTD_URI.equalsIgnoreCase(systemId))
            {
                // First look for the DTD in the classpath.
                input = resolveDTDResource();

                if (input == null)
                {
                    try {
                        // Kick resolution back to Digester.
                        input = reader.resolveEntity(publicId, systemId);
                    
                    } catch (IOException ex) {
                        LOG.error(ex);
                    }
                }
            }
        }
        return input;
    }

    /**
     * Looks for the DTD in the classpath as resouce
     * {@link #DTD_RESOURCE}.
     *
     * @return The DTD, or <code>null</code> if not found.
     */
    private InputSource resolveDTDResource()
    {
        InputStream stream =
            getClass().getResourceAsStream(DTD_RESOURCE);
        if (stream != null)
        {
            LOG.debug("Located DTD in classpath using " +
                      "resource path '" + DTD_RESOURCE + '\'');
            return new InputSource(stream);
        }
        else
        {
            LOG.debug("DTD resource '" + DTD_RESOURCE + "' not " +
                      "found in classpath");
            return null;
        }
    }
}
