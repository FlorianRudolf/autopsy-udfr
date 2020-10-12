/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.juitxp.autopsy.backend.Account;
import org.juitxp.autopsy.backend.AccountManager;
import org.juitxp.autopsy.backend.AccountTools;
import org.juitxp.autopsy.backend.ChatMessage;
import org.juitxp.autopsy.backend.ChatThread;
import org.juitxp.autopsy.backend.Contact;
import org.juitxp.autopsy.backend.RandomTools;
import org.juitxp.autopsy.backend.UFEDParser.InstalledApplication;
import org.juitxp.autopsy.backend.UFEDParser.TaggedFile;
import org.juitxp.autopsy.backend.UFEDParser.UFEDFileContext;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.AbstractFile;
//import org.sleuthkit.datamodel.Account;
import org.sleuthkit.datamodel.AccountFileInstance;
import org.sleuthkit.datamodel.Blackboard;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE;
import org.sleuthkit.datamodel.CommunicationsManager;
import org.sleuthkit.datamodel.LocalFilesDataSource;
import org.sleuthkit.datamodel.Relationship;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;
import org.sleuthkit.datamodel.TskDataException;
import org.sleuthkit.datamodel.blackboardutils.ArtifactsHelper;
import org.sleuthkit.datamodel.blackboardutils.CommunicationArtifactsHelper;
import org.sleuthkit.datamodel.blackboardutils.attributes.MessageAttachments;

/**
 *
 * @author florian
 */
public class AutopsyUFEDFileContext extends UFEDFileContext
{
    private Map<org.sleuthkit.datamodel.Account.Type, CommunicationArtifactsHelper> communications_helpers = new HashMap<org.sleuthkit.datamodel.Account.Type, CommunicationArtifactsHelper>();
    
    private String module_name;
    private Case current_case;
    private SleuthkitCase sk_current_case;
    private LocalFilesDataSource data_source;
    private Path case_directory;
    
    AutopsyUFEDFileContext(Case current_case, LocalFilesDataSource data_source, Path unzipped_ufdr_directory, String module_name, Logger logger) throws Blackboard.BlackboardException
    {
        super(unzipped_ufdr_directory, logger);
        
        this.current_case = current_case;
        this.data_source = data_source;
        this.module_name = module_name;
        
        sk_current_case = this.current_case.getSleuthkitCase();
        
        case_directory = Paths.get(getCurrentCase().getCaseDirectory());
        
        initArtifactAndAttributeTypes();
    }
    
    public String getModuleName()
    {
        return module_name;
    }
    
    public Case getCurrentCase()
    {
        return current_case;
    }

    public SleuthkitCase getCurrentSleuthkitCase()
    {
        return sk_current_case;
    }
    
    public LocalFilesDataSource getDataSource()
    {
        return data_source;
    }
    
    public Blackboard getBlackboard()
    {
        return sk_current_case.getBlackboard();
    }
    
    public CommunicationsManager getCommunicationsManager() throws TskCoreException
    {
        return sk_current_case.getCommunicationsManager();
    }
    
    public Path getCaseDirectory()
    {
        return case_directory;
    }

    private ArtifactsHelper artifacts_helper;
    
    public ArtifactsHelper getArtifactsHelper()
    {
        if (artifacts_helper == null)
        {
            artifacts_helper = new ArtifactsHelper(getCurrentSleuthkitCase(), getModuleName(), getDataSource());
        }
        
        return artifacts_helper;
    }
    
    
    
    public org.sleuthkit.datamodel.Account.Type getOrAddAccountType(String accountTypeName, String displayName) throws TskCoreException
    {
        org.sleuthkit.datamodel.Account.Type account_type = sk_current_case.getCommunicationsManager().getAccountType(accountTypeName);
        if (account_type == null)
            account_type = sk_current_case.getCommunicationsManager().addAccountType(accountTypeName, displayName);
        return account_type;
    }
    
    public org.sleuthkit.datamodel.Account.Type getUnknownAccountType() throws TskCoreException
    {
        return getOrAddAccountType("UNKNOWN", "Unknown");
    }
    
