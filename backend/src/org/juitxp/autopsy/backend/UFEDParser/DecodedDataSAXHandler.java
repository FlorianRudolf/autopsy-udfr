/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend.UFEDParser;

import java.util.Arrays;
import java.util.List;
import org.juitxp.autopsy.backend.UFEDParser.Handler.BaseHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author florian
 */


class DecodedDataSAXHandler extends DefaultHandler
{
    List<String> supportedNodeNames = Arrays.asList("model","multiField","field","modelField","multiModelField");
    
    String current_element;
    DecodedDataNode current_node;
    
    BaseHandler new_decoded_data;
    
    
    DecodedDataSAXHandler(BaseHandler new_decoded_data)
    {
        current_node = null;
        this.new_decoded_data = new_decoded_data;
    }
    
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
//        System.out.println("startElement: " + current_element);
        current_element = qName;
        
        DecodedDataNode node = null;
        if (qName.equals("model"))
        {
            node = new DecodedDataModel( current_node, attributes.getValue("type"), attributes.getValue("id"), attributes );
        }
        else if (qName.equals("field"))
        {
            node = new DecodedDataSingleField( current_node, attributes.getValue("type"), attributes.getValue("name") );
        }
        else if (qName.equals("multiField"))
        {
            node = new DecodedDataMultiField( current_node, attributes.getValue("type"), attributes.getValue("name") );
        }
        else if (qName.equals("modelField") || qName.equals("multiModelField"))
        {
            node = new DecodedDataMultiModelField( current_node, attributes.getValue("type"), attributes.getValue("name") );
        }

        if (node != null)
        {
            if (current_node != null)
                current_node.addChildren(node);
            current_node = node;
        }
    }

    
    @Override
    public void characters(char ch[], int start, int length) throws SAXException
    {
        if (current_node == null)
            return;
        
        if (current_element.equals("value"))
        {
            if (current_node == null)
                throw new SAXException("DecodedDataHandler: current element is value but current node is null");
            
            String value = new String(ch, start, length).trim();
            if (value.length() > 0)
            {
                if (!(current_node instanceof DecodedDataSingleField) && !(current_node instanceof DecodedDataMultiField))
                {
                    System.out.println("chars not instance of field " + value);
                    current_node.print();
                    throw new SAXException("DecodedDataHandler: current element is value but current node is not instance of Field");
                }
            
                current_node.setValue(value);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (supportedNodeNames.contains(qName))
        {
            DecodedDataNode current_parent_node = current_node.getParent();
            
            if (current_node != null && current_parent_node == null)
            {
                if (current_node instanceof DecodedDataModel)
                    new_decoded_data.newModel( (DecodedDataModel) current_node );
            }
            
            current_node = current_parent_node;
        }
    }
}
