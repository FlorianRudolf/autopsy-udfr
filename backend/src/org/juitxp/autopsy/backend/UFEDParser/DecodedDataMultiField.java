/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend.UFEDParser;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author florian
 */
public class DecodedDataMultiField extends DecodedDataField
{
    List<String> values = new ArrayList<String>();
    public List<String> getValues() { return values; }
    
    @Override
    void print(int indent)
    {
        System.out.println(new String(new char[indent]).replace("\0", " ") + "MultiField name=" + name + " type=" + type);
        for (String value: values)
            System.out.println(new String(new char[indent+2]).replace("\0", " ") + "value=" + value);
    }
    
    @Override
    void setValue(String value)
    {
        this.values.add(value);
    }
    
    @Override
    void addChildren(DecodedDataNode node) { System.out.println("MultiField::addChildren ERROR"); }
    
    
    DecodedDataMultiField(DecodedDataNode parent, String type, String name)
    {
        super(parent, type, name);
    }
}
