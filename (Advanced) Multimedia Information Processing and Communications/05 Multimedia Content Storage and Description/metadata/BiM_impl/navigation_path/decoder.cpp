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

#include "codec.h"
#include "global_header.h"
#include "bitstream.h"
#include "position_code.h"
#include "polymorphism.h"
#include "build_TBCs.h"
#include "parse_file.h"
#include "SubstGrp.h"

#ifdef WRITE_DEBUG_INFO 
extern ofstream report;
#endif

void codec::decode_path(int mode, bool poscode){
	DOM_Node node;
	SubstGroup *ptr_SubstGroup;
	DOM_Element elem;
	unsigned int value,num_substGrp_elem;
	schema_type *type;
	bool last_element=false;
	unsigned int num_bits,num_childs,num_polys;
	bitstream bitstream_decoded;

#ifdef WRITE_DEBUG_INFO	
	report<<"in decode_binary"<<endl<<flush;
#endif
	//type=path.item(0).type;
	rel_path.depth=0;

	if(mode==ABS_ROOT){
		//read root----------------------------------------
		num_bits=bitstream_decoded.bit_length(parse_file::elements.get_length()+1);
	
		value=bitstream_decoded.read_bits(num_bits);
		if(value==(unsigned int)((1<<num_bits)-1)) return;//termination code in root!

		node=parse_file::elements.item(value-1);
		elem=(DOM_Element &)node;

		if(elem.hasChildNodes()){	//anonymous type
			DOMString type="AnonymousRoot";
			path.item(0).type=build_TBCs::search_type(type,elem);
		}
		else{
			DOMString type=elem.getAttribute("type");
			//delete_namespace(type);
			path.item(0).type=build_TBCs::search_type(type);
		}

		path.item(0).element_declaration=node;

		strcpy(path.item(0).name,elem.getAttribute("name").transcode());
	
#ifdef WRITE_DEBUG_INFO
		char *iname=path.item(0).name;
		report<<"detected root node "<<iname<<endl;
#endif
		//--------------------------------------------------
		//read rest of path
		path.depth=1;
		while(1){
		
			type=path.item(path.depth-1).type;
//			char *type_name=type->name.transcode();

			//NF-Bif(num_childs=detect_if_extended_dec(type_name,true));
			//NF-Belse
			num_childs=type->tableA.get_length();
	
				
			num_bits=bitstream_decoded.bit_length(num_childs+2);//+3-1
			value=bitstream_decoded.read_bits(num_bits);


			if(value==(unsigned int)(1<<num_bits)-1){	//all bits=1,termination code
#ifdef WRITE_DEBUG_INFO
				report<<"detected termination code. Next value belongs to table B of the type "
					<<type->name.transcode()<<endl;
#endif
				//NF-Bif(num_childs=detect_if_extended_dec(type_name,false));
				//NF-Belse
					num_childs=type->tableB.get_length();

				value=bitstream_decoded.read_bits(bitstream_decoded.bit_length(num_childs));
	
				node=type->tableB.item(value-1);//extension code has code 0
				last_element=true;
			}
			else{
				num_childs=type->tableA.get_length()+2;
				if(value>num_childs){
#ifdef WRITE_DEBUG_INFO
					report<<"detected unknown child => skip this BiM unit"<<endl;
#endif
					return;
				}
				node=type->tableA.item(value-2);
				last_element=false;
			}

			//process substgroups
			elem=(DOM_Element &) node;
			DOMString elm_name=elem.getAttribute("name");
			if(elm_name.equals("")) elm_name=elem.getAttribute("ref");

			if((ptr_SubstGroup=
			    is_head_of_substitution_group(elm_name))){
#ifdef WRITE_DEBUG_INFO
					report<<"read substitution group info"<<endl;
#endif
				if(bitstream_decoded.read_bits(1)){//substitution present!!
					num_childs=ptr_SubstGroup->num_members;
					num_substGrp_elem=bitstream_decoded.read_bits(bitstream_decoded.bit_length(num_childs-1));
#ifdef WRITE_DEBUG_INFO
					report<<"num_members: "<<num_childs<<", child: "<<num_substGrp_elem<<endl;
#endif
					elem=retrieve_substgroup_elem(ptr_SubstGroup,num_substGrp_elem);
				}
			}	
			
			if(elem.getAttribute("ref")!=NULL){	//reference!!
				DOMString ref=elem.getAttribute("ref");
				//char *refname=ref.transcode();
				//delete_namespace(ref);//UNnms
				unsigned int num;
				if(elem.getNodeName().equals("element")){
					num=parse_file::elements.search_node(ref);
					{
					  DOM_Node dom_nd=
					    parse_file::elements.item(num-1);//ch_index
					  elem=((DOM_Element&)dom_nd);
					}
//					elem=(DOM_Element&)parse_file::elements.item(num-1);//ch_index
				}
				else if(elem.getNodeName().equals("attribute")){
					num=parse_file::attributes.search_node(ref);
					{
					  DOM_Node dom_nd=
					    parse_file::attributes.item(num-1);//ch_index
					  elem=((DOM_Element&)dom_nd);
					}
//					elem=(DOM_Element&)parse_file::attributes.item(num-1);//ch_index
				}
			}
			//char *copyname=elem.getAttribute("name").transcode();
			strcpy(path.item(path.depth).name,elem.getAttribute("name").transcode());
			if(!strncmp(path.item(path.depth).name,"dummy_child",11)){
#ifdef WRITE_DEBUG_INFO
				report<<"detected extended child => skip this unit";
#endif
				return;
			}

			path.item(path.depth).element_declaration=node;
	
#ifdef WRITE_DEBUG_INFO
			report<<"detected node "<<path.item(path.depth).name<<endl;
#endif
			DOMString types=elem.getAttribute("type");
			if(types==NULL){	//Anonymous type
				DOMString name="Anonymous";
				name.appendData(elem.getAttribute("name"));
				if(elem.getParentNode().getParentNode()!=parse_file::get_schema()){
					name.appendData("_in_");
					name.appendData(path.item(path.depth-1).type->name);
				}
			
				path.item(path.depth).type=build_TBCs::search_type(name,elem);
				path.item(path.depth).theor_type=path.item(path.depth).type;
			}
			else{
				//delete_namespace(types);
			
				path.item(path.depth).type=build_TBCs::search_type(types);
				path.item(path.depth).theor_type=path.item(path.depth).type;

#ifdef WRITE_DEBUG_INFO
				report<<"check, if polymorphism, type: ";
				report<<path.item(path.depth).type->name.transcode()<<endl;
//				char *name_decl=path.item(path.depth).element_declaration.getNodeName().transcode();
				if(path.item(path.depth).type->poly!=NULL)
					report<<"polypointer not zero"<<endl;
#endif
				if(path.item(path.depth).type->poly!=NULL 
					&& (path.item(path.depth).element_declaration.getNodeName().equals("element")||
						!strcmp(path.item(path.depth).name,"simple_content"))){
					int type_code=bitstream_decoded.read_bits(1);
#ifdef WRITE_DEBUG_INFO
					report<<"taking 1 bit of polymorphism"<<endl;
#endif
					if(type_code){

						DOM_Element poly=path.item(path.depth).type->poly;
						num_polys=atoi(poly.getAttribute("non_abstract_childs").transcode());
						unsigned int poly_value=bitstream_decoded.read_bits(bitstream_decoded.bit_length(num_polys));
						DOMString new_type=polymorphism::get_poly_type(poly,poly_value);
						path.item(path.depth).type=build_TBCs::search_type(new_type);
#ifdef WRITE_DEBUG_INFO
						report<<"polymorphism detected!!!"<<endl;
						report<<"number of childs of "<<poly.getNodeName().transcode()<<": "<<num_polys<<endl;
						report<<"new type is "<<path.item(path.depth).type->name.transcode()<<endl;
#endif
					}
				}
			}
			path.depth++;
			if(last_element){
		
				// read the position codes
				unsigned int i;
				path.item(0).position=1;//position of root element always 1!
				for(i=1;i<path.depth;i++){
					if(path.item(i).text_attrib!=ATTRIBUTE&&poscode) position_code::read_position(bitstream_decoded,PATH,i);
					else path.item(i).position=1;
#ifdef WRITE_DEBUG_INFO
					report<<"detected position of "<<path.item(i).name
						<<", value="<<path.item(i).position<<endl;
#endif
				}
			
				break;
			}		
		}
	}
	else{	//REL mode
#ifdef WRITE_DEBUG_INFO
		report<<"rel mode"<<endl<<flush;
#endif
		if(path.depth>0) path.depth--;

		while(1){
			if(path.depth==0){
				num_childs=parse_file::elements.get_length();
				num_bits=bitstream_decoded.bit_length(num_childs+1);
				value=bitstream_decoded.read_bits(num_bits);
				if(value==(unsigned int)((1<<num_bits)-1)) return;//termination code in root!
				last_element=false;

				node=parse_file::elements.item(value-1);
				elem=(DOM_Element &)node;

				if(elem.hasChildNodes()){	//anonymous type
						DOMString type="AnonymousRoot";
					path.item(0).type=build_TBCs::search_type(type,elem);
				}
				else{
					DOMString type=elem.getAttribute("type");
					//delete_namespace(type);
					path.item(0).type=build_TBCs::search_type(type);
				}

				path.item(0).element_declaration=node;
				strcpy(path.item(0).name,elem.getAttribute("name").transcode());
				rel_path.item(rel_path.depth)=path.item(0);	

#ifdef WRITE_DEBUG_INFO
				char *nombre=path.item(0).name;
				report<<"detected root node "<<nombre<<endl;
#endif
				//path.depth++;	
			}
			else{
			
//				char *type_name=path.item(path.depth-1).typeName;
				
				if(rel_path.depth>0)
					type=rel_path.item(rel_path.depth-1).type;
				else
					type=path.item(path.depth-1).type;

				num_childs=type->tableA.get_length();
				num_bits=bitstream_decoded.bit_length(num_childs+2);//+3-1
		
				value=bitstream_decoded.read_bits(num_bits);
			
				if(value==(unsigned int)(1<<num_bits)-1){	//all bits=1,termination code
#ifdef WRITE_DEBUG_INFO
					report<<"detected termination code. Next value belongs to table B of the type "
						<<type->name.transcode()<<endl;
#endif
					num_childs=type->tableB.get_length();
					value=bitstream_decoded.read_bits(bitstream_decoded.bit_length(num_childs));
			
					if(rel_path.depth>0){
						type=rel_path.item(rel_path.depth-1).type;
						node=rel_path.item(rel_path.depth-1).element_declaration;
					}else{
						type=path.item(path.depth-1).type;
						node=path.item(path.depth-1).element_declaration;
					}
			
				
					if(!value);//handle extension code here
					else
						node=type->tableB.item(value-1);
					last_element=true;
				}
				else if(value==0){
					if(path.depth>0) path.depth--;
					//if((path.depth-1-rel_path.depth)>0){
					if(path.depth>0){
						rel_path.item(rel_path.depth).type=path.item(path.depth-1).type;//set type of go father to 
//						char *name=rel_path.item(rel_path.depth).type->name.transcode();
					}

					node=path.item(path.depth).element_declaration;
		
				}
				else{
				
					node=type->tableA.item(value-2);
#ifdef WRITE_DEBUG_INFO
					for(unsigned int u=0;u<type->tableA.get_length();u++){
						report<<"child "<<u<<" is: "<<type->tableA.item(u).getNodeName().transcode();
						{
						  DOM_Node dom_nd=
						    type->tableA.item(u);
						  elem=((DOM_Element&)dom_nd);
						}
//						elem=(DOM_Element&)type->tableA.item(u);	
						report<<"  name:"<<elem.getAttribute("name").transcode();
						report<<endl<<flush;
					}
#endif
					last_element=false;
				}

				//process substgroups
				elem=(DOM_Element &) node;
				DOMString elm_name=elem.getAttribute("name");
				if(elm_name.equals("")) elm_name=elem.getAttribute("ref");

				if((ptr_SubstGroup=
				    is_head_of_substitution_group(elm_name))){
#ifdef WRITE_DEBUG_INFO
					report<<"read substitution group info"<<endl;
#endif
					if(bitstream_decoded.read_bits(1)){//substitution present!!
						num_childs=ptr_SubstGroup->num_members;
						num_substGrp_elem=bitstream_decoded.read_bits(bitstream_decoded.bit_length(num_childs-1));
#ifdef WRITE_DEBUG_INFO
						report<<"num_members: "<<num_childs<<", child: "<<num_substGrp_elem<<endl;
#endif
						elem=retrieve_substgroup_elem(ptr_SubstGroup,num_substGrp_elem);
					}
				}		
				
				if(elem.getAttribute("ref")!=NULL){	//reference!!
					DOMString ref=elem.getAttribute("ref");
//					char *nombre=ref.transcode();
					//delete_namespace(ref);//UNnms
					unsigned int num;
					if(elem.getNodeName().equals("element")){
						num=parse_file::elements.search_node(ref);
						{
						  DOM_Node dom_nd=
						    parse_file::elements.item(num-1);//ch_index
						  elem=((DOM_Element&)dom_nd);
						}
//						elem=(DOM_Element&)parse_file::elements.item(num-1);//ch_index
					}
					else if(elem.getNodeName().equals("attribute")){
						num=parse_file::attributes.search_node(ref);
						{
						  DOM_Node dom_nd=
						    parse_file::attributes.item(num-1);//ch_index
						  elem=((DOM_Element&)dom_nd);
						}
//						elem=(DOM_Element&)parse_file::attributes.item(num-1);//ch_index
					}
				}

				//set attribute correctly;
				if(elem.getNodeName().equals("attribute"))
					rel_path.item(rel_path.depth).text_attrib=ATTRIBUTE;
				else
					rel_path.item(rel_path.depth).text_attrib=TEXTUAL;

				if(value!=0){
					strcpy(rel_path.item(rel_path.depth).name,elem.getAttribute("name").transcode());
					strcpy(path.item(path.depth).name,rel_path.item(rel_path.depth).name);
				}else strcpy(rel_path.item(rel_path.depth).name,"go_father");

		
				path.item(path.depth).element_declaration=node;//InAg
				rel_path.item(rel_path.depth).element_declaration=node;
				
#ifdef WRITE_DEBUG_INFO			
				report<<"detected node "<<rel_path.item(rel_path.depth).name<<endl;
#endif			
				DOMString types=elem.getAttribute("type");
	
				if(types==NULL&&value!=0){	//Anonymous type
					DOMString name="Anonymous";
					name.appendData(elem.getAttribute("name"));
					if(elem.getParentNode().getParentNode()!=parse_file::schema){
						name.appendData("_in_");
						if(rel_path.depth>0)
							name.appendData(rel_path.item(rel_path.depth-1).type->name);
						else
							name.appendData(path.item(path.depth-1).type->name);
					}
					rel_path.item(rel_path.depth).type=build_TBCs::search_type(name,elem);
					rel_path.item(rel_path.depth).theor_type=rel_path.item(rel_path.depth).type;
					path.item(path.depth).type=rel_path.item(rel_path.depth).type;//InAg
					path.item(path.depth).theor_type=path.item(path.depth).type;//InAg
				}
				else if(value!=0){

					rel_path.item(rel_path.depth).type=build_TBCs::search_type(types);
					rel_path.item(rel_path.depth).theor_type=rel_path.item(rel_path.depth).type;
					path.item(path.depth).type=rel_path.item(rel_path.depth).type;//InAg
					path.item(path.depth).theor_type=path.item(path.depth).type;//InAg
					//UNout if(value!=0 && path.item(path.depth).type->poly!=NULL){
					if(value!=0 && rel_path.item(rel_path.depth).type->poly!=NULL){
						int type_code=bitstream_decoded.read_bits(1);
	//					report<<"taking 1 bit of polymorphism"<<endl;
						if(type_code){
	
							DOM_Element poly=rel_path.item(rel_path.depth).type->poly;
					//		unsigned int num_polys=poly.getparse_file::elementsByTagName("*").getLength();
							unsigned int num_polys=atoi(poly.getAttribute("non_abstract_childs").transcode());
							unsigned int poly_value=bitstream_decoded.read_bits(bitstream_decoded.bit_length(num_polys));
							DOMString new_type=polymorphism::get_poly_type(poly,poly_value);
							path.item(path.depth).type=build_TBCs::search_type(new_type);//InAg
							rel_path.item(rel_path.depth).type=path.item(path.depth).type;

#ifdef WRITE_DEBUG_INFO
							report<<"polymorphism detected!!!"<<endl;
							report<<"number of childs of "<<poly.getNodeName().transcode()<<": "<<num_polys<<endl;
							report<<"new type is "<<rel_path.item(rel_path.depth).type->name.transcode()<<endl;
#endif
						}
					}					
				}
			}
	
			rel_path.depth++;
			if(value) path.depth++;//InAg

			if(last_element){
		
				//we must read the position codes
				unsigned int i;
#ifdef WRITE_DEBUG_INFO	
				for(i=0;i<rel_path.depth;i++){
					report<<"rel_path("<<i<<")="<<rel_path.item(i).name;
					report<<", typeName: "<<rel_path.item(i).typeName;
					report<<", type->name: "<<rel_path.item(i).type->name.transcode()<<endl;
				}
				report<<endl;
				for(i=0;i<path.depth;i++){
					report<<"path("<<i<<")="<<path.item(i).name;
					report<<", typeName: "<<path.item(i).typeName;
					if(path.item(i).type)
						report<<", type->name: "<<path.item(i).type->name.transcode()<<endl;
				}
#endif
				for(i=0;i<rel_path.depth;i++){
					if(strcmp(rel_path.item(i).name,"go_father")){
					
						if(rel_path.item(i).text_attrib!=ATTRIBUTE&&poscode){
#ifdef WRITE_DEBUG_INFO	
							report<<"---------"<<endl;
							report<<"read code of "<<rel_path.item(i).name<<endl;
#endif
							position_code::read_position(bitstream_decoded,REL_PATH,i);
						}
						else rel_path.item(i).position=1;
#ifdef WRITE_DEBUG_INFO
						report<<"detected position of "<<rel_path.item(i).name
							<<", value="<<rel_path.item(i).position<<endl;
#endif
					}
				}
				
				break;
			}
		}
	}
}
