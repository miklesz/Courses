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

#include "position_code.h"
#include "global_header.h"
#include "parse_file.h"
#include "codec.h"
#include "SubstGrp.h"
#include <stdlib.h>
#include <fstream.h>

#ifdef WRITE_DEBUG_INFO 
extern ofstream report;
#endif


bool position_code::is_in_extension_stack(char *name){
	int i;
	for(i=0;i<ext_stack_depth;i++){
		if(!strcmp(name,ext_stack[i])) return true;
	}
	return false;
}


void position_code::write_position(bitstream &bitstream_out, int type_of_path, int path_depth){
	schema_type *type;
	bool in_co=false;
	int M_Co;
	int num_bits;

	if(path_depth>0)
		type=codec::get_path_item(type_of_path,path_depth-1).type;
	else
		type=codec::get_path_item(type_of_path,path_depth).type;

#ifdef WRITE_DEBUG_INFO
	char *name=type->name.transcode();
	report<<"basetype is: "<<name<<endl;
#endif

	if(use_local_poscode(type->type_definition, codec::get_path_item(type_of_path,path_depth).name, &in_co)){
#ifdef WRITE_DEBUG_INFO
		report<<"local position code"<<endl<<flush;
#endif
		num_bits=use_vuimsbf(codec::get_path_item(type_of_path,path_depth).element_declaration);
		if(num_bits==-1)
			bitstream_out.write_var_len_int(codec::get_path_item(type_of_path,path_depth).position);
		else if(num_bits==0);//don't write position at all
		else if(num_bits>4)//if more than 4 bits are necessary=>use vuimsbf
			bitstream_out.write_var_len_int(codec::get_path_item(type_of_path,path_depth).position);
		else
			bitstream_out.write_bits(codec::get_path_item(type_of_path,path_depth).position,num_bits);
	}else{

#ifdef WRITE_DEBUG_INFO
		report<<"global position code"<<endl<<flush;
#endif
		if((M_Co=detect_M_Co(type->type_definition))==-1)
			bitstream_out.write_var_len_int(codec::get_path_item(type_of_path,path_depth).position);
		else{
			num_bits=bitstream_out.bit_length(M_Co);
			if(num_bits<=16)
				bitstream_out.write_bits(codec::get_path_item(type_of_path,path_depth).position,num_bits);
			else
				bitstream_out.write_var_len_int(codec::get_path_item(type_of_path,path_depth).position);
		}
	}		
}

void position_code::read_position(bitstream &bitstream_in, int type_of_path, int path_depth){
	//read position code
	schema_type *type;
	bool in_co=false;
	int M_Co;
	int num_bits;

	if(path_depth>0)
		type=codec::get_path_item(type_of_path,path_depth-1).type;
	else if(codec::get_path_item(PATH,codec::get_path_depth(PATH)-codec::get_path_depth(type_of_path)-1).type){
		type=codec::get_path_item(PATH,codec::get_path_depth(PATH)-codec::get_path_depth(type_of_path)-1).type;//element is first in rel_path, take type of prev_path
	}else return;	

	char *name=type->name.transcode();


	if(use_local_poscode(type->type_definition, codec::get_path_item(type_of_path,path_depth).name, &in_co)){
#ifdef WRITE_DEBUG_INFO	
		report<<"basetype is: "<<name<<endl;
		report<<"local position code";
#endif
		num_bits=use_vuimsbf(codec::get_path_item(type_of_path,path_depth).element_declaration);
#ifdef WRITE_DEBUG_INFO
		if(!num_bits) report<<", position not coded!"<<endl<<flush;
		else report<<endl<<flush;
#endif
		if(num_bits==-1)//maxOccurs==unbounded=>use vuimsbf
			codec::set_position(type_of_path,path_depth,bitstream_in.read_var_len_int());
		else if(num_bits==0)//don't read position at all, default is '1'
			codec::set_position(type_of_path, path_depth,1);
		else if(num_bits>4)//if more than 4 bits are necessary=>use vuimsbf
			codec::set_position(type_of_path,path_depth,bitstream_in.read_var_len_int());
		else
			codec::set_position(type_of_path,path_depth,bitstream_in.read_bits(num_bits));
	}else{
#ifdef WRITE_DEBUG_INFO	
		report<<"global position code"<<endl<<flush;
#endif
		if((M_Co=detect_M_Co(type->type_definition))==-1)
			codec::set_position(type_of_path,path_depth,bitstream_in.read_var_len_int());
		else{
			num_bits=bitstream_in.bit_length(M_Co);
			if(num_bits<=16)
				codec::set_position(type_of_path,path_depth,bitstream_in.read_bits(num_bits));
			else
				codec::set_position(type_of_path,path_depth,bitstream_in.read_var_len_int());
		}
	}
}

