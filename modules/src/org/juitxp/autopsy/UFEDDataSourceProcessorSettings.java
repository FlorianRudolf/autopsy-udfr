/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author florian
 */
public class UFEDDataSourceProcessorSettings
{
    public UFEDDataSourceProcessorSettings(String path,
                                           Boolean file_parsing_enabled, Boolean installed_application_parsing_enabled,
                                           Boolean contact_parsing_enabled, Boolean chat_parsing_enabled)
    {
        this.path = Paths.get(path);
        
        this.file_parsing_enabled = file_parsing_enabled;
        this.installed_application_parsing_enabled = installed_application_parsing_enabled;
        this.contact_parsing_enabled = contact_parsing_enabled;
        this.chat_parsing_enabled = chat_parsing_enabled;
    }
    
    
    public Path path;

    public Boolean file_parsing_enabled;
    public Boolean installed_application_parsing_enabled;
    public Boolean contact_parsing_enabled;
    public Boolean chat_parsing_enabled;
}
