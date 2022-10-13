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
(ISO/IEC 15938-1) conforming products. 

This copyright notice must be included in all copies or derivative works.

************************************************************************/
#include "SubstGrp.h"
#include "global_header.h"
#include <stdlib.h>
#include <string.h>
#include <fstream.h>
#include "build_TBCs.h"

#ifdef WRITE_DEBUG_INFO 
extern ofstream rep_sbstGrp;
#endif


extern SubstGroup *FirstSubstGroup;

void insert_SubstGrpElem(DOM_Node insert_node,SubstGroup *ptr_SubstGroup){
	nodePointer *new_nodePointer=new nodePointer;
	nodePointer *node_ptr;
	new_nodePointer->next=NULL;
	new_nodePointer->node=insert_node;

/*
#ifdef WRITE_DEBUG_INFO
	DOM_Element elem=(DOM_Element &)insert_node;
	rep_sbstGrp<<"insert node: "<<elem.getAttribute("name").transcode();
	rep_sbstGrp<<" in group: "<<ptr_SubstGroup->name.transcode()<<endl;
#endif
*/
	ptr_SubstGroup->num_members++;
	if(ptr_SubstGroup->nodeList==NULL)
		ptr_SubstGroup->nodeList=new_nodePointer;
	else{
		node_ptr=ptr_SubstGroup->nodeList;
		while(node_ptr->next!=NULL) node_ptr=node_ptr->next;
		node_ptr->next=new_nodePointer;
	}
}

SubstGroup *is_head_of_substitution_group(DOMString name){
	SubstGroup *ptr_SubstGroup=FirstSubstGroup;
	//rep_sbstGrp<<endl<<"check, if element "<<name.transcode()<<" is head of substitution group"<<endl;
	while(ptr_SubstGroup){
		if(ptr_SubstGroup->name.equals(name)){
			//rep_sbstGrp<<"yes !!"<<endl;
			return ptr_SubstGroup;
		}
		ptr_SubstGroup=ptr_SubstGroup->next;
	}
	return NULL;
}

void orderAlpha_substGroup(SubstGroup *ptr_SubstGroup){
	bool changed;
	nodePointer *node_ptr;
	nodePointer *ndptr;
	nodePointer **p_ndptr;
	while(1){
		changed=false;
		node_ptr=ptr_SubstGroup->nodeList;
		//rep_sbstGrp<<"--------"<<endl;
		p_ndptr=&(ptr_SubstGroup->nodeList);
		while(node_ptr&&node_ptr->next){
			DOM_Element elem1=(DOM_Element &)node_ptr->node;
			DOM_Element elem2=(DOM_Element &)node_ptr->next->node;
			char *name1=elem1.getAttribute("name").transcode();
			char *name2=elem2.getAttribute("name").transcode();
			//rep_sbstGrp<<"compare "<<name1<<" with "<<name2;
			if(strcmp(name1,name2)>0){//name2 is preceding name1
				ndptr=node_ptr->next;
				node_ptr->next=node_ptr->next->next;
				ndptr->next=node_ptr;
				*p_ndptr=ndptr;
				p_ndptr=&(ndptr->next);
				//rep_sbstGrp<<": change"<<endl;
				changed=true;
			}else{
				p_ndptr=&(node_ptr->next);
				node_ptr=node_ptr->next;
				//rep_sbstGrp<<": no change"<<endl;
			}
		}
		if(changed==false) break;
	}
}

void orderAlpha_AllSubstGroups(SubstGroup *ptr_substGroup){
	while(ptr_substGroup){
		orderAlpha_substGroup(ptr_substGroup);
		ptr_substGroup=ptr_substGroup->next;
	}
}

