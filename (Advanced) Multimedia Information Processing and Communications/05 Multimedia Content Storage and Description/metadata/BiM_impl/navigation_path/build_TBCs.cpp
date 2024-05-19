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
#include "build_TBCs.h"
#include "node_list.h"
#include "polymorphism.h"
#include "alphabetical_order.h"
#include "parse_file.h"
#include "global_header.h"
#include "SubstGrp.h"
#include "bitstream.h"

#ifdef WRITE_DEBUG_INFO 
extern ofstream report;
extern ofstream poly_log;
extern ofstream type_log;
extern ofstream comp_log;
#endif

group_stack::group_stack(){
	this->depth=0;
}

void group_stack::push_group(unsigned char type, unsigned int beginA, unsigned int beginB){
	this->beginA[this->depth]=beginA;
	this->beginB[this->depth]=beginB;
	this->type_of_group[this->depth]=type;

#ifdef WRITE_DEBUG_INFO
	if(type==ALL)
		report<<"push, type: ALL"<<endl;
	else if(type==SEQUENCE)
		report<<"push, type: SEQUENCE"<<endl;
	else if(type==CHOICE)
		report<<"push, type: CHOICE"<<endl;
	else
		report<<"push, type: unknown"<<endl;

	report<<"set beginA: "<<beginA<<endl;
	report<<"set beginB: "<<beginB<<endl;
#endif

	this->depth++;
}

void group_stack::pull_group(){
	unsigned char type=this->type_of_group[this->depth-1];

#ifdef WRITE_DEBUG_INFO
	if(type==ALL)
		report<<"pull, type: ALL"<<endl;
	else if(type==SEQUENCE)
		report<<"pull, type: SEQUENCE"<<endl;
	else if(type==CHOICE)
		report<<"pull, type: CHOICE"<<endl;
	else
		report<<"pull, type: unknown"<<endl;
#endif

	if(this->depth>0)
		this->depth--;
}

unsigned char group_stack::get_actual_type(void){
	return this->type_of_group[this->depth-1];
}

unsigned char group_stack::get_previous_type(void){
	if(this->depth>1)
		return this->type_of_group[this->depth-2];
	else
		return 0;
}

unsigned char group_stack::get_beginA(void){

#ifdef WRITE_DEBUG_INFO
	report<<"get  beginA: "<<this->beginA[this->depth-1]<<endl;
#endif

	if(this->depth>0)
		return this->beginA[this->depth-1];
	else
		return 0;
}

unsigned char group_stack::get_beginB(void){

#ifdef WRITE_DEBUG_INFO
	report<<"get  beginB: "<<this->beginB[this->depth-1]<<endl;
#endif

	if(this->depth>0)
		return this->beginB[this->depth-1];
	else
		return 0;
}

bool build_TBCs::has_prefix(DOMString name){	
	unsigned int i;
	DOMString prefix;
	for(i=0;i<name.length(); i++){
		if(name.charAt(i)==':'){
			return true;
		}
	}
	return false;
}

bool build_TBCs::compare_wo_nms(DOMString str1,DOMString str2)//compare without namespace
{
	unsigned int i;
	DOMString nm_wo_nms1=str1;
	DOMString nm_wo_nms2=str2;

	for(i=0;i<str1.length(); i++){
		if(str1.charAt(i)==':'){
			nm_wo_nms1=str1.substringData(i+1,str1.length());
		}
	}
	for(i=0;i<str2.length(); i++){
		if(str2.charAt(i)==':'){
			nm_wo_nms2=str2.substringData(i+1,str2.length());
		}
	}

	if(nm_wo_nms1.equals(nm_wo_nms2)){
		if(!str1.equals(str2)){
#ifdef WRITE_DEBUG_INFO
			comp_log<<"not equal with nms: "<<str1.transcode()<<" to: "<<str2.transcode()<<endl;
#endif
		}
		return true;
	}
	else return false;
}

