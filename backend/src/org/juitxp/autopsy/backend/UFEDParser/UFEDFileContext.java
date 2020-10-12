/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend.UFEDParser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.juitxp.autopsy.backend.AccountManager;

/**
 *
 * @author florian
 */
public class UFEDFileContext
{
    protected Logger logger;
    protected Path unzipped_ufdr_directory;
            
    
    public UFEDFileContext(Path unzipped_ufdr_directory, Logger logger)
    {
        this.logger = logger;
        this.unzipped_ufdr_directory = unzipped_ufdr_directory;
    }
    
    
    private AccountManager account_manager = new AccountManager();
    
    public AccountManager getAccountManager()
    {
        return account_manager;
    }
    
    
    private List<TaggedFile> files = new ArrayList<TaggedFile>();
    private HashMap<String, TaggedFile> id_file_map = new HashMap<String, TaggedFile>();
    private HashMap<Path, TaggedFile> path_file_map = new HashMap<Path, TaggedFile>();

    
    public TaggedFile getFileByID(String id)
    {
        return id_file_map.get(id);
    }
    
    public TaggedFile getFileByPath(Path path)
    {
        return path_file_map.get(path);
    }
    
    public List<TaggedFile> getFiles()
    {
        return files;
    }
    

    
    
    public void addFile(TaggedFile tagged_file)
    {        
        if (!tagged_file.local_path.isAbsolute())
        {
            tagged_file.local_path = unzipped_ufdr_directory.resolve(tagged_file.local_path);
        }
        
        if (tagged_file.size <= 0)
        {
            tagged_file.size = tagged_file.local_path.toFile().length();
        }
        
        if (getFileByID(tagged_file.id) != null)
        {
            logger.log(Level.SEVERE, "Error, file already present (same id)");
            logger.log(Level.SEVERE, getFileByID(tagged_file.id).toString());
            tagged_file.print();
        }
        else
        {
            id_file_map.put(tagged_file.id, tagged_file);
        }
        
        if (getFileByPath(tagged_file.mobile_path) != null)
        {
            logger.log(Level.SEVERE, "Error, file already present (same path)");
            logger.log(Level.SEVERE, getFileByID(tagged_file.id).toString());
            tagged_file.print();
        }
        else
        {
            path_file_map.put(tagged_file.mobile_path, tagged_file);
        }
        
        files.add(tagged_file);
    }
    
    
    
    private List<InstalledApplication> installed_applications = new ArrayList<InstalledApplication>();
    
    public void addInstalledApplication(InstalledApplication app)
    {
        installed_applications.add(app);
    }
    
    public List<InstalledApplication> getInstalledAppliactions()
    {
        return installed_applications;
    }
}
