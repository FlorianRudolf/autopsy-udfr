/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend.UFEDParser.Handler;

import org.juitxp.autopsy.backend.UFEDParser.Handler.BaseHandler;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.juitxp.autopsy.backend.RandomTools;
import org.juitxp.autopsy.backend.UFEDParser.DecodedDataModel;
import org.juitxp.autopsy.backend.UFEDParser.DecodedDataMultiModelField;
import org.juitxp.autopsy.backend.UFEDParser.TaggedFile;
import org.juitxp.autopsy.backend.UFEDParser.UFEDFileContext;

/**
 *
 * @author florian
 */
/**
 *
 * @author florian
 */
public class FileHandler extends BaseHandler
{
    public FileHandler(UFEDFileContext context, Logger logger)
    {
        super(context, logger);
    }

    @Override
    public void newSection(String current_section)
    {
    }
    
    @Override
    public void newFile(TaggedFile file)
    {
        context.addFile(file);
    }
    
    @Override
    public void newModel(DecodedDataModel model)
    {
        if (model.getType().equals("Email"))
        {
            newEmail(model);
        }
    }
    
    public TaggedFile getOrCreateAttachmentFile(DecodedDataModel attachment_model)
    {       
        String file_id = attachment_model.getAttribute("file_id");
        if (file_id == null)
        {
            file_id = "attachment_" + attachment_model.getID();
        }
        
        if (file_id != null)
        {
            TaggedFile attachment_file = context.getFileByID(file_id);
            if (attachment_file != null)
                return attachment_file;
         
            String attachment_extracted_path = attachment_model.getFieldValue("attachment_extracted_path");
            if (attachment_extracted_path != null) 
            {
                Path aep = Paths.get(attachment_extracted_path.replace("\\","/"));
                String filename = attachment_model.getFieldValue("Filename");
                
                Path ufed_path = aep;
//                if (filename != null)
//                {
//                    ufed_path = ufed_path.resolve(filename);
//                }
                
                Path local_path = aep;
                
                TaggedFile result = new TaggedFile();
                
                result.fs = "extracted";
                
                result.id = file_id;
                result.local_path = local_path;
                result.mobile_path = ufed_path;
                
                return result;
            }
        }
        else
        {
            
        }
        
        return null;
    }
    
    public void newEmail(DecodedDataModel email_model)
    {
        DecodedDataMultiModelField attachments_field = (DecodedDataMultiModelField) email_model.getChildren().get("Attachments");
        if (attachments_field != null)
        {
            for (DecodedDataModel attachment_model: attachments_field.getModels())
            {
                TaggedFile attachment_file = getOrCreateAttachmentFile(attachment_model);
                if (attachment_file != null)
                {
                    context.addFile(attachment_file);
                }
                else
                {
                    logger.log(Level.SEVERE, "Error creating attachment file");
                    logger.log(Level.SEVERE, attachment_model.toString());
                    attachment_model.print();
                }
            }
        }

        attachments_field = (DecodedDataMultiModelField) email_model.getChildren().get("Attachment");
        if (attachments_field != null)
        {
            for (DecodedDataModel attachment_model: attachments_field.getModels())
            {
                TaggedFile attachment_file = getOrCreateAttachmentFile(attachment_model);
                if (attachment_file != null)
                {
                    context.addFile(attachment_file);
                }
                else
                {
                    logger.log(Level.SEVERE, "Error creating attachment file");
                    logger.log(Level.SEVERE, attachment_model.toString());
                    attachment_model.print();
                }
            }
        }
    }
    

}
