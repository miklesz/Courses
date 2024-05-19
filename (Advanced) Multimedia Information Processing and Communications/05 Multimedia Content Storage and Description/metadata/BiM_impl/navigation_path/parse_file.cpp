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

#include <fstream.h>
#include "parse_file.h" 
#include "global_header.h"
#include "namespaces.h"
#include <stdlib.h>
#include <string.h>

#ifdef WRITE_DEBUG_INFO 
extern ofstream report;
extern ofstream in_sch_log;
#endif



extern name_space global_nms;	
/*
global namespace maps URLs to short internal identifiers,
e.g.

source_nms:                            |  target_nms:
http://www.mpeg7.org/2001/MPEG-7_Schema is mapped to: g0
http://www.w3.org/2000/10/XMLSchema     is mapped to: g1
http://www.w3.org/XML/1998/namespace    is mapped to: g2

local namespace maps qualifiers to short internal identifiers,
e.g.
source_nms: |  target_nms:      
mpeg7        is mapped to: g0
xml          is mapped to: g2

*/


bool is_global_nms(char *nms){
	int i=1;
	if(nms[0]=='g'){
		while(nms[i]){
			if(nms[i]>='0'&&nms[i]<='9');
			else return false;
			i++;
		}
		return true;
	}else
		return false;
}


