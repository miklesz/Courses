Documentation for the usage of the navigation path module
=========================================================


This package contains two program modules, which have been developed for the test of the navigation path part
of Mpeg-7 Systems (ISO/IEC 15938-1).
Only the module in the folder "navigation_path" contains the standard-relevant part. (Several files
*.cpp *.h, which are compiled into a dynamic link library. This DLL is called from the JAVA-main(), that implements
the other parts of the standard.)

The implementation of the program is based on the Xerces XML parser (see: http://xml.apache.org).
The xerces-c_1_4.dll needs to be installed on the system in order to run the program. 
The respective library xerces-c_1.lib is needed to compile the code.

In order to test the navigation_path module independently from the Java main(), the module from the folder
"interface_textual" can be used: it parses a description depth-first, and generates textual paths to all 
the elements and attributes in absolute or relative mode.
The calling parameters are:

interface_textual <description> <schema> <mode>

where
<description> is the path to the description, which shall be processed,
<schema> is the path to the schema
<mode> is '1' for absolute mode and '2' for relative mode

For the test of the navigation path part, e.g. the following files can be used (files from the MPEG-7 testset):

monsterJr4Ver3.xml          with schema:	Mpeg7Ver3.xsd
mdsExamplesClause4_7.xml    with schema:	MdsExampleTestClause4-7.xsd 
mdsExamplesClause8_10.xml   with schema:	MdsExampleTestClause8-10.xsd
mdsExamplesClause11_12.xml  with schema:	MdsExampleTestClause11-12.xsd
mdsExamplesClause13_15.xml  with schema:	MdsExampleTestClause13-15.xsd
mdsExamplesClause17.xml     with schema:	MdsExampleTestClause17.xsd

The interface_textual generates the files "results_<name of description>_<mode of coding>.txt", which log the test. 
For every processed element, the input path, the encoded binary representation (output of the encode_path function 
of the DLL), and the decoded textual path (output of the decode_path function of the DLL) are shown.
The number in front of the '%' -sign gives the length of the binary path in bits, that is read from the decoder (if an 
error occurs this needs not necessarily be equal to the number of bits, that are written from the encoder).
The length of the binary path written from the encoder is compared to the length read by the decoder, and it's
reported, if the length is not equal.
The path before encoding is also compared to the path after decoding, and a discrepancy is also reported.

Each path consists of one or several chunks of the form:
/<type of the element>#<name of the element>#<position of the element>/
An attribute is indicated with '@' in front of the typename.
An absolute path begins with '/', a relative one not.
"##" indicates a step to the father of a node in case of relative path.

The batchfiles 

examples/fdis/video/testVideo.bat 
examples/testVisual.bat 
examples/testMDS.bat
examples/multiple_payload/ex.bat

can be used to process the example files automatically.

In this release also the source code for the multiple payload coding, (confer FDIS 15938-1, 7.6.5.6)
is included. Examples are provided in examples/multiple_payload.


Short description of the individual *.cpp/*.h files, contained in the 'navigationPath_DLL.zip' archive
======================================================================================================

navigation_path.cpp:
- contains the interface to the top-level encoder and decoder functions encode_path() and decode_path().
- calls the Xerces parser, that generates a DOM-representation of the schema in memory

encoder.cpp:
- implements the encoder function for the two modes 'relative' and 'absolute'

decoder.cpp:
- implements the decoder function

parse_file.cpp:
- implements the call of the xerces parser
- builds up a single schema, if sub-schemas are included/imported
- a function is provided to write the composed schema to file

build_TBCs.cpp:
- provides a function, that builds the TreeBranchCode tables (type A/B) for the complex types 
  of the schema

bitstream.cpp:
- contains functions to write/read the variable-length codes in/from the bitstream

textual_path.cpp:
- contains functions to write/read a textual path to/from the internal data structure, that represents a path

path_operations.cpp:
- provides functions to operate on the internal path data structure

alphabetical_order:
- handles the alphabetical ordering of the child-list of complex types, taking care of the
  content model (e.g. elements inside of a 'sequence' must not be ordered alphabetically)

position_code.cpp:
- provides functions to write/read the position codes to/from a path
- implements the functions to detect the content model of elements

SubstGrp.cpp:
- provides functions to handle substitutionGroups

polymorphism.cpp:
- contains a function to build up the inheritance tree
- provides a function to retrieve the type-code of an element, using the inheritance tree

namespaces.cpp:
- provides functions to handle namespaces, basically operating on a table-structure, 
  that maps qualifiers (e.g. "mpeg7:") to the internal namespace representation (e.g. "g0"), 
  and expanded qualifiers ("http://...") to the internal representation.

node_list.cpp:
- provides fuctions to operate on a 'node_list' ('node_lists' are used to represent the 
  child list of TBC tables)

schema_type.h:
- defines the data-structure for a TBC table, with child-lists A/B

xml_element.h:
- defines the data-structure, that is used for the items of the internal path representation

path_type.h:
- defines the data-structure for the internal path representation

The other header files define the data structures/classes, that are used in the *.cpp file
with the same name.


