/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author florian
 */
public class ChatMessage
{
    public enum Direction
    {
        INCOMING("INCOMING"),
        OUTGOING("OUTGOING"),
        UNKNOWN("UNKNOWN");

        private final String typeStr;

        Direction(String typeStr)
        {
            this.typeStr = typeStr;
        }

        public String getDisplayName()
        {
            return typeStr;
        }
    }
    
    Account from_account;
    public Account getFromAccount() { return from_account; }
    
    String subject;
    public String getSubject() { return subject; }
    
    String body;
    public String getBody() { return body; }
    
    Direction direction = Direction.UNKNOWN;
    public Direction getDirection() { return direction; }
    public void setIncoming()
    {
        direction = Direction.INCOMING;
    }
    public void setOutgoing()
    {
        direction = Direction.OUTGOING;
    }
    
    
    
    long timestamp = 0;
    public long getTimestamp() { return timestamp; }
    
    List<String> attachment_file_ids = new ArrayList<String>();
    List<String> attachment_urls = new ArrayList<String>();
    
    
    public Boolean isNonEmptyMessage()
    {
        return ((subject != null) && (!subject.isEmpty())) ||
               ((body != null) && (!body.isEmpty())) ||
               (timestamp > 0) ||
               (!attachment_file_ids.isEmpty()) ||
               (!attachment_urls.isEmpty());
    }
    
    
    
    public void addFileAttachment(String file_id)
    {
        if ((file_id != null) && (!file_id.isEmpty()))
            attachment_file_ids.add(file_id);
    }
    
    public void addURLAttachment(String url)
    {
        if ((url != null) && (!url.isEmpty()))
            attachment_urls.add(url);
    }
    
    public List<String> getFileAttachments() { return attachment_file_ids; }
    public List<String> getURLAttachments() { return attachment_urls; }
    
    
    
    public ChatMessage(Account from_account, String subject, String body, long timestamp)
    {
        this.from_account = from_account;
        this.subject = subject;
        this.body = body;
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString()
    {
        if (isNonEmptyMessage())
        {
            String result = "[ChatMessage (from=" + from_account + ")]";
            if ((subject != null) && (!subject.isEmpty()))
                result += "\n    subject: " + subject;
//            if ((body != null) && (!body.isEmpty()))
//                result += "\n    body: " + body;
            if (direction != null)
                result += "\n    direction: " + direction;
            if (timestamp > 0)
                result += "\n    timestamp: " + timestamp;
            if (!attachment_file_ids.isEmpty())
                result += "\n    file attachments: " + attachment_file_ids;
            if (!attachment_urls.isEmpty())
                result += "\n    url attachments: " + attachment_urls;
            
            return result;
        }
        else
        {
            return "[ChatMessage (from=" + from_account + ")] EMPTY";
        }
    }
    
}