DOM_Document parse_file::parseFile(char *xmlFile){
    DOMParser parser;
	DOM_Element D_elem;
	name_space local_nms;		//contains table with identifiers, 
								//that map qualifiers to the global namespace (e.g. "mpeg7"<->"g4")
	char location[100];
	char base_path[100];
	char concat_path[100];
	char univ_nms[100];
	char new_attrb[30];
	char global_short[10];		//namespace qualifier in global short form (e.g. "g4")
	unsigned int u, loc_start;
	bool imported;//true, if "sub-schema" is imported, false if included
	DOM_Document parsed_schema;//UNinclude_sch

	char *p_pref;
	char *tar_ns_pref;
	char *itemname;
	char *tar_attr_name;
	char *elem_name;
	char *p_elem_name;

    DOMBiMErrorHandler errorHandler;
    parser.setErrorHandler(&errorHandler);
	parser.setDoValidation(false);

#ifdef WRITE_DEBUG_INFO
	report<<"call xerces parser, file:"<<xmlFile<<endl<<flush;
	in_sch_log<<"parse_file"<<endl;
#endif


    try
    {
        parser.parse(xmlFile);
    }

    catch (const XMLException& toCatch)
    {
        cerr << "\nError during parsing: '" << xmlFile << "'\n"
             << "Exception message is:  \n"
             << StrX(toCatch.getMessage()) << "\n" << endl;
        return DOM_Document(); 
    }
    catch (const DOM_DOMException& toCatch)
    {
        cerr << "\nError during parsing: '" << xmlFile << "'\n"
             << "Exception message is:  \n"
             << toCatch.msg.transcode() << "\n" << endl;
        XMLPlatformUtils::Terminate();
		return DOM_Document(); 
    }
    catch (...)
    {
       cerr << "\nUnexpected exception during parsing: '" << xmlFile << "'\n";
        XMLPlatformUtils::Terminate();
		return DOM_Document(); 
    }   

	//UNinclude_sch
	parsed_schema=parser.getDocument();
	DOM_Node sch_root=parsed_schema.getLastChild();
	//DOM_TreeWalker walker=schema.createTreeWalker(sch_root,0x00000003,NULL,1);
	DOM_NodeList list=sch_root.getChildNodes();
	DOM_NamedNodeMap attrlist=sch_root.getAttributes();
	unsigned int attr_list_len=attrlist.getLength();

	strcpy(base_path,xmlFile);

	u=0;
	loc_start=0;
	while(base_path[u]){
		if(base_path[u]=='\\'||base_path[u]=='/') loc_start=u+1;
		u++;
	}
	base_path[loc_start]=0;

	//detect the namespace prefix for the targetNamespace
	tar_ns_pref=NULL;
	DOMString tar_nms=((DOM_Element &)sch_root).getAttribute("targetNamespace");
	strcpy(univ_nms,tar_nms.transcode());
	global_nms.insert_nms_save(univ_nms);
	strcpy(global_short,global_nms.retrieve_nms(univ_nms));
	local_nms.insert_nms_save("target",global_short);	

	for(u=0;u<attr_list_len;u++){
		tar_attr_name=attrlist.item(u).getNodeName().transcode();

		if(!strncmp(tar_attr_name,"xmlns",5)){
			DOMString nms_id=((DOM_Element &)sch_root).getAttribute(tar_attr_name);
			strcpy(univ_nms,nms_id.transcode());
			global_nms.insert_nms_save(univ_nms);
			strcpy(global_short,global_nms.retrieve_nms(univ_nms));
		}
		if(!strcmp(tar_attr_name,"xmlns")){
			local_nms.insert_nms_save("default",global_short);
		}else if(!strncmp(tar_attr_name,"xmlns",5)){
			DOMString nms_id=((DOM_Element &)sch_root).getAttribute(tar_attr_name);
			local_nms.insert_nms_save((strrchr(tar_attr_name,':')+1),global_short);
		}
	}
	
	local_nms.print_nms();

	int list_length=list.getLength();
	unsigned int ll2;
	for(unsigned int i=0;i<(ll2=list.getLength());i++){
		p_pref=NULL;
		DOMString name=list.item(i).getNodeName();
		if(name.equals("include")||name.equals("import")){
		
			if(name.equals("import")){//detect the namespace
				imported=true;
				DOM_Node dom_nd=list.item(i);
				char *nmsp=((DOM_Element &)dom_nd).
				  getAttribute("namespace").transcode();
//				char *nmsp=((DOM_Element &)list.item(i)).getAttribute("namespace").transcode();
				p_pref=global_nms.retrieve_nms(nmsp);
			}else
				imported=false;

#ifdef WRITE_DEBUG_INFO
			if(imported) in_sch_log<<"imported=TRUE"<<endl;
			else in_sch_log<<"imported=FALSE"<<endl;
#endif

			DOM_Node ToReplace=list.item(i);

			{
			  DOM_Node dom_nd=list.item(i);
			  D_elem=(DOM_Element &)dom_nd;
			}
//			D_elem=(DOM_Element &)list.item(i);
			strcpy(location,D_elem.getAttribute("schemaLocation").transcode());

#ifdef WRITE_DEBUG_INFO
			in_sch_log<<"found include/import:  ";
			in_sch_log<<location<<endl;
#endif
			if(location[0]=='.'){
				if(location[1]=='.'){//detect how many move-ups in directory structure
					int ups=0;
					for(u=0;location[u];u+=3){
						if(location[u]=='.'&&location[u+1]=='.'){
							ups++;
							loc_start=u+3;
						}else break;
					}
					if((u=strlen(base_path))>2) u-=2;
					for(;u;u--){
						if(base_path[u]=='\\'||base_path[u]=='/') ups--;
						if(!ups) break;
					}if(ups!=0){
						while(ups--){
							strcat(base_path,"..\\");
							u+=3;
						}
						u--;
					}
					strcpy(concat_path,base_path);
					concat_path[u+1]=0;//delete the pathfragment, that is overridden by ".."
					strcat(concat_path,&location[loc_start]);
				}else{
					u=0;
					loc_start=0;
					while(location[u]){//find last occurrence of '\'
						if(location[u]=='\\'||base_path[u]=='/') loc_start=u+1;
						u++;
					}
					strcpy(concat_path,base_path);
					strcat(concat_path,&location[loc_start]);
				}
			}else{
				strcpy(concat_path,base_path);
				strcat(concat_path,location);
			}

#ifdef WRITE_DEBUG_INFO
			in_sch_log<<"concat_path:  "<<concat_path<<endl;
#endif
			DOM_Document ImportedDoc=parseFile(concat_path);//recursive function: parse included file
			DOM_Node RootOfIncluded=ImportedDoc.getLastChild();

#ifdef WRITE_DEBUG_INFO
			in_sch_log<<"RootOfIncluded: "<<RootOfIncluded.getNodeName().transcode()<<endl;
#endif

			DOM_NodeList imported_childs=RootOfIncluded.getChildNodes();
			unsigned int num_imp=imported_childs.getLength();

			//sch_root.replaceChild(ImportedRoot,ToReplace);
	
			sch_root.removeChild(ToReplace);//remove the <include>-node
			list_length--; //there is one cild less;

			DOM_Element imp_ch;
			tar_ns_pref=local_nms.retrieve_nms("target");
			for(unsigned int j=0;j<num_imp;j++){
				short type=imported_childs.item(j).getNodeType();
				if(type==DOM_Node::TEXT_NODE || type==DOM_Node::COMMENT_NODE)
					continue;
			
				DOM_Node imported_child=parsed_schema.importNode(imported_childs.item(j), true);
				if(imported){
					if(p_pref){//insert prefix to nodenames
						imp_ch=(DOM_Element &)imported_child;
						if(!imp_ch.getAttribute("name").equals("")){
							strcpy(new_attrb,p_pref);
							strcat(new_attrb,":");
							elem_name=imp_ch.getAttribute("name").transcode();
							//insert new prefix, if no prefix present;
							if((p_elem_name=strrchr(elem_name,':'))) 
								strcat(new_attrb,p_elem_name+1);
							else{
								p_elem_name=elem_name;
								strcat(new_attrb,p_elem_name);
							}
							imp_ch.setAttribute("name",new_attrb);
							sch_root.appendChild(imported_child);//insert node of the imported schema
							list_length++;
#ifdef WRITE_DEBUG_INFO		
							in_sch_log<<new_attrb<<endl;
#endif
						}
					}
				}else{
					
					if(tar_ns_pref){//insert prefix to nodenames
						imp_ch=(DOM_Element &)imported_child;
						if(!imp_ch.getAttribute("name").equals("")){
							if(imported_child.getNodeName().equals("complexType")||
								imported_child.getNodeName().equals("simpleType")){
								strcpy(new_attrb,tar_ns_pref);
								strcat(new_attrb,":");
							}else
								strcpy(new_attrb,"");
							elem_name=imp_ch.getAttribute("name").transcode();
							//inser new prefix, if no prefix present;
							if((p_elem_name=strrchr(elem_name,':'))) 
								strcpy(new_attrb,elem_name);
							else{
								p_elem_name=elem_name;
								strcat(new_attrb,p_elem_name);
							}
							imp_ch.setAttribute("name",new_attrb);
							sch_root.appendChild(imported_child);//insert node of the imported schema
							list_length++;
#ifdef WRITE_DEBUG_INFO
							in_sch_log<<"inserted: "<<new_attrb<<endl;
#endif
						}
					}
				}
			}
#ifdef WRITE_DEBUG_INFO		
			in_sch_log<<endl<<endl;
#endif
		}
		if(name.equals("element")||name.equals("complexType")||name.equals("simpleType")||
			name.equals("attributeGroup")||name.equals("attribute")){
			{
			  DOM_Node dom_nd=list.item(i);
			  D_elem=(DOM_Element &)dom_nd;
			}
//			D_elem=(DOM_Element &)list.item(i);
			char *node_name=D_elem.getAttribute("name").transcode();
			//insert correct global short namespace
			if(strcmp(node_name,"")){
				if((p_elem_name=strrchr(node_name,':'))){
					for(u=0;node_name[u]!=':';u++) univ_nms[u]=node_name[u];
					univ_nms[u]=0;
					if(is_global_nms(univ_nms)) continue;
					p_pref=local_nms.retrieve_nms(univ_nms);
					strcpy(new_attrb,p_pref);
					strcat(new_attrb,":");
					strcat(new_attrb,p_elem_name+1);
					D_elem.setAttribute("name",new_attrb);
				}else{
					p_pref=local_nms.retrieve_nms("target");
					strcpy(new_attrb,p_pref);
					strcat(new_attrb,":");
					strcat(new_attrb,node_name);
					D_elem.setAttribute("name",new_attrb);
				}
#ifdef WRITE_DEBUG_INFO
				in_sch_log<<"changed element name: "<<D_elem.getAttribute("name").transcode()<<endl;
#endif
			}
			if(!D_elem.getAttribute("substitutionGroup").equals("")){
				itemname=D_elem.getAttribute("substitutionGroup").transcode();
				if((p_elem_name=strrchr(itemname,':'))){
					for(u=0;itemname[u]!=':';u++) univ_nms[u]=itemname[u];
						univ_nms[u]=0;

					if(!is_global_nms(univ_nms)){
						strcpy(new_attrb,local_nms.retrieve_nms(univ_nms));
						strcat(new_attrb,p_elem_name);
						D_elem.setAttribute("substitutionGroup",new_attrb);
					}
				}else{
					strcpy(new_attrb,local_nms.retrieve_nms("default"));
					strcat(new_attrb,":");
					strcat(new_attrb,itemname);
					D_elem.setAttribute("substitutionGroup",new_attrb);	
				}
			}
			if(!D_elem.getAttribute("type").equals("")){
				itemname=D_elem.getAttribute("type").transcode();
				if((p_elem_name=strrchr(itemname,':'))){
					for(u=0;itemname[u]!=':';u++) univ_nms[u]=itemname[u];
						univ_nms[u]=0;

					if(!is_global_nms(univ_nms)){
						strcpy(new_attrb,local_nms.retrieve_nms(univ_nms));
						strcat(new_attrb,p_elem_name);
						D_elem.setAttribute("type",new_attrb);
					}
				}else{
					strcpy(new_attrb,local_nms.retrieve_nms("default"));
					strcat(new_attrb,":");
					strcat(new_attrb,itemname);
					D_elem.setAttribute("type",new_attrb);	
				}
			}
			//set all nested elements to the correct global nms identifier
			DOM_TreeWalker walker=schema.createTreeWalker(list.item(i),0x00000003,NULL,1);
			walker.firstChild();
			while(walker.getCurrentNode()!=list.item(i)){
				do{
				       {
					 DOM_Node dom_nd=walker.getCurrentNode();
					 D_elem=(DOM_Element &)dom_nd;
				       }
//					D_elem=(DOM_Element &)walker.getCurrentNode();
					/*
					if(!D_elem.getAttribute("name").equals("")){
						itemname=D_elem.getAttribute("name").transcode();
						if(p_elem_name=strrchr(itemname,':')){
							for(u=0;itemname[u]!=':';u++) univ_nms[u]=itemname[u];
								univ_nms[u]=0;

							if(!is_global_nms(univ_nms)){
								strcpy(new_attrb,local_nms.retrieve_nms(univ_nms));
								strcat(new_attrb,p_elem_name);
								D_elem.setAttribute("name",new_attrb);
							}
						}else{
							strcpy(new_attrb,local_nms.retrieve_nms("default"));
							strcat(new_attrb,":");
							strcat(new_attrb,itemname);
							D_elem.setAttribute("name",new_attrb);	
						}
					}*/
					if(!D_elem.getAttribute("type").equals("")){
						itemname=D_elem.getAttribute("type").transcode();
						if((p_elem_name=strrchr(itemname,':'))){
							for(u=0;itemname[u]!=':';u++) univ_nms[u]=itemname[u];
								univ_nms[u]=0;

							if(!is_global_nms(univ_nms)){
								strcpy(new_attrb,local_nms.retrieve_nms(univ_nms));
								strcat(new_attrb,p_elem_name);
								D_elem.setAttribute("type",new_attrb);
							}
						}else{
							strcpy(new_attrb,local_nms.retrieve_nms("default"));
							strcat(new_attrb,":");
							strcat(new_attrb,itemname);
							D_elem.setAttribute("type",new_attrb);	
						}
					}
					if(!D_elem.getAttribute("ref").equals("")){
						itemname=D_elem.getAttribute("ref").transcode();
						if((p_elem_name=strrchr(itemname,':'))){
							for(u=0;itemname[u]!=':';u++) univ_nms[u]=itemname[u];
								univ_nms[u]=0;

							if(!is_global_nms(univ_nms)){
								strcpy(new_attrb,local_nms.retrieve_nms(univ_nms));
								strcat(new_attrb,p_elem_name);
								D_elem.setAttribute("ref",new_attrb);
							}
						}else{
							strcpy(new_attrb,local_nms.retrieve_nms("default"));
							strcat(new_attrb,":");
							strcat(new_attrb,itemname);
							D_elem.setAttribute("ref",new_attrb);	
						}
					}
					if(!D_elem.getAttribute("base").equals("")){
						itemname=D_elem.getAttribute("base").transcode();
						if((p_elem_name=strrchr(itemname,':'))){
							for(u=0;itemname[u]!=':';u++) univ_nms[u]=itemname[u];
								univ_nms[u]=0;

							if(!is_global_nms(univ_nms)){
								strcpy(new_attrb,local_nms.retrieve_nms(univ_nms));
								strcat(new_attrb,p_elem_name);
								D_elem.setAttribute("base",new_attrb);
							}
						}else{
							strcpy(new_attrb,local_nms.retrieve_nms("default"));
							strcat(new_attrb,":");
							strcat(new_attrb,itemname);
							D_elem.setAttribute("base",new_attrb);	
						}
					}
					if(!D_elem.getAttribute("substitutionGroup").equals("")){
						itemname=D_elem.getAttribute("substitutionGroup").transcode();
						if((p_elem_name=strrchr(itemname,':'))){
							for(u=0;itemname[u]!=':';u++) univ_nms[u]=itemname[u];
								univ_nms[u]=0;

							if(!is_global_nms(univ_nms)){
								strcpy(new_attrb,local_nms.retrieve_nms(univ_nms));
								strcat(new_attrb,p_elem_name);
								D_elem.setAttribute("substitutionGroup",new_attrb);
							}
						}else{
							strcpy(new_attrb,local_nms.retrieve_nms("default"));
							strcat(new_attrb,":");
							strcat(new_attrb,itemname);
							D_elem.setAttribute("substitutionGroup",new_attrb);	
						}
					}
				}while(!walker.firstChild().isNull());
				while(walker.nextSibling().isNull()){
					if(walker.parentNode().isNull()) break;
				}
			}

		}	
	}
	return parsed_schema;
}