    public org.sleuthkit.datamodel.Account.Type getAccountTypeFromString(String source) throws TskCoreException
    {
        org.sleuthkit.datamodel.Account.Type account_type = null;

        if (source != null)
        {
            if (source.equalsIgnoreCase("WhatsApp"))
            {
                return org.sleuthkit.datamodel.Account.Type.WHATSAPP;
            }
            else if (source.equalsIgnoreCase("iPhoneRecentsLog"))
            {
                return getOrAddAccountType("IPHONERECENTSLOG", "iPhone Recents Log");
            }
            else
            {
                System.out.println("Unsupported source " + source);
            }
        }

        return org.sleuthkit.datamodel.Account.Type.PHONE;
    }
    
    
    public org.sleuthkit.datamodel.Account.Type getSleuthkitAccountType(Account.Type type) throws TskCoreException
    {
        if (type == null)
            return getUnknownAccountType();
        
        switch (type)
        {
            case PHONE:
                return org.sleuthkit.datamodel.Account.Type.PHONE;
                
            case SMS:
                return org.sleuthkit.datamodel.Account.Type.MESSAGING_APP;
                
            case WHATSAPP:
                return org.sleuthkit.datamodel.Account.Type.WHATSAPP;
            
            case EMAIL:
                return org.sleuthkit.datamodel.Account.Type.EMAIL;
                
            case IMESSAGE:
                return getOrAddAccountType("IMESSAGE", "iMessage");
                
            case UNKNOWN:
                return getUnknownAccountType();
                
            default:
                return org.sleuthkit.datamodel.Account.Type.DEVICE;
        }
    }
    
    
    
    
    
    
    public CommunicationArtifactsHelper getOrAddCommunicationArtifactsHelper(org.sleuthkit.datamodel.Account.Type account_type) throws TskCoreException, TskCoreException
    {
        CommunicationArtifactsHelper communications_helper = communications_helpers.get(account_type);
        if (communications_helper == null)
        {
            communications_helper = new CommunicationArtifactsHelper(sk_current_case, module_name, data_source, account_type);
            communications_helpers.put(account_type, communications_helper);
        }
        return communications_helper;
    }
    
    public CommunicationArtifactsHelper getOrAddCommunicationArtifactsHelper(String source) throws TskCoreException, TskCoreException
    {
        return getOrAddCommunicationArtifactsHelper(getAccountTypeFromString(source));
    }
    
    
    
    BlackboardAttribute makeBlackboardAttribute(BlackboardAttribute.Type attribute_type, String value)
    {
        if ((value != null) && (!value.isEmpty()))
        {
            return new BlackboardAttribute(attribute_type, getModuleName(), value);
        }
        
        return null;
    }
    
