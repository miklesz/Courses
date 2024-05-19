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
#ifndef SCHEMA_TYPE
#define SCHEMA_TYPE

#include <dom/DOM.hpp>
#include "node_list.h"


struct schema_type{
	DOMString name;
	node_list tableA;			//contains only complexTypes & Termination code
	node_list tableB;			//contains also simpleTypes
	DOM_Node type_definition;	//pointer to the node in the schema tree
	DOM_Element poly;			//pointer to the corresponding poly_element in the poly_tree

	/*
	schema_type operator= (const schema_type &a){
		name=a.name;
		tableA=a.tableA;
		tableB=a.tableB;
		type_definition=a.type_definition;
		poly=a.poly;
		return(*this);
	}*/

};

#endif