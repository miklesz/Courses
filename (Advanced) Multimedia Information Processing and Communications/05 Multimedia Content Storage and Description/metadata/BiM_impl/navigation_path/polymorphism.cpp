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

#include "polymorphism.h"
#include "build_TBCs.h"
#include "parse_file.h"
#include "global_header.h"
#include <stdio.h>
#include <stdlib.h>
#include <fstream.h>

#ifdef WRITE_DEBUG_INFO 
extern ofstream report;
#endif

#ifdef WRITE_POLY_INFO
extern ofstream poly_log;
#endif

unsigned int polymorphism::search_poly_type(DOM_Element elem,DOMString name){

	unsigned int i,j,k,value,depth=0,order=0,child_value=0;
	unsigned int length,total;
	unsigned int list_value[500];

#ifdef WRITE_DEBUG_INFO 
	report<<"target element : "<<name.transcode()<<endl;
	report<<"in search_poly_type, name:"<<elem.getNodeName().transcode()<<endl;
#endif

#ifdef WRITE_POLY_INFO
	poly_log<<"in search_poly_type, name:"<<elem.getNodeName().transcode()<<endl;
	poly_log<<"target element : "<<name.transcode()<<endl;
#endif
	

	//childs.set_num_nodes(0);//initialize child-list
	childs.delete_node_list();//initialize child-list
	childs.add_DOM_NodeList(elem.getChildNodes());

	while(1){
		node_list temp;
		node_list grandChild;

		length=childs.get_length();
		
		if(length==0){
			return 0;
		}
#ifdef WRITE_DEBUG_INFO 
		report<<"length of temp after init: "<<temp.get_length()<<endl;
		report<<"length of childs: "<<length<<endl;
#endif

#ifdef WRITE_POLY_INFO
		poly_log<<"length of temp after init: "<<temp.get_length()<<endl;
		poly_log<<"length of childs: "<<length<<endl;
#endif
		//DOMString preParentName=childs.item(0).getParentNode().getNodeName();
	 	value=child_value;
		child_value=0;

		for(i=0;i<length;i++){
#ifdef WRITE_DEBUG_INFO 
			report<<"i: "<<i<<"  = "<<childs.item(i).getNodeName().transcode()<<endl;
			report<<childs.item(i).getNodeName().transcode()<<endl;
#endif
#ifdef WRITE_POLY_INFO
			poly_log<<"i: "<<i<<"  = "<<childs.item(i).getNodeName().transcode()<<endl;
			poly_log<<childs.item(i).getNodeName().transcode()<<endl;
#endif			
			DOMString PTypeName=childs.item(i).getNodeName();
			
			node_list preTemp;
			
			DOMString parentName=childs.item(i).getParentNode().getNodeName();
			DOMString preParentName=parentName;
             
			if(i>0){
				preParentName=childs.item(i-1).getParentNode().getNodeName();
			}
			//DOMString preParentName=childs.item(i).getParentNode().getNodeName();
#ifdef WRITE_DEBUG_INFO 
			report<<"Parent Name : "<<parentName.transcode()<<endl;
			report<<"Last Parent Name : "<<preParentName.transcode()<<endl;
			report<<"add node list with number of childs: "<<preTemp.get_length()<<endl;
#endif
#ifdef WRITE_POLY_INFO
			poly_log<<"Parent Name : "<<parentName.transcode()<<endl;
			poly_log<<"Last Parent Name : "<<preParentName.transcode()<<endl;
			poly_log<<"add node list with number of childs: "<<preTemp.get_length()<<endl;
#endif	
			
			if(parentName.equals(preParentName)){
				total=value;
			}
			else {
				for(k=0;k<order;k++){
					if(value==list_value[k]){
					  value++;
					  total=value;
					}
				}
			}
		
			list_value[order]=total;
#ifdef WRITE_DEBUG_INFO 
			report<<"value = "<<total<<endl;
			report<<"list_value ["<<order<<"] = "<<list_value[order]<<endl;
#endif
#ifdef WRITE_POLY_INFO
			poly_log<<"value = "<<total<<endl;
			poly_log<<"list_value ["<<order<<"] = "<<list_value[order]<<endl;
#endif
			order++;


			if(PTypeName.equals(name)){
#ifdef WRITE_DEBUG_INFO 
				report<<"element found, its the "<<total<<" th"<<endl;
				report<<"Parent Name : "<<childs.item(i).getParentNode().getNodeName().transcode()<<endl;
#endif
#ifdef WRITE_POLY_INFO		
				poly_log<<"element found, its the "<<total<<" th"<<endl;
				poly_log<<"Parent Name : "<<childs.item(i).getParentNode().getNodeName().transcode()<<endl;
#endif
				return total;
			}
			//delete_namespace(PTypeName);
			if(build_TBCs::compare_wo_nms(PTypeName,name)){//UNnms ensure, that name is found, if its searched without namespace
#ifdef WRITE_DEBUG_INFO 
				report<<"element found, its the "<<total<<" th"<<endl;
#endif
#ifdef WRITE_POLY_INFO
				poly_log<<"element found, its the "<<total<<" th"<<endl;
#endif
				return total;
			}
			
			//DOMString preParentName=parentName;
		
			preTemp.add_DOM_NodeList(childs.item(i).getChildNodes());
			temp.add_DOM_NodeList(childs.item(i).getChildNodes());	
		
			grandChild=preTemp;
			
			while(grandChild.get_length()){
				node_list preTemp;
#ifdef WRITE_DEBUG_INFO 
				report<<"there are children!!!, num Of Children is "<<grandChild.get_length()<<endl;
#endif
#ifdef WRITE_POLY_INFO
				poly_log<<"there are children!!!, num Of Children is "<<grandChild.get_length()<<endl;
#endif				
				for(j=0;j<grandChild.get_length();j++){
					preTemp.add_DOM_NodeList(grandChild.item(j).getChildNodes());
#ifdef WRITE_DEBUG_INFO 
					report<<"total children until j= "<<j<<" is "<<preTemp.get_length()<<" children"<<endl;
#endif
#ifdef WRITE_POLY_INFO
					poly_log<<"total children until j= "<<j<<" is "<<preTemp.get_length()<<" children"<<endl;					
#endif
					value++;
					if(child_value==0){
						child_value=value;
					}
				}
				grandChild=preTemp;
			
				if(grandChild.get_length()){
#ifdef WRITE_DEBUG_INFO 
				report<<"there are children!!!, num Of Children is "<<grandChild.get_length()<<endl;
#endif
#ifdef WRITE_POLY_INFO
				poly_log<<"there are children!!!, num Of Children is "<<grandChild.get_length()<<endl;				
#endif	
				}
			} 
			value++;
/*
#ifdef WRITE_DEBUG_INFO
			report<<"value = "<<value<<endl;
			poly_log<<"value = "<<value<<endl;
#endif */
		}
		childs=temp;
		depth++;
	}	
}

