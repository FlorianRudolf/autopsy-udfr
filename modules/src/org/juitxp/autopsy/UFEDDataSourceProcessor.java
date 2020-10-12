/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.juitxp.autopsy;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import java.util.stream.Collectors;


import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.util.Exceptions;

import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.casemodule.services.FileManager;

import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataSourceProcessor;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataSourceProcessorCallback;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataSourceProcessorProgressMonitor;

import org.sleuthkit.autopsy.datasourceprocessors.AutoIngestDataSourceProcessor;

import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.LocalFilesDataSource;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskDataException;
import org.sleuthkit.datamodel.Blackboard.BlackboardException;
import org.sleuthkit.datamodel.blackboardutils.ArtifactsHelper;

import org.sleuthkit.datamodel.DerivedFile;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskData;

import org.juitxp.autopsy.backend.FileTools;
import org.juitxp.autopsy.backend.UFEDParser.DecodedDataField;
import org.juitxp.autopsy.backend.UFEDParser.DecodedDataModel;
import org.juitxp.autopsy.backend.UFEDParser.DecodedDataMultiModelField;
import org.juitxp.autopsy.backend.UFEDParser.DecodedDataSingleField;
import org.juitxp.autopsy.backend.UFEDParser.NewObjectHandler;
import org.juitxp.autopsy.backend.UFEDParser.SAXHandler;
import org.juitxp.autopsy.backend.UFEDParser.TaggedFile;
import org.juitxp.autopsy.backend.UFEDParser.UFEDParserTools;
import org.sleuthkit.datamodel.Account;
import org.sleuthkit.datamodel.Blackboard;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE;
import org.sleuthkit.datamodel.blackboardutils.CommunicationArtifactsHelper;
import org.xml.sax.SAXParseException;


import org.juitxp.autopsy.backend.UFEDParser.Handler.FileHandler;
import org.juitxp.autopsy.backend.UFEDParser.Handler.ContactHandler;
import org.juitxp.autopsy.backend.UFEDParser.Handler.ChatHandler;
import org.juitxp.autopsy.backend.UFEDParser.Handler.InstalledApplicationHandler;


/**
 *
 * @author florian
 */
@ServiceProviders(value = {
    @ServiceProvider(service = DataSourceProcessor.class),
    @ServiceProvider(service = AutoIngestDataSourceProcessor.class)
})
public class UFEDDataSourceProcessor implements DataSourceProcessor, AutoIngestDataSourceProcessor
{
    private static final int UFED_FILES_DEPTH = 10;
    private final UFEDDataSourceProcessorConfigPanel configPanel;
    private UFEDDataProcessorSwingWorker swingWorker;
    
    private static final Logger logger = Logger.getLogger(UFEDDataSourceProcessor.class.getName());
    
    public UFEDDataSourceProcessor()
    {
        configPanel = new UFEDDataSourceProcessorConfigPanel();
    }
    
    @Override
    @NbBundle.Messages({
        "UFEDDataSourceProcessor.dataSourceType=UFDR Image"
    })
    public String getDataSourceType()
    {
        return Bundle.UFEDDataSourceProcessor_dataSourceType();
    }
    
