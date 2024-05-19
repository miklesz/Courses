/***********************************************************************
This software module was originally developed by Siemens AG and Munich 
University of Technology in the course of development of the MPEG-7 
Systems (ISO/IEC 15938-1) standard. 

This software module is an implementation of a part of one or more 
MPEG-7 Systems (ISO/IEC 15938-1) tools as specified by the 
MPEG-7 Systems (ISO/IEC 15938-1) standard. 

ISO/IEC gives users of the MPEG-7 Systems (ISO/IEC 15938-1) free license
to this software module or modifications thereof for use in hardware or
software products claiming conformance to the MPEG-7 Systems 
(ISO/IEC 15938-1). 

Those intending to use this software module in hardware or software 
products are advised that its use may infringe existing patents.

The original developer of this software module and his/her company, the
subsequent editors and their companies, and ISO/IEC have no liability
for use of this software module or modifications thereof in an
implementation. 

Copyright is not released for non MPEG-7 Systems (ISO/IEC 15938-1) 
conforming products. 

The organizations named above retain full right to use the code for 
their own purpose, assign or donate the code to a third party and to 
inhibit third parties from using the code for non MPEG-7 Systems 
(ISO/IEC 15938-1) conforming 
products. 

This copyright notice must be included in all copies or derivative works.

************************************************************************/

#include "ErrorHandler.hpp"
#include "build_TBCs.h"

#ifndef PARSE_FILE
#define PARSE_FILE


#include "codec.h"
#include <dom/DOM_DOMException.hpp>
#include <sax/SAXException.hpp>
#include <sax/SAXParseException.hpp>


class parse_file{ 
	static DOM_Document doc,schema; //DOMTree of the XML-document and thee schema
	static node_list elements;		//list of all the elements defined under schema
	static node_list attributes;	//list of all the attribute defined under schema
	static node_list extensions;	//list of all the extensions of the NAMED types
	static bool new_schema;         //indicates, if the schema is of old or new syntax
public:
	static DOM_Document parseFile(char *xmlFile);
	static void prepare_schema(DOM_Node node);
	static void rewrite_XML_file(DOM_Document doc);
	static void delete_text_nodes(DOM_Node node);
	static DOM_Document get_schema(void);
	static void set_schema(DOM_Document schema_in);
	static node_list& get_elements();
	static node_list& get_attributes();
	static node_list& get_extensions();
	static void analyze_schema(DOM_Document schema);
	//friend schema_type* build_TBCs::search_type(DOMString,DOM_Node startpoint=NULL);
	
	friend void codec::encode_path(int mode, bool poscode);
	friend void codec::decode_path(int mode, bool poscode);
	friend class build_TBCs;
	friend class polymorphism;
};

#endif