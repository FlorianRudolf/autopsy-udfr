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
public abstract class DecodedDataField extends DecodedDataNode
{
    String name;
    
    String getName() { return name; }
    
    DecodedDataField(DecodedDataNode parent, String type, String name)
    {
        super(parent, type);
        this.name = name;
    }    
}