void build_TBCs::search_declaration(int type_of_path){	
	unsigned int i;
	unsigned int num_subst_elem;
	SubstGroup *ptr_SubstGroup;
	node_list list;
	DOM_Element elem;	


	if(codec::get_path_depth(type_of_path)==0){	//root node!!
		DOM_Element elem;
		
		unsigned int num=parse_file::elements.search_node(codec::get_path_item(type_of_path,0).name);
		if(num==0){
#ifdef WRITE_DEBUG_INFO
			report<<codec::get_path_item(type_of_path,0).name<<" not found in root"<<endl;
#endif
			exit(0);
		}else{
		        DOM_Node dom_nd=
			  parse_file::elements.item(num-1);//ch_index
			elem=(DOM_Element&)dom_nd;
//			elem=(DOM_Element&)parse_file::elements.item(num-1);//ch_index
			codec::set_elem_declaration(type_of_path,elem);
			return;
		}
	}

	if(strcmp(codec::get_next_item(type_of_path).name,"")){
		list=codec::get_previous_item(type_of_path).type->tableA;
	}
	else
		list=codec::get_previous_item(type_of_path).type->tableB;//element is last element in path->use table B

	codec::set_substgroups(type_of_path,0,0,NULL);//initialize the substitutionGroup info
	for(i=0 ; i<list.get_length() ; i++){
	        DOM_Node dom_nd= list.item(i);
		elem=(DOM_Element &) dom_nd;
//		elem=(DOM_Element &) list.item(i);
//		char *nombre=elem.getAttribute("name").transcode();
//		char *name2=codec::get_actual_item(type_of_path).name;
		/*
		if(elem.getAttribute("name").equals(pathpointer->item(pathpointer->depth).name)
		   ||elem.getAttribute("ref").equals(pathpointer->item(pathpointer->depth).name)) 
			break;*/
		if(compare_wo_nms(elem.getAttribute("name"),codec::get_actual_item(type_of_path).name)
		   ||compare_wo_nms(elem.getAttribute("ref"),codec::get_actual_item(type_of_path).name)) 
			break;
	}

	if(i==list.get_length()){
		for(i=0;i<list.get_length();i++){
		        DOM_Node dom_nd= list.item(i);
		        elem=(DOM_Element &) dom_nd;
//			elem=(DOM_Element &) list.item(i);
			DOMString elm_name=elem.getAttribute("name");
			if(elm_name.equals("")) elm_name=elem.getAttribute("ref");
			if((ptr_SubstGroup=
			    is_head_of_substitution_group(elm_name))){
				num_subst_elem=search_substGroup_elem(ptr_SubstGroup,codec::get_actual_item(type_of_path).name);				
			}else
				num_subst_elem=0;
			if(num_subst_elem){
				codec::set_substgroups(type_of_path,num_subst_elem,ptr_SubstGroup->num_members,ptr_SubstGroup);
				break;
			}
		}
	}else{//encoder must insert a '0' bit, if element is head of substitution group,
		  //but no substitution is present
	        {
		  DOM_Node dom_nd= list.item(i);
		  elem=(DOM_Element &) dom_nd;
		}
//		elem=(DOM_Element &) list.item(i);
		DOMString elm_name=elem.getAttribute("name");
		if(elm_name.equals("")) elm_name=elem.getAttribute("ref");

		if((ptr_SubstGroup=
		    is_head_of_substitution_group(elm_name))){
			codec::set_substgroups(type_of_path,0,ptr_SubstGroup->num_members,ptr_SubstGroup);				
		}
	}

	if(i==list.get_length()){

#ifdef WRITE_DEBUG_INFO
		report<<"declaration of "<<codec::get_actual_item(type_of_path).name<<" not found in the "
		   <<codec::get_path_item(type_of_path,codec::get_path_depth(type_of_path)-1).type->name.transcode()<<" type definition!!!"<<endl;

		report<<"ordered list of type: "<<codec::get_path_item(type_of_path,codec::get_path_depth(type_of_path)-1).type->name.transcode()<<", TableA:"<<endl;
		for(i=0;i<codec::get_path_item(type_of_path,codec::get_path_depth(type_of_path)-1).type->tableA.get_length();i++){
			char *ch_name=codec::get_path_item(type_of_path,codec::get_path_depth(type_of_path)-1).type->tableA.item(i).getNodeName().transcode();
			report<<ch_name<<"  ";
			DOM_Node dom_nd=
			  codec::get_path_item(type_of_path,
					       codec::
					       get_path_depth(type_of_path)-1).type->tableA.item(i);
			DOM_Element elem=(DOM_Element &) dom_nd;
//			DOM_Element elem=(DOM_Element &)codec::get_path_item(type_of_path,codec::get_path_depth(type_of_path)-1).type->tableA.item(i);
			char *name2=elem.getAttribute("name").transcode();
			if(!strcmp(name2,"")) name2=elem.getAttribute("ref").transcode();
			report<<name2<<endl;
		}

		report<<endl;
		report<<"ordered list of type: "<<codec::get_path_item(type_of_path,codec::get_path_depth(type_of_path)-1).type->name.transcode()<<", TableB:"<<endl;

		for(i=0;i<codec::get_path_item(type_of_path,codec::get_path_depth(type_of_path)-1).type->tableB.get_length();i++){
			char *ch_name=codec::get_path_item(type_of_path,codec::get_path_depth(type_of_path)-1).type->tableB.item(i).getNodeName().transcode();
			report<<ch_name<<"  ";
			DOM_Node dom_nd=
			  codec::get_path_item(type_of_path,
					       codec::
					       get_path_depth(type_of_path)-1).type->tableB.item(i);
			DOM_Element elem=(DOM_Element &) dom_nd;
//			DOM_Element elem=(DOM_Element &)codec::get_path_item(type_of_path,codec::get_path_depth(type_of_path)-1).type->tableB.item(i);
			char *name2=elem.getAttribute("name").transcode();
			if(!strcmp(name2,"")) name2=elem.getAttribute("ref").transcode();
			report<<name2<<endl;
		}
		report<<endl;
#endif
		exit(1);
	}
	codec::set_elem_declaration(type_of_path,elem);

	if(elem.getAttribute("ref")!=NULL){
		//reference!!
		DOMString ref=elem.getAttribute("ref");
		//delete_namespace(ref);//UNnms
//		char *refname=ref.transcode();
		unsigned int num;
		if(elem.getNodeName().equals("element")){
			num=parse_file::elements.search_node(ref);
			{
			  DOM_Node dom_nd=
			    parse_file::elements.item(num-1);//ch_index
			  elem=(DOM_Element&) dom_nd;
			}
//			elem=(DOM_Element&)parse_file::elements.item(num-1);//ch_index
		}
		else if(elem.getNodeName().equals("attribute")){
			num=parse_file::attributes.search_node(ref);
			{
			  DOM_Node dom_nd=
			    parse_file::attributes.item(num-1);//ch_index
			  elem=(DOM_Element&) dom_nd;
			}
//			elem=(DOM_Element&)parse_file::attributes.item(num-1);//ch_index
		}
	}

	if(elem.getAttribute("type").equals("")){	//anonymous type!!
		DOMString name="Anonymous";
		name.appendData(elem.getAttribute("name"));

		if(elem.getParentNode().getParentNode()!=parse_file::get_schema()){
			name.appendData("_in_");
			name.appendData(codec::get_path_item(type_of_path,codec::get_path_depth(type_of_path)-1).type->name);
		}
//		char *sfn=name.transcode();
		codec::set_type(type_of_path,search_type(name,elem));
	}
	else{ 
		DOMString type=elem.getAttribute("type");

		//delete_namespace(type);
//		char *stp=type.transcode();
		codec::set_theor_type(type_of_path,search_type(type));
	}
}


