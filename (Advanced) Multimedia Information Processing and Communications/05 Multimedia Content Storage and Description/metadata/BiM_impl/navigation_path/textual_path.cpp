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

#include "textual_path.h"
#include "bitstream.h"
#include "global_header.h"
#include "codec.h"
#include "build_TBCs.h"
#include "namespaces.h"
#include <stdio.h>
#include <fstream.h>

#ifdef WRITE_DEBUG_INFO 
extern ofstream report;
#endif

extern name_space global_nms;	

void textual_path::write_textual_path(int mode){
	unsigned int i;
	char str_buf[200];
	char txt_path_untr[1000];
	int index=0;
	//struct path_type *pathp;
	DOMString typename_wo_nms;
	char *nm_wo_nms;
	int type_of_path;

#ifdef WRITE_DEBUG_INFO
	report<<"write_textual_path"<<endl<<flush;
#endif
	txt_path_untr[0]=0;

	//write length of the binary path as textual number
	sprintf(str_buf,"%d",bitstream::get_bitpointer());//convert bitpointer to text with radix 10
//	itoa(bitstream::get_bitpointer(),str_buf,10);//convert bitpointer to text with radix 10
	index+=sprintf(txt_path_untr,"%s",str_buf);
	index+=sprintf(txt_path_untr+index,"%%");

	if(mode==ABS_ROOT){
		type_of_path=PATH;
		index+=sprintf(txt_path_untr+index,"/");
	}else if(mode==REL&&codec::get_path_depth(PREV_PATH)==0){
		type_of_path=REL_PATH;	
	}
	else{
		type_of_path=REL_PATH;
	}

	for(i=0;i<codec::get_path_depth(type_of_path);i++){
		if(!strcmp(codec::get_path_item(type_of_path,i).name,"go_father")){
#ifdef WRITE_DEBUG_INFO
			report<<"write go_father"<<endl<<flush;
#endif
			index+=sprintf(txt_path_untr+index,"##/");
		}
		else{
			if(codec::get_path_item(type_of_path,i).text_attrib==TEXTUAL){
#ifdef WRITE_DEBUG_INFO
				report<<"write "<<codec::get_path_item(type_of_path,i).name<<endl<<flush;
#endif
				typename_wo_nms=codec::get_path_item(type_of_path,i).type->name;

				if(!strncmp(typename_wo_nms.transcode(),"Anonymous",9));
					//do not write type in case of anonymous type
					//index+=sprintf(txt_path_untr+index,&str_buf[9]);		
				else{
					//delete_namespace(typename_wo_nms);
					sprintf(str_buf,typename_wo_nms.transcode());
					//if(nm_wo_nms=strrchr(str_buf,':')) nm_wo_nms++;//to delete namespace in path
					//else nm_wo_nms=str_buf;

					index+=sprintf(txt_path_untr+index,str_buf);
				}
				index+=sprintf(txt_path_untr+index,"#");

				if((nm_wo_nms=
				    strrchr(codec::get_path_item(type_of_path,i).name,':')))
				  nm_wo_nms++;
				else nm_wo_nms=codec::get_path_item(type_of_path,i).name;
				index+=sprintf(txt_path_untr+index,nm_wo_nms);

				index+=sprintf(txt_path_untr+index,"#");
				sprintf(str_buf,"%d",
					codec::get_path_item(type_of_path,i).position);
//				itoa(codec::get_path_item(type_of_path,i).position,str_buf,10);
				index+=sprintf(txt_path_untr+index,"%s",str_buf);
	
				if(i!=(codec::get_path_depth(type_of_path)-1)){
					index+=sprintf(txt_path_untr+index,"/");
				}
			}
			else if(codec::get_path_item(type_of_path,i).text_attrib==ATTRIBUTE){
#ifdef WRITE_DEBUG_INFO	
				report<<"write "<<codec::get_path_item(type_of_path,i).name<<endl<<flush;
#endif
				index+=sprintf(txt_path_untr+index,"@");
				typename_wo_nms=codec::get_path_item(type_of_path,i).type->name;

				if(!strncmp(typename_wo_nms.transcode(),"Anonymous",9));
					//do not write type in case of anonymous type
					//index+=sprintf(txt_path_untr+index,&str_buf[9]);		
				else{
					//delete_namespace(typename_wo_nms);
					sprintf(str_buf,typename_wo_nms.transcode());
					if((nm_wo_nms=strrchr(str_buf,':'))) nm_wo_nms++;
					else nm_wo_nms=str_buf;

					index+=sprintf(txt_path_untr+index,str_buf);
				}

				index+=sprintf(txt_path_untr+index,"#");
				//if(nm_wo_nms=strrchr(pathp->item(i).name,':')) nm_wo_nms++;
				//else nm_wo_nms=codec::get_path_item(type_of_path,i).name;
				//index+=sprintf(txt_path_untr+index,nm_wo_nms);
				index+=sprintf(txt_path_untr+index,codec::get_path_item(type_of_path,i).name);
			}
		}
	}
	global_nms.retransform_path(txt_path_untr,txt_path);
}

