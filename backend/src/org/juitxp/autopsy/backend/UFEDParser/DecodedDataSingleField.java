/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend.UFEDParser;

/**
 *
 * @author florian
 */
public class DecodedDataSingleField extends DecodedDataField
{
    String value;
    public String getValue() { return value; }
    
    @Override
    void print(int indent)
    {
        System.out.println(new String(new char[indent]).replace("\0", " ") + "Field name=" + name + " type=" + type + " value=" + value);
    }
    
    @Override
    void setValue(String value) { this.value = value; }
    
    @Override
    void addChildren(DecodedDataNode node) { System.out.println("Field::addChildren ERROR"); }
    
    DecodedDataSingleField(DecodedDataNode parent, String type, String name)
    {
        super(parent, type, name);
        this.value = null;
    }
    
}
