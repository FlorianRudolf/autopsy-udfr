/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend.UFEDParser.Handler;

import java.util.logging.Logger;
import org.juitxp.autopsy.backend.UFEDParser.DecodedDataModel;
import org.juitxp.autopsy.backend.UFEDParser.TaggedFile;
import org.juitxp.autopsy.backend.UFEDParser.UFEDFileContext;

/**
 *
 * @author florian
 */
public abstract class BaseHandler
{
    protected Logger logger;
    
    protected UFEDFileContext context;
    
    public BaseHandler(UFEDFileContext context, Logger logger)
    {
        this.logger = logger;
        this.context = context;
    }
    
    public abstract void newSection(String current_section);
    
    public void newFile(TaggedFile file) {}
    public void newModel(DecodedDataModel model) {}
}
