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
public abstract class DecodedDataNode
{
    DecodedDataNode parent;
    public DecodedDataNode getParent() { return parent; }
    
    String type;
    public String getType() { return type; }
    
    abstract void print(int indent);
    public void print() { print(0); }
    
    abstract void setValue(String value);
    abstract void addChildren(DecodedDataNode node);
    
    DecodedDataNode(DecodedDataNode parent, String type)
    {
        this.parent = parent;
        this.type = type;
    }
}