    BlackboardAttribute makeBlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE attribute_type, String value)
    {
        return makeBlackboardAttribute(new BlackboardAttribute.Type(attribute_type), value);
    }
    
    
    void addAttributeIfNotNull(BlackboardAttribute.Type attribute_type, String value, Collection<BlackboardAttribute> attributes)
    {
        BlackboardAttribute attr = makeBlackboardAttribute(attribute_type, value);
        if (attr != null)
        {
            attributes.add(attr);
        }
    }
    
    void addAttributeIfNotNull(BlackboardAttribute.ATTRIBUTE_TYPE attribute_type, String value, Collection<BlackboardAttribute> attributes)
    {
        addAttributeIfNotNull(new BlackboardAttribute.Type(attribute_type), value, attributes);
    }
       
    
    AccountFileInstance self_device_account_file_instance = null;
    
    AccountFileInstance createSelfDeviceAccountFileInstance() throws TskCoreException
    {
        if (self_device_account_file_instance == null)
        {
            self_device_account_file_instance = getCommunicationsManager().createAccountFileInstance(org.sleuthkit.datamodel.Account.Type.DEVICE, "Device Owner",
                                                                                                     getModuleName(), getDataSource());
        }
        
        return self_device_account_file_instance;
    }
    
    private Map<org.juitxp.autopsy.backend.Account, AccountFileInstance> account_file_instance_map = new HashMap<org.juitxp.autopsy.backend.Account, AccountFileInstance>();
    
    AccountFileInstance createAccountFileInstance(org.juitxp.autopsy.backend.Account account)
    {
        if (account == null)
            return null;
        
        AccountFileInstance account_file_instance = account_file_instance_map.get(account);
        if (account_file_instance == null)
        {
            try
            {
                org.sleuthkit.datamodel.Account.Type sk_type = getSleuthkitAccountType(account.getType());

                if (sk_type == org.sleuthkit.datamodel.Account.Type.PHONE && !AccountTools.isValidPhoneNumber(account.getIdentifier()))
                    sk_type = getUnknownAccountType();
                if (sk_type == org.sleuthkit.datamodel.Account.Type.EMAIL && !AccountTools.isValidEmailAddress(account.getIdentifier()))
                    sk_type = getUnknownAccountType();

                account_file_instance = getCommunicationsManager().createAccountFileInstance(sk_type, account.getIdentifier(),
                                                                                             getModuleName(), getDataSource());
            }
            catch (TskCoreException ex)
            {
                return null;
            }
            
            account_file_instance_map.put(account, account_file_instance);
        }
        
        return account_file_instance;
    }
    
    
    
    
    void initArtifactAndAttributeTypes() throws Blackboard.BlackboardException
    {
        installed_application_guid = getBlackboard().getOrAddAttributeType("JUITXP_APP_GUID", BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, "Application GUID");
        installed_application_identifier = getBlackboard().getOrAddAttributeType("JUITXP_APP_IDENTIFIER", BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, "Application Identifier");
    }
    
    
    
    
    public void addFilesToAutopsy()
    {
        path_content_map.put(Paths.get("/"), getDataSource());
        for (TaggedFile file: getFiles())
        {
            addFileToAutopsy(file);
        }
    }
    
    private HashMap<Path, AbstractFile> path_content_map = new HashMap<Path, AbstractFile>();
    private HashMap<String, AbstractFile> fileid_content_map = new HashMap<String, AbstractFile>();
    
    private AbstractFile addFileToAutopsy(TaggedFile file)
    {
        try
        {
            Path ufed_path;
            if (file.mobile_path.isAbsolute())
            {
                ufed_path = Paths.get( "/" + file.fs + file.mobile_path );
            }
            else
            {
                ufed_path = Paths.get( "/" + file.fs).resolve(file.mobile_path);
            }

            AbstractFile new_file = getOrMakeContent(file.id, ufed_path, file.local_path, true, file.size, file.crtime, file.mtime);
            
            fileid_content_map.put(file.id, new_file);
            
            return new_file;
        }
        catch (TskCoreException ex)
        {
            logger.log(Level.SEVERE, "Failed adding new file with id " + file.id, ex);
            logger.log(Level.SEVERE, file.toString(), ex);
        }
        
        return null;
    }
    
    private AbstractFile getOrMakeContent(String file_id, Path ufed_path, Path local_path, Boolean is_file, long size, long crtime, long mtime) throws TskCoreException
    {   
        AbstractFile content = path_content_map.get(ufed_path);
        if (content == null)
        {
            Path parent_path = ufed_path.getParent();
            Path filename = ufed_path.getFileName();

            AbstractFile parent = (parent_path == null) ? getDataSource() : getOrMakeContent(null, parent_path, null, false, 0, 0, 0);
            
            if (local_path != null)
            {
                local_path = this.unzipped_ufdr_directory.resolve(local_path);
            }
            
            if (file_id == null)
            {
                file_id = "GENERATED_" + RandomTools.getRandomString(16, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
            }
            
            AbstractFile file;
            if (is_file)
            {
                file = getCurrentSleuthkitCase().addLocalFile(filename.toString(), local_path.toString(), size, 0, crtime, 0, mtime, true, TskData.EncodingType.NONE, parent);
            }
            else
            {
                file = getCurrentSleuthkitCase().addLocalDirectory(parent.getId(), filename.toString());
            }
            
            path_content_map.put(ufed_path, file);
            return file;
        }
        else
        {
            return content;
        }
    }
    
    
    
    
    
    
    private BlackboardAttribute.Type installed_application_guid;
    private BlackboardAttribute.Type installed_application_identifier;

    
    public void addInstalledApplicationsToAutopsy()
    {
        for (InstalledApplication app: getInstalledAppliactions())
        {
            try
            {
                List<BlackboardAttribute> attributes = new ArrayList<BlackboardAttribute>();
                
                addAttributeIfNotNull(installed_application_identifier, app.identifier, attributes);
                addAttributeIfNotNull(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_VERSION, app.version, attributes);
                addAttributeIfNotNull(installed_application_guid, app.guid, attributes);

                getArtifactsHelper().addInstalledProgram(app.name, 0, attributes);
            }
            catch (TskCoreException ex)
            {
                logger.log(Level.SEVERE, "Failed adding new installed programm " + app.name, ex);
            }
            catch (Blackboard.BlackboardException ex)
            {
                logger.log(Level.SEVERE, "Failed adding new installed programm " + app.name, ex);
            }
        }
    }
    
    
    
    
    private ATTRIBUTE_TYPE getBlackboardAttributeTypeFromAccountType(org.juitxp.autopsy.backend.Account.Type type)
    {
        switch (type)
        {
            case PHONE:
            case SMS:
                return ATTRIBUTE_TYPE.TSK_PHONE_NUMBER;
            case WHATSAPP:
            case IMESSAGE:
                return ATTRIBUTE_TYPE.TSK_ID;
            case EMAIL:
                return ATTRIBUTE_TYPE.TSK_EMAIL;
        }
        
        return null;
    }
    
    public void addContactsToAutopsy() throws TskCoreException, TskDataException, Blackboard.BlackboardException
    {
        logger.log(Level.INFO, "Adding contacts...");
        
        AccountFileInstance self_device_account_file_instance = createSelfDeviceAccountFileInstance();
        
        for (Contact contact: getAccountManager().getContacts())
        {
            List<org.juitxp.autopsy.backend.Account> valid_accounts = new ArrayList<org.juitxp.autopsy.backend.Account>();
            for (org.juitxp.autopsy.backend.Account account: contact.getAccounts())
            {
                if (getAccountManager().getContactsForAccount(account).size() == 1)
                {
                    valid_accounts.add(account);
                }
                else
                {
                    logger.log(Level.INFO, "Skipping account " + account + " for contact " + contact + " because of multiple occurances");
                }
            }
            
            BlackboardArtifact contactArtifact = getDataSource().newArtifact(ARTIFACT_TYPE.TSK_CONTACT);
            List<BlackboardAttribute> attributes = new ArrayList<BlackboardAttribute>();
            
            attributes.add( new BlackboardAttribute(ATTRIBUTE_TYPE.TSK_NAME, getModuleName(), contact.getNameString()) );
            
            for (org.juitxp.autopsy.backend.Account account: valid_accounts)
            {
                ATTRIBUTE_TYPE at = getBlackboardAttributeTypeFromAccountType(account.getType());
                
                AccountFileInstance account_file_instance = createAccountFileInstance(account);
                
                getCommunicationsManager().addRelationships(self_device_account_file_instance, Collections.singletonList(account_file_instance), contactArtifact, Relationship.Type.CONTACT, 0);
                
                ATTRIBUTE_TYPE attr_type = getBlackboardAttributeTypeFromAccountType(account.getType());
                if (attr_type != null)
                    attributes.add( new BlackboardAttribute(attr_type, getModuleName(), account.getIdentifier()) );
            }
            
            contactArtifact.addAttributes(attributes);
            
            getBlackboard().postArtifact(contactArtifact, getModuleName());
        }
        
        logger.log(Level.INFO, "Adding contacts done");

    }
    
    
    
    public void addChatsToAutopsy() throws TskCoreException, Blackboard.BlackboardException, TskDataException
    {
        logger.log(Level.INFO, "Adding messages...");
        
        AccountFileInstance self_device_account_file_instance = createSelfDeviceAccountFileInstance();
        
//        List<Contact> owner_device_contacts = getAccountManager().getDeviceOwnerContacts();
        
        for (ChatThread thread: getAccountManager().getChatThreads())
        {
            AccountFileInstance self_account_file_instance = self_device_account_file_instance;
            List<Account> device_owner_accounts = getAccountManager().getDeviceOwnerAccounts( thread.getAccountType() );
            if (device_owner_accounts.size() == 1)
            {
                self_account_file_instance = createAccountFileInstance( device_owner_accounts.get(0) );
            }

//            if (self_account_file_instance == null)
//            {
//                logger.log(Level.SEVERE, "Cannot create account file instance for account " + thread.getSelfAccount());
//                logger.log(Level.SEVERE, thread.toString());
//                continue;
//            }
                        
            CommunicationArtifactsHelper communications_helper;
            try
            {
                communications_helper = new CommunicationArtifactsHelper(getCurrentSleuthkitCase(),
                                                                         getModuleName(),
                                                                         getDataSource(),
                                                                         getSleuthkitAccountType(thread.getAccountType()),
                                                                         self_account_file_instance.getAccount().getAccountType(),
                                                                         self_account_file_instance.getAccount().getTypeSpecificID());
            }
            catch (TskCoreException ex)
            {
                logger.log(Level.SEVERE, "Error creating communication artifacts helper", ex);
                logger.log(Level.SEVERE, thread.toString());
                continue;
            }
            
            
            for (ChatMessage message: thread.getMessages())
            {
                AccountFileInstance from_account_file_instance = createAccountFileInstance( message.getFromAccount() );

                String from_id = null;
                if (from_account_file_instance != null)
                    from_id = from_account_file_instance.getAccount().getTypeSpecificID();
                
                List<String> recipient_ids = new ArrayList<String>();
                for (org.juitxp.autopsy.backend.Account participant: thread.getParticipants())
                {
                    if (participant != message.getFromAccount())
                    {
                        AccountFileInstance participant_acocunt_file_instance = createAccountFileInstance( participant );
                        
                        if (participant_acocunt_file_instance != null)
                        {
                            recipient_ids.add( participant_acocunt_file_instance.getAccount().getTypeSpecificID() );
                        }
                        else
                        {
                            logger.log(Level.SEVERE, "Cannot create account file instance for account " + participant + ", skipping");
                            logger.log(Level.SEVERE, message.toString());
                        }
                    }
                }
                
                CommunicationArtifactsHelper.CommunicationDirection comm_dir;
                switch (message.getDirection())
                {
                    case OUTGOING:
                        comm_dir = CommunicationArtifactsHelper.CommunicationDirection.OUTGOING;
                        break;
                    case INCOMING:
                        comm_dir = CommunicationArtifactsHelper.CommunicationDirection.INCOMING;
                        break;
                    default:
                        comm_dir = CommunicationArtifactsHelper.CommunicationDirection.UNKNOWN;
                }
                
                BlackboardArtifact sk_msg = null;
                try
                {
                    sk_msg = communications_helper.addMessage("UFDR DSP", comm_dir, from_id, recipient_ids, message.getTimestamp(), CommunicationArtifactsHelper.MessageReadStatus.UNKNOWN, message.getSubject(), message.getBody(), thread.getID());
                }
                catch (TskCoreException ex)
                {
                    logger.log(Level.SEVERE, "Error creating message", ex);
                    logger.log(Level.SEVERE, message.toString());
                    continue;
                }
                
                List<MessageAttachments.FileAttachment> file_attachments = new ArrayList<MessageAttachments.FileAttachment>();
                for (String file_id: message.getFileAttachments())
                {
                    AbstractFile attachment_file = fileid_content_map.get(file_id);
                    if (attachment_file != null)
                    {
                        file_attachments.add( new MessageAttachments.FileAttachment(attachment_file) );
                    }
                    else
                    {
                        logger.log(Level.WARNING, "Attachment file id " + file_id  + " not found");
                    }
                }

                List<MessageAttachments.URLAttachment> url_attachments = new ArrayList<MessageAttachments.URLAttachment>();
                for (String url: message.getURLAttachments())
                {
                    url_attachments.add( new MessageAttachments.URLAttachment(url) );
                }

                if ((!file_attachments.isEmpty()) || (!url_attachments.isEmpty()))
                {
                    MessageAttachments message_attachments = new MessageAttachments(file_attachments, url_attachments);
                    communications_helper.addAttachments(sk_msg, message_attachments);
                }
            }
        }
     
        
        logger.log(Level.INFO, "Adding messages done");
    }
    
    
}
