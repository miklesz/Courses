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

#include <stdlib.h>
#include <string.h>
#include <fstream.h>
#include "namespaces.h"
#include "global_header.h"

#ifdef WRITE_DEBUG_INFO 
extern ofstream in_sch_log;
#endif

name_space::name_space(){
	this->first_entry=NULL;
}

name_space::~name_space(){
	NMSTable *p_table=this->first_entry;
	NMSTable *next_p_table;
	while(p_table){
		next_p_table=p_table->next;
		if(p_table->source_nms) delete [] (p_table->source_nms);
		if(p_table->target_nms) delete [] (p_table->target_nms);
		delete(p_table);
		p_table=next_p_table;
	}
}

void name_space::insert_nms_save(char *nms){
	NMSTable *p_table=this->first_entry;
	char *new_nms_entry;
	char *new_tar_entry;
	char new_g_nms[10];
	char char_buffer[10];

	int nms_counter=0;
	while(p_table){
		nms_counter++;
		if(!strcmp(nms,p_table->source_nms)) return; //nms already inserted
		if(p_table->next)
			p_table=p_table->next;
		else
			break;
	}
	new_nms_entry=new char[strlen(nms)+1];
	strcpy(new_nms_entry,nms);

	strcpy(new_g_nms,"g");
	itoa(nms_counter,char_buffer,10);
	strcat(new_g_nms,char_buffer);
	new_tar_entry=new char[strlen(new_g_nms)+1];
	strcpy(new_tar_entry,new_g_nms);

	if(p_table==NULL){
		p_table=new(NMSTable);
		this->first_entry=p_table;
	}else{
		p_table->next=new(NMSTable);
		p_table=p_table->next;
	}

	p_table->next=NULL;
	p_table->source_nms=new_nms_entry;
	p_table->target_nms=new_tar_entry;
}

void name_space::insert_nms_save(char *nms,char *gbl_sh){
	NMSTable *p_table=this->first_entry;
	char *new_nms_entry;
	char *new_tar_entry;

	int nms_counter=0;
	while(p_table){
		nms_counter++;
		if(!strcmp(nms,p_table->source_nms)) return; //nms already inserted
		if(p_table->next)
			p_table=p_table->next;
		else
			break;
	}
	new_nms_entry=new char[strlen(nms)+1];
	strcpy(new_nms_entry,nms);

	new_tar_entry=new char[strlen(gbl_sh)+1];
	strcpy(new_tar_entry,gbl_sh);

	if(p_table==NULL){
		p_table=new(NMSTable);
		this->first_entry=p_table;
	}else{
		p_table->next=new(NMSTable);
		p_table=p_table->next;
	}

	p_table->next=NULL;
	p_table->source_nms=new_nms_entry;
	p_table->target_nms=new_tar_entry;
}

char *name_space::retrieve_nms(char *nms){
	NMSTable *p_table=this->first_entry;
	while(p_table){
		if(!strcmp(nms,p_table->source_nms)) return p_table->target_nms;
		p_table=p_table->next;
	}
	return NULL;//nms is not inserted in the table
}

char *name_space::retrieve_exp_nms(char *nms){
	NMSTable *p_table=this->first_entry;
	while(p_table){
		if(!strcmp(nms,p_table->target_nms)) return p_table->source_nms;
		p_table=p_table->next;
	}
	return NULL;//nms is not inserted in the table
}

void name_space::print_nms(){
	NMSTable *p_table=this->first_entry;
	while(p_table){
		in_sch_log<<p_table->source_nms<<" is mapped to: "<<p_table->target_nms<<endl;		
		p_table=p_table->next;
	}
}

void name_space::order_alpha_nms(){

	bool changed;
	NMSTable *node_ptr;
	NMSTable *ndptr;
	NMSTable **p_ndptr;
	while(1){
		changed=false;
		in_sch_log<<"--------"<<endl;
		node_ptr=this->first_entry;
		p_ndptr=&(this->first_entry);
		while(node_ptr&&node_ptr->next){
			char *name1=node_ptr->source_nms;
			char *name2=node_ptr->next->source_nms;
			in_sch_log<<"compare "<<name1<<" with "<<name2;
			if(strcmp(name1,name2)>0){//name2 is preceding name1
				ndptr=node_ptr->next;
				node_ptr->next=node_ptr->next->next;
				ndptr->next=node_ptr;
				*p_ndptr=ndptr;
				p_ndptr=&(ndptr->next);
				in_sch_log<<": change"<<endl;
				changed=true;
			}else{
				p_ndptr=&(node_ptr->next);
				node_ptr=node_ptr->next;
				in_sch_log<<": no change"<<endl;
			}
		}
		if(changed==false) break;
	}
}

