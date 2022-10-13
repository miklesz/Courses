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
#ifndef XML_ELEMENT
#define XML_ELEMENT

#include "schema_type.h"
#include "global_header.h"
#include "SubstGrp.h"
#include <stdlib.h>
#include <string.h>

struct xml_element{
	char name[100];
	char typeName[100];
	bool text_attrib;
	unsigned int position;
	unsigned int SubstGrp_num;   //number of members of substgroup;
	unsigned int SubstGrp_child; //child of Substgroup, that is selected
    SubstGroup *SubstGrp;

	DOM_Node node;				//pointer to the node in the XML tree
	DOM_Node element_declaration;	//declaration of the element in the schema tree
	schema_type *type;
	schema_type *theor_type;	//points to the theoretical type that the element belongs to
								//in elements without polymorphism is the same as type

	xml_element operator= (const xml_element &a){
		strcpy(name,a.name);
		strcpy(typeName,a.typeName);
		text_attrib=a.text_attrib;
		position=a.position;
		node=a.node;
		element_declaration=a.element_declaration;
		type=a.type;
		theor_type=a.theor_type;
		return(*this);
	}

	friend int operator==(const xml_element& a,const xml_element& b){
		if(!strcmp(a.name,b.name) && a.position==b.position && a.node==b.node){
			if(a.type->name.equals(b.type->name))
				return 1;
			else
				return 0;
		}
		else return 0;
	}
};

#endif