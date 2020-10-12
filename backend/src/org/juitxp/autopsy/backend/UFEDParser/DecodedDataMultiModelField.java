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
public class DecodedDataMultiModelField extends DecodedDataField
{
    List<DecodedDataModel> children = new ArrayList<DecodedDataModel>();
    public List<DecodedDataModel> getModels() { return children; }

    @Override
    void print(int indent)
    {
        System.out.println(new String(new char[indent]).replace("\0", " ") + "MultiModelField name=" + name + " type=" + type);
        for (DecodedDataNode node: children)
            node.print(indent+2);
    }
    
    @Override
    void setValue(String value) { System.out.println("MultiModelField::setValue ERROR"); }
    
    @Override
    void addChildren(DecodedDataNode node)
    {
        if (!(node instanceof DecodedDataModel))
            System.out.println("MultiModelField::addChildren ERROR ");
        else
        {
            DecodedDataModel model = (DecodedDataModel) node;
            children.add(model);
        }
    }

    
    DecodedDataMultiModelField(DecodedDataNode parent, String type, String name)
    {
        super(parent, type, name);
    }    
}
