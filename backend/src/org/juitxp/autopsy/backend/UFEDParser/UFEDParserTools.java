/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy.backend.UFEDParser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import org.juitxp.autopsy.backend.FileTools;

/**
 *
 * @author florian
 */
public class UFEDParserTools
{
    public static void fixUFEDXMLPasswordSection(Path report_xml, Path destination_xml) throws IOException
    {
        Boolean password = false;

        Scanner myReader = new Scanner(report_xml);
        try (PrintStream out = new PrintStream(new FileOutputStream(destination_xml.toFile())))
        {
            while (myReader.hasNextLine())
            {
                String line = myReader.nextLine();

                if (!password)
                {
                    if (line.indexOf("<modelType type=\"Password\">") != -1)
                    {
                        password = true;
                    }
                    else
                    {
                        out.println(line);
                    }
                }
                else
                {
                    if (line.indexOf("</modelType>") != -1)
                    {
                        password = false;
                    }
                }
            }
        }
    }
}
