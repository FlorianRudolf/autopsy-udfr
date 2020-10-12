/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend.UFEDParser;

import org.juitxp.autopsy.backend.UFEDParser.Handler.BaseHandler;
import java.util.Arrays;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author florian
 */


public class SAXHandler extends DefaultHandler
{
    String current_section;
    DefaultHandler section_handler;
    List<String> supportedSectionNames = Arrays.asList("taggedFiles","decodedData");
    

    String current_element;
    DecodedDataNode current_node = null;
    
    
    BaseHandler new_object_handler;
    

    public SAXHandler(BaseHandler new_object_handler)
    {
        this.new_object_handler = new_object_handler;
    }
    
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        current_element = qName;
        
        if ("decodedData".equals(current_section) && "modelType".equals(qName))
        {
            new_object_handler.newSection("decodedData/" + attributes.getValue("type"));
        }
        
        if (supportedSectionNames.contains(qName))
        {
            current_section = qName;
            
            if (current_section.equals("taggedFiles"))
            {
                new_object_handler.newSection("taggedFiles");
                section_handler = new TaggedFileSAXHandler(new_object_handler);
            }
            else if (current_section.equals("decodedData"))
            {
                section_handler = new DecodedDataSAXHandler(new_object_handler);
            }
        }
        else
        {
            if (section_handler != null)
                section_handler.startElement(uri, localName, qName, attributes);
        }
    }
    
    
    @Override
    public void characters(char ch[], int start, int length) throws SAXException
    {
        if (section_handler != null)
            section_handler.characters(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (supportedSectionNames.contains(qName))
            section_handler = null;
        
        if (section_handler != null)
            section_handler.endElement(uri, localName, qName);
    }
}
