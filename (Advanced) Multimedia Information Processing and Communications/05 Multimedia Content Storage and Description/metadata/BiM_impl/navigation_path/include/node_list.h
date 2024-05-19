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

#ifndef NODE_LIST
#define NODE_LIST

#include <dom/DOM.hpp>
#include <util/PlatformUtils.hpp>
#include <parsers/DOMParser.hpp>
#include <dom/DOM_TreeWalker.hpp>
#include <dom/DOM_Document.hpp>
#include "global_header.h"
#include "SubstGrp.h"



typedef struct NodeInstance{
	DOM_Node node;
	float versionNum;
	struct NodeInstance *next;
}node_instance;

class node_list{	
private:
	node_instance *first;
	float temp_version;
	//DOM_Node temp_Node;
	unsigned int num_nodes;

public:
	node_list();	//Constructor
	~node_list();	//Destructor
	void add_node(DOM_Node node,bool checking);
	DOM_Node item(unsigned int num);
	float getVersion(unsigned int num);
	void setVersion(unsigned int num, float version);
	unsigned int search_node(DOMString name);
	unsigned int get_length();
	void search_extensions_with_base(node_list& list,DOMString name);
	//void set_num_nodes(unsigned int num);
	void delete_node(unsigned int num);
	void swap_Nodes(unsigned int num);
	void swap_area(unsigned int begin_block1,unsigned int begin_block2,unsigned int end_block2);
	//void order_list();
	void add_DOM_NodeList(DOM_NodeList list);
	void delete_node_list();
	//node_list operator=(node_list& list);
	void operator=(node_list& list);
};

#endif
