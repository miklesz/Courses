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

#ifndef SUBSTGRP
#define SUBSTGRP

#include <dom/DOM.hpp>

typedef struct node_pointer{
	DOM_Node node;
	struct node_pointer *next;
}nodePointer;

typedef struct Subst_Grp{
	unsigned int num_members;
	DOMString name;
	DOM_Node *base;
	nodePointer *nodeList;
	struct Subst_Grp *next;
}SubstGroup;

void extract_SubstGroups(DOM_Document doc);
void print_all_substgroups();
unsigned int search_substGroup_elem(SubstGroup *ptr_SubstGroup,char *name);
SubstGroup *is_head_of_substitution_group(DOMString name);
DOM_Element retrieve_substgroup_elem(SubstGroup *ptr_SubstGroup,unsigned int num_elem);

#endif