DOMString polymorphism::get_poly_type(DOM_Element elem,unsigned int num){
	unsigned int i,j,k, value, depth=0, child_value=0, order=0;
	unsigned int total, length;
	unsigned int list_value[500];
#ifdef WRITE_DEBUG_INFO 
	report<<"in get_poly_type"<<endl;
	report<<"num being searched = "<<num<<endl;
#endif
#ifdef WRITE_POLY_INFO
	poly_log<<"in get_poly_type"<<endl;
	poly_log<<"num being searched = "<<num<<endl;
#endif
	//childs.set_num_nodes(0);//initialize child-list
	childs.delete_node_list();//initialize child-list
	childs.add_DOM_NodeList(elem.getChildNodes());

	while(1){
		node_list temp;
		node_list grandChild;

		value=child_value;
		child_value=0;

		length=childs.get_length();

		for(i=0;i<length;i++){

#ifdef WRITE_DEBUG_INFO
			report<<"i: "<<i<<"  = "<<childs.item(i).getNodeName().transcode()<<endl;
#endif
#ifdef WRITE_POLY_INFO
			poly_log<<"i: "<<i<<"  = "<<childs.item(i).getNodeName().transcode()<<endl;			
#endif		
			
			DOM_Node dom_nd=childs.item(i);
			DOM_Element item=(DOM_Element &)dom_nd;
//			DOM_Element item=(DOM_Element&)childs.item(i);
			node_list preTemp;
			DOMString parentName=childs.item(i).getParentNode().getNodeName();
			DOMString preParentName=parentName;

			if(i>0){
				preParentName=childs.item(i-1).getParentNode().getNodeName();
			}

			if(parentName.equals(preParentName)){
				total=value;
			}
			else {
				for(k=0;k<order;k++){
					if(value==list_value[k]){
					  value++;
					  total=value;
					}
				}
			}
			
			list_value[order]=total;

#ifdef WRITE_DEBUG_INFO			
			report<<"value = "<<total<<endl;
			report<<"list_value ["<<order<<"] = "<<list_value[order]<<endl;
#endif

#ifdef WRITE_POLY_INFO
			poly_log<<"value = "<<total<<endl;
			poly_log<<"list_value ["<<order<<"] = "<<list_value[order]<<endl;
#endif
			order++;
			
									
			//NOABS if(value==num && item.getAttribute("abstract")==NULL)
			if(total==num){

#ifdef WRITE_DEBUG_INFO
			report<<"item is found, value = "<<total<<endl;
#endif
#ifdef WRITE_POLY_INFO
			poly_log<<"item is found, value = "<<total<<endl;
#endif
				return item.getNodeName();
			}			
			
			//DOMString preParentName=childs.item(i).getParentNode().getNodeName();
										
			preTemp.add_DOM_NodeList(childs.item(i).getChildNodes()); 
			temp.add_DOM_NodeList(childs.item(i).getChildNodes());

		   	grandChild=preTemp;		

			while(grandChild.get_length()){
#ifdef WRITE_DEBUG_INFO
				report<<"there are children!!!, num Of Children is "<<grandChild.get_length()<<endl;
#endif
#ifdef WRITE_POLY_INFO
				poly_log<<"there are children!!!, num Of Children is "<<grandChild.get_length()<<endl;
#endif					
				node_list preTemp;
				for(j=0;j<grandChild.get_length();j++){  //NOABS if(item.getAttribute("abstract")==NULL)
						value++;
						if(child_value==0){
							child_value=value;
						}
						preTemp.add_DOM_NodeList(grandChild.item(j).getChildNodes());

#ifdef WRITE_DEGBUG_INFO
						report<<"total children until j= "<<j<<" is "<<preTemp.get_length()<<"children"<<endl;
#endif
#ifdef WRITE_POLY_INFO
						poly_log<<"total children until j= "<<j<<" is "<<preTemp.get_length()<<"children"<<endl;					
#endif						
				}
				grandChild=preTemp;  				
			}
			value++;
#ifdef WRITE_DEBUG_INFO
			report<<"value = "<<value<<endl;
#endif
#ifdef WRITE_POLY_INFO
			poly_log<<"value = "<<value<<endl;
#endif
		}
		childs=temp;
	    depth++;
	}	
}


