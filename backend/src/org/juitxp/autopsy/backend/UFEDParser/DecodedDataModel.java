/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend.UFEDParser;

import java.util.HashMap;
import java.util.Map;
import org.xml.sax.Attributes;

/**
 *
 * @author florian
 */
public class DecodedDataModel extends DecodedDataNode
{
    String id;
    Map<String, String> attributes = new HashMap<String, String>();
    
    Map<String, DecodedDataField> children = new HashMap<String, DecodedDataField>();
    public Map<String, DecodedDataField> getChildren() { return children; }
    
    public String getFieldValue(String name)
    {
        DecodedDataField field = children.get(name);
        if (field == null)
            return null;
        if (!(field instanceof DecodedDataSingleField))
            return null;
        
        return ((DecodedDataSingleField) field).getValue();
    }
    
    public String getAttribute(String name)
    {
        return attributes.get(name);
    }
    
    public String getID() { return id; }
    
    @Override
    void print(int indent)
    {
        System.out.println(new String(new char[indent]).replace("\0", " ") + "Model id=" + id + " type=" + getType());
        System.out.println(new String(new char[indent]).replace("\0", " ") + "  attributes: " + attributes);
        for (Map.Entry<String, DecodedDataField> fields: children.entrySet())
            fields.getValue().print(indent+2);
    }
    
    @Override
    void setValue(String value) { System.out.println("Model::setValue ERROR " + value); }
    
    @Override
    void addChildren(DecodedDataNode node)
    {
        if (!(node instanceof DecodedDataField))
            System.out.println("Model::addChildren ERROR ");
        else
        {
            DecodedDataField field = (DecodedDataField) node;
            children.put(field.getName(), field);
        }
    }
    
    DecodedDataModel(DecodedDataNode parent, String type, String id, Attributes sax_attributes)
    {
        super(parent, type);
        this.id = id;
        
        for (int i = 0; i != sax_attributes.getLength(); ++i)
        {
            attributes.put( sax_attributes.getQName(i), sax_attributes.getValue(i) );
        }
    }    
}
