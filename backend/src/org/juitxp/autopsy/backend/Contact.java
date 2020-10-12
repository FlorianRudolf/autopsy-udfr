/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author florian
 */
public class Contact
{
    private Boolean is_device_owner = false;
    private List<String> names = new ArrayList<String>();
    private List<Account> accounts = new ArrayList<Account>();
    private List<String> base_identifiers = new ArrayList<String>();
    
    

    public Contact()
    {}
    
    public Contact(String name, Collection<Account> accounts)
    {
        addName(name);
        for (Account account: accounts)
        {
            addAccount(account);
        }
    }
    
    public Contact(String name, Account account)
    {
        addName(name);
        addAccount(account);
    }
    
    
    public void setDeviceOwner()
    {
        is_device_owner = true;
    }
    
    public Boolean isDeviceOwner()
    {
        return is_device_owner;
    }
    

    public List<String> getNames()
    {
        return names;
    }

    public void addName(String name)
    {
        if (name == null || name.isEmpty())
            return;

        for (int i = 0; i != names.size(); ++i)
        {
            String old_name = names.get(i);
            
            if ( AccountTools.isNameSubsetOf(name, old_name) )
                return;
            if ( AccountTools.isNameSubsetOf(old_name, name) )
            {
                names.set(i, name);
                return;
            }
        }
        
        if (!names.contains(name))
            names.add(name);
    }
    
    public String getNameString()
    {
        if (names.isEmpty())
            return "";
        
        if (names.size() == 1)
            return names.get(0);
        
        return names.toString();
    }
    

    public List<Account> getAccounts()
    {
        return accounts;
    }

    public void addAccount(Account account)
    {
        if (!accounts.contains(account))
        {
            accounts.add(account);
            addBaseIdentifier(account);
        }
    }
    
    public Boolean hasAccount(Account account_to_check)
    {
        for (Account account: accounts)
        {
            if (account.equals(account_to_check))
            {
                return true;
            }
        }
        return false;
    }
    
    public List<String> getBaseIdentifiers()
    {
        return base_identifiers;
    }
    
    private void addBaseIdentifier(Account account)
    {
        String bi = account.getBaseIdentifier();
        if (!base_identifiers.contains(bi))
            base_identifiers.add(bi);
    }
    
    
    public void merge(Contact other)
    {
        for (String name: other.getNames())
            addName(name);
        
        for (Account account: other.getAccounts())
            addAccount(account);
        
        if (other.isDeviceOwner())
        {
            setDeviceOwner();
        }
    }
    
    
    public float bestNameShareScore(Contact other)
    {
        float best = -1.0f;
        
        for (String name: getNames())
        {
            for (String other_name: other.getNames())
            {
                float score = AccountTools.nameShareScore(name, other_name);
                
                if (best < 0.0f || score > best)
                {
                    best = score;
                }
            }
        }

        return best;
    }
    
    
    
    public Boolean canBeMerged(Contact other)
    {
        Boolean result = true;
        
        if (accounts.isEmpty())
            return false;
        
        float best_name_share_score = bestNameShareScore(other);
        
        Boolean name_match = getNames().isEmpty() || other.getNames().isEmpty() || (best_name_share_score == 1.0f);        
        List<String> common_base_identifier = getSharedBaseIdentifier(other);

        if (!common_base_identifier.isEmpty())
        {
            if ((best_name_share_score > 0.5f) && getBaseIdentifiers().containsAll(common_base_identifier) && other.getBaseIdentifiers().containsAll(common_base_identifier))
            {
                // Contacts have all the same base identifier -> merging
                return true;
            }

            if ( (((getNames().isEmpty()) || (best_name_share_score > 0.5f)) && (getBaseIdentifiers().containsAll(common_base_identifier))) ||
                 (((other.getNames().isEmpty()) || (best_name_share_score > 0.5f)) && (other.getBaseIdentifiers().containsAll(common_base_identifier))) )
            {
                // shared BaseIdentifiers are included in this or other AND there is a name shareing
                return true;
            }
        }   

        return false;
    }
    
    List<Account> getSharedAccounts(Contact other)
    {
        List<Account> result = new ArrayList<Account>();
        
        for (Account account: accounts)
        {
            if (other.getAccounts().contains(account))
                result.add(account);
        }
        
        return result;
    }
    
    
    List<String> getSharedBaseIdentifier(Contact other)
    {
        List<String> result = new ArrayList<String>();
        
        for (String bi: base_identifiers)
        {
            if (other.getBaseIdentifiers().contains(bi))
                result.add(bi);
        }
        
        return result;
    }
    
    
    
    @Override
    public String toString()
    {
        String result = new String();
        result += "Contact\n";
        result += "  names: " + names + "\n";
        result += "  base identifiers: " + base_identifiers + "\n";
        result += "  account: " + accounts + "\n";
        return result;
    }

}
