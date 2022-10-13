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

#include "build_TBCs.h"
#include "global_header.h"
#include <fstream.h>

#ifdef WRITE_DEBUG_INFO 
extern ofstream report;
#endif

node_list::node_list(){
	num_nodes=0;
	first=NULL;
}

node_list::~node_list(){
	node_instance *p_nodeI=first;
	node_instance *p_nodeI2;
	//report<<"destructor on "<<p_nodeI<<endl;
	first=NULL;
	while(p_nodeI){
		p_nodeI2=p_nodeI->next;
		delete p_nodeI;
		p_nodeI=p_nodeI2;
	}
}

void node_list::add_node(DOM_Node node,bool checking=true){
	node_instance nodeI;
	node_instance *p_nodeI=this->first;
	node_instance *p_nodeI2;
	//report<<"add_node"<<endl;
	if(checking){	//looking if the node is already inserted in the list	
		DOM_Element elem=(DOM_Element&)node;
		DOMString name=elem.getAttribute("name");
		if(name.equals("")) 
			name=elem.getAttribute("ref");
//		char *check=name.transcode();
		DOM_Element item;
		for(;p_nodeI;){
			item=(DOM_Element&)(p_nodeI->node);
			if(name.equals(item.getAttribute("name"))||name.equals(item.getAttribute("ref"))){
				p_nodeI->node=node;
				return;
			}
			p_nodeI=p_nodeI->next;
		}
	}
	//the node is not inserted
	p_nodeI=new node_instance;
	p_nodeI->next=NULL;
	p_nodeI->node=node;
	p_nodeI->versionNum=1.0f;
	this->num_nodes++;
	if(!first){
		this->first=p_nodeI;
//		report<<"first: "<<p_nodeI<<endl; 
	}else{
		p_nodeI2=this->first;
		while(p_nodeI2->next) p_nodeI2=p_nodeI2->next;
		p_nodeI2->next=p_nodeI;
	}

}

DOM_Node node_list::item(unsigned int num){
	node_instance *p_nodeI=first;
	//report<<"item: "<<num<<endl;
	if(!first){
#ifdef WRITE_DEBUG_INFO
		report<<"child No. "<<num<<"not present in node_list"<<endl;
#endif
		exit(0);
	}
	while(num){
		p_nodeI=p_nodeI->next;
		if(!p_nodeI){
#ifdef WRITE_DEBUG_INFO
			report<<"child No. "<<num<<"not present in node_list"<<endl;
#endif
			exit(0);
		}
		num--;
	}
	return p_nodeI->node;
}

float node_list::getVersion(unsigned int num){
	node_instance *p_nodeI=first;
	//report<<"getVersion"<<endl;
	while(num){
		p_nodeI=p_nodeI->next;
		num--;
	}
	return p_nodeI->versionNum;
}

void node_list::setVersion(unsigned int num, float version){
	node_instance *p_nodeI=first;
	//report<<"setVersion"<<endl;
	while(num){
		p_nodeI=p_nodeI->next;
		num--;
	}
	p_nodeI->versionNum=version;
}

unsigned int node_list::search_node(DOMString name){
	unsigned int i=0;
	DOM_Element elem;
//	char *sname=name.transcode();
	node_instance *p_nodeI=first;
	//report<<"search_node"<<endl<<flush;
	for(;p_nodeI;i++){
		elem=(DOM_Element&)p_nodeI->node;
//		char *chname=elem.getAttribute("name").transcode();
		//if(elem.getAttribute("name").equals(name)||elem.getAttribute("ref").equals(name))
		//UNnms: new compare function:
		if(build_TBCs::compare_wo_nms(elem.getAttribute("name"),name)
			||build_TBCs::compare_wo_nms(elem.getAttribute("ref"),name)) 
			return(i+1); 	//the first code is reserved for the extension code
							//the code for 'go to father' is added in the encoder/decoder
							//as table B does not have this code
							//code 0 signals, that the name has not been found
		p_nodeI=p_nodeI->next;
	}
	return(0);	//the name has not been found
}

unsigned int node_list::get_length(){
	return num_nodes;
}