    @Override
    public JPanel getPanel()
    {
        return configPanel;
    }
    
    
/**
     * Tests the selected path.
     *
     * This functions checks permissions to the path directly and then to each
     * of its top most children, if it is a folder.
     */
    @Override
    @NbBundle.Messages({
        "UFEDDataSourceProcessor.noPathSelected=Please select a UFDR file or a directory containing the extracted UFDR file",
        "UFEDDataSourceProcessor.notReadable=Selected path is not readable",
        "UFEDDataSourceProcessor.noUFDRFile=Selected file has no UFDR extension",
        "UFEDDataSourceProcessor.ioError=I/O error occured trying to test the selected folder",
        "UFEDDataSourceProcessor.childNotReadable=Top level path [ %s ] is not readable",
        "UFEDDataSourceProcessor.notAFile=The selected path is not a file nor a directory",
        "UFEDDataSourceProcessor.UFDRDirectoryContainsNoXML=The selected directory contains no XML file",
        "UFEDDataSourceProcessor.UFDRDirectoryContainsMultipleXML=The selected directory contains multiple XML files"
    })
    public boolean isPanelValid()
    {
        configPanel.clearErrorText();
        String selectedPathStr = configPanel.getSelectedFilePath();
        if (selectedPathStr.isEmpty())
        {
            configPanel.setErrorText(Bundle.UFEDDataSourceProcessor_noPathSelected());
            return false;
        }
        
        
        Path selectedPath = Paths.get(selectedPathStr);
        
        //Test permissions
        if (!Files.isReadable(selectedPath))
        {
            configPanel.setErrorText(Bundle.UFEDDataSourceProcessor_notReadable());
            return false;
        }
        

//        

        try
        {
            BasicFileAttributes attr = Files.readAttributes(selectedPath,
                    BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);

            if (attr.isRegularFile())
            {
                String extension = FilenameUtils.getExtension(selectedPath.toString());

                if (!extension.equalsIgnoreCase("ufdr"))
                {
                    configPanel.setErrorText(Bundle.UFEDDataSourceProcessor_noUFDRFile());
                    return false;        
                }
            }
            else if (attr.isDirectory())
            {
                
                File ufed_dir = selectedPath.toFile();
                File[] matchingFiles = ufed_dir.listFiles(new FilenameFilter()
                {
                    public boolean accept(File dir, String name) {
                        return name.endsWith("xml");
                    }
                });
                
                if (matchingFiles.length == 0)
                {
                    configPanel.setErrorText(Bundle.UFEDDataSourceProcessor_UFDRDirectoryContainsNoXML());
                    return false;
                }
                else if (matchingFiles.length > 1)
                {
                    configPanel.setErrorText(Bundle.UFEDDataSourceProcessor_UFDRDirectoryContainsMultipleXML());
                    return false;
                }
            }

        }
        catch (IOException | UncheckedIOException ex)
        {
            configPanel.setErrorText(Bundle.UFEDDataSourceProcessor_ioError());
            logger.log(Level.WARNING, "[USED DSP] I/O exception encountered trying to test the UFED file.", ex);
            return false;
        }

        return true;
    }
    
    
    
    /**
     * Tests if the given path is an UFED Folder.
     *
     * This function assumes the calling thread has sufficient privileges to
     * read the folder and its child content.
     *
     * @param dataSourcePath Path to test
     * @return 100 if the folder passes the UFED Folder check, 0 otherwise.
     * @throws AutoIngestDataSourceProcessorException if an I/O error occurs
     * during disk reads.
     */
    @Override
    public int canProcess(Path dataSourcePath) throws AutoIngestDataSourceProcessorException
    {
        return 100;
//        try {
//            return 100;
//        } catch (IOException ex) {
//            throw new AutoIngestDataSourceProcessorException("[UFED DSP] encountered I/O error " + ex.getMessage(), ex);
//        }
//        return 0;
    }
    
    
    /**
     * Processes the UFED folder that the examiner selected. The heavy lifting is
     * done off of the EDT, so this function will return while the 
     * path is still being processed.
     * 
     * This function assumes the calling thread has sufficient privileges to
     * read the folder and its child content, which should have been validated 
     * in isPanelValid().
     */
    @Override
    @NbBundle.Messages({
        "UFEDDataSourceProcessor.noCurrentCase=No case is open."
    })
    public void run(DataSourceProcessorProgressMonitor progressMonitor, DataSourceProcessorCallback callback)
    {
        progressMonitor.setIndeterminate(true);

//        String selectedFilePath = configPanel.getSelectedFilePath();
//        Path selectedPath = Paths.get(selectedFilePath);

        try
        {
            Case currentCase = Case.getCurrentCaseThrows();
            String uniqueUUID = UUID.randomUUID().toString();
            
            UFEDDataSourceProcessorSettings settings = configPanel.getSettings();
            
            //Move heavy lifting to a background task.
            swingWorker = new UFEDDataProcessorSwingWorker(currentCase, settings, uniqueUUID,
                                                           progressMonitor, callback);
            swingWorker.execute();
        }
        catch (NoCurrentCaseException ex)
        {
            logger.log(Level.WARNING, "[UFED DSP] No case is currently open.", ex);
            callback.done(DataSourceProcessorCallback.DataSourceProcessorResult.CRITICAL_ERRORS,
                    Lists.newArrayList(Bundle.UFEDDataSourceProcessor_noCurrentCase(),
                            ex.getMessage()), Lists.newArrayList());
        }
    }
    
    
    
/**
     * Processes the UFED Folder encountered in an auto-ingest context. The heavy
     * lifting is done off of the EDT, so this function will return while the 
     * path is still being processed.
     * 
     * This function assumes the calling thread has sufficient privileges to
     * read the folder and its child content.
     * 
     * @param deviceId
     * @param dataSourcePath
     * @param progressMonitor
     * @param callBack
     */
    @Override
    public void process(String deviceId, Path dataSourcePath, DataSourceProcessorProgressMonitor progressMonitor, DataSourceProcessorCallback callBack) {
        progressMonitor.setIndeterminate(true);

        try
        {
            Case currentCase = Case.getCurrentCaseThrows();
            
            UFEDDataSourceProcessorSettings settings = configPanel.getSettings();
            
            //Move heavy lifting to a background task.
            swingWorker = new UFEDDataProcessorSwingWorker(currentCase, settings, deviceId,
                                                           progressMonitor, callBack);
            swingWorker.execute();
        }
        catch (NoCurrentCaseException ex)
        {
            logger.log(Level.WARNING, "[UFED DSP] No case is currently open.", ex);
            callBack.done(DataSourceProcessorCallback.DataSourceProcessorResult.CRITICAL_ERRORS,
                    Lists.newArrayList(Bundle.UFEDDataSourceProcessor_noCurrentCase(),
                            ex.getMessage()), Lists.newArrayList());
        }
    }

