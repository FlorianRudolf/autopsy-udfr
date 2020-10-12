/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend;

import java.util.Objects;
import static java.util.Objects.hash;
import static org.juitxp.autopsy.backend.AccountTools.isValidEmailAddress;
import static org.juitxp.autopsy.backend.AccountTools.isValidPhoneNumber;
import static org.juitxp.autopsy.backend.AccountTools.normalizeEmailAddress;
import static org.juitxp.autopsy.backend.AccountTools.normalizePhoneNumber;

/**
 *
 * @author florian
 */
public class Account
{
    public enum Type
    {
        WHATSAPP("WHATSAPP"),
        PHONE("PHONE"),
        SMS("SMS"),
        IMESSAGE("IMESSAGE"),
        EMAIL("EMAIL"),
        DEVICE("DEVICE"),
        UNKNOWN("UNKNOWN");
        
        private final String typeStr;
        

        Type(String typeStr)
        {
            this.typeStr = typeStr;
        }

        public String getDisplayName()
        {
            return typeStr;
        }
        
        public static Type fromUFEDContact(String type, String category, String domain)
        {
            if (type != null)
            {
                if (type.equalsIgnoreCase("PhoneNumber"))
                {
                    return Type.PHONE;
                }
                else if (type.equalsIgnoreCase("UserID"))
                {
                    if (category != null)
                    {
                        if (category.equalsIgnoreCase("SMS"))
                        {
                            return Type.SMS;
                        }
                        else if (category.equalsIgnoreCase("iMessage"))
                        {
                            return Type.IMESSAGE;
                        }
                        else if (category.equalsIgnoreCase("WhatsApp"))
                        {
                            return Type.WHATSAPP;
                        }
                    }

                    System.out.println("Unsupported category " + category);
                }
                else if (type.equalsIgnoreCase("EmailAddress"))
                {
                    return Type.EMAIL;
                }
            }

            return Type.UNKNOWN;
        }
        
        
        Boolean isValidIdentifier(String value)
        {
            switch (this)
            {
                case WHATSAPP:
                    return AccountTools.isValidWhatsAppID(value);
                case PHONE:
                    return AccountTools.isValidPhoneNumber(value);
                case SMS:
                    return AccountTools.isValidPhoneNumber(value);
                case IMESSAGE:
                    return AccountTools.isValidPhoneNumber(value) || AccountTools.isValidEmailAddress(value);
                case EMAIL:
                    return AccountTools.isValidEmailAddress(value);
                case UNKNOWN:
                    return true;
            }
            
            return false;
        }
        
        
        String normalizeIdentifier(String value)
        {
            String normalized_identifier = null;
            switch (this)
            {
                case WHATSAPP:
                    normalized_identifier = AccountTools.normalizeWhatsAppID(value);
                    break;
                case PHONE:
                    normalized_identifier = AccountTools.normalizePhoneNumber(value);
                    break;
                case SMS:
                    normalized_identifier = AccountTools.normalizeAccountIdentifier(value);
                    break;
                case IMESSAGE:
                    normalized_identifier = AccountTools.normalizeAccountIdentifier(value);
                    break;
                case EMAIL:
                    normalized_identifier = AccountTools.normalizeEmailAddress(value);
                    break;
            }

            return (normalized_identifier != null) ? normalized_identifier : value;
        }
        
    }
    
    public class AccountTypeException extends Exception
    {
        public AccountTypeException(String message)
        {
            super(message);
        }
    }
    
    
    private final Type type;
    private final Boolean is_type_valid;
    
    private final String identifier;

    
    
    public Account(Type type, String identifier)
    {   
        this.type = type;
        this.is_type_valid = type.isValidIdentifier(identifier);
        
        this.identifier = type.normalizeIdentifier(identifier);
    }
    
    
    
    public static Account fromUFEDChat(String source, String id, String name)
    {
        if (source.equalsIgnoreCase("WhatsApp"))
        {
            return new Account(Account.Type.WHATSAPP, id);
        }
        else if (source.contains("iMessage"))
        {
            return new Account(Account.Type.IMESSAGE, id);
        }
        else
        {
            if (id == null)
            {
                if (name == null)
                    return null;
                
                return new Account(Account.Type.UNKNOWN, name);
            }
            else
            {
                if (AccountTools.isValidPhoneNumber(id))
                {
                    return new Account(Account.Type.PHONE, id);
                }
                else if (AccountTools.isValidEmailAddress(id))
                {
                    return new Account(Account.Type.EMAIL, id);
                }
                
                else if (AccountTools.isValidEmailAddress(name))
                {
                    return new Account(Account.Type.EMAIL, name);
                }
            }
        }
        
        System.out.println("Error! Cannot determine account type from chat: " + source + " (id " + id + ", name " + name + ")");
        return new Account(Account.Type.UNKNOWN, id);
    }


    public Type getType() { return type; }
    public Boolean isTypeValid() { return is_type_valid; }
    public String getIdentifier() { return identifier; }
    
    public String getBaseIdentifier()
    {
        String result = null;
        switch (type)
        {
            case PHONE:
            case SMS:
            case WHATSAPP:
                result = getPhoneNumber();
                if (result == null)
                {
                    result = getIdentifier();
                }
                break;
                
            case EMAIL:
                result = getEMail();
                break;
                
            case IMESSAGE:
                result = getPhoneNumber();
                if (result == null)
                    result = getEMail();
                break;
        }
        
        if (result == null)
            result = getIdentifier();
        
        return result;
    }
    
    public String getPhoneNumber()
    {
        if (identifier == null)
            return null;
        
        if (type == Type.PHONE)
            return identifier;
        
        if (type == Type.SMS)
            return AccountTools.isValidPhoneNumber(identifier) ? identifier : null;
        
        if (type == Type.IMESSAGE)
            return AccountTools.isValidPhoneNumber(identifier) ? identifier : null;
            
        if (type == Type.WHATSAPP)
        {
            int split_index = identifier.indexOf('@');
            if (split_index < 0)
                return null;
                
            String number = identifier.substring(0, split_index);
            if (number.charAt(0) != '+')
            {
                number = "+" + number;
            }
            return number;
        }
        
        return null;
    }
    
    public String getEMail()
    {
        return AccountTools.isValidEmailAddress(identifier) ? identifier : null;
    }
    
    public Boolean isRelated(Account other)
    {
        String this_phone = getPhoneNumber();
        String other_phone = other.getPhoneNumber();        
        if (!(this_phone == null && other_phone == null))
            return Objects.equals(this_phone, other_phone);
        
        String this_email = getEMail();
        String other_email = other.getEMail();        
        if (!(this_email == null && other_email == null))
            return Objects.equals(this_email, other_email);

        return false;
    }
    
    

    @Override
    public String toString()
    {
        String result = "[" + type + "] " + identifier;
        if (!isTypeValid())
            result += " (invalid)";
        return result;
    } 

    @Override
    public boolean equals(Object o)
    {        
        if (this == o) return true;        
        if (o == null || (!(o instanceof Account))) return false;       
        Account oa = (Account) o;
        
        return Objects.equals(identifier, oa.identifier) &&
               Objects.equals(type, oa.type);
    }

    @Override
    public int hashCode()
    {
        return hash(type, identifier);
    }    

}
