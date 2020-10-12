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
public class InstalledApplication
{
    public String identifier;
    public String name;
    public String version;
    public String guid;
    
    @Override
    public String toString()
    {
        String result = "[InstalledAppliaction] (";
        if (identifier != null)
            result += "identifier=" + identifier + ",";
        if (name != null)
            result += "name=" + name + ",";
        if (version != null)
            result += "version=" + version + ",";
        if (guid != null)
            result += "guid=" + guid + ",";
        result += ")";
        return result;
    }
}
