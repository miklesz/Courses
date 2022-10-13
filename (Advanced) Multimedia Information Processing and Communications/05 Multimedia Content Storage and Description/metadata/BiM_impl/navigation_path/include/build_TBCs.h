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
//#include "node_list.h"
#ifndef BUILD_TBCS
#define BUILD_TBCS


#include "parse_file.h"
#include "path_type.h"
#include "global_header.h"

class build_TBCs{
	static schema_type types[MAX_NUMBER_OF_TYPES];
	static unsigned int types_read;

public:
	static bool has_prefix(DOMString name);
	static bool compare_wo_nms(DOMString str1,DOMString str2);
	static void search_declaration(int type_of_path);
	static schema_type* detect_textual_anonymous_type(char *name);
	static schema_type* search_type(DOMString,DOM_Node startpoint=parse_file::get_schema().getLastChild());
	static void write_type_statistic();
};

class group_stack{
	unsigned char type_of_group[MAX_MBG_DEPTH];
	unsigned int  beginA[MAX_MBG_DEPTH];		//begin of Group (seq,choice) in Table A
	unsigned int  beginB[MAX_MBG_DEPTH];
	unsigned int  depth;

public:
	group_stack();
	void push_group(unsigned char type, unsigned int beginA, unsigned int beginB);
	void pull_group();
	unsigned char get_actual_type(void);
	unsigned char get_previous_type(void);
	unsigned char get_beginA(void);
	unsigned char get_beginB(void);
	friend class build_TBCs;
};


#endif