package org.tigris.scarab.soap;

import org.apache.axis.Constants;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.wsdl.fromJava.Types;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

import javax.xml.namespace.QName;
import java.io.IOException;

public class SoapIssueSer implements Serializer
{
    /** SERIALIZER STUFF
     */
    /**
     * Serialize an element named name, with the indicated attributes
     * and value.
     * @param name is the element name
     * @param attributes are the attributes...serialize is free to add more.
     * @param value is the value
     * @param context is the SerializationContext
     */
    public void serialize(QName name, Attributes attributes,
                          Object value, SerializationContext context)
        throws IOException
    {
        if (!(value instanceof SoapIssue))
            throw new IOException("Can't serialize a " + value.getClass().getName() + " with a SoapIssueSerializer.");
        SoapIssue data = (SoapIssue)value;

        context.startElement(name, attributes);
        context.serialize(new QName("", SoapIssue.ID), null, data.getId());
        context.serialize(new QName("", SoapIssue.NAME), null, data.getName());
        context.endElement();
    }
    public String getMechanismType() { return Constants.AXIS_SAX; }

    public Element writeSchema(Class javaType, Types types) throws Exception {
        return null;
    }
}
