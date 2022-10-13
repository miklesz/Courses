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

#include "alphabetical_order.h"
#include "global_header.h"
#include "namespaces.h"
#include <string.h>
#include <iostream.h>
#include <fstream.h>

#ifdef WRITE_DEBUG_INFO 
extern ofstream report;
#endif
     	
extern name_space global_nms;

alphabetical_order::alphabetical_order(){
	for(int i=0;i<MAX_NODE_LIST_LEN;i++){
		lock_listA[i].index=0;
		lock_listB[i].index=0;
		lock_listA[i].signature=NULL;
		lock_listB[i].signature=NULL;
	}
}

alphabetical_order::~alphabetical_order(){
	for(int i=0;i<MAX_NODE_LIST_LEN;i++){
		if(lock_listA[i].signature) delete [] lock_listA[i].signature;
		if(lock_listB[i].signature) delete [] lock_listB[i].signature;
	}
}
/*
set_lock: mark a part of a child-list, so that no alphabetical ordering is perfomed
in this part (the part has been in a sequence in the type definition)
*/
void alphabetical_order::set_lock(bool type, node_list *child_list, unsigned int begin, unsigned int end){
#ifdef WRITE_DEBUG_INFO
	report<<"set_lock, type: "<<type<<" begin: "<<begin<<" end: "<<end<<endl;
#endif
	char signature[1000];
	strcpy(signature,":sequence ");
	if(type==TABLE_A){
		for(unsigned int i=begin;i<end;i++){
			lock_listA[i].index=begin+1;
			DOM_Node dom_nd=child_list->item(i);
			strcat(signature,((DOM_Element &)dom_nd).
			       getAttribute("name").transcode());
//			strcat(signature,((DOM_Element &)child_list->
//					  item(i)).getAttribute("name").transcode());
			strcat(signature," ");
			if(strlen(signature)>900) break;
		}
		lock_listA[begin].signature=new char[strlen(signature)+1];
		strcpy(lock_listA[begin].signature,signature);
	}else{
		for(unsigned int i=begin;i<end;i++){
			lock_listB[i].index=begin+1;
			DOM_Node dom_nd=child_list->item(i);
			strcat(signature,((DOM_Element &)dom_nd).
			       getAttribute("name").transcode());
//			strcat(signature,((DOM_Element &)child_list->item(i)).
//			  getAttribute("name").transcode());
			strcat(signature," ");
			if(strlen(signature)>900) break;
		}
		lock_listB[begin].signature=new char[strlen(signature)+1];
		strcpy(lock_listB[begin].signature,signature);
	}
		
}

int has_global_nms_prefix(char *name){
	int i=1;
	if(name[0]=='g'){
		while(name[i]>='0'&&name[i]<='9') i++;
		if(i>1&&name[i]==':') return i;
		return 0;
	}
	return false;
}

//if child2 is alphabetically previous to child1 => swap childs
//attributes are allways after elements
bool alphabetical_order::compare_childs(bool type, unsigned int index1, unsigned int index2, DOM_Node child1, DOM_Node child2){
	DOM_Element elem;
	char nms_pref1[10];
	char nms_pref2[10];
	char name1[1000],name2[1000];
	int i,i1,i2,result;

	if((child1.getNodeName().equals("attribute"))&&child2.getNodeName().equals("element"))
		return true;
	if((child1.getNodeName().equals("element"))&&child2.getNodeName().equals("attribute"))
		return false;

	if(type==TABLE_A){
		if(lock_listA[index1].signature!=NULL)
			strcpy(name1,lock_listA[index1].signature);
		else{
			elem=(DOM_Element &)child1;
			strcpy(name1,elem.getAttribute("name").transcode());
		}
		if(lock_listA[index2].signature!=NULL)
			strcpy(name2,lock_listA[index2].signature);
		else{
			elem=(DOM_Element &)child2;
			strcpy(name2,elem.getAttribute("name").transcode());
		}	
	}else{
		if(lock_listB[index1].signature!=NULL)
			strcpy(name1,lock_listB[index1].signature);
		else{
			elem=(DOM_Element &)child1;
			strcpy(name1,elem.getAttribute("name").transcode());
		}
		if(lock_listB[index2].signature!=NULL)
			strcpy(name2,lock_listB[index2].signature);
		else{
			elem=(DOM_Element &)child2;
			strcpy(name2,elem.getAttribute("name").transcode());
		}
	}


#ifdef WRITE_DEBUG_INFO
	report<<"compare name1: "<<name1<<"  with name2: "<<name2<<endl;
#endif

	i1=has_global_nms_prefix(name1);
	i2=has_global_nms_prefix(name2);

	if(i1&&i2){
		i=0;
		while(name1[i]!=':') nms_pref1[i]=name1[i];
		nms_pref1[i]=0;
		i=0;
		while(name2[i]!=':') nms_pref2[i]=name2[i];
		nms_pref2[i]=0;
		result=global_nms.compare_nms(nms_pref1,nms_pref2);
		if(result==1) return false; //namespace1 is preceding namespace2 ->alphabetical order correct
		if(result==-1) return true;//namespace2 is preceding namespace1 -> swap
	}

	if(strcmp(&name1[i1],&name2[i2])>0) return true;
	else return false;
}

