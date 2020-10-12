/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.validator.routines.EmailValidator;

/*
 * @author florian
 */
public class AccountTools
{
    public static Boolean isValidPhoneNumber(String value, String defaultRegion)
    {
        if ((value == null) || value.isEmpty())
            return false;
        try
        {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            phoneUtil.parse(value, "AT");
            return true;
        }
        catch (NumberParseException ex)
        {
            return false;
        }
    }
    
    public static Boolean isValidPhoneNumber(String value)
    {
        return isValidPhoneNumber(value, "AT");
    }
    
    public static String normalizePhoneNumber(String value, String defaultRegion)
    {
        try
        {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            PhoneNumber phone_number = phoneUtil.parse(value, defaultRegion);
            return phoneUtil.format(phone_number, PhoneNumberUtil.PhoneNumberFormat.E164);
        }
        catch (NumberParseException ex)
        {
            return value;
        }
    }
    
    public static String normalizePhoneNumber(String value)
    {
        return normalizePhoneNumber(value, "AT");
    }
    
    
    
    
    public static boolean isValidEmailAddress(String value)
    {
        if ((value == null) || value.isEmpty())
            return false;
        
        EmailValidator validator = EmailValidator.getInstance(true, true);
        return validator.isValid(value);
        
//        try
//        {
//           InternetAddress emailAddr = new InternetAddress(value);
//           emailAddr.validate();
//           return true;
//        }
//        catch (AddressException ex)
//        {
//           return false;
//        }
    }
    
    public static boolean isValidWhatsAppID(String value)
    {
        try
        {
           InternetAddress emailAddr = new InternetAddress(value.replaceAll("[^\\w\\.@\\+]", "").toLowerCase());
           emailAddr.validate();
           return true;
        }
        catch (AddressException ex)
        {
           return false;
        }
    }
    
    
    
    public static String normalizeEmailAddress(String value)
    {
        return value.toLowerCase();
    }
    
    
    
    public static String normalizeWhatsAppID(String value)
    {
        return value.replaceAll("[^\\w\\.@\\+]", "").toLowerCase();
    }
    
    
    
    
    
    public static String normalizeAccountIdentifier(String identifier)
    {
        if (isValidPhoneNumber(identifier))
        {
            return normalizePhoneNumber(identifier);
        }
        else if (isValidEmailAddress(identifier))
        {
            return normalizeEmailAddress(identifier);
        }
        
        return null;
    }
    
    
    
    
    
    
    
    
    static String[] tokens_to_ignore_array = {"ing", "dr", "dipl", "fh", "techn"};
    static List<String> tokens_to_delete = Arrays.asList( tokens_to_ignore_array );
    
    static List<String> getNameTokens(String name)
    {        
        List<String> tokens = Arrays.asList(name.split(" |/"));
        List<String> results = new ArrayList<String>();
        
        for (int i = 0; i != tokens.size(); ++i)
        {
            String token = tokens.get(i).toLowerCase();
            
            token = token.replaceAll("ö", "oe");
            token = token.replaceAll("ä", "ae");
            token = token.replaceAll("ü", "ue");
            token = token.replaceAll("ß", "sz");
            token = token.replaceAll("[\\W]|\\.", "");
            
            if (!(tokens_to_delete.contains(token)))
                results.add(token);
        }
        
        return results;
    }
    
    static Boolean isNameSubsetOf(String child, String parent)
    {
        List<String> child_tokens = getNameTokens(child);
        List<String> parent_tokens = getNameTokens(parent);
        
        return parent_tokens.containsAll(child_tokens);
    }
    
    static float nameShareScore(String name1, String name2)
    {
        if (name1.equals(name2))
            return 1.0f;
        
        List<String> name1_tokens = getNameTokens(name1);
        List<String> name2_tokens = getNameTokens(name2);
        
        int name1_count = 0;
        int name2_count = 0;
        
        for (String name1_token: name1_tokens)
            name1_count += name1_token.length();

        for (String name2_token: name2_tokens)
            name2_count += name2_token.length();
            
        
        int shared_count = 0;
        for (String token: name1_tokens)
        {
            if (name2_tokens.contains(token))
                shared_count += token.length();
        }
        
        if (name1_count + name2_count == 2*shared_count)
            return 1.0f;

        return Float.max((float)shared_count / (float)(name1_count), (float)shared_count / (float)(name2_count));
    }
    
    static Boolean areNamesEqual(String name1, String name2)
    {
        return nameShareScore(name1, name2) == 1.0f;
    }
    
}
