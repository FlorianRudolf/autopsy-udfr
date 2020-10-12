/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend.UFEDParser;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author florian
 */
public class TaggedFile
{
    public String id;
    
    public String fs;
    public String fsid;
    
    public Path local_path;
    public Path mobile_path;
        
    public String mimetype;
    public long size;
    public long crtime;
    public long mtime;
    
    Map<String, String> metadata = new HashMap<String, String>();
    
    @Override
    public String toString()
    {
        String result = "File with id: " + id;
        result += "\n  fs: " + fs;
        result += "\n  fsid: " + fsid;
        result += "\n  local_path: " + local_path;
        result += "\n  mobile_path: " + mobile_path;
        result += "\n  mimetype: " + mimetype;
        result += "\n  size: " + size;
        result += "\n  crtime: " + crtime;
        result += "\n  mtime: " + mtime;
        
        for (Map.Entry<String, String> entry : metadata.entrySet())
        {
            result += "\n    " + entry.getKey() + ": " + entry.getValue();
        }
        
        return result;
    }
    
    public void print()
    {
        System.out.println(this);
    }    
}