void parse_file::prepare_schema(DOM_Node node){
	unsigned int i;
	DOM_TreeWalker walker=schema.createTreeWalker(node,0x00000003,NULL,1);
	DOM_NodeList list=node.getChildNodes();
	unsigned int list_length=list.getLength();
//	int removed=0;
//	report<<"listlength: "<<list_length;
	for(i=0;i<list.getLength();i++){
		short type=list.item(i).getNodeType();
	
		DOMString name=list.item(i).getNodeName();
	
		if(type==DOM_Node::TEXT_NODE || type==DOM_Node::COMMENT_NODE){
			node.removeChild(list.item(i));
			i--;	// there's a child less!!
			list_length--;
			//report<<" removed: "<<i<<" ,# removed: "<<removed++;
			//eliminating the text nodes we achieve a improvement in the speed
			//when we will search for the types
		}
		else if(name.equals("element")){
//			report<<"add element: "<<endl;
			elements.add_node(list.item(i),true);
		}
		else if(name.equals("attribute")){
			attributes.add_node(list.item(i),true);
		}
		else if(name.equals("complexType") || name.equals("simpleType")){
			DOM_Node dom_nd=list.item(i);
			DOM_Element elem=(DOM_Element &)dom_nd;
//			DOM_Element elem=(DOM_Element&)list.item(i);
			walker.setCurrentNode(elem);
			DOM_Node son=walker.firstChild();
			DOM_Node grandson=walker.firstChild();
			if(elem.getAttribute("derivedBy").equals("extension")){	
				//extension in the old schema specification
				extensions.add_node(elem,false);
				new_schema=false;
			}
			else if(grandson!=NULL && grandson.getNodeName().equals("extension")){
				//extension in the new schema specification
				extensions.add_node(grandson,false);
				new_schema=true;
			}
		}
	}
}


