/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend.UFEDParser.Handler;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.juitxp.autopsy.backend.Account;
import org.juitxp.autopsy.backend.AccountTools;
import org.juitxp.autopsy.backend.ChatMessage;
import org.juitxp.autopsy.backend.ChatThread;
import org.juitxp.autopsy.backend.UFEDParser.DecodedDataModel;
import org.juitxp.autopsy.backend.UFEDParser.DecodedDataMultiModelField;
import org.juitxp.autopsy.backend.UFEDParser.TaggedFile;
import org.juitxp.autopsy.backend.UFEDParser.UFEDFileContext;

/**
 *
 * @author florian
 */
public class ChatHandler extends BaseHandler
{
    public ChatHandler(UFEDFileContext context, Logger logger)
    {
        super(context, logger);
    }
    
    @Override
    public void newModel(DecodedDataModel model)
    {
        if (model.getType().equals("Chat"))
        {
            newChat(model);
        }
        if (model.getType().equals("Email"))
        {
            newEMail(model);
        }
    }
    
    
    
    
    public void addEMailParticipants(DecodedDataMultiModelField field, ChatThread thread, Boolean is_owner)
    {
        if (field == null)
            return;
        
        for (DecodedDataModel models: field.getModels())
        {
            String identifier = models.getFieldValue("Identifier");
            String name = models.getFieldValue("Name");
            
            org.juitxp.autopsy.backend.Account account = null;
            if (identifier != null)
            {
                account = new org.juitxp.autopsy.backend.Account(org.juitxp.autopsy.backend.Account.Type.EMAIL, identifier);
            }
            else
            {
                if (name != null)
                {
                    if (AccountTools.isValidEmailAddress(name))
                    {
                        account = new org.juitxp.autopsy.backend.Account(org.juitxp.autopsy.backend.Account.Type.EMAIL, name);
                    }
                    else
                    {
                        account = new org.juitxp.autopsy.backend.Account(org.juitxp.autopsy.backend.Account.Type.UNKNOWN, name);
                    }
                }
            }
            
            if (account != null)
            {
                thread.addParticipant(account);
                context.getAccountManager().addChatContact(name, account, is_owner);
            }
        }
    }
    
    public void newEMail(DecodedDataModel email_model)
    {
        ChatThread thread = new ChatThread( email_model.getID() );
        
        addEMailParticipants( (DecodedDataMultiModelField) email_model.getChildren().get("From"), thread, false);
        
        org.juitxp.autopsy.backend.Account from_account = null;
        if (thread.getParticipants().size() > 0)
            from_account = thread.getParticipants().get(0);
        
        addEMailParticipants( (DecodedDataMultiModelField) email_model.getChildren().get("To"), thread, false);
        addEMailParticipants( (DecodedDataMultiModelField) email_model.getChildren().get("Cc"), thread, false);
        addEMailParticipants( (DecodedDataMultiModelField) email_model.getChildren().get("Bcc"), thread, false);
        
        String ts_str = email_model.getFieldValue("TimeStamp");
        
        ChatMessage message = new ChatMessage(from_account,
                                              email_model.getFieldValue("Subject"),
                                              email_model.getFieldValue("Body"),
                                              (ts_str != null) ? Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(ts_str)).getEpochSecond() : 0);
        
        DecodedDataMultiModelField attachments_field = (DecodedDataMultiModelField) email_model.getChildren().get("Attachments");
        if (attachments_field != null)
        {
            for (DecodedDataModel attachment_model: attachments_field.getModels())
            {
                message.addFileAttachment(attachment_model.getAttribute("file_id"));
                message.addURLAttachment(attachment_model.getAttribute("URL"));
            }
        }

        attachments_field = (DecodedDataMultiModelField) email_model.getChildren().get("Attachment");
        if (attachments_field != null)
        {
            for (DecodedDataModel attachment_model: attachments_field.getModels())
            {
                message.addFileAttachment(attachment_model.getAttribute("file_id"));
                message.addURLAttachment(attachment_model.getAttribute("URL"));
            }
        }
        

        

        thread.addMessage(message);       
        context.getAccountManager().addChatThread(thread);
    }
    
    
    
    

    public void newChat(DecodedDataModel model)
    {
        Map<String,Account> id_account_map = new HashMap<String,Account>();
        
        String chat_id = model.getFieldValue("id");
        if (chat_id == null)
            chat_id = model.getID();
        
        ChatThread thread = new ChatThread(chat_id);
        
        String source = model.getFieldValue("Source");
        
        DecodedDataMultiModelField participants_field = (DecodedDataMultiModelField) model.getChildren().get("Participants");
        for (DecodedDataModel participant: participants_field.getModels())
        {
            Boolean is_phone_owner = Boolean.parseBoolean(participant.getFieldValue("IsPhoneOwner"));
            String identifier = participant.getFieldValue("Identifier");            
            String name = participant.getFieldValue("Name");
            
            Account account = Account.fromUFEDChat(source, identifier, name);
            
            thread.addParticipant(account);
            id_account_map.put(identifier, account);
            
            context.getAccountManager().addChatContact(name, account, is_phone_owner);
        }
        
        
        
        DecodedDataMultiModelField messages_field = (DecodedDataMultiModelField) model.getChildren().get("Messages");
        for (DecodedDataModel message_model: messages_field.getModels())
        {
            String from_id = null;
            DecodedDataMultiModelField from_field = (DecodedDataMultiModelField) message_model.getChildren().get("From");
            if (!from_field.getModels().isEmpty())
            {
                DecodedDataModel from_model = from_field.getModels().get(0);
                from_id = from_model.getFieldValue("Identifier");
            }
            
            if (from_field.getModels().size() > 1)
            {
                logger.log(Level.WARNING, "Multiple from accounts for message with ID! " + message_model.getID());
            }


            String ts_str = message_model.getFieldValue("TimeStamp");
            
            ChatMessage message = new ChatMessage(id_account_map.get(from_id),
                                                  message_model.getFieldValue("Subject"),
                                                  message_model.getFieldValue("Body"),
                                                  (ts_str != null) ? Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(ts_str)).getEpochSecond() : 0);      
            
            
            DecodedDataMultiModelField attachments_field = (DecodedDataMultiModelField) message_model.getChildren().get("Attachments");
            for (DecodedDataModel attachment_model: attachments_field.getModels())
            {
                String file_id = attachment_model.getAttribute("file_id");
                if ((file_id == null) && (attachment_model.getFieldValue("attachment_extracted_path") != null))
                {
                    file_id = "attachment_" + attachment_model.getID();
                }
                
                message.addFileAttachment(file_id);
                message.addURLAttachment(attachment_model.getAttribute("URL"));
            }
            
            attachments_field = (DecodedDataMultiModelField) message_model.getChildren().get("Attachment");
            for (DecodedDataModel attachment_model: attachments_field.getModels())
            {
                String file_id = attachment_model.getAttribute("file_id");
                if ((file_id == null) && (attachment_model.getFieldValue("attachment_extracted_path") != null))
                {
                    file_id = "attachment_" + attachment_model.getID();
                }
                
                message.addFileAttachment(file_id);
                message.addURLAttachment(attachment_model.getAttribute("URL"));
            }
            
            thread.addMessage(message);
        }
        
        context.getAccountManager().addChatThread(thread);
    }

    @Override
    public void newSection(String current_section)
    {
    }

    @Override
    public void newFile(TaggedFile file)
    {
    }
    
}
