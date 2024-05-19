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
#ifndef ALPHABETICAL_ORDER
#define ALPHABETICAL_ORDER

#include <dom/DOM.hpp>
#include "node_list.h"
//#include "decoder_config.h"

typedef struct{
	unsigned int index;
	char *signature;
} LockList;


class alphabetical_order{
	LockList lock_listA[MAX_NODE_LIST_LEN];//contains information, which childs shall not change their order
	LockList lock_listB[MAX_NODE_LIST_LEN];//if the childs are in a sequence group

public:
	alphabetical_order();
	~alphabetical_order();
	bool compare_childs(bool type, unsigned int index1, unsigned int index2, DOM_Node child1, DOM_Node child2);
	void order_list_alphabetical(bool type,unsigned int begin, node_list *child_list);	//void order_group_alphabetical(decoder_config::changedTypes *ordered_paths[], int begin, int end);
	void set_lock(bool type, node_list *child_list, unsigned int begin, unsigned int end);
};

#endif