void parse_file::delete_text_nodes(DOM_Node node){
	unsigned int i;
#ifdef WRITE_DEBUG_INFO
	report<<"delete_text_nodes"<<endl<<flush;
#endif
	DOM_NodeList list=node.getChildNodes();
//	int numero=list.getLength();
	
	for(i=0;i<list.getLength();i++){
		short type=list.item(i).getNodeType();
		if(type==DOM_Node::TEXT_NODE || type==DOM_Node::COMMENT_NODE){
			node.removeChild(list.item(i));
			i--;	// there's a child less!!	
		}
		/*
		else if(list.item(i).getNodeName().equals("element")){
			elements.add_node(list.item(i),true);
		}*/
	}
}

void parse_file::rewrite_XML_file(DOM_Document doc){
	unsigned int i;
	DOM_Node node;
	DOM_NamedNodeMap list;
	DOM_TreeWalker walker=doc.createTreeWalker(doc.getLastChild(),0x00000003,NULL,1);
	ofstream xml_file;

	xml_file.open("new_schema.xml");

	node=walker.getCurrentNode();
	while(1){
		for(node=walker.getCurrentNode();!node.isNull();node=walker.firstChild()){
			xml_file<<"<"<<node.getNodeName().transcode();
	
			list=node.getAttributes();
			for(i=0;i<list.getLength();i++){
				DOM_Node attrib=list.item(i);
				xml_file<<" "<<attrib.getNodeName().transcode()<<"=\""
					<<attrib.getNodeValue().transcode()<<"\"";
			}
			xml_file<<">"<<endl;
		}
		//now we are in a leaf, let's go to write the content and going backward closing elements
		if(walker.getCurrentNode().hasChildNodes())
			xml_file<<walker.getCurrentNode().getFirstChild().getNodeValue().transcode();
		xml_file<<"</"<<walker.getCurrentNode().getNodeName().transcode()<<">"<<endl;
		node=walker.nextSibling();
		if(node.isNull()){	// doesn't exist a brother
			while(walker.nextSibling().isNull()){
				node=walker.parentNode();
				if (node.isNull()) 
					return;	//we are in the root node!!
				xml_file<<"</"<<node.getNodeName().transcode()<<">"<<endl;
			}
		}
	}
}