unsigned int search_substGroup_elem(SubstGroup *ptr_SubstGroup,char *name){
	nodePointer *node_ptr=ptr_SubstGroup->nodeList;
	int i=1;
	//rep_sbstGrp<<"search elem "<<name<<" in ";
	//rep_sbstGrp<<"substitution group: "<<ptr_SubstGroup->name.transcode()<<endl;
	while(node_ptr){
		//rep_sbstGrp<<"check: "<<i<<endl;
		DOM_Element elem=(DOM_Element &)node_ptr->node;
		char *name2=elem.getAttribute("name").transcode();
		if(build_TBCs::compare_wo_nms(name,name2)){
			//rep_sbstGrp<<"element found, it's the "<<i<<" th"<<endl;
			break;
		}
		i++;
		node_ptr=node_ptr->next;
	}
	if(node_ptr==NULL) return 0;
	else return i;
}

void print_all_substgroups(){
#ifdef WRITE_DEBUG_INFO 
	SubstGroup *ptr_SubstGroup=FirstSubstGroup;
	while(ptr_SubstGroup){
		rep_sbstGrp<<endl;
		rep_sbstGrp<<"substitution group: "<<ptr_SubstGroup->name.transcode()<<endl;
		rep_sbstGrp<<"  has "<<ptr_SubstGroup->num_members<<" members"<<endl;
		nodePointer *node_ptr=ptr_SubstGroup->nodeList;
		while(node_ptr){
			DOM_Element elem=(DOM_Element &)node_ptr->node;
			rep_sbstGrp<<elem.getAttribute("name").transcode()<<endl;
			node_ptr=node_ptr->next;
		}
		ptr_SubstGroup=ptr_SubstGroup->next;
	}
#endif
}

DOM_Element retrieve_substgroup_elem(SubstGroup *ptr_SubstGroup,unsigned int num_elem){
	nodePointer *node_ptr=ptr_SubstGroup->nodeList;
	while(node_ptr&&num_elem){
		node_ptr=node_ptr->next;
		num_elem--;
	}
	DOM_Element elem=(DOM_Element &)node_ptr->node;
	return elem;
}

void insert_element_in_substGroup(DOM_Node element,DOMString name_of_base){
	SubstGroup *ptr_SubstGroup=FirstSubstGroup;

	for(;ptr_SubstGroup!=NULL;){//check, if there is already a Substgroup, for the desired basename
		if(ptr_SubstGroup->name.equals(name_of_base)){
			insert_SubstGrpElem(element,ptr_SubstGroup);
			return;
		}
		ptr_SubstGroup=ptr_SubstGroup->next;
	}
	SubstGroup *ptr_new_SubstGroup=new SubstGroup;//create new substgroup
	ptr_new_SubstGroup->next=NULL;
	ptr_new_SubstGroup->name=name_of_base;
	ptr_new_SubstGroup->nodeList=NULL;
	ptr_new_SubstGroup->num_members=0;

	insert_SubstGrpElem(element,ptr_new_SubstGroup);
	if(FirstSubstGroup==NULL)
		FirstSubstGroup=ptr_new_SubstGroup;
	else{
		ptr_SubstGroup=FirstSubstGroup;
		while(ptr_SubstGroup->next!=NULL) ptr_SubstGroup=ptr_SubstGroup->next;
		ptr_SubstGroup->next=ptr_new_SubstGroup;
	}
}

void extract_SubstGroups(DOM_Document doc){

	DOM_Node node;
	DOM_Element elem;
	DOM_TreeWalker walker=doc.createTreeWalker(doc.getLastChild(),0x00000003,NULL,1);
	DOMString name_subst_group;

	while(1){
		for(node=walker.getCurrentNode();!node.isNull();node=walker.firstChild()){
			if(node.getNodeName().equals("element")){
				elem=(DOM_Element &)node;
				name_subst_group=elem.getAttribute("substitutionGroup");
				if(!name_subst_group.equals("")){
					insert_element_in_substGroup(node,name_subst_group);	
				}
			}
		}
		node=walker.nextSibling();
		if(node.isNull()){	// a brother doesn't exist 
			while(walker.nextSibling().isNull()){
				node=walker.parentNode();
				if(node.isNull()){
					orderAlpha_AllSubstGroups(FirstSubstGroup);//order all substgroups alphabeticall
					return;	//we are in the root node!!
				}
			}
		}
	}
}