schema_type* build_TBCs::detect_textual_anonymous_type(char *name){	
	char temp_name[300];

	strcpy(temp_name,name);
	schema_type	*type;
#ifdef WRITE_DEBUG_INFO
	report<<"detect_textual_anonymous_type("<<name<<")"<<endl<<flush;
#endif
	DOM_Node node=parse_file::get_schema().getLastChild();
//	node_list list=parse_file::elements;
	char *pointer;
//	report<<"anonymous_type!!! "<<name<<endl;
	pointer=strrchr(temp_name,'_');	//find last ocurrence of "_"
	while(pointer!=NULL && !strncmp(pointer-3,"_in",3)){	//the anonymous type is not defined under schema
		//looking for another type;
		if(!strncmp(pointer+1,"Anonymous",9)){	//the anonymous type is based in another anonymous_type
			type=detect_textual_anonymous_type(pointer+1);
			return type;
			//list=type->tableB;
		}
		else{
			type=search_type(pointer+1,node);
			return type;
			//list=type->tableB;
		}
		*(pointer-3)='\0';
		pointer=strrchr(temp_name,'_');	//find last ocurrence of "_"
			
	}
	if(pointer==NULL){	//the anonymous type is defined under schema
		unsigned int num=parse_file::elements.search_node(temp_name+9);
		node=parse_file::elements.item(num-1);//ch_index
		type=search_type(name,node);
//		char *nombre=type->name.transcode();
	}
	return type;
}


