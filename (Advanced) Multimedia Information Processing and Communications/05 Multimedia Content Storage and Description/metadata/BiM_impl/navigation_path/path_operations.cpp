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

#include "codec.h"
#include "global_header.h"
#include "SubstGrp.h"
#include <iostream.h>
#include <fstream.h>

#ifdef WRITE_DEBUG_INFO 
extern ofstream report;
#endif

xml_element codec::get_path_item(int type_of_path,int num){
	if(type_of_path==PATH)
		return path.item(num);
	else if(type_of_path==REL_PATH)
		return rel_path.item(num);
	else
		return prev_path.item(num);
};

unsigned int codec::get_path_depth(int type_of_path){
	if(type_of_path==PATH)
		return path.depth;
	else if(type_of_path==REL_PATH)
		return rel_path.depth;
	else
		return prev_path.depth;
}

void codec::set_path_depth(int type_of_path, unsigned int value){
	if(type_of_path==PATH)
		path.depth=value;
	else if(type_of_path==REL_PATH)
		rel_path.depth=value;
	else
		prev_path.depth=value;
}

void codec::set_substgroups(int type_of_path, unsigned int child, unsigned int num_members, SubstGroup *substGrp){
	if(type_of_path==PATH){
		path.item(path.depth).SubstGrp_child=child;
		path.item(path.depth).SubstGrp_num=num_members;
		path.item(path.depth).SubstGrp=substGrp;
	}else if(type_of_path==REL_PATH){
		rel_path.item(rel_path.depth).SubstGrp_child=child;
		rel_path.item(rel_path.depth).SubstGrp_num=num_members;
		rel_path.item(rel_path.depth).SubstGrp=substGrp;
	}else{
		prev_path.item(prev_path.depth).SubstGrp_child=child;
		prev_path.item(prev_path.depth).SubstGrp_num=num_members;
		prev_path.item(prev_path.depth).SubstGrp=substGrp;
	}
}

void codec::increment_path_depth(int type_of_path){
	if(type_of_path==PATH)
		path.depth++;
	else if(type_of_path==REL_PATH)
		rel_path.depth++;
	else
		prev_path.depth++;
}

xml_element codec::get_actual_item(int type_of_path){
	if(type_of_path==PATH)
		return path.item(path.depth);
	else if(type_of_path==REL_PATH)
		return rel_path.item(rel_path.depth);
	else
		return prev_path.item(prev_path.depth);
}

xml_element codec::get_previous_item(int type_of_path){
	if(type_of_path==PATH)
		return path.item(path.depth-1);
	else if(type_of_path==REL_PATH)
		return rel_path.item(rel_path.depth-1);
	else
		return prev_path.item(prev_path.depth-1);
}

xml_element codec::get_next_item(int type_of_path){
	if(type_of_path==PATH)
		return path.item(path.depth+1);
	else if(type_of_path==REL_PATH)
		return rel_path.item(rel_path.depth+1);
	else
		return prev_path.item(prev_path.depth+1);
}

void codec::set_actual_item(int type_of_path,xml_element new_item){
	if(type_of_path==PATH)
		path.item(path.depth)=new_item;
	else if(type_of_path==REL_PATH)
		rel_path.item(rel_path.depth)=new_item;
	else
		prev_path.item(prev_path.depth)=new_item;
}

void codec::set_path_item(int type_of_path,xml_element new_item, int num){
	if(type_of_path==PATH)
		path.item(num)=new_item;
	else if(type_of_path==REL_PATH)
		rel_path.item(num)=new_item;
	else
		prev_path.item(num)=new_item;
}

void codec::set_text_attrib(int type_of_path,bool text_attrib){
	if(type_of_path==PATH)
		path.item(path.depth).text_attrib=text_attrib;
	else if(type_of_path==REL_PATH)
		rel_path.item(rel_path.depth).text_attrib=text_attrib;
	else
		prev_path.item(prev_path.depth).text_attrib=text_attrib;
}

