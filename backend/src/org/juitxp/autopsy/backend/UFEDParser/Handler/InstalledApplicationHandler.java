/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend.UFEDParser.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.juitxp.autopsy.backend.UFEDParser.DecodedDataModel;
import org.juitxp.autopsy.backend.UFEDParser.InstalledApplication;
import org.juitxp.autopsy.backend.UFEDParser.TaggedFile;
import org.juitxp.autopsy.backend.UFEDParser.UFEDFileContext;

/**
 *
 * @author florian
 */
public class InstalledApplicationHandler extends BaseHandler
{
    public InstalledApplicationHandler(UFEDFileContext context, Logger logger)
    {
        super(context, logger);
    }
    
    @Override
    public void newModel(DecodedDataModel model)
    {
        if (model.getType().equals("InstalledApplication"))
        {
            newInstalledApplication(model);
        }
    }
    
    private void newInstalledApplication(DecodedDataModel model)
    {
        InstalledApplication app = new InstalledApplication();
        
        app.identifier = model.getFieldValue("Identifier");
        app.name = model.getFieldValue("Name");
        if (app.name == null)
            app.name = app.identifier;
        
        app.version = model.getFieldValue("Version");
        app.guid = model.getFieldValue("AppGUID");

        context.addInstalledApplication(app);
    }

    @Override
    public void newSection(String string)
    {
    }

    @Override
    public void newFile(TaggedFile tf)
    {
    }
}