void node_list::search_extensions_with_base(node_list& list,DOMString name){

	DOM_Element elem;
	DOMString base;
	node_instance *p_nodeI=first;
	//report<<"search extensions with base: "<<name.transcode()<<endl;
	for(;p_nodeI;p_nodeI=p_nodeI->next){
		elem=(DOM_Element&)p_nodeI->node;
		base=elem.getAttribute("base");
		char *basenm=base.transcode();
		//delete_namespace(base);
		/*
		if(base.equals(name))
			list.add_node(elem,false);*/
		if(build_TBCs::compare_wo_nms(name,basenm))
			list.add_node(elem,false);
	}
	//return list;
}

/*
void node_list::set_num_nodes(unsigned int num){
	num_nodes=num;
}*/

void node_list::delete_node(unsigned int num){
	node_instance *p_nodeI=first;
	node_instance *p_nodeI2;
	//report<<"delete node"<<endl<<flush;
	if(num_nodes>0){
		num_nodes--;
		if(num==0){
			first=p_nodeI->next;
			delete p_nodeI;
			return;
		}
		while(num){
			p_nodeI2=p_nodeI;
			p_nodeI=p_nodeI->next;
		}
		p_nodeI2->next=p_nodeI->next;
		delete p_nodeI;
	}
}

void node_list::swap_Nodes(unsigned int num){
	node_instance *p_nodeI=first;
	node_instance *p_nodeI2=first;
	node_instance **pp_nodeI=&(first);
	//report<<"swap node"<<endl<<flush;
	while(num){
		pp_nodeI=&(p_nodeI->next);
		p_nodeI=p_nodeI->next;
		num--;
	}
	if(p_nodeI->next){
		p_nodeI2=p_nodeI->next;
		*pp_nodeI=p_nodeI2;
		p_nodeI2->next=p_nodeI;
		p_nodeI->next=p_nodeI->next->next;
	}
}

void node_list::swap_area(unsigned int begin_block1,unsigned int begin_block2,unsigned int end_block2){
	node_instance *p_nodeI=first;
	node_instance *p_nodeI2;
	node_instance *p_nodeI3;
	node_instance **pp_nodeI=&(first);
	node_instance **pp_nodeI2;
	node_instance **pp_nodeI3;
	unsigned int i=begin_block1;
	//report<<"swap area"<<endl<<flush;
	while(i){
		pp_nodeI=&(p_nodeI->next);
		p_nodeI=p_nodeI->next;
		i--;
	}
	i=begin_block2-begin_block1;
	p_nodeI2=p_nodeI;
	while(i){
		pp_nodeI2=&(p_nodeI2->next);
		p_nodeI2=p_nodeI2->next;
		i--;
	}
	i=end_block2-begin_block2;
	p_nodeI3=p_nodeI2;
	while(i){
		pp_nodeI3=&(p_nodeI3->next);
		p_nodeI3=p_nodeI3->next;
		i--;
	}
	*pp_nodeI=p_nodeI2;
	*pp_nodeI2=p_nodeI3;
	*pp_nodeI3=p_nodeI;
}

/*
void node_list::order_list(){
	unsigned int i;
	node_list new_version;
	//new_version.set_num_nodes(0);
	for(i=0;i<num_nodes;i++){
		if(((DOM_Element&)nodes[i]).getAttribute("version")!=NULL){
			char *nombre=((DOM_Element&)nodes[i]).getAttribute("name").transcode();
			new_version.add_node(nodes[i]);
			delete_node(i);
		}
	}
	for(i=0;i<new_version.get_length();i++)
		nodes[num_nodes++]=new_version.item(i);
}
*/

void node_list::add_DOM_NodeList(DOM_NodeList list){
	unsigned int i;
	//report<<"add node list"<<endl;
	for(i=0;i<list.getLength();i++)
		this->add_node(list.item(i),false);
}

void node_list::delete_node_list(){
	node_instance *p_nodeI=first;
	node_instance *p_nodeI2;
	first=NULL;
	num_nodes=0;
	while(p_nodeI){
		p_nodeI2=p_nodeI->next;
		delete p_nodeI;
		p_nodeI=p_nodeI2;
	}
}

void node_list::operator=(node_list& list){
	unsigned int i;
	//report<<"= operator"<<endl;
	if(this->first!=NULL){
		this->delete_node_list();
	}
	for(i=0;i<list.get_length();i++){
		this->add_node(list.item(i),false);
		//versionNum[i]=list.getVersion(i);
	}
	num_nodes=i;
//	return(*this);
}
