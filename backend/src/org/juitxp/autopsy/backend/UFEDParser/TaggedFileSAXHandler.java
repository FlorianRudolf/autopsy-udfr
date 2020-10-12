/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend.UFEDParser;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.juitxp.autopsy.backend.UFEDParser.Handler.BaseHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.juitxp.autopsy.backend.UFEDParser.Handler.FileHandler;

/**
 *
 * @author florian
 */
class TaggedFileSAXHandler extends DefaultHandler
{
    List<String> supportedSections = Arrays.asList("accessInfo","metadata");
    List<String> supportedItems = Arrays.asList("timestamp","item");
    
    String tagged_files_section;
    
    String current_element;
    String current_name;
    
    TaggedFile current_file;
    
    BaseHandler new_tagged_file;
    
    
    TaggedFileSAXHandler(BaseHandler new_tagged_file)
    {
        this.tagged_files_section = null;
        
        this.current_file = null;
        this.new_tagged_file = new_tagged_file;
    }
    
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        current_element = qName;
        
        DecodedDataNode node = null;
        if (qName.equals("file"))
        {
            current_file = new TaggedFile();
            current_file.id = attributes.getValue("id");
            
            current_file.fs = attributes.getValue("fs");
            current_file.fsid = attributes.getValue("fsid");
            
            current_file.mobile_path = Paths.get(attributes.getValue("path"));
            current_file.size = Long.parseLong(attributes.getValue("size"));
        }
        else if (supportedSections.contains(qName))
        {
            tagged_files_section = qName;
        }
        else if (supportedItems.contains(qName))
        {
            current_name = attributes.getValue("name");
        }
    }

    
    @Override
    public void characters(char ch[], int start, int length) throws SAXException
    {
        if (tagged_files_section == null)
            return;
        
        String value = new String(ch, start, length).trim();
        if (value.isEmpty())
            return;
        
        if (tagged_files_section.equals("accessInfo"))
        {
            if (current_element.equals("timestamp"))
            {
                long ts = Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(value)).getEpochSecond();
                
                if (current_name.equals("CreationTime"))
                    current_file.crtime = ts;
                else if (current_name.equals("ModifyTime"))
                    current_file.mtime = ts;
            }
        }
        else if (tagged_files_section.equals("metadata"))
        {
            if (current_element.equals("item"))
            {
                if (current_name.equals("Local Path"))
                {
                    current_file.local_path = Paths.get(value.replace("\\","/"));
                }
                else
                {
                    current_file.metadata.put(current_name, value);
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (qName.equals("file"))
        {
            new_tagged_file.newFile(current_file);
            current_file = null;
        }
    }
}