int position_code::M_contentModel(DOM_TreeWalker walker){
	int M=0,Mn;
	int N;
				
	char *name=walker.getCurrentNode().getNodeName().transcode();

#ifdef WRITE_DEBUG_INFO	
	report<<"name of element: "<<name<<endl<<flush;
#endif

	DOM_Element elem;
	DOM_Node dom_nd=walker.getCurrentNode();
	elem=(DOM_Element &)dom_nd;
//	elem=(DOM_Element &)walker.getCurrentNode();

	if(elem.getAttribute("maxOccurs").equals("")){	
		if(elem.getAttribute("minOccurs").equals(""))
			N=1;
		else 
			N=atoi(elem.getAttribute("minOccurs").transcode());

	}else if(elem.getAttribute("maxOccurs").equals("unbounded"))
		return -1;
	else{
		N=atoi(elem.getAttribute("maxOccurs").transcode());
	}

	if(!strcmp(name,"sequence")){
		walker.firstChild();
	
		do{
			Mn=M_contentModel(walker);
			if(Mn==-1)
				return -1;
			else
				M+=Mn;
		}while(!walker.nextSibling().isNull());
	
		walker.parentNode();
		return M*N;
	}else if(!strcmp(name,"choice")){
		walker.firstChild();

		do{
			Mn=M_contentModel(walker);
			if(Mn==-1)
				return -1;
			else
				if(Mn>M) M=Mn;
		}while(!walker.nextSibling().isNull());

		walker.parentNode();
		return M*N;
	}else if(!strcmp(name,"all")){
		walker.firstChild();

		do{
			Mn=M_contentModel(walker);
			if(Mn==-1)
				return -1;
			else
				if(Mn>M) M=Mn;
		}while(!walker.nextSibling().isNull());	

		walker.parentNode();
		return M*N;
	}else if(!strcmp(name,"element")){
		return N;
	}else
		return 0;
}

int position_code::detect_M_Co(DOM_Node type){
	bool up=false;
	char *name;
	DOM_Node cand_node;
	DOM_TreeWalker walker=parse_file::get_schema().createTreeWalker(type,0x00000003,NULL,1);
	DOM_Element elem;
	int M=0,Mn=0;

//	bool local_code=true;

	DOMString base_name;
	DOM_Node basetype;
	schema_type *sch_type;

#ifdef WRITE_DEBUG_INFO
	report<<"in detect M Content Model"<<endl<<flush;
#endif

	while(1){

		if(up==false){
			cand_node=walker.getCurrentNode();
			name=cand_node.getNodeName().transcode();
			elem=(DOM_Element &)cand_node;

#ifdef WRITE_DEBUG_INFO
			report<<"name of element: "<<name<<endl<<flush;
#endif

			if(!strcmp(name,"extension")){
				base_name=elem.getAttribute("base");
				//delete_namespace(base_name);
				sch_type=build_TBCs::search_type(base_name,parse_file::get_schema().getLastChild());
				basetype=sch_type->type_definition;
				Mn=detect_M_Co(basetype);
				if(Mn==-1){
#ifdef WRITE_DEBUG_INFO
					report<<"content model is unbounded"<<endl<<flush;
#endif
					return -1;
				}
				else
					if(Mn>M) M=Mn;
				up=false;
			}else if(!strcmp(name,"sequence")||
				!strcmp(name,"choice")||
				!strcmp(name,"all")){
				Mn=M_contentModel(walker);
				if(Mn==-1){
#ifdef WRITE_DEBUG_INFO
					report<<"content model is unbounded"<<endl<<flush;
#endif
					return -1;
				}
				else
					if(Mn>M) M=Mn;
				up=true;
			}else if(!strcmp(name,"restriction"))
				up=false;
		}

		if(up==false&&(!walker.firstChild().isNull())){
			up=false;
		}else if(!walker.nextSibling().isNull()){
			up=false;
		}else if(!walker.parentNode().isNull()){
			up=true;
		}else{
#ifdef WRITE_DEBUG_INFO
			report<<"content model has max number of elements: "<<M<<endl<<flush;
#endif
			return M;
		}
	
	}	
}

//gives back number of bits to code position, -1 if vuimsbf is used
int position_code::use_vuimsbf(DOM_Node element_declaration){
	DOM_Element elem;
	int Occurs;

	if(element_declaration.isNull()) return 0;

	elem=(DOM_Element &)element_declaration;

	if(elem.getAttribute("maxOccurs").equals("")){
		if(elem.getAttribute("minOccurs").equals("")) return 0;
		else{
			Occurs=atoi(elem.getAttribute("minOccurs").transcode());
			if(Occurs>15)
				return -1;
			else 
				return bitstream::bit_length(Occurs);
		}

	}else if(elem.getAttribute("maxOccurs").equals("unbounded")){
		return -1;

	}else if((Occurs=atoi(elem.getAttribute("maxOccurs").transcode()))>15){
		return -1;

	}else if(Occurs==1){
		return 0;

	}else
		return bitstream::bit_length(Occurs);	
}

