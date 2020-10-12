# Overview
--------------------------

This module enables Autopsy to import Cellebrite UFDR report files. The following data is parsed and imported:
 - Files: The file- and directory structure of the mobile device is available in the data source section.
 - Contacts: Contacts with name, e-mail, phone information.
 - Installed applications
 - Communication: Chats and E-Mail, including attachments.
 
A cellebrite UFDR file is basically a ZIP container including an XML file with extracted information as well as the data files. Importing the .ufdr file and the extracted UFDR file is supported. In case of importing an .ufdr file, the file is extracted to the case folder (UFEDSources subfolder). The cellebrite UFDR file format presents a lot of information but not all is handled by this module.

Limitations:
 - The UFDR XML file is not documented publicly and an XML schema is not available. Therefore, reverse engineering was used to develop this module. There is not guarantee for perfect correctness.
 - Additionally, there might be chances between the XML schema of differend Cellebrite versions. This module might not work with all UFDR files.
 - E-Mails are currently imported as chat messages (not as E-Mails Messages), SMS and MMS are currently not imported at all.

Take a look at the introduction video: https://youtu.be/ycSEazdrkHs


# Dependencies and Development
--------------------------

This module depends on the following libraries:
 - https://commons.apache.org/proper/commons-validator/
 - https://javaee.github.io/javamail/
 - https://github.com/google/libphonenumber

Please respect the corresponding licenses of these modules!

The module itself depends on the the backend java library. The module has been developed for Autopsy 4.15 in an Linux environment using NetBeans 11.


# Testdata
--------------------------

In the folder testdata, an examplary UFDR file is provided. This file was manually generated and anonymized. The included jpg and off file are available under CC licenses:

 - En-Creative-Commons.ogg https://commons.wikimedia.org/wiki/File:En-Creative-Commons.ogg
 - 4454825783_dbcb233af5_b.jpg https://search.creativecommons.org/photos/e20a3699-02b7-4e96-aaf9-db5cb43161b6


# Disclaimer
--------------------------

The usage of this source code and module is at your own risk!
Cellebrite is a registered trademark.


# Acknowledgement
--------------------------

I would like to thank the Austrian Ferderal Ministry of Justice for supporting the development of this module.