schema_type* build_TBCs::search_type(DOMString type,DOM_Node startpoint){	
	unsigned int i,list_length;

	bool order_alpha;
	DOM_Element elem;
//	DOM_Node startpoint,nullnode;
	unsigned int beginTableA;
	unsigned int beginTableB;
	schema_type* result_type;

#ifdef WRITE_DEBUG_INFO
	report<<"search_type: "<<type.transcode()<<endl;
//	char *tnmtp=type.transcode();
#endif	
	

/*
	if(start_in.getNodeValue().equals("nullnode"))
		startpoint=parse_file::get_schema().getLastChild();
	else
		startpoint=start_in;
*/
//	char *startname=startpoint.getNodeName().transcode();

	//we search the type in the stack of types
	for(i=0;i<types_read ;i++){
//		char *instypes=types[i].name.transcode();
		if(type.equals(types[i].name)){
#ifdef WRITE_DEBUG_INFO
			report<<"type "<<types[i].name.transcode()<<" already inserted, return it!"<<endl; 
#endif
			return (&types[i]);
		}
	}
#ifdef WRITE_DEBUG_INFO
	if(i==types_read)
		report<<"type: "<<type.transcode()<<" not yet in typevector"<<endl;
#endif
/*
	if(!strncmp(type.transcode(),"Anonymous",9)){
		for(i=0;i<types_read ;i++){
			char *instypes=types[i].name.transcode();
			if(type.equals(types[i].name)){
#ifdef WRITE_DEBUG_INFO
				report<<"type "<<types[i].name.transcode()<<" already inserted, return it!"<<endl; 
#endif
				return (&types[i]);
			}
		}
	}else{
		for(i=0;i<types_read ;i++){
			if(compare_wo_nms(type,types[i].name)){
#ifdef WRITE_DEBUG_INFO
				report<<"type "<<types[i].name.transcode()<<" already inserted, return it!"<<endl; 
				comp_log<<"type found: "<<type.transcode()<<" typetable "<<types[i].name.transcode()<<endl;
#endif
				return (&types[i]);
			}
		}
	}
*/
	// the type is not inserted yet in the types vector

	DOM_NodeList list=startpoint.getChildNodes();
	list_length=list.getLength();

	for(i=0;i<list_length;i++){  
		//it could be better to do this with a walker??

		
		if(list.item(i).getNodeName().equals("annotation")){
			{
			  DOM_Node dom_nd=
			    list.item(i);
			  elem=(DOM_Element&) dom_nd;
			}
//			DOM_Element elem=(DOM_Element &)list.item(i);
			DOM_TreeWalker walker=parse_file::get_schema().createTreeWalker(elem,0x00000003,NULL,1);
			//while(walker.getCurrentNode().getNodeName().equals("appinfo")||
				  //walker.getCurrentNode().getNodeName().equals("annotation"))
			walker.firstChild();
			char *checkname=walker.getCurrentNode().getNodeName().transcode();
			if(strcmp(checkname,"appinfo"));
			else if((result_type=
				 search_type(type,walker.getCurrentNode())))
				return result_type;
		}

		if(!list.item(i).getNodeName().equals("complexType")
			&&!list.item(i).getNodeName().equals("simpleType")
			&&!list.item(i).getNodeName().equals("attributeGroup"))//UNinAtb
			continue;

		{
		  DOM_Node dom_nd=
		    list.item(i);
		  elem=(DOM_Element&) dom_nd;
		}
//		elem=(DOM_Element &) list.item(i);
		if(elem.getAttribute("name").equals(type)|| elem.getAttribute("name").equals("")) 
		//if we are searching for an anonymous type, the attribute name is empty
			break;
		//if(compare_wo_nms(elem.getAttribute("name"),type)) break; //if the type is without prefix, it could not be found
	}
//	char *typenm=elem.getAttribute("name").transcode();
	if(i==list.getLength()){
		//the type is probably a simple type defined in the XML Schema specification 
		//(string,integer,ID,...), so the best is to create a new node under schema and assign
		//it as the the type of the xml element. The type is created without childs, so the
		//node_lists tableA and tableB of the type will be empty
		if(startpoint.getNodeName().equals("appinfo"))
			return (schema_type*) NULL;
		DOM_Element simple=parse_file::get_schema().createElement("simpleType");
		simple.setAttribute("name",type);

		parse_file::get_schema().getLastChild().appendChild(simple);
		elem=simple;
	}else
//		char *l_no_name=list.item(i).getNodeName().transcode();

	//if the type is a simpleType we don't do anything

	if(list.item(i).getNodeName().equals("complexType")||
		list.item(i).getNodeName().equals("attributeGroup")){//UNinAtb
		//DOM_Element elem=(DOM_Element &)list.item(i);
		DOM_TreeWalker walker=parse_file::get_schema().createTreeWalker(elem,0x00000003,NULL,1);
		if(elem.getAttribute("base")!=NULL
			||walker.firstChild().getNodeName().equals("complexContent")
			||walker.getCurrentNode().getNodeName().equals("simpleContent")){	
			//extension/restriction in the old and in the new schema!!!
			DOMString base=elem.getAttribute("base");	//old schema	
			if(base==NULL)		//new schema
			{
			  DOM_Node dom_nd=
			    walker.firstChild();
			  base=((DOM_Element&)dom_nd).
			    getAttribute("base");
			}
//				base=((DOM_Element&)walker.firstChild()).getAttribute("base");
			//delete_namespace(base);
			schema_type *pointer=search_type(base);//recursive function !!!				
			//we add the childs we have found in the recursive calls 
			if(pointer->type_definition.getNodeName().equals("simpleType")){
				DOM_Element content=parse_file::get_schema().createElement("content");
				content.setAttribute("name","simple_content");
				content.setAttribute("type",base);
				types[types_read].tableB.add_node(content,true);
			}
			else{
				types[types_read].tableA=pointer->tableA;
				types[types_read].tableB=pointer->tableB;
			}
		}
		walker.setCurrentNode(list.item(i));
		walker.firstChild();
		//UN_inserted
		//save marks, where alphabetical element ordering begins!
		//(elements of base type come first)
		beginTableA=types[types_read].tableA.get_length();
		beginTableB=types[types_read].tableB.get_length();

		alphabetical_order alo;

		order_alpha=true;
		group_stack gpstck;
		//char *nombre=walker.getCurrentNode().getNodeName().transcode();
		while(walker.getCurrentNode()!=list.item(i)){
			do{
				DOMString name=walker.getCurrentNode().getNodeName();
				//delete_namespace(name);
			
				if(name.equals("element")){
					DOM_Node node=walker.getCurrentNode();
					DOM_Element wa_elem=(DOM_Element &)node;
					types[types_read].tableB.add_node(node,true);

					if(wa_elem.getAttribute("ref")!=NULL){		//reference!!
						DOMString ref=wa_elem.getAttribute("ref");
						//search for the element referenced
						//delete_namespace(ref);//UNnms
						unsigned int num=parse_file::elements.search_node(ref);
						if(num==0){
#ifdef WRITE_DEBUG_INFO
//							char *refname=ref.transcode();
							report<<"Element referenced "<<ref.transcode()<<" not found under schema!!!"<<flush<<endl;
#endif
							exit(1);
						}
						{
						  DOM_Node dom_nd=
						    parse_file::elements.item(num-1);//ch_index
						  wa_elem=((DOM_Element&)dom_nd);
						}
//						wa_elem=(DOM_Element&)parse_file::elements.item(num-1);//ch_index
					}
					if(wa_elem.getAttribute("type")==NULL && wa_elem.hasChildNodes()){	//anonymous type
						DOM_TreeWalker walker=parse_file::get_schema().createTreeWalker(wa_elem,0x00000003,NULL,1);
						if(walker.firstChild().getNodeName().equals("complexType")){
							types[types_read].tableA.add_node(node,true);
//							report<<"anyadimos "<<((DOM_Element&)node).getAttribute("name").transcode()<<" a la tabla A, elementos="
//								<<types[types_read].tableA.get_length()<<endl;
						}
						walker.parentNode();
						break;
					}
					unsigned int j;
					DOMString type=wa_elem.getAttribute("type");
//					char *tpnm=type.transcode();

					//DOMString prefix=delete_namespace(type);
				
					if(!has_prefix(type))	//simpleType
						break;
					else{
						DOM_NodeList list=parse_file::get_schema().getLastChild().getChildNodes();
						for(j=0;j<list.getLength();j++){
							if(!list.item(j).getNodeName().equals("complexType")
								&& !list.item(j).getNodeName().equals("simpleType"))
								continue;
							{
							  DOM_Node dom_nd=
							    list.item(j);
							  wa_elem=((DOM_Element&)dom_nd);
							}
//							wa_elem=(DOM_Element &)list.item(j);
							if(wa_elem.getAttribute("name").equals(type))
								break;
						}
						if((j=list.getLength())){
							for(j=0;j<list.getLength();j++){
								if(!list.item(j).getNodeName().equals("complexType")
									&& !list.item(j).getNodeName().equals("simpleType"))
									continue;
								{
								  DOM_Node dom_nd=
								    list.item(j);
								  wa_elem=((DOM_Element&)dom_nd);
								}
//								wa_elem=(DOM_Element &)list.item(j);
								if(compare_wo_nms(wa_elem.getAttribute("name"),type))
									break;
							}
						}
						if(j==list.getLength()){
#ifdef WRITE_DEBUG_INFO
							report<<"Type "<<type.transcode()<<" not found in the schema!!!"<<endl;
#endif
							//exit(1);
						}else if(list.item(j).getNodeName().equals("complexType")){
							types[types_read].tableA.add_node(node,true);
						}
					}
					break;
				}
				if(name.equals("attribute")){
					types[types_read].tableB.add_node(walker.getCurrentNode(),true);
					//report<<"anyadimos "<<((DOM_Element&)walker.getCurrentNode()).getAttribute("name").transcode()<<" a la tabla B, elementos="
//						<<types[types_read].tableB.get_length()<<endl;
					break;	//es correcto??	
				}
				if(name.equals("attributeGroup")){//UNinAtb
				        DOM_Node dom_nd=
					  walker.getCurrentNode();
					DOMString AttGrp=((DOM_Element&)dom_nd)
					  .getAttribute("ref").transcode();
//					DOMString AttGrp=((DOM_Element &)walker.getCurrentNode()).getAttribute("ref").transcode();
					//DOMString prefix=delete_namespace(AttGrp);
//					char *attgp=AttGrp.transcode();
					schema_type *pointer=search_type(AttGrp);

					for(unsigned int u=0;u<pointer->tableB.get_length();u++)
					  types[types_read].tableB.add_node(pointer->tableB.item(u),true);
				}
				if(name.equals("sequence")){//move into sequence
					gpstck.push_group(SEQUENCE,types[types_read].tableA.get_length(),types[types_read].tableB.get_length());
				}else if(name.equals("choice")){//move into choice
					gpstck.push_group(CHOICE,types[types_read].tableA.get_length(),types[types_read].tableB.get_length());
				}		
			}while(!walker.firstChild().isNull());
			while(walker.nextSibling().isNull()){

				walker.parentNode();
				DOMString name=walker.getCurrentNode().getNodeName();

				if(name.equals("sequence")){//move up out of sequence
					alo.set_lock(TABLE_A,&types[types_read].tableA,gpstck.get_beginA(),types[types_read].tableA.get_length());
					alo.set_lock(TABLE_B,&types[types_read].tableB,gpstck.get_beginB(),types[types_read].tableB.get_length());
					gpstck.pull_group();//decrement group_stack
				}else if(name.equals("choice")){//move up out of choice
					if(gpstck.get_previous_type()==SEQUENCE){
						alo.order_list_alphabetical(TABLE_A,gpstck.get_beginA(), &types[types_read].tableA);
						alo.order_list_alphabetical(TABLE_B,gpstck.get_beginB(), &types[types_read].tableB);
					}
					gpstck.pull_group();//decrement group_stack
				}

				if(walker.getCurrentNode()==list.item(i)) 
					break;
			}
		}	
		//alo.order_list_alphabetical(TABLE_A,gpstck.get_beginA(), &types[types_read].tableA);
		//alo.order_list_alphabetical(TABLE_B,gpstck.get_beginB(), &types[types_read].tableB);

	}
	if(elem.getAttribute("name").equals(""))//if an anonymous type is inserted, the name is empty
		types[types_read].name=type;
	else
		types[types_read].name=elem.getAttribute("name");
//	char *insertedname=types[types_read].name.transcode();
	types[types_read].type_definition=list.item(i);
	//creating the poly_element

#ifdef WRITE_DEBUG_INFO
	report<<"create poly info for: "<<type.transcode()<<endl;
#endif
#ifdef WRITE_POLY_INFO
	poly_log<<"create poly info for: "<<type.transcode()<<endl;
#endif

	types[types_read].poly=polymorphism::create_poly_element(list.item(i));
	
	if(types[types_read].poly!=NULL){
		DOM_Element elem=(DOM_Element&)types[types_read].type_definition;

#ifdef WRITE_DEBUG_INFO
		report<<"type.poly is "<<elem.getAttribute("name").transcode()<<endl;
#endif

		//NOABS if(elem.getAttribute("abstract").equals("true"))
		//NOABS 	types[types_read].poly.setAttribute("abstract","true");
	}else{

#ifdef WRITE_DEBUG_INFO
		report<<"type.poly is Null!"<<endl;
#endif
	}
	//types[types_read].tableA.order_list();
	//types[types_read].tableB.order_list();

#ifdef WRITE_DEBUG_INFO
	report<<"ordered list of type: "<<type.transcode()<<", TableA:"<<endl;
	type_log<<"ordered list of type: "<<type.transcode()<<", TableA:"<<endl;

	for(i=0;i<types[types_read].tableA.get_length();i++){
		char *ch_name=types[types_read].tableA.item(i).getNodeName().transcode();
		report<<ch_name<<"  ";
		type_log<<ch_name<<"  ";
		DOM_Node dom_nd=
		  types[types_read].tableA.item(i);
		DOM_Element elem=(DOM_Element&)dom_nd;
//		DOM_Element elem=(DOM_Element &)types[types_read].tableA.item(i);
		char *name2=elem.getAttribute("name").transcode();
		report<<name2<<endl;
		type_log<<name2<<endl;
	}
	report<<endl;
	type_log<<endl;

	report<<"ordered list of type: "<<type.transcode()<<", TableB:"<<endl;
	type_log<<"ordered list of type: "<<type.transcode()<<", TableB:"<<endl;

	for(i=0;i<types[types_read].tableB.get_length();i++){
		char *ch_name=types[types_read].tableB.item(i).getNodeName().transcode();
		report<<ch_name<<"  ";
		type_log<<ch_name<<"  ";
		DOM_Node dom_nd=
		  types[types_read].tableB.item(i);
		DOM_Element elem=(DOM_Element&)dom_nd;
//		DOM_Element elem=(DOM_Element &)types[types_read].tableB.item(i);
		char *name2=elem.getAttribute("name").transcode();
		report<<name2<<endl;
		type_log<<name2<<endl;
	}
	report<<"types_read: "<<types_read<<endl;
	report<<endl;
	type_log<<endl;
	
#endif
	return (&types[types_read++]);
}

