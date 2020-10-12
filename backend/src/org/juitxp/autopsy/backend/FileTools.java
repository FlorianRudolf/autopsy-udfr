package org.juitxp.autopsy.backend;


import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author florian
 */
public class FileTools
{
    
    public static Path getRandomFilename(Path directory, int length)
    {
        Path result = null;
        while ((result == null) || (Files.exists(result)))
        {
            String filename = RandomTools.getRandomString(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
            result = directory.resolve(filename);
        }
        
        return result;
    }
    
    public static Path getRandomFilename(Path directory)
    {
        return getRandomFilename(directory, 16);
    }
    
    
    
    public static void unzip(Path zipFilePath, Path destDirectory) throws IOException
    {
        int BUFFER_SIZE = 1024*1024;
        
        if (!Files.exists(destDirectory))
            Files.createDirectories(destDirectory);
        
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath.toFile()));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null)
        {
            Path filePath = destDirectory.resolve(entry.getName());
            
            Path parent_directory = filePath.getParent();
            if (!Files.exists(parent_directory))
                Files.createDirectories(parent_directory);
            
            if (!entry.isDirectory())
            {
                // if the entry is a file, extracts it
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath.toFile()));
                byte[] bytesIn = new byte[BUFFER_SIZE];
                int read = 0;
                while ((read = zipIn.read(bytesIn)) != -1)
                {
                    bos.write(bytesIn, 0, read);
                }
                bos.close();
            }
            else
            {
                if (!Files.exists(filePath))
                    Files.createDirectories(filePath);
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    
}