void parse_file::analyze_schema(DOM_Document schema){
	DOM_Node node;
	DOM_NamedNodeMap list;
	char stack_of_types[15][200];
	int stack_depth=0,stptr;
	DOM_TreeWalker walker=schema.createTreeWalker(schema.getLastChild(),0x00000003,NULL,1);
	DOMString nameOfType;
	node=walker.getCurrentNode();
	while(1){
		for(node=walker.getCurrentNode();!node.isNull();node=walker.firstChild()){
		
			if(node.getNodeName().equals("complexType")){
				DOM_Element elem;
				elem=(DOM_Element &) node;
				DOMString name=elem.getAttribute("name");
				if(name.equals("")){
					strcpy(stack_of_types[stack_depth],"Anonymous");
					nameOfType="Anonymous";
					{
					  DOM_Node dom_nd=node.getParentNode();
					  elem=(DOM_Element &)dom_nd;
					}
//					elem=(DOM_Element &) node.getParentNode() ;
					DOMString name=elem.getAttribute("name");
					nameOfType.appendData(name);
					strcat(stack_of_types[stack_depth],name.transcode());
					stptr=stack_depth;
					while(stptr){
						nameOfType.appendData("_in_");
						nameOfType.appendData(stack_of_types[stptr-1]);
						stptr--;
					}
				}else{
					nameOfType=name;
					strcpy(stack_of_types[stack_depth],name.transcode());
				}
#ifdef WRITE_DEBUG_INFO
				report<<"found type: "<<nameOfType.transcode()<<endl;
#endif
				char *test_name=nameOfType.transcode();
				if(!strncmp(test_name,"Anonymous",9)){
					build_TBCs::detect_textual_anonymous_type(test_name);
					build_TBCs::search_type(nameOfType,elem);
				}
				else
					build_TBCs::search_type(nameOfType);
				stack_depth++;
			}
	
		}
		if(walker.getCurrentNode().hasChildNodes())

		node=walker.nextSibling();
		if(node.isNull()){	// doesn't exist a brother
			while(walker.nextSibling().isNull()){
				node=walker.parentNode();
				if(node.isNull()) 	return;	//we are in the root node!!
				if(node.getNodeName().equals("complexType")) stack_depth--;
			}
		}
	}
}

