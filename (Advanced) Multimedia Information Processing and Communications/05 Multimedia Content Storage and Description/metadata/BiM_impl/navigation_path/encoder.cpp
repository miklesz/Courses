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
#include "parse_file.h"
#include "polymorphism.h"

#ifdef WRITE_DEBUG_INFO 
extern ofstream report;
#endif

//param poscode: if TRUE write position codes, else don't
void codec::encode_path(int mode, bool poscode){
	unsigned int i;
	int b_len;
	unsigned int num_childs;
	unsigned int num_bits;	
	unsigned int branch;
	unsigned int num_substGrp_chld;
	unsigned int num_polys;
	unsigned int value;
	bitstream bitstream_encoded; //the bitstream, into which this function writes its results

	if(mode==ABS_ROOT){

		b_len=bitstream_encoded.bit_length(parse_file::elements.get_length()+1);
	
		if(!strcmp(path.item(0).name,"")){
#ifdef WRITE_DEBUG_INFO
			report<<"Code root termination, num_bits="<<b_len<<", value="<<((1<<b_len)-1)<<endl<<flush;
#endif
			bitstream_encoded.write_bits(((1<<b_len)-1),b_len);
			return;
		}else{
			branch=parse_file::elements.search_node(path.item(0).name);
#ifdef WRITE_DEBUG_INFO
			report<<"Code root, value="<<branch<<", num_bits="<<b_len<<endl<<flush;
#endif
			bitstream_encoded.write_bits(branch,b_len);
		}

		for(i=1;i<path.depth;i++){
			schema_type *type=path.item(i-1).type;

			//char *type_name=type->name.transcode();

			if(i!=path.depth-1){
				num_childs=type->tableA.get_length();
				num_bits=bitstream_encoded.bit_length(num_childs+2);	//num_childs+3-1
													//+3 for father,extension and termination code
													//-1 as listindex runs from 0 to num_childs-1
				branch=type->tableA.search_node(path.item(i).name)+1;//+1 for father
			}
			else{
				//write the termination code
				num_bits=bitstream_encoded.bit_length(path.item(path.depth-2).type->tableA.get_length()+2);
#ifdef WRITE_DEBUG_INFO
				report<<"write Termination code, next value belongs to tableB of type: ";
				report<<type->name.transcode()<<endl;
#endif
				bitstream_encoded.write_bits((1<<num_bits)-1,num_bits);

				num_childs=type->tableB.get_length();
				num_bits=bitstream_encoded.bit_length(num_childs);	//num_childs+1-1
				                                    //tableB has only extension code
				                                    //therefore only "+1"
				branch=type->tableB.search_node(path.item(i).name);
			}
#ifdef WRITE_DEBUG_INFO
			report<<"writing SBC of "<<path.item(i).name<<", num_bits="<<num_bits;
#endif
	
//process substitution groups
			if((num_childs=
			    path.item(i).SubstGrp_num)){
			  //element is head-element of a substitution group
				if(branch==0){
					if(i!=path.depth-1){
						branch=type->tableA.search_node(path.item(i).SubstGrp->name)+1;
					}else{
						branch=type->tableB.search_node(path.item(i).SubstGrp->name);
					}	
				}
#ifdef WRITE_DEBUG_INFO
				report<<", value="<<branch<<endl;
#endif
				bitstream_encoded.write_bits(branch,num_bits);
#ifdef WRITE_DEBUG_INFO
				report<<"write SubstGrp info"<<endl;
#endif
				if((num_substGrp_chld=
				    path.item(i).SubstGrp_child)){
					num_bits=bitstream_encoded.bit_length(num_childs-1);
					bitstream_encoded.write_bits(1,1);
#ifdef WRITE_DEBUG_INFO
					report<<"writing SubstGrp Code of "<<path.item(i).name<<", num_bits="
							<<num_bits<<", value="<<num_substGrp_chld-1<<endl;
#endif
					bitstream_encoded.write_bits(num_substGrp_chld-1,num_bits);//substitution present
				}else
					bitstream_encoded.write_bits(0,1);//no substitution present	
			}else{
#ifdef WRITE_DEBUG_INFO
				report<<", value="<<branch<<endl;
#endif
				bitstream_encoded.write_bits(branch,num_bits);
			}

			//polyref
//			char *acttypename=path.item(i).type->name.transcode();
			if(path.item(i).theor_type!=NULL)
//				char *theorname=path.item(i).theor_type->name.transcode();

			if(path.item(i).theor_type!=NULL && path.item(i).theor_type->poly!=NULL
				&& path.item(i).text_attrib!=ATTRIBUTE){

				if(path.item(i).type!=path.item(i).theor_type){
#ifdef WRITE_DEBUG_INFO
					report<<"exists polymorphism:"<<endl;
					report<<"type: "<<path.item(i).type->name.transcode()<<endl;
					report<<"theor_type: "<<path.item(i).theor_type->name.transcode()<<endl;
					report<<"writing Type Code of "<<path.item(i).name
						<<", 1 bit, value=1"<<endl;
#endif
					bitstream_encoded.write_bits(1,1);

					DOM_Element poly=path.item(i).theor_type->poly;

					num_polys=atoi(poly.getAttribute("non_abstract_childs").transcode());
					value=polymorphism::search_poly_type(poly,path.item(i).type->name);
					num_bits=bitstream_encoded.bit_length(num_polys);
					bitstream_encoded.write_bits(value,num_bits);

#ifdef WRITE_DEBUG_INFO
					report<<"writing bits of polymorphism, searching type "
						<<path.item(i).type->name.transcode()<<" in the poly_tree "
						<<path.item(i).theor_type->name.transcode()<<endl;
					report<<", value="<<value<<", num_bits="<<bitstream::bit_length(num_polys)
						<<" num_polys: "<<num_polys<<endl;
#endif
				}
				else{
#ifdef WRITE_DEBUG_INFO
					report<<"writing Type Code of "<<path.item(i).name
						<<", 1 bit, value=0, type is: "<<path.item(i).theor_type->name.transcode()<<endl;
#endif
					bitstream_encoded.write_bits(0,1);
				}
			}		
		}
		 
		if(poscode){
			for(i=1;i<path.depth;i++){
//				char *itemname=path.item(i).name;
				//if(path.item(i).node.getNodeType()==DOM_Node::ELEMENT_NODE)
				
				if(path.item(i).text_attrib!=ATTRIBUTE) position_code::write_position(bitstream_encoded,PATH,i);
#ifdef WRITE_DEBUG_INFO	
				report<<"position of: "<<path.item(i).name<<" written!"<<endl;
#endif
			}
		}

	}
	else{	//REL mode
//		compare_paths(mode);
		for(i=0;i<path.depth;i++){
			schema_type *type;
			if(i==0){
				/*
				if(mode==REL) continue;//and 1. element is of simple type	
				else if(mode==ABS_TOP && (prev_path.item(prev_path.depth-1).type==NULL
					||prev_path.item(prev_path.depth-1).type->type_definition.getNodeName().equals("simpleType")))
					   continue;
				else type=prev_path.item(prev_path.depth-1).type; */
				if(prev_path.depth==0){
					b_len=bitstream_encoded.bit_length(parse_file::elements.get_length()+1);
			
					if(!strcmp(path.item(0).name,"")){
#ifdef WRITE_DEBUG_INFO
						report<<"Code root termination, num_bits="<<b_len<<", value="<<((1<<b_len)-1)<<endl<<flush;
#endif
						bitstream_encoded.write_bits(((1<<b_len)-1),b_len);
						return;
					}else{
						branch=parse_file::elements.search_node(path.item(0).name);
#ifdef WRITE_DEBUG_INFO
						report<<"Code root, value="<<branch<<", num_bits="<<b_len<<endl<<flush;
#endif
						bitstream_encoded.write_bits(branch,b_len);
					}
				}
				continue;
			}
			else type=path.item(i-1).type;		
	
			if(i!=path.depth-1){
				num_childs=type->tableA.get_length();
				num_bits=bitstream_encoded.bit_length(num_childs+2);	//num_childs+3-1
				if (!strcmp(path.item(i).name,"go_father")) 
					branch=0;
				else{
					branch=type->tableA.search_node(path.item(i).name);
					if(branch==0){
#ifdef WRITE_DEBUG_INFO
						report<<path.item(i).name<<" not found!!!"<<endl;
#endif
						exit(1);
					}
					branch++;//code one is the extension code
				}
			}
			else{
				//write the termination code
				num_bits=bitstream_encoded.bit_length(path.item(path.depth-2).type->tableA.get_length()+2);
#ifdef WRITE_DEBUG_INFO
				report<<"writing termination ";
#endif
				bitstream_encoded.write_bits((1<<num_bits)-1,num_bits);

				num_childs=type->tableB.get_length();
				num_bits=bitstream_encoded.bit_length(num_childs);	//num_childs+1-1
				branch=type->tableB.search_node(path.item(i).name);
			}
			//bitstream_encoded.write_bits(branch,num_bits);
#ifdef WRITE_DEBUG_INFO			
			report<<"writing SBC of "<<path.item(i).name<<", num_bits="<<num_bits;
#endif

//process substitution groups
			if((num_childs=
			    path.item(i).SubstGrp_num)) {
			  //element is head-element of a substitution group
				if(branch==0){
					if(i!=path.depth-1){
						branch=type->tableA.search_node(path.item(i).SubstGrp->name)+1;
					}else{
						branch=type->tableB.search_node(path.item(i).SubstGrp->name);
					}	
				}
#ifdef WRITE_DEBUG_INFO			
				report<<", value="<<branch<<endl;
#endif
				bitstream_encoded.write_bits(branch,num_bits);
#ifdef WRITE_DEBUG_INFO
				report<<"write SubstGrp info"<<endl;
#endif
				if((num_substGrp_chld=
				    path.item(i).SubstGrp_child)) {
					num_bits=bitstream_encoded.bit_length(num_childs-1);
					bitstream_encoded.write_bits(1,1);
#ifdef WRITE_DEBUG_INFO
					report<<"writing SubstGrp Code of "<<path.item(i).name<<", num_bits="
							<<num_bits<<", value="<<num_substGrp_chld<<endl;
#endif
					bitstream_encoded.write_bits(num_substGrp_chld-1,num_bits);//substitution present
				}else
					bitstream_encoded.write_bits(0,1);//no substitution present	
			}else{
#ifdef WRITE_DEBUG_INFO			
				report<<", value="<<branch<<endl;
#endif
				bitstream_encoded.write_bits(branch,num_bits);
			}
			if(path.item(i).theor_type!=NULL && path.item(i).theor_type->poly!=NULL && branch!=0){
				if(path.item(i).type!=path.item(i).theor_type){
					//exists polymorphim
					bitstream_encoded.write_bits(1,1);
#ifdef WRITE_DEBUG_INFO
					report<<"writing Type Code of "<<path.item(i).name
						<<", 1 bit, value=1"<<endl;
#endif
					DOM_Element poly=path.item(i).theor_type->poly;
					
					num_polys=atoi(poly.getAttribute("non_abstract_childs").transcode());
					value=polymorphism::search_poly_type(poly,path.item(i).type->name);
					num_bits=bitstream_encoded.bit_length(num_polys);

					bitstream_encoded.write_bits(value,num_bits);
#ifdef WRITE_DEBUG_INFO
					report<<"writing bits of polymorphism, searching type "
						<<path.item(i).type->name.transcode()<<" in the poly_tree "
						<<path.item(i).theor_type->name.transcode()<<endl;
					report<<", value="<<value<<", num_bits="<<bitstream_encoded.bit_length(num_polys)
						<<" num_polys: "<<num_polys<<endl;
#endif
				}
				else{			
#ifdef WRITE_DEBUG_INFO
					report<<"writing Type Code of "<<path.item(i).name
						<<", 1 bit, value=0"<<endl;
#endif
					bitstream_encoded.write_bits(0,1);
				}
			}
		}
		if(poscode){
			for(i=1;i<path.depth;i++){
				if(!strcmp(path.item(i).name,"go_father")||path.item(i).element_declaration==NULL)
					continue;
#ifdef WRITE_DEBUG_INFO
				report<<"---------"<<endl;
				report<<"write code of "<<path.item(i).name<<endl;
#endif
				if(path.item(i).text_attrib!=ATTRIBUTE) position_code::write_position(bitstream_encoded,PATH,i);
			}
		}
		prev_path=path;
	}
#ifdef WRITE_DEBUG_INFO
	report<<endl;
#endif
}
