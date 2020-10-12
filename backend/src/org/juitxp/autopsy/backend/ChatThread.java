/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author florian
 */
public class ChatThread
{
    String id;
    
    Account.Type account_type = null;
    
    List<Account> participants = new ArrayList<Account>();
    
    List<ChatMessage> messages = new ArrayList<ChatMessage>();
    
    public ChatThread(String id)
    {
        this.id = id;
    }
    
    public void addParticipant(Account account)
    {
        if ((account != null) && (!participants.contains(account)))
        {
            participants.add(account);
            
            if (account_type == null)
            {
                account_type = account.getType();
            }
            else if ((account_type != account.getType()) || (!account.isTypeValid()))
            {
                account_type = Account.Type.UNKNOWN;
            }
        }
    }
    

    public String getID()
    {
        return id;
    }
    
    public Account.Type getAccountType()
    {
        return account_type;
    }
    
    public List<Account> getParticipants()
    {
        return participants;
    }
    
    public List<ChatMessage> getMessages()
    {
        return messages;
    }
    
    
    public void addMessage(ChatMessage message)
    {
//        if (message.direction == null)
//        {
//            if ((message.from_account != null) && (self_account == message.getFromAccount()))
//            {
//                message.direction = ChatMessage.Direction.OUTGOING;
//            }
//            else if (participants.contains(message.from_account))
//            {
//                message.direction = ChatMessage.Direction.INCOMING;
//            }
//            else
//            {
//                message.direction = ChatMessage.Direction.UNKNOWN;
//            }
//        }
        
        addParticipant(message.getFromAccount());
        
        messages.add(message);
    }
    
    public Boolean isNonEmptyChat()
    {
        for (ChatMessage message: messages)
        {
            if (message.isNonEmptyMessage())
                return true;
        }
        
        return false;
    }
    
    
    @Override
    public String toString()
    {
        String result = "[ChatThread (id=" + id + "]\n" +
                        "  participants: " +  participants + "\n";
            //        + "  self account: " + self_account;
        
        for (ChatMessage message: messages)
        {
            result += "\n  " + message;
                    
        }
        
        return result;
    } 

}