DOM_Document parse_file::get_schema(){
	return schema;
}

void parse_file::set_schema(DOM_Document schema_in){
	schema=schema_in;
}

node_list& parse_file::get_elements(){
	return elements;
}
node_list& parse_file::get_attributes(){
	return attributes;
}
node_list& parse_file::get_extensions(){
	return extensions;
}


DOMBiMErrorHandler::DOMBiMErrorHandler() :

    fSawErrors(false)
{
}

DOMBiMErrorHandler::~DOMBiMErrorHandler()
{
}


// ---------------------------------------------------------------------------
//  DOMBiMHandlers: Overrides of the SAX ErrorHandler interface
// ---------------------------------------------------------------------------
void DOMBiMErrorHandler::error(const SAXParseException& e)
{
    fSawErrors = true;
    cerr << "\nError at file " << StrX(e.getSystemId())
         << ", line " << e.getLineNumber()
         << ", char " << e.getColumnNumber()
         << "\n  Message: " << StrX(e.getMessage()) << endl;
}

void DOMBiMErrorHandler::fatalError(const SAXParseException& e)
{
    fSawErrors = true;
    cerr << "\nFatal Error at file " << StrX(e.getSystemId())
         << ", line " << e.getLineNumber()
         << ", char " << e.getColumnNumber()
         << "\n  Message: " << StrX(e.getMessage()) << endl;
}

void DOMBiMErrorHandler::warning(const SAXParseException& e)
{
    cerr << "\nWarning at file " << StrX(e.getSystemId())
         << ", line " << e.getLineNumber()
         << ", char " << e.getColumnNumber()
         << "\n  Message: " << StrX(e.getMessage()) << endl;
}

void DOMBiMErrorHandler::resetErrors()
{
}