void build_TBCs::write_type_statistic(){
#ifdef WRITE_DEBUG_INFO
	unsigned int i,anonymous_types=0;
	unsigned int max_tblA=0;
	unsigned int max_tblB=0;
	unsigned int num_chA,num_chB;
	unsigned int simple_types=0;
	unsigned int bits_for_tblA=0;
	unsigned int bits_for_tblB=0;
	unsigned int childs_overall=0;

	int tableA[45],tableB[45];

	type_log<<"In write_type_statistic"<<endl;
	for(i=0;i<45;i++){
		tableA[i]=0;
		tableB[i]=0;
	}
	for(i=0;i<types_read;i++){
		num_chA=types[i].tableA.get_length();
		num_chB=types[i].tableB.get_length();
		if(num_chA==0&&num_chB==0){
			simple_types++;
			continue;
		}
		type_log<<"type "<<types[i].name.transcode()<<endl;
		if(!strncmp(types[i].name.transcode(),"Anonymous",9)) anonymous_types++;
		num_chA=types[i].tableA.get_length();
		num_chB=types[i].tableB.get_length();
		bits_for_tblA+=bitstream::bit_length(num_chA+2);
		bits_for_tblB+=bitstream::bit_length(num_chB+1);

		type_log<<"tableA: "<<num_chA<<endl;
		type_log<<"tableB: "<<num_chB<<endl;
		childs_overall+=num_chB;
		if(num_chA<45) tableA[num_chA]++;
		if(num_chB<45) tableB[num_chB]++;
		if(num_chA>max_tblA) max_tblA=num_chA;
		if(num_chB>max_tblB) max_tblB=num_chB;
	}
	for(i=0;i<45;i++){
		type_log<<"tableA["<<i<<"]="<<tableA[i]<<endl;
		type_log<<"tableB["<<i<<"]="<<tableB[i]<<endl<<endl;
	}

	type_log<<"overall number of anonymous types: "<<anonymous_types<<endl;
	type_log<<"overall number of complex types: "<<types_read-simple_types<<endl;
	type_log<<"overall number of implicit simple types: "<<simple_types<<endl;
	type_log<<"childs overall: "<<childs_overall<<endl;
	type_log<<"bits for tableA: "<<bits_for_tblA<<endl;
	type_log<<"bits for tableB: "<<bits_for_tblB<<endl;
	type_log<<"max TableA: "<<max_tblA<<endl;
	type_log<<"max TableB: "<<max_tblB<<endl;
#endif
}
