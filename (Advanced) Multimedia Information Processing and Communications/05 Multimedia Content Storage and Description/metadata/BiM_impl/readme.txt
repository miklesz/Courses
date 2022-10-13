Installation
------------
The Java Sun JDK (version 1.2.2 or later) is needed to use the BiM reference software.
Just run the bim.bat under Windows (or the bim.sh under UNIX).

IMPORTANT :
If you had installed a previous version of the BiM reference software, please remove
any old remaining bim.jar files which can stay in the jre/lib/ext/ directory.
This version doesn't need anymore that the BiM.jar file should be copied in the
jre/lib/ext directory.

This package also contains :
- the gnu_regexp package (version 1.1.3)
- the Xerces-Java parser (version 1.4.3)
- xerces-c_1_4.dll (version 1.4)

The following files, located in the lib/ subdirectory :
- navigation_path.dll
- xerces-c_1_4.dll
should be copied in the correct system directory 
(e.g. in the c:/winnt/system32 directory on Windows NT/2000 machines)
(e.g. in the c:/windows/system directory on Windows 9x/ME machines)

Test
----
The bim.bat launches the BiM Graphic User Interface which is able
to encode and decode MPEG-7 XML files.

The testall.bat files in the examples subdirectories are a batch 
files able to encode and decode files in the local directory.

Use
---
With the good classpath :
set CLASSPATH=../../lib/xerces.jar;../../lib/bim.jar;../../lib/gnu-regexp-1.1.3.jar

Graphic User Interface Encoder/Decoder:
Usage : java com.expway.ref.BiMGUI

Encoding :
Usage : java com.expway.ref.BiMEncoder [-abdt] input_xml_filename output_bim_filename [schema_filename]
-t uses textual path (default)
-b uses binary path (schema file is required)
-d implies that the xml file is a piece of description (default)
-a implies that the xml file is a textual access unit
-c enables the f&b compatibility mode (default is disabled)

Decoding :
Usage : java com.expway.ref.BiMDecoder [-abdt] input_bim_filename output_xml_filename [schema_filename]
-t uses textual path (default)(only used in textual access unit mode)
-b uses binary path (schema file is required)(only used in textual access unit mode)
-d implies that the xml file is a piece of description (default)
-a implies that the xml file is a textual access unit
-c enables the f&b compatibility mode (default is disabled)

Description
-----------
- Siemens navigation_path module

The xerces-c_1_4.dll contains the library of the Xerces CPP parser of
the Apache organization (http://xml.apache.org/index.html).
If you want to compile on your own, you have to get the 
xerces c++ sources from there.

The navigation_path.dll contains the functions necessary to encode
a textual path in a binary path (which is written in the bitstream),
and to decode a binary path from the bitstream into a textual path.