void codec::set_position(int type_of_path,int path_depth, unsigned int position){
	if(type_of_path==PATH)
		path.item(path_depth).position=position;
	else if(type_of_path==REL_PATH)
		rel_path.item(path_depth).position=position;
	else
		prev_path.item(path_depth).position=position;
}

void codec::set_name(int type_of_path,char *name){
	if(type_of_path==PATH)
		strcpy(path.item(path.depth).name,name);
	else if(type_of_path==REL_PATH)
		strcpy(rel_path.item(rel_path.depth).name,name);
	else
		strcpy(prev_path.item(prev_path.depth).name,name);
}

void codec::clear_path(int type_of_path){
	if(type_of_path==PATH)
		strcpy(path.item(path.depth+1).name,"");
	else if(type_of_path==REL_PATH)
		strcpy(rel_path.item(rel_path.depth+1).name,"");
	else
		strcpy(prev_path.item(prev_path.depth+1).name,"");
}

void codec::set_typeName(int type_of_path,char *typeName){
	if(type_of_path==PATH)
		strcpy(path.item(path.depth).typeName,typeName);
	else if(type_of_path==REL_PATH)
		strcpy(rel_path.item(rel_path.depth).typeName,typeName);
	else
		strcpy(prev_path.item(prev_path.depth).typeName,typeName);
}

void codec::set_elem_declaration(int type_of_path, DOM_Element elem){
	if(type_of_path==PATH)
		path.item(path.depth).element_declaration=elem;
	else if(type_of_path==REL_PATH)
		rel_path.item(rel_path.depth).element_declaration=elem;
	else
		prev_path.item(prev_path.depth).element_declaration=elem;
}

void codec::set_type(int type_of_path,schema_type *type){
	if(type_of_path==PATH)
		path.item(path.depth).type=type;
	else if(type_of_path==REL_PATH)
		rel_path.item(rel_path.depth).type=type;
	else
		prev_path.item(prev_path.depth).type=type;
}

void codec::set_theor_type(int type_of_path,schema_type *type){
	if(type_of_path==PATH)
		path.item(path.depth).theor_type=type;
	else if(type_of_path==REL_PATH)
		rel_path.item(rel_path.depth).theor_type=type;
	else
		prev_path.item(prev_path.depth).theor_type=type;
}

void codec::compare_paths(unsigned int desired_mode){
	//desired_mode specifies in which mode we want to have the rel_path
	//at the end of the function. Usually we want it in REL mode, but sometimes
	//we want it in ABS_TOP mode. path and prev_path must be expressed
	//in ABS_ROOT mode
	unsigned int i,j;

#ifdef WRITE_DEBUG_INFO
	report<<"compare paths"<<endl;
	for(i=0;i<prev_path.depth;i++){
		report<<"prev_path("<<i<<") is: "<<prev_path.item(i).name<<" / "<<prev_path.item(i).type->name.transcode()<<endl;
	}
	for(i=0;i<path.depth;i++){
		report<<"path("<<i<<") is: "<<path.item(i).name<<" / "<<path.item(i).type->name.transcode()<<endl;
	}
#endif
	rel_path.depth=0;
	/*
	if (desired_mode==ABS_TOP && prev_path.depth>2) 
		prev_path.depth=2;*/

	for(i=0;i<prev_path.depth && i<path.depth && path.item(i)==prev_path.item(i);i++);

	for(j=prev_path.depth; j>i ; j--){
		rel_path.item(rel_path.depth)=prev_path.item(j-2);
		strcpy(rel_path.item(rel_path.depth++).name,"go_father");
	}

	for( ; i<path.depth ; i++){
		rel_path.item(rel_path.depth++)=path.item(i);
	}
#ifdef WRITE_DEBUG_INFO
	for(i=0;i<rel_path.depth;i++){
		report<<"rel_path("<<i<<") is: "<<rel_path.item(i).name<<" / "<<rel_path.item(i).type->name.transcode()<<endl;
	}
#endif	
}
