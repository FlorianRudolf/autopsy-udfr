/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend.UFEDParser.Handler;

import org.juitxp.autopsy.backend.UFEDParser.Handler.BaseHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.juitxp.autopsy.backend.Contact;
import org.juitxp.autopsy.backend.UFEDParser.DecodedDataModel;
import org.juitxp.autopsy.backend.UFEDParser.DecodedDataMultiModelField;
import org.juitxp.autopsy.backend.UFEDParser.TaggedFile;
import org.juitxp.autopsy.backend.UFEDParser.UFEDFileContext;

/**
 *
 * @author florian
 */
public class ContactHandler extends BaseHandler
{
    
    
    public ContactHandler(UFEDFileContext context, Logger logger)
    {
        super(context, logger);
    }
    
    @Override
    public void newModel(DecodedDataModel model)
    {
        if (model.getType().equals("Contact"))
        {
            newContact(model);
        }
    }
        
    private void newContact(DecodedDataModel model)
    {
        String name = model.getFieldValue("Name");
        String source = model.getFieldValue("Source");

        List<org.juitxp.autopsy.backend.Account> accounts = new ArrayList<org.juitxp.autopsy.backend.Account>();
        
        DecodedDataMultiModelField multi_model_field = (DecodedDataMultiModelField) model.getChildren().get("Entries");
        if (multi_model_field != null)
        {
            for (DecodedDataModel entry : multi_model_field.getModels())
            {
                String type = entry.getType();
                String value = entry.getFieldValue("Value");
                String cat = entry.getFieldValue("Category");
                String domain = entry.getFieldValue("Domain");
                
                org.juitxp.autopsy.backend.Account.Type account_type = org.juitxp.autopsy.backend.Account.Type.fromUFEDContact(type, cat, domain);
                if (account_type == org.juitxp.autopsy.backend.Account.Type.UNKNOWN)
                    continue;
                
                org.juitxp.autopsy.backend.Account account = new org.juitxp.autopsy.backend.Account(account_type, value);
                if (account.getIdentifier() == null)
                {
                    System.out.println("Found invalid account for contact " + name);
                    System.out.println("  " + type);
                    System.out.println("  " + value);
                    System.out.println("  " + cat);
                    System.out.println("  " + domain);
                }
                
                accounts.add( account );
            }
        }
        
        context.getAccountManager().addContact(name, accounts);
    }
//    
//    public void cleanUpContacts()
//    {
//        logger.log(Level.INFO, "Cleaning up contacts...");
//        logger.log(Level.INFO, "Number of contacts before merging: " + context.getAccountManager().getContacts().size());
//        context.getAccountManager().mergeCandiates();
//        logger.log(Level.INFO, "Number of contacts after merging: " + context.getAccountManager().getContacts().size());
//        logger.log(Level.INFO, "Cleaning up contacts done");
//    }

    @Override
    public void newSection(String current_section)
    {
    }

    @Override
    public void newFile(TaggedFile file)
    {
    }
    
}