bool position_code::use_local_poscode(DOM_Node type, char *cand_name, bool *in_co){
	bool up=false;
	bool local_code=true;
	bool local_code_memo;
	DOMString base_name;
	DOM_Node basetype;
	schema_type *sch_type;
	char *name;
	char *ref;
	char *nodename;
	SubstGroup *ptr_substGrp;

	DOM_Node cand_node;
	DOM_TreeWalker walker=parse_file::get_schema().createTreeWalker(type,0x00000003,NULL,1);
	DOM_Element elem;

#ifdef WRITE_DEBUG_INFO
	report<<"in use_local_poscode"<<endl<<flush;
#endif

	while(1){

		if(up==false){
			cand_node=walker.getCurrentNode();
			nodename=cand_node.getNodeName().transcode();
#ifdef WRITE_DEBUG_INFO
			report<<"name of element: "<<nodename<<endl<<flush;
#endif
			elem=(DOM_Element &)cand_node;

			name=elem.getAttribute("name").transcode();
			ref=elem.getAttribute("ref").transcode();

			if(!strcmp(name,cand_name)){
				*in_co=true;
#ifdef WRITE_DEBUG_INFO
				report<<"element "<<name<<" found"<<endl;
#endif
				if(local_code==false) return false;
			}
			if(!strcmp(ref,cand_name)){
				*in_co=true;
#ifdef WRITE_DEBUG_INFO
				report<<"element "<<name<<" found"<<endl;
#endif
				if(local_code==false) return false;
			}
			if(strcmp(ref,"")&&(ptr_substGrp=is_head_of_substitution_group(ref))){
				if(search_substGroup_elem(ptr_substGrp,cand_name)){
					*in_co=true;
#ifdef WRITE_DEBUG_INFO
					report<<"element "<<name<<" found"<<endl;
#endif
					if(local_code==false) return false;		
				}
			}
			if(!strcmp(nodename,"extension")){
				base_name=elem.getAttribute("base");
				//delete_namespace(base_name);//UNnms
				sch_type=build_TBCs::search_type(base_name,parse_file::get_schema().getLastChild());
				basetype=sch_type->type_definition;
				elem=(DOM_Element &)basetype;
				name=elem.getAttribute("name").transcode();

				if(is_in_extension_stack(name)) return true;
				strcpy(ext_stack[ext_stack_depth],name);
				ext_stack_depth++;

				local_code_memo=use_local_poscode(basetype, cand_name, in_co);

				if(ext_stack_depth) ext_stack_depth--;
				else{
					elem=(DOM_Element &)basetype;
					name=elem.getAttribute("name").transcode();
#ifdef WRITE_DEBUG_INFO
					report<<"ext stack underflow!"<<endl;
					report<<" name: "<<name<<endl;
					report<<"return from extension "<<basetype.getNodeName().transcode();
#endif
					exit(0);
				}
				
				if(*in_co==true) return local_code_memo;
			}

			if(!strcmp(nodename,"sequence")||
				!strcmp(nodename,"choice")||
				!strcmp(nodename,"all")){
				if(!occurs_ok(cand_node)){
#ifdef WRITE_DEBUG_INFO
					report<<"occurs_ok: global code"<<endl<<flush;
#endif
					local_code=false;	
				}
			}
			if(!strcmp(nodename,"element")){
				if(!occurs_ok(cand_node)){
#ifdef WRITE_DEBUG_INFO
					report<<"occurs_ok: global code"<<endl<<flush;
#endif
					local_code=false;
					up=true;
				}			
			}
		}

		if(up==false&&(!walker.firstChild().isNull())){
			up=false;
		}else if(!walker.nextSibling().isNull()){
			up=false;
		}else if(!walker.parentNode().isNull()){
			up=true;
		}else{
			if(*in_co)  return local_code;
			else return true;
		};
	
	}	
}

//if return==false, maxOccurs>1 therefore use global poscode
//else maxOcccurs <=1 
bool position_code::occurs_ok(DOM_Node cand_node){
	DOM_Element elem;
	elem=(DOM_Element &)cand_node;

	if(elem.getAttribute("maxOccurs").equals("")){
	
		if(elem.getAttribute("minOccurs").equals(""))
			return true;
		else if(atoi(elem.getAttribute(DOMString::transcode("minOccurs")).transcode())<=1)
			return true;
		else 
			return false;

	}else if(elem.getAttribute("maxOccurs").equals("unbounded")){
		return false;

	}else if(atoi(elem.getAttribute(DOMString::transcode("maxOccurs")).transcode())<=1){
		return true;

	}else
		return false;
}