DOM_Element polymorphism::create_poly_element(DOM_Node type_definition){
	unsigned int i,j;
	DOMString poly_name=((DOM_Element&)type_definition).getAttribute("name");
	//looking if the type is already inserted in the poly_tree
	char *polname=poly_name.transcode();


#ifdef WRITE_DEBUG_INFO
	report<<"in create poly element"<<endl;
	report<<"polname: "<<polname<<endl;
#endif
#ifdef WRITE_POLY_INFO
	poly_log<<"in create poly element"<<endl;
	poly_log<<"polname: "<<polname<<endl;
#endif
	DOM_NodeList childs=poly_tree.getFirstChild().getChildNodes();
	for(i=0;i<childs.getLength();i++){
		if(childs.item(i).getNodeName().equals(poly_name)||
			build_TBCs::compare_wo_nms(childs.item(i).getNodeName(),poly_name)){
#ifdef WRITE_POLY_INFO
		poly_log<<"element already in poly_tree, return!"<<endl;
#endif
		DOM_Node dom_nd=childs.item(i);
		return (DOM_Element &)dom_nd;
//		return (DOM_Element&)childs.item(i);

		}
#ifdef WRITE_DEBUG_INFO
		//poly_log<<"elements, already inserted in poly_tree: "<<childs.item(i).getNodeName().transcode()<<endl;
#endif
	}
	//the type is not inserted in the poly_tree
	unsigned int num_childs=0;	//number of derived complexTypes that are non_abstract
	node_list list;
	parse_file::extensions.search_extensions_with_base(list,poly_name);
	if(list.get_length()==0) return DOM_Element();	//null document

#ifdef WRITE_DEBUG_INFO
	report<<"listlength of search_extensions with base: "<<list.get_length()<<endl;
#endif
#ifdef WRITE_POLY_INFO
	poly_log<<"listlength of search_extensions with base:: "<<list.get_length()<<endl;
#endif

	DOM_Element poly_elem=poly_tree.createElement(poly_name);

	poly_tree.getLastChild().appendChild(poly_elem);
	//NOABS if(((DOM_Element&)type_definition).getAttribute("abstract").equals("true"))
	//NOABS		poly_elem.setAttribute("abstract","true");
	for(i=0;i<list.get_length();i++){
		DOM_Element elem;
		if(parse_file::new_schema) {
		  DOM_Node dom_nd=list.item(i).getParentNode().getParentNode();
		  elem=(DOM_Element &)dom_nd;
//			elem=(DOM_Element&)list.item(i).getParentNode().getParentNode();
		}
		else {
		  DOM_Node dom_nd=list.item(i);	//old schema
		  elem=(DOM_Element &)dom_nd;
//			elem=(DOM_Element&)list.item(i);	//old schema
		}
		char *nombre=elem.getAttribute("name").transcode();

#ifdef WRITE_DEBUG_INFO
		report<<"new poly elem: "<<nombre<<endl;
#endif
#ifdef WRITE_POLY_INFO
		poly_log<<"new poly elem: "<<nombre<<endl;
#endif

		DOM_Element new_poly_elem=create_poly_element(elem);	//recursive function
		if(new_poly_elem==NULL){
			DOM_Element poly_child=poly_tree.createElement(elem.getAttribute("name"));
			DOM_NodeList childs=poly_elem.getChildNodes();
			for(j=0;j<childs.getLength();j++){
				if(strcmp(poly_child.getNodeName().transcode(),childs.item(j).getNodeName().transcode())==-1)
					break;
			}
			poly_elem.insertBefore(poly_child,childs.item(j));
			//incrementing number_childs, only in case if the type included is not abstract

#ifdef WRITE_DEBUG_INFO
			report<<"insert child in: "<<poly_elem.getNodeName().transcode()<<endl;
#endif
#ifdef WRITE_POLY_INFO
			poly_log<<"insert child in: "<<poly_elem.getNodeName().transcode()<<endl;
#endif
			/*NOABS
			if(elem.getAttribute("abstract").equals("true")){
				poly_child.setAttribute("abstract","true");
			}
			else*/
				num_childs++;
		}
		else{	//we must include the sub_poly_tree
			DOM_Node clone=new_poly_elem.cloneNode(true);	//cloning including the sub-tree
			DOM_NodeList childs=poly_elem.getChildNodes();
			for(j=0;j<childs.getLength();j++)
				if(strcmp(clone.getNodeName().transcode(),childs.item(j).getNodeName().transcode())==-1)
					break;
			poly_elem.insertBefore(clone,childs.item(j));
#ifdef WRITE_DEBUG_INFO
			report<<"insert child in: "<<poly_elem.getNodeName().transcode()<<endl;
#endif
#ifdef WRITE_POLY_INFO
			poly_log<<"insert child in: "<<poly_elem.getNodeName().transcode()<<endl;
#endif
			//including the num_childs non abstract of this type, and the type itself
			DOMString num=new_poly_elem.getAttribute("non_abstract_childs");
			num_childs+=atoi(num.transcode());
			//NOABS if(!new_poly_elem.getAttribute("abstract").equals("true"))
				num_childs++;
		}
	}
#ifdef WRITE_DEBUG_INFO
	report<<"return poly_elem: "<<poly_elem.getNodeName().transcode()<<" with childs: "<<num_childs<<endl;
	report<<" #childs: "<<poly_elem.getChildNodes().getLength()<<endl;
#endif
#ifdef WRITE_POLY_INFO
	poly_log<<"return poly_elem: "<<poly_elem.getNodeName().transcode()<<" with childs: "<<num_childs<<endl;
	poly_log<<" #childs: "<<poly_elem.getChildNodes().getLength()<<endl;
#endif

	char non_abstract_childs[10];
	sprintf(non_abstract_childs,"%d",num_childs);
//	itoa(num_childs,non_abstract_childs,10);
	poly_elem.setAttribute("non_abstract_childs",non_abstract_childs);
	return poly_elem;
}

void polymorphism::create_poly_tree(){
	poly_tree=parse_file::get_schema().createDocument();	//creating a null document
	poly_tree.appendChild(poly_tree.createElement("types")) ;//we can have only one child under the root
}

DOM_Document polymorphism::get_poly_tree(){
	return poly_tree;
}

