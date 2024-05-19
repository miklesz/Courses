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
#ifndef CODEC
#define CODEC

#include "global_header.h"
#include "path_type.h"
#include "xml_element.h"

class codec{
private:
	static struct path_type path,rel_path,prev_path;

	/* MPEG-7 Systems (ISO/IEC 15938-1) 7.5.5.6 Multiple Payload Mode */
	static int m_NumberOfMultiOccurrenceLayer;

public:

	static void encode_path(int mode, bool poscode);
	static void decode_path(int mode, bool poscode);
	static xml_element get_path_item(int type_of_path, int num);
	static void set_path_item(int type_of_path,xml_element new_item, int num);
	static unsigned int get_path_depth(int type_of_path);
	static void set_path_depth(int type_of_path, unsigned int value);
	static void increment_path_depth(int type_of_path);
	static xml_element get_actual_item(int type_of_path);
	static void set_actual_item(int type_of_path,xml_element new_item);
	static xml_element get_previous_item(int type_of_path);
	static xml_element get_next_item(int type_of_path);
	static void set_text_attrib(int type_of_path,bool text_attrib);
	static void set_position(int type_of_path,int path_depth, unsigned int position);
	static void set_name(int type_of_path,char *name);
	static void clear_path(int type_of_path);
	static void set_typeName(int type_of_path,char *typeName);
	static void set_type(int type_of_path,schema_type *type);
	static void set_theor_type(int type_of_path,schema_type *type);
	static void compare_paths(unsigned int desired_mode);
	static void set_elem_declaration(int type_of_path, DOM_Element elem);
	static void set_substgroups(int type_of_path, unsigned int child, unsigned int num_members, SubstGroup *substGrp);

	/* MPEG-7 Systems (ISO/IEC 15938-1) 7.5.5.6 Multiple Payload Mode */
	static void encode_firstpath(int mode, bool poscode);
	static void encode_increment(int mode, bool poscode);
	static void encode_termination(void);
	static void encode_gap(void);
	static void decode_firstpath(int mode, bool poscode);
	static void decode_increment(int value);
	static void set_NumberOfMultiOccurrenceLayer(void);
	static int  get_NumberOfMultiOccurrenceLayer(void);
};

#endif