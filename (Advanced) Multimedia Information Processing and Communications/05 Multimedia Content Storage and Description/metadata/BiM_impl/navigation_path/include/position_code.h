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
#ifndef POSITION_CODE
#define POSITION_CODE

#include <dom/DOM.hpp>
#include "bitstream.h"
#include "global_header.h"

class position_code{
	static char ext_stack[10][MAX_NAME_LENGTH];
	static int ext_stack_depth;
public:
	static bool is_in_extension_stack(char *name);
	static void write_position(bitstream &bitstream_out, int type_of_path, int path_depth);
	static void read_position(bitstream &bitstream_in, int type_of_path, int path_depth);
	static int  M_contentModel(DOM_TreeWalker walker);
	static int  detect_M_Co(DOM_Node type);
	static int  use_vuimsbf(DOM_Node element_declaration);
	static bool use_local_poscode(DOM_Node type, char *cand_name, bool *in_co);
	static bool occurs_ok(DOM_Node cand_node);
};

#endif