void alphabetical_order::order_list_alphabetical(bool type,unsigned int begin, node_list *child_list){
	unsigned int i,u,len,v;
	DOM_Element elem;
	bool changed;
	DOM_Node temp_Node;
	unsigned int begin_block1;
	unsigned int begin_block2;

	len=child_list->get_length();
	if(!len) return;
	//else len=len-1;//access of item(i+1) in loop
#ifdef WRITE_DEBUG_INFO
	report<<"order_alpha"<<endl;
	if(type==TABLE_A){
		for(u=0;u<len;u++)
			report<<"["<<u<<"]="<<lock_listA[u].index<<" ";
		report<<endl;
		for(u=0;u<len;u++){
			if(lock_listA[u].signature) report<<"["<<u<<"]="<<"1 ";
			else report<<"["<<u<<"]="<<"0 ";
		}
	}else{
		for(u=0;u<len+1;u++)
			report<<"["<<u<<"]="<<lock_listB[u].index<<" ";
		report<<endl;
		for(u=0;u<len;u++){
			if(lock_listB[u].signature) report<<"["<<u<<"]="<<"1 ";
			else report<<"["<<u<<"]="<<"0 ";
		}
	}	
	report<<endl;
#endif
	while(1){
		changed=false;
#ifdef WRITE_DEBUG_INFO
		report<<"begin: "<<begin<<" len: "<<len<<endl;
#endif
		for(i=begin;i<len;){

			if(type==TABLE_A){
				if((begin_block1=lock_listA[i].index)){
					while(lock_listA[i].index==begin_block1&&i<len) i++;
#ifdef WRITE_DEBUG_INFO
					report<<"lock detected, begin_block1: "<<begin_block1-1<<" end: "<<i<<endl;
#endif
				}else{
					begin_block1=++i;
				}
				if(i==len) break; //group goes till end if list
#ifdef WRITE_DEBUG_INFO
				report<<"compare_childs (A): "<<begin_block1-1<<" with: "<<i<<endl;
#endif
				if(compare_childs(TABLE_A,begin_block1-1,i,child_list->item(begin_block1-1),child_list->item(i))){
					u=i;
					if((begin_block2=lock_listA[u].index)){
						while(lock_listA[u].index==begin_block2) u++;
#ifdef WRITE_DEBUG_INFO
						report<<"lock detected, begin_block2: "<<begin_block2-1<<" end: "<<u<<endl;
#endif
					}else
						begin_block2=++u;
#ifdef WRITE_DEBUG_INFO
					report<<"swap area: begin1: "<<begin_block1-1<<" begin2: "<<begin_block2-1<<" u: "<<u<<endl;
#endif
					child_list->swap_area(begin_block1-1,begin_block2-1,u);

					//set new_lock_list
					for(v=begin_block1-1;v<begin_block1+u-begin_block2;v++)
						lock_listA[v].index=begin_block1;
			
					char *sig_memo=lock_listA[begin_block1-1].signature;
					lock_listA[begin_block1-1].signature=lock_listA[begin_block2-1].signature;
					lock_listA[begin_block2-1].signature=NULL;
					begin_block2=v;//new begin of block 2;
					lock_listA[begin_block2].signature=sig_memo;

					for(;v<u;v++)
						lock_listA[v].index=begin_block2+1;
		
					i=begin_block2;	
					changed=true;
				}
			}else{
				if((begin_block1=lock_listB[i].index)){
					while(lock_listB[i].index==begin_block1&&i<len) i++;
#ifdef WRITE_DEBUG_INFO
					report<<"lock detected, begin_block1: "<<begin_block1-1<<" end: "<<i<<endl;
#endif
				}else{
					begin_block1=++i;
				}if(i==len) break; //group goes till end of list
#ifdef WRITE_DEBUG_INFO
				report<<"compare_childs (B): "<<begin_block1-1<<" with: "<<i<<endl;
#endif
				if(compare_childs(TABLE_B,begin_block1-1,i,child_list->item(begin_block1-1),child_list->item(i))){
					u=i;
					if((begin_block2=lock_listB[u].index)){
						while(lock_listB[u].index==begin_block2) u++;
#ifdef WRITE_DEBUG_INFO
						report<<"lock detected, begin_block2: "<<begin_block2-1<<" end: "<<u<<endl;
#endif
					}else
						begin_block2=++u;
#ifdef WRITE_DEBUG_INFO
					report<<"swap area: begin1: "<<begin_block1-1<<" begin2: "<<begin_block2-1<<" u: "<<u<<endl;
#endif
					child_list->swap_area(begin_block1-1,begin_block2-1,u);

					//set new_lock_list
					for(v=begin_block1-1;v<begin_block1+u-begin_block2;v++)
						lock_listB[v].index=begin_block1;

					char *sig_memo=lock_listB[begin_block1-1].signature;
					lock_listB[begin_block1-1].signature=lock_listB[begin_block2-1].signature;
					lock_listB[begin_block2-1].signature=NULL;
					begin_block2=v;//new begin of block 2;
					lock_listB[begin_block2].signature=sig_memo;

					for(;v<u;v++)
						lock_listB[v].index=begin_block2+1;
				
					i=begin_block2;	
					changed=true;
				}
			}
		}
		if(changed==false) break;
	}

	/*
	//just for test purpose
	report<<"ordered list:"<<endl;
	for(i=begin;i<child_list->get_length();i++){
		char *ch_name=child_list->item(i).getNodeName().transcode();
		report<<ch_name<<"  ";
		elem=(DOM_Element &)child_list->item(i);
		char *name2=elem.getAttribute("name").transcode();
		report<<name2<<endl;
	}
	report<<endl;
	*/
}
