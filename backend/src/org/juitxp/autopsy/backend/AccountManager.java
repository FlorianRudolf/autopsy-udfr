/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author florian
 */
public class AccountManager
{    
    private List<Contact> contacts = new ArrayList<Contact>();
    private Map<Account, List<Contact>> account_contact_map = null;
    private Map<String, List<Contact>> base_identifier_contact_map = null;
    
//    private List<Contact> self_contacts = new ArrayList<Contact>();
    

    
    public List<Contact> getDeviceOwnerContacts()
    {
        List<Contact> device_owner_contacts = new ArrayList<Contact>();
        for (Contact contact: contacts)
        {
            if (contact.isDeviceOwner())
            {
                device_owner_contacts.add(contact);
            }
        }
        
        return device_owner_contacts;
    }
    
    public List<Account> getDeviceOwnerAccounts(Account.Type account_type)
    {
        List<Account> accounts = new ArrayList<Account>();
        
        for (Contact device_owner_contacts: getDeviceOwnerContacts())
        {
            for (Account account: device_owner_contacts.getAccounts())
            {
                if (account.getType().equals(account_type))
                {
                    accounts.add(account);
                }
            }
        }
        
        return accounts;
    }
    
//    public void addSelfContact(Contact contact)
//    {
//        for (Contact sc: self_contacts)
//        {
//            if (contact == sc)
//            {
//                return;
//            }
//            
//            if (sc.canBeMerged(contact))
//            {
//                sc.merge(contact);
//                return;
//            }
//        }
//        
//        self_contacts.add(contact);
//    }
    
//    public Boolean isSelfContact(Contact contact)
//    {
//        for (Contact sc: self_contacts)
//        {
//            if (contact == sc)
//                return true;
//        }
//            
//        return false;
//    }
    
    public Boolean isAccountOfDeviceOwner(Account account)
    {
        List<Contact> contacts_for_account = getContactsForAccount(account);
        
        if (contacts_for_account != null)
        {
            for (Contact contact: contacts_for_account)
            {
                if (contact.isDeviceOwner())
                    return true;
            }
        }
            
        return false;
    }
    
    public Map<Account, List<Contact>> getAccountContactMap()
    {
        lazyCheckMakeInternals();
        return account_contact_map;
    }
    
    public Map<String, List<Contact>> getBaseIdentifierContactMap()
    {
        lazyCheckMakeInternals();
        return base_identifier_contact_map;
    }
    
    public List<Contact> getContacts()
    {
        return contacts;
    }
    
    
    public Contact addContact(Contact contact)
    {
        contacts.add(contact);
        invalidateInternals();
        
        return contact;
    }
    
    public Contact addContact(String name, Collection<Account> accounts)
    {
        return addContact(new Contact(name, accounts));
    }
    

    
    public void mergeCandiates()
    {
        Boolean finished = false;
        while (!finished)
        {
            finished = true;
            for (int i = 0; i < contacts.size(); ++i)
            {
                for (int j = i+1; j < contacts.size();)
                {
                    Contact c1 = contacts.get(i);
                    Contact c2 = contacts.get(j);

                    if (c2.canBeMerged(c1))
                    {
                        c1.merge(c2);
                        contacts.remove(j);
                        finished = false;
                    }
                    else
                        ++j;
                }
            }
        }
        
        invalidateInternals();
    }
    
    
    
    
    private void invalidateInternals()
    {
        account_contact_map = null;
    }
    
    
    private void lazyCheckMakeInternals()
    {
        if (account_contact_map == null)
        {
            account_contact_map = new HashMap<Account, List<Contact>>();
            
            for (Contact contact: contacts)
            {
                for (Account account: contact.getAccounts())
                {
                    List<Contact> account_contacts = account_contact_map.get(account);
                    if (account_contacts == null)
                    {
                        account_contacts = new ArrayList<Contact>();
                        account_contact_map.put(account, account_contacts);
                    }

                    account_contacts.add(contact);
                }
            }
        }
        
        if (base_identifier_contact_map == null)
        {
            base_identifier_contact_map = new HashMap<String, List<Contact>>();
            
            for (Contact contact: contacts)
            {
                for (String bi: contact.getBaseIdentifiers())
                {
                    List<Contact> account_contacts = base_identifier_contact_map.get(bi);
                    if (account_contacts == null)
                    {
                        account_contacts = new ArrayList<Contact>();
                        base_identifier_contact_map.put(bi, account_contacts);
                    }

                    Boolean found = false;
                    for (Contact ac: account_contacts)
                    {
                        if (contact == ac)
                            found = true;
                    }
                    
                    if (!found)
                        account_contacts.add(contact);
                }
            }
        }
    }
    
    public List<Contact> getContactsForAccount(Account account)
    {
        return getAccountContactMap().get(account);
    }
    
    public List<Contact> getContactsForBaseIdentifier(String base_identifier)
    {
        return getBaseIdentifierContactMap().get(base_identifier);
    }
       
    
    List<ChatThread> chat_threads = new ArrayList<ChatThread>();
    
    public void addChatThread(ChatThread thread)
    {
        // determining message direction
        for (ChatMessage message: thread.getMessages())
        {
            if ((message.getDirection() == null) || (message.getDirection() == ChatMessage.Direction.UNKNOWN))
            {
                if (isAccountOfDeviceOwner( message.getFromAccount() ))
                {
                    message.setOutgoing();
                }
                else
                {
                    for (Account participant: thread.getParticipants())
                    {
                        if (isAccountOfDeviceOwner(participant))
                        {
                            message.setIncoming();
                            break;
                        }
                    }
                }
            }
        }
        
        chat_threads.add(thread);
    }
    
    public List<ChatThread> getChatThreads()
    {
        return chat_threads;
    }
    
    public void addChatContact(String name, Account account, Boolean is_phone_owner)
    {
        List<Contact> contacts = getContactsForAccount(account);
        if (contacts == null)
        {
            String bi = account.getBaseIdentifier();
            contacts = getContactsForBaseIdentifier( bi );
        }

        if (contacts != null)
        {
            for (Contact contact: contacts)
            {
                if (is_phone_owner)
                {
                    contact.setDeviceOwner();
//                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!! Something is broken");
                }
                else
                {                    
                    contact.addName(name);
                }
            }
        }
        else
        {
            Contact contact = new Contact(name, account);
            if (is_phone_owner)
            {
                contact.setDeviceOwner();            
            }
            addContact(contact);
        }
    }
    
}