int name_space::is_alpha_order_correct(char *glob_short1,char *glob_short2){
	bool found1=false;
	bool found2=false;
	NMSTable *node_ptr=this->first_entry;
	while(node_ptr){
		if(!strcmp(node_ptr->target_nms,glob_short1)){
			found1=true;
			if(found2) return -1;
		}
		if(!strcmp(node_ptr->target_nms,glob_short2)){
			found2=true;
			if(found1) return 1;
		}
		node_ptr=node_ptr->next;
	}
	return 0;//at least one of the names to compare is not in the list!
}

void name_space::transform_path(const char *path, char *transf_path){
	int path_pos=0;
	int start_pos=0;
	int result_pos=0;
	int qual_end;
	int i;
	char global_nms[50];
	char result_nms[10];
	bool copy=true;
	strcpy(transf_path,"");
	while(1){
		if(!strncmp(&path[path_pos],"http",4)){
			start_pos=path_pos;
			copy=false;
			while(path[path_pos]!='#'&&path[path_pos]){
				if(path[path_pos]==':') qual_end=path_pos;
				path_pos++;
			}
		}

		if(copy==false){
			for(i=0;i<qual_end-start_pos;i++) global_nms[i]=path[start_pos+i];
			global_nms[i]=0;
			strcpy(result_nms,this->retrieve_nms(global_nms));
			i=0;
			while(result_nms[i])
				transf_path[result_pos++]=result_nms[i++];
			copy=true;
			path_pos=qual_end;
		}
		if(copy==true) 
			transf_path[result_pos++]=path[path_pos];
		if(!path[path_pos]) return;
		path_pos++;
	}
}

void name_space::retransform_path(const char *path, char *transf_path){
	int path_pos=0;
	int start_pos=0;
	int result_pos=0;
	int qual_end;
	int i;
	char global_nms[50];
	char result_nms[10];
	bool copy=true;
	strcpy(transf_path,"");
	while(1){
		if(!strncmp(&path[path_pos],"g",1)&&path[path_pos+1]>='0'&&path[path_pos+1]<='9'){
			start_pos=path_pos++;
			while(path[path_pos]>='0'&&path[path_pos]<='9'){	
				path_pos++;
			}
			if(path[path_pos]==':'){
				qual_end=path_pos;
				copy=false;
			}else{
				path_pos=start_pos;
				copy=true;
			}
		}

		if(copy==false){
			for(i=0;i<qual_end-start_pos;i++) global_nms[i]=path[start_pos+i];
			global_nms[i]=0;
			strcpy(result_nms,this->retrieve_exp_nms(global_nms));
			i=0;
			while(result_nms[i])
				transf_path[result_pos++]=result_nms[i++];
			copy=true;
			path_pos=qual_end;
		}
		if(copy==true) 
			transf_path[result_pos++]=path[path_pos];
		if(!path[path_pos]) return;
		path_pos++;
	}
}

name_space::operator=(name_space& nms){

	NMSTable *p_table=nms.first_entry;
	NMSTable *p_table_new;
	NMSTable *p_table_old=NULL;

	char *new_nms_entry;
	char *new_tar_entry;

	while(p_table){

		p_table_new=new(NMSTable);

		if(p_table_old)
			p_table_old->next=p_table_new;

		if(this->first_entry==NULL)
			this->first_entry=p_table_new;

		new_nms_entry=new char[strlen(p_table->source_nms)+1];
		strcpy(new_nms_entry,p_table->source_nms);
		p_table_new->source_nms=new_nms_entry;

		new_tar_entry=new char[strlen(p_table->target_nms)+1];
		strcpy(new_tar_entry,p_table->target_nms);
		p_table_new->target_nms=new_tar_entry;
		p_table_new->next=NULL;
		p_table_old=p_table_new;
		p_table=p_table->next;
	
	}

}