void textual_path::read_textual_path(int type_of_path,const char *textual, int mode){
	char type_name[200];
	char elem_name[200];
	char position[8];
	unsigned int i=0,textpointer=0;
	int index=0;
	//DOM_Node nullnode;
	//nullnode.setNodeValue("nullnode");

#ifdef WRITE_DEBUG_INFO
	report<<"read_textual_path"<<endl;
#endif

	codec::set_path_depth(type_of_path,0);
	if(mode==ABS_ROOT) textpointer++;	//skipping the "/"
	else if((mode==REL)&&(codec::get_path_depth(PREV_PATH)>0)){	//take the  last element of the previous path
		codec::set_path_item(type_of_path,codec::get_path_item(PREV_PATH,codec::get_path_depth(PREV_PATH)-2),0);
#ifdef WRITE_DEBUG_INFO
		report<<"path[0] type is "<<codec::get_path_item(type_of_path,0).type->name.transcode()<<endl;
#endif
		codec::set_path_depth(type_of_path,1);
	}
	while(textual[textpointer]!='\0'){
		codec::clear_path(type_of_path);//ensure, that next path is empty (selection tableA/B in search_declaration)
		if(textual[textpointer]=='@'){
			codec::set_text_attrib(type_of_path,ATTRIBUTE);

			textpointer++;
			for(i=0;textual[textpointer]!='#';i++,textpointer++)
				type_name[i]=textual[textpointer];
			type_name[i]='\0';
			codec::set_typeName(type_of_path,type_name);
	
			textpointer++;
			for(i=0;textual[textpointer]!='#'&&textual[textpointer];i++,textpointer++)
				elem_name[i]=textual[textpointer];
			elem_name[i]='\0';
			codec::set_name(type_of_path,elem_name);

#ifdef WRITE_DEBUG_INFO
			report<<"attribute detected: "<<codec::get_actual_item(type_of_path).name<<endl;
#endif
			//search_declaration();
		}
		else{
			codec::set_text_attrib(type_of_path,TEXTUAL);
			if(!strncmp(&textual[textpointer],"##",2)){	//go to father

				codec::set_actual_item(type_of_path,codec::get_path_item(PREV_PATH,codec::get_path_depth(PREV_PATH)-codec::get_path_depth(type_of_path)-2));
				codec::set_name(type_of_path,"go_father");
#ifdef WRITE_DEBUG_INFO
				char *nombre=codec::get_actual_item(type_of_path).type->name.transcode();
				report<<"detected: go father, type is:"<<nombre<<endl;
#endif
				textpointer+=3;
				codec::increment_path_depth(type_of_path);
				continue;
			}

			for(i=0;textual[textpointer]!='#'&&textual[textpointer];i++,textpointer++)
				type_name[i]=textual[textpointer];
			type_name[i]='\0';
			codec::set_typeName(type_of_path,type_name);

			textpointer++;
			for(i=0;textual[textpointer]!='#'&&textual[textpointer];i++,textpointer++)
				elem_name[i]=textual[textpointer];
			elem_name[i]='\0';
			codec::set_name(type_of_path,elem_name);

#ifdef WRITE_DEBUG_INFO
			report<<"element detected: "<<codec::get_actual_item(type_of_path).name<<endl;
#endif
			textpointer++;
			for(i=0;textual[textpointer]!='/' && textual[textpointer]!='\0';i++,textpointer++)
				position[i]=textual[textpointer];
			position[i]='\0';
			codec::set_position(type_of_path,codec::get_path_depth(type_of_path),atoi(position));
			if(textual[textpointer]=='/') textpointer++;
		}
		codec::increment_path_depth(type_of_path);
	}

	i=codec::get_path_depth(type_of_path);
	/*
	if(mode==ABS_ROOT) pathpointer->depth=0;
	else pathpointer->depth=1;*/
	if(codec::get_path_depth(PREV_PATH)==0||mode==ABS_ROOT) 
		codec::set_path_depth(type_of_path,0);
	else 
		codec::set_path_depth(type_of_path,1);//	else pathpointer->depth=1;
#ifdef WRITE_DEBUG_INFO
	report<<"find declaration"<<endl;
#endif
	for(;codec::get_path_depth(type_of_path)<i;codec::increment_path_depth(type_of_path)){

		if(!strcmp(codec::get_actual_item(type_of_path).name,"go_father")) continue;

			strcpy(type_name,codec::get_actual_item(type_of_path).typeName);
#ifdef WRITE_DEBUG_INFO
			report<<"type_name("<<codec::get_path_depth(type_of_path)<<"): "<<type_name<<endl;
#endif
			if(!strcmp(type_name,"")){
				index=sprintf(type_name,"Anonymous");
				index+=sprintf(type_name+index,codec::get_actual_item(type_of_path).name);
				if(codec::get_path_depth(type_of_path)>0){
					index+=sprintf(type_name+index,"_in_");
					index+=sprintf(type_name+index,codec::get_path_item(type_of_path,codec::get_path_depth(type_of_path)-1).type->name.transcode());
				}
#ifdef WRITE_DEBUG_INFO
				report<<"detect anonymous type! "<<type_name<<endl;
#endif
				codec::set_type(type_of_path,build_TBCs::detect_textual_anonymous_type(type_name));
//				char *antypnm=codec::get_actual_item(type_of_path).type->name.transcode();
			}
			else{
#ifdef WRITE_DEBUG_INFO
				report<<"detect type "<<type_name<<endl;
#endif
				codec::set_type(type_of_path,build_TBCs::search_type(type_name));
			}
			codec::set_theor_type(type_of_path,NULL);
			build_TBCs::search_declaration(type_of_path);	
	}
}

char * textual_path::get_textual_path(){
	return txt_path;
}