    @Override
    public void cancel()
    {
        if (swingWorker != null)
        {
            swingWorker.cancel(true);
        }
    }

    @Override
    public void reset()
    {
        //Clear the current selected file path.
        configPanel.clearSelectedFilePath();
    }
    
    
    
    
    
    
    
    
    private class UFEDDataProcessorSwingWorker extends SwingWorker<LocalFilesDataSource, Void>
    {
        private final Case currentCase;
        private final UFEDDataSourceProcessorSettings settings;
        private final String uniqueUUID;
        
        private final DataSourceProcessorProgressMonitor progressMonitor;
        private final DataSourceProcessorCallback callback;        
        

        public UFEDDataProcessorSwingWorker(
                Case currentCase,
                UFEDDataSourceProcessorSettings settings,
                String uniqueUUID,
                DataSourceProcessorProgressMonitor progressMonitor,
                DataSourceProcessorCallback callback)
        {
            this.currentCase = currentCase;
            this.settings = settings;
            this.uniqueUUID = uniqueUUID;
            
            this.progressMonitor = progressMonitor;
            this.callback = callback;
        }
        
        
        

        
        

        @Override
        @NbBundle.Messages({
            
            "UFEDDataSourceProcessor.preppingFiles=Preparing to add files to the case database",
            "UFEDDataSourceProcessor.unzippingUFDR=Unzipping ufdr file...",
            "UFEDDataSourceProcessor.checkingUFDRXML=Checking UFDR XML...",
            "UFEDDataSourceProcessor.creatingDataSource=Creating data source...",
            "UFEDDataSourceProcessor.fixingUFDRXML=Fixing UFDR XML...",
            "UFEDDataSourceProcessor.parsingUFDRXML=Parsing UFDR XML...",
            "UFEDDataSourceProcessor.parsingFiles=Parsing files...",
            "UFEDDataSourceProcessor.parsingInstalledApplications=Parsing installed applications...",
            "UFEDDataSourceProcessor.parsingContacts=Parsing contacts...",
            "UFEDDataSourceProcessor.parsingChats=Parsing chats..."
        })
        protected LocalFilesDataSource doInBackground() throws TskCoreException,
                TskDataException, IOException, BlackboardException, ParserConfigurationException, SAXException
        {
            progressMonitor.setProgressText(Bundle.UFEDDataSourceProcessor_preppingFiles());
            
            

            Path case_directory = Paths.get(currentCase.getCaseDirectory());
            Path ufdr_sources_directory = case_directory.resolve("UFEDSources");
            Path current_ufdr_sources_directory = ufdr_sources_directory.resolve(uniqueUUID);
            
            logger.log(Level.INFO, "Fetching and createing UFDR directory: " + current_ufdr_sources_directory);

            if (!Files.exists(current_ufdr_sources_directory))
            {
                Files.createDirectories(current_ufdr_sources_directory);
            }
            
            
            Path UFDRPath = settings.path;
            BasicFileAttributes attr = Files.readAttributes(UFDRPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);

            
            
            Boolean is_directory = attr.isDirectory();            
            Path unzipped_ufdr_directory = is_directory ? UFDRPath : current_ufdr_sources_directory;
            Boolean unzipped_ufdr_directory_is_in_case_directory = !is_directory;
            
            logger.log(Level.INFO, "Checking is input if source is dirctory or file: " + is_directory);
            
            if (!is_directory)
            {
                logger.log(Level.INFO, "Source is file -> unzipping");
                progressMonitor.setProgressText(Bundle.UFEDDataSourceProcessor_unzippingUFDR());            
                FileTools.unzip(UFDRPath, unzipped_ufdr_directory);
            }
            
            

            
            
            progressMonitor.setProgressText(Bundle.UFEDDataSourceProcessor_checkingUFDRXML());
            
            File ufed_dir = unzipped_ufdr_directory.toFile();
            File[] matchingFiles = ufed_dir.listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name) {
                    return name.endsWith("xml");
                }
            });

            Path report_xml = matchingFiles[0].toPath();
            logger.log(Level.INFO, "Found Report XML: " + report_xml);
            
            Boolean xml_okay = true;
            
            try
            {
                logger.log(Level.INFO, "Validating XML");
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();

                DefaultHandler handler = new DefaultHandler();
                logger.log(Level.INFO, "Validate-Parsing XML...");
                saxParser.parse(report_xml.toFile(), handler);
                logger.log(Level.INFO, "Validate-Parsing XML done");
            }
            catch (IOException io_ex)
            {
                xml_okay = false;
            }
            catch (SAXParseException sax_ex)
            {
                xml_okay = false;
            }
            
            if (!xml_okay)
            {
                logger.log(Level.INFO, "XML is not okay, trying to fix password section");
                progressMonitor.setProgressText(Bundle.UFEDDataSourceProcessor_fixingUFDRXML());
                
                if (is_directory)
                {
                    // source is already extracted, make fixed report xml in current_ufdr_sources_directory
                    Path destination_xml = current_ufdr_sources_directory.resolve(report_xml.getFileName());
                    
                    logger.log(Level.INFO, "Fixing Report XML to " + destination_xml);
                    UFEDParserTools.fixUFEDXMLPasswordSection(report_xml, destination_xml);
                    logger.log(Level.INFO, "Fixing done");
                    report_xml = destination_xml;
                }
                else
                {
                    Path tmp_filename = FileTools.getRandomFilename(current_ufdr_sources_directory);
                    
                    logger.log(Level.INFO, "Fixing Report XML to " + tmp_filename);
                    UFEDParserTools.fixUFEDXMLPasswordSection(report_xml, tmp_filename);
                    logger.log(Level.INFO, "Fixing done");
                    
                    logger.log(Level.INFO, "Swapping files:");
                    Path bak_filename = Paths.get(report_xml.toString() + ".bak");
                    logger.log(Level.INFO, "  " + report_xml + " -> " + bak_filename);
                    Files.move(report_xml, bak_filename);
                    logger.log(Level.INFO, "  " + tmp_filename + " -> " + report_xml);
                    Files.move(tmp_filename, report_xml);
                }
            }
            
            progressMonitor.setProgressText(Bundle.UFEDDataSourceProcessor_creatingDataSource());

            logger.log(Level.INFO, "Creating data source...");
            LocalFilesDataSource dataSource = currentCase.getServices().getFileManager().addLocalFilesDataSource(
                    uniqueUUID,
                    "UFED Image " + UFDRPath.getFileName().toString(), //Name
                    "", //Timezone
                    new ArrayList<String>(),
                    new ProgressMonitorAdapter(progressMonitor));
            
            
            progressMonitor.setProgressText(Bundle.UFEDDataSourceProcessor_parsingUFDRXML());
            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            
            
            logger.log(Level.INFO, "Creating context...");
            AutopsyUFEDFileContext context = new AutopsyUFEDFileContext(currentCase, dataSource, unzipped_ufdr_directory, "UFED Parser", logger);
            

            
            if (settings.file_parsing_enabled)
            {
                logger.log(Level.INFO, "Parsing Files...");
                progressMonitor.setProgressText(Bundle.UFEDDataSourceProcessor_parsingFiles());
                FileHandler new_file_handler = new FileHandler(context, logger);
                saxParser.parse(report_xml.toFile(), new SAXHandler(new_file_handler));
                logger.log(Level.INFO, "Parsing Files done");
                
                logger.log(Level.INFO, "Adding files...");
                context.addFilesToAutopsy();
                logger.log(Level.INFO, "Adding files done");
            }
            
            if (settings.installed_application_parsing_enabled)
            {
                logger.log(Level.INFO, "Parsing Installed Applications...");
                progressMonitor.setProgressText(Bundle.UFEDDataSourceProcessor_parsingInstalledApplications());
                InstalledApplicationHandler new_installed_application_handler = new InstalledApplicationHandler(context, logger);
                saxParser.parse(report_xml.toFile(), new SAXHandler(new_installed_application_handler));
                logger.log(Level.INFO, "Parsing Installed Applications done");
                
                logger.log(Level.INFO, "Adding Installed Applications...");
                context.addInstalledApplicationsToAutopsy();
                logger.log(Level.INFO, "Adding Installed Applications done");

            }
            
            if (settings.contact_parsing_enabled)
            {
                logger.log(Level.INFO, "Parsing Contacts...");
                progressMonitor.setProgressText(Bundle.UFEDDataSourceProcessor_parsingContacts());
                ContactHandler new_contact_handler = new ContactHandler(context, logger);
                saxParser.parse(report_xml.toFile(), new SAXHandler(new_contact_handler));
                logger.log(Level.INFO, "Parsing Contacts done");
            }
            
            if (settings.chat_parsing_enabled)
            {
                logger.log(Level.INFO, "Parsing Chats...");
                progressMonitor.setProgressText(Bundle.UFEDDataSourceProcessor_parsingChats());
                ChatHandler new_chat_handler = new ChatHandler(context, logger);
                saxParser.parse(report_xml.toFile(), new SAXHandler(new_chat_handler));
                logger.log(Level.INFO, "Parsing Chats done");
            }
            
            if (settings.contact_parsing_enabled)
            {
                logger.log(Level.INFO, "Adding Contacts...");
                context.getAccountManager().mergeCandiates();
                context.addContactsToAutopsy();
                logger.log(Level.INFO, "Adding Contacts done");
            }
            
            if (settings.chat_parsing_enabled)
            {
                logger.log(Level.INFO, "Adding Chats...");
                context.addChatsToAutopsy();
                logger.log(Level.INFO, "Adding Chats done");
            }
            
            return dataSource;
        }

        @Override
        @NbBundle.Messages({
            "UFEDDataSourceProcessor.unexpectedError=Internal error occurred while processing UFED report"
        })
        public void done() {
            try
            {
                LocalFilesDataSource newDataSource = get();
                callback.done(DataSourceProcessorCallback.DataSourceProcessorResult.NO_ERRORS,
                        Lists.newArrayList(), Lists.newArrayList(newDataSource));
            }
            catch (InterruptedException ex)
            {
                logger.log(Level.WARNING, "[UFED DSP] Thread was interrupted while processing the UFED report."
                        + " The case may or may not have the complete UFED report.", ex);
                callback.done(DataSourceProcessorCallback.DataSourceProcessorResult.NO_ERRORS, 
                        Lists.newArrayList(), Lists.newArrayList());
            }
            catch (ExecutionException ex)
            {
                logger.log(Level.SEVERE, "[UFED DSP] Unexpected internal error while processing UFED report.", ex);
                callback.done(DataSourceProcessorCallback.DataSourceProcessorResult.CRITICAL_ERRORS,
                        Lists.newArrayList(Bundle.UFEDDataSourceProcessor_unexpectedError(),
                                ex.toString()), Lists.newArrayList());
            }
        }

        /**
         * Makes the DSP progress monitor compatible with the File Manager
         * progress updater.
         */
        private class ProgressMonitorAdapter implements FileManager.FileAddProgressUpdater
        {

            private final DataSourceProcessorProgressMonitor progressMonitor;

            ProgressMonitorAdapter(DataSourceProcessorProgressMonitor progressMonitor)
            {
                this.progressMonitor = progressMonitor;
            }

            @Override
            @NbBundle.Messages({
                "UFEDDataSourceProcessor.fileAdded=Added %s to the case database"
            })
            public void fileAdded(AbstractFile newFile)
            {
                progressMonitor.setProgressText(String.format(Bundle.UFEDDataSourceProcessor_fileAdded(), newFile.getName()));
            }
        }
    }
    
       
}
