#include <dom/DOM.hpp>
#include <util/PlatformUtils.hpp>
#include <parsers/DOMParser.hpp>
#include <stdlib.h>
#include <fstream.h>
#include <stdio.h>
#include <string.h>
#include "ErrorHandler.hpp"
#include "namespaces.h"
#include <dom/DOM_DOMException.hpp>
#include <sax/SAXException.hpp>
#include <sax/SAXParseException.hpp>

#define FULL_QUALIFIED_NAMES
#define ABS_ROOT 1
#define REL 2

//main(), that call the encoder and decoder functions
#define JAVA_MAIN 0
#define CPP_PATH_MAIN 1
#define DEC_CONF_MAIN 2
#define WRITE_DEBUG_INFO

char *encode_path(const char *previous_element,const char *actual_element,const char *sch_file, int mode_path, int select_main, int i);
char *decode_path(const char *previous_element,char *txt_bin_path, const char *sch_file, int mode_path, int select_main);
bool compare_wo_nms(DOMString str1,DOMString str2);
void open_logs();
void close_logs();
void write_statistic(char *schema_file);

class node_list{	
private:
	DOM_Node nodes[300];
	unsigned int num_nodes;
public:
	void add_node(DOM_Node node,bool checking=false){
		unsigned int i;
		if(checking){	//looking is the node is already inserted in the list	
			DOM_Element elem=(DOM_Element&)node;
			DOMString name=elem.getAttribute("name");
			if(name.equals("")) 
				name=elem.getAttribute("ref");
			DOM_Element item;
			for(i=0;i<num_nodes;i++){
				item=(DOM_Element&)nodes[i];
				if(name.equals(item.getAttribute("name"))||name.equals(item.getAttribute("ref"))){
					nodes[i]=node;
					return;
				}
			}
		}
		//the node is not inserted
		nodes[num_nodes++]=node;	
	}

	DOM_Node item(int num){
		return nodes[num];

	}
	unsigned int search_node(DOMString name){
		unsigned int i;
		DOM_Element elem;
		char *sname=name.transcode();
		for(i=0;i<num_nodes;i++){
			elem=(DOM_Element&) nodes[i];
			char *chname=elem.getAttribute("name").transcode();
			//if(elem.getAttribute("name").equals(name)||elem.getAttribute("ref").equals(name))
			//UNnms: new compare function:
			if(compare_wo_nms(elem.getAttribute("name"),name)
				||compare_wo_nms(elem.getAttribute("ref"),name)) 
				return(i+2); //the value 0 is reserved for the father,
							 //the value 1 is reserved for the extension code
		}
		return(0);	//the name has not been found
	}
	unsigned int get_length(){
		return num_nodes;
	}
	node_list operator=(node_list& list){
		unsigned int i;
		for(i=0;i<list.get_length();i++)
			nodes[i]=list.item(i);
		num_nodes=i;
		return(*this);
	}
};

/*
struct DOMString_list{
	DOMString name;
	DOMString *next;
};

struct namespaces{
	DOMString master;
	struct DOMString_list *list;
};
*/

struct namespaces{
	DOMString nms_short;
	DOMString qualified;
};

struct schema_type{
	DOMString name;
	node_list elem_childs;
	DOM_Node type_definition;	//pointer to the node in the schema tree
};

struct xml_element{
	DOMString name;
	unsigned int position;
	DOM_Node node;				//pointer to the node in the XML tree
	DOM_Node element_declaration;	//declaration of the element in the schema tree
	schema_type *type;

	xml_element operator= (const xml_element &a){
		name=a.name;
		position=a.position;
		node=a.node;
		element_declaration=a.element_declaration;
		type=a.type;
		return(*this);
	}
	friend int operator== (const xml_element& a,const xml_element& b){
		if(a.name.equals(b.name) && a.position==b.position && a.node==b.node)
			return 1;
		else return 0;
	}
};

struct path_type{
	xml_element element[100];
	unsigned int depth;

	xml_element& item(int num){
		if(num>0) return element[num];
		else return element[0];
	}

	path_type operator= (const path_type &a){
		depth=a.depth;
		for(unsigned int i=0;i<depth;i++)
			element[i]=a.element[i];
		return (*this);
	}
}path,rel_path,prev_path;

typedef struct node_pointer{
	DOM_Node node;
	struct node_pointer *next;
}nodePointer;

typedef struct Subst_Grp{
	unsigned int num_members;
	DOMString name;
	DOM_Node *base;
	nodePointer *nodeList;
	struct Subst_Grp *next;
}SubstGroup;

void extract_SubstGroups(DOM_Document doc);
void print_all_substgroups();
unsigned int search_substGroup_elem(SubstGroup *ptr_SubstGroup,char *name);
SubstGroup *is_head_of_substitution_group(DOMString name);
DOM_Element retrieve_substgroup_elem(SubstGroup *ptr_SubstGroup,unsigned int num_elem);

DOM_Document doc,schema;
SubstGroup *FirstSubstGroup=NULL;
schema_type types[400];
unsigned int types_read;

ofstream report;
//ofstream xml;
ofstream text_file;
//ifstream bin_r;
ifstream read_text;
ofstream results;
ofstream in_sch_log;

unsigned int mode,num_file;

unsigned int bitpointer=0;
int no_of_path;
char bits[1000];


name_space global_nms;
name_space local_nms_top;

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

bool has_nms_prefix(char *nms){
	while(*nms) if(*nms++==':') return true;
	return false;
}


void insert_SubstGrpElem(DOM_Node insert_node,SubstGroup *ptr_SubstGroup){
	nodePointer *new_nodePointer=new nodePointer;
	nodePointer *node_ptr;
	new_nodePointer->next=NULL;
	new_nodePointer->node=insert_node;

	ptr_SubstGroup->num_members++;
	if(ptr_SubstGroup->nodeList==NULL)
		ptr_SubstGroup->nodeList=new_nodePointer;
	else{
		node_ptr=ptr_SubstGroup->nodeList;
		while(node_ptr->next!=NULL) node_ptr=node_ptr->next;
		node_ptr->next=new_nodePointer;
	}
}

SubstGroup *is_head_of_substitution_group(DOMString name){
	SubstGroup *ptr_SubstGroup=FirstSubstGroup;

	while(ptr_SubstGroup){
		if(ptr_SubstGroup->name.equals(name)){
		
			return ptr_SubstGroup;
		}
		ptr_SubstGroup=ptr_SubstGroup->next;
	}
	return NULL;
}

unsigned int search_substGroup_elem(SubstGroup *ptr_SubstGroup,char *name){
	nodePointer *node_ptr=ptr_SubstGroup->nodeList;
	int i=1;
	while(node_ptr){
	
		DOM_Element elem=(DOM_Element &)node_ptr->node;
		char *name2=elem.getAttribute("name").transcode();
		if(compare_wo_nms(name,name2)){
			break;
		}
		i++;
		node_ptr=node_ptr->next;
	}
	if(node_ptr==NULL) return 0;
	else return i;
}

void print_all_substgroups(){
 
	SubstGroup *ptr_SubstGroup=FirstSubstGroup;
	while(ptr_SubstGroup){
		report<<endl;
		report<<"substitution group: "<<ptr_SubstGroup->name.transcode()<<endl;
		report<<"  has "<<ptr_SubstGroup->num_members<<" members"<<endl;
		nodePointer *node_ptr=ptr_SubstGroup->nodeList;
		while(node_ptr){
			DOM_Element elem=(DOM_Element &)node_ptr->node;
			report<<elem.getAttribute("name").transcode()<<endl;
			node_ptr=node_ptr->next;
		}
		ptr_SubstGroup=ptr_SubstGroup->next;
	}

}

DOM_Element retrieve_substgroup_elem(SubstGroup *ptr_SubstGroup,unsigned int num_elem){
	DOM_Element elem;
	nodePointer *node_ptr=ptr_SubstGroup->nodeList;
	if(num_elem>ptr_SubstGroup->num_members)
		report<<"request to access indefined element in substgroup!"<<endl;
	num_elem--;
	while(node_ptr&&num_elem){
		node_ptr=node_ptr->next;
		num_elem--;
	}
	elem=(DOM_Element &)node_ptr->node;
	return elem;
}

void insert_element_in_substGroup(DOM_Node element,DOMString name_of_base){
	SubstGroup *ptr_SubstGroup=FirstSubstGroup;

	for(;ptr_SubstGroup!=NULL;){//check, if there is already a Substgroup, for the desired basename
		if(ptr_SubstGroup->name.equals(name_of_base)){
			insert_SubstGrpElem(element,ptr_SubstGroup);
			return;
		}
		ptr_SubstGroup=ptr_SubstGroup->next;
	}
	SubstGroup *ptr_new_SubstGroup=new SubstGroup;//create new substgroup
	ptr_new_SubstGroup->next=NULL;
	ptr_new_SubstGroup->name=name_of_base;
	ptr_new_SubstGroup->nodeList=NULL;
	ptr_new_SubstGroup->num_members=0;

	insert_SubstGrpElem(element,ptr_new_SubstGroup);
	if(FirstSubstGroup==NULL)
		FirstSubstGroup=ptr_new_SubstGroup;
	else{
		ptr_SubstGroup=FirstSubstGroup;
		while(ptr_SubstGroup->next!=NULL) ptr_SubstGroup=ptr_SubstGroup->next;
		ptr_SubstGroup->next=ptr_new_SubstGroup;
	}
}

void extract_SubstGroups(DOM_Document doc){

	DOM_Node node;
	DOM_Element elem;
	DOM_TreeWalker walker=doc.createTreeWalker(doc.getLastChild(),0x00000003,NULL,1);
	DOMString name_subst_group;

	while(1){
		for(node=walker.getCurrentNode();!node.isNull();node=walker.firstChild()){
			if(node.getNodeName().equals("element")){
				elem=(DOM_Element &)node;
				name_subst_group=elem.getAttribute("substitutionGroup");
				if(!name_subst_group.equals("")){
					insert_element_in_substGroup(node,name_subst_group);	
				}
			}
		}
		node=walker.nextSibling();
		if(node.isNull()){	// doesn't exist a brother
			while(walker.nextSibling().isNull()){
				node=walker.parentNode();
				if (node.isNull()) 
					return;	//we are in the root node!!
			}
		}
	}
}


DOM_Document parseFile(char *xmlFile,bool call_from_top){
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
	report<<"call xerces parser"<<endl<<flush;

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
				char *nmsp=((DOM_Element &)list.item(i)).getAttribute("namespace").transcode();
				p_pref=global_nms.retrieve_nms(nmsp);
			}else
				imported=false;


			DOM_Node ToReplace=list.item(i);

			D_elem=(DOM_Element &)list.item(i);
			strcpy(location,D_elem.getAttribute("schemaLocation").transcode());

			if(location[0]=='.'){
				if(location[1]=='.'){//detect how many move-ups in directory structure
					int ups=0;
					for(u=0;location[u];u+=3){
						if(location[u]=='.'&&location[u+1]=='.'){
							ups++;
							loc_start=u+3;
						}else break;
					}
					if(u=strlen(base_path)>2) u-=2;
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
			DOM_Document ImportedDoc=parseFile(concat_path,false);//recursive function: parse included file
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
							if(p_elem_name=strrchr(elem_name,':')) 
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
							if(p_elem_name=strrchr(elem_name,':')) 
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
			D_elem=(DOM_Element &)list.item(i);
			char *node_name=D_elem.getAttribute("name").transcode();
	
			//insert correct global short namespace
			if(strcmp(node_name,"")){
				if(p_elem_name=strrchr(node_name,':')){
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
			}
			if(!D_elem.getAttribute("substitutionGroup").equals("")){
				itemname=D_elem.getAttribute("substitutionGroup").transcode();
				if(p_elem_name=strrchr(itemname,':')){
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
				if(p_elem_name=strrchr(itemname,':')){
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
					D_elem=(DOM_Element &)walker.getCurrentNode();
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
						if(p_elem_name=strrchr(itemname,':')){
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
						if(p_elem_name=strrchr(itemname,':')){
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
						if(p_elem_name=strrchr(itemname,':')){
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
						if(p_elem_name=strrchr(itemname,':')){
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
	if(call_from_top){
		local_nms_top=local_nms;
	}

	return parsed_schema;
}


void rewrite_XML_file(DOM_Document doc){
	unsigned int i;
	DOM_Node node;
	DOM_NamedNodeMap list;
	DOM_TreeWalker walker=doc.createTreeWalker(doc.getLastChild(),0x00000003,NULL,1);
	ofstream xml_file;

	xml_file.open("new_schema_inft.xml");//write new schema to file;

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

bool has_prefix(DOMString name){	
	unsigned int i;
	DOMString prefix;
	for(i=0;i<name.length(); i++){
		if(name.charAt(i)==':'){
			return true;
		}
	}
	return false;
}

bool compare_wo_nms(DOMString str1,DOMString str2)//compare without namespace
{
	unsigned int i;
	DOMString nm_wo_nms1=str1;
	DOMString nm_wo_nms2=str2;

	char *name1compl=str1.transcode();
	char *name2compl=str2.transcode();

	unsigned int str_len1=str1.length();
	for(i=0;i<str_len1; i++){
		if(str1.charAt(i)==':'){
			nm_wo_nms1=str1.substringData(i+1,str1.length());
		}
	}
	for(i=0;i<str2.length(); i++){
		if(str2.charAt(i)==':'){
			nm_wo_nms2=str2.substringData(i+1,str2.length());
		}
	}
	char *name1=nm_wo_nms1.transcode();
	char *name2=nm_wo_nms2.transcode();

	if(nm_wo_nms1.equals(nm_wo_nms2)) return true;
	else return false;
}

int look_position(const DOM_TreeWalker walker){
	DOM_TreeWalker temp_walker(walker);
	int occu=1;
	DOMString name=temp_walker.getCurrentNode().getNodeName();
	while(!temp_walker.previousSibling().isNull())
		if(name.equals(temp_walker.getCurrentNode().getNodeName())) occu++;
	return occu;
}

unsigned int bit_length(unsigned int num){
	int i=0;
	while(num){
		i++;
		num>>=1;
	}
	return i;
}

void compare_paths(){
	unsigned int i,j;

	rel_path.depth=0;
	/*
	if (mode==ABS_TOP && prev_path.depth>2) 
		prev_path.depth=2;*/

	for(i=0 ; i<prev_path.depth && i<path.depth && path.item(i)==prev_path.item(i) ; i++){
		char *nombre=path.item(i).name.transcode();
		char *nombre2=prev_path.item(i).name.transcode();
		int posicion=path.item(i).position;
		int posicion2=prev_path.item(i).position;
	}
	
	char *nombre=path.item(i).name.transcode();
	char *nombre2=prev_path.item(i).name.transcode();


	for(j=prev_path.depth ; j>i ; j--){
		rel_path.item(rel_path.depth)=prev_path.item(j-2);
		rel_path.item(rel_path.depth++).name="go_father";
	}

	for( ; i<path.depth ; i++){
		char *nombre=path.item(i).name.transcode();
		rel_path.item(rel_path.depth++)=path.item(i);
	}

}

bool look_if_unbounded (DOM_Node node){	
	char *nombre=node.getNodeName().transcode();
	if(node.getNodeName().equals("content")) return false;
	if(((DOM_Element &)node).getAttribute("maxOccurs").equals("unbounded"))
		return true;
	node=node.getParentNode();
	char *nombre2=node.getNodeName().transcode();
	while(node.getNodeName().equals("choice")||node.getNodeName().equals("sequence")){
			DOM_Element elem=(DOM_Element &)node;
			if(elem.getAttribute("maxOccurs").equals("unbounded"))
				return true;
			node=node.getParentNode();
	}
	return false;
}

DOMString delete_namespace(DOMString name){	
	unsigned int i;
	DOMString prefix;
	for(i=0;i<name.length(); i++){
		if(name.charAt(i)==':'){
			prefix=name.substringData(0,i);
			name.deleteData(0,i+1);
			break;
		}
	}
	return prefix;
}

void write_textual_path(path_type path,unsigned int i){
	char *declname=path.item(i).element_declaration.getNodeName().transcode();
	char *namen=path.item(i).name.transcode();
	char type[300];
	char type_tr[300];

	if(path.item(i).name.equals("simple_content")){
		strcpy(type,((DOM_Element&)path.item(i).element_declaration).getAttribute("type").transcode());
		global_nms.retransform_path(type,type_tr);
		text_file<<type_tr<<"#"<<path.item(i).name.transcode()<<"#1";
	}
	else if(path.item(i).element_declaration.isNull()||
		path.item(i).element_declaration.getNodeName().equals("attribute")){
		text_file<<"@";
		if(path.item(i).type==NULL){
			strcpy(type,((DOM_Element&)path.item(i).element_declaration).getAttribute("type").transcode());
			//delete_namespace(type);
			global_nms.retransform_path(type,type_tr);
			text_file<<type_tr;
		}else if(strncmp(path.item(i).type->name.transcode(),"Anonymous",9)){	//non anonymous_type
			strcpy(type,path.item(i).type->name.transcode());
			global_nms.retransform_path(type,type_tr);
			text_file<<type_tr;
		}
		text_file<<"#"<<path.item(i).name.transcode();
	}
	else if(path.item(i).element_declaration.getNodeName().equals("element")){
		if(path.item(i).type==NULL){
			strcpy(type,((DOM_Element&)path.item(i).element_declaration).getAttribute("type").transcode());
			global_nms.retransform_path(type,type_tr);
			text_file<<type_tr;
		}else if(strncmp(path.item(i).type->name.transcode(),"Anonymous",9)){	//non anonymous_type
			strcpy(type,path.item(i).type->name.transcode());
			global_nms.retransform_path(type,type_tr);
			text_file<<type_tr;
		}
		text_file<<"#"<<path.item(i).name.transcode()<<"#"<<path.item(i).position;
	}

}


void write_path(int num_call){
	unsigned int i;
	unsigned int old_bitpointer=bitpointer;
	bool slash=true;

	if(num_call==423)
		in_sch_log<<"path 423"<<endl;

	if(mode==ABS_ROOT){
		text_file<<no_of_path<<":";
		for(i=0;i<path.depth;i++){
			text_file<<"/";
			write_textual_path(path,i);
		}
	}
	else{
		compare_paths();
		if(num_call==1) text_file<<"1:/"<<endl;
		text_file<<no_of_path<<":";
		for(i=0;i<rel_path.depth;i++){
			if(i&&slash) text_file<<"/";
			slash=true;
			char *name =rel_path.item(i).name.transcode();
			if (rel_path.item(i).name.equals("go_father")){
				if(i!=0) text_file<<"##";
				else slash=false;
			}else{
				write_textual_path(rel_path,i);	
			}
		}
		text_file<<endl;
		text_file<<no_of_path+1<<":";
		for(i=0;i<path.depth;i++){
			text_file<<"/";
			write_textual_path(path,i);
		}
		prev_path=path;
	}
	text_file<<endl;
	no_of_path++;
}



schema_type* search_type(DOMString type,bool xsi_mode,DOM_Node startpoint=schema.getLastChild()){	
	unsigned int i;
	DOM_Element elem;

	//we search the type in the stack of types
	char *naME=type.transcode();

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
	}else if(xsi_mode==true){
		for(i=0;i<types_read ;i++){
			if(compare_wo_nms(type,types[i].name)){
#ifdef WRITE_DEBUG_INFO
				report<<"type "<<types[i].name.transcode()<<" already inserted, return it!"<<endl; 
#endif
				return (&types[i]);
			}
		}	
	}else{
		for(i=0;i<types_read ;i++){
			if(type.equals(types[i].name)){
#ifdef WRITE_DEBUG_INFO
				report<<"type "<<types[i].name.transcode()<<" already inserted, return it!"<<endl; 
#endif
				return (&types[i]);
			}
		}
	}
	// the type is not inserted yet in the types vector

	char *nombre=type.transcode();
	DOM_NodeList list=startpoint.getChildNodes();
	char *nombre2=startpoint.getNodeName().transcode();

	for(i=0;i<list.getLength();i++){  
		//it could be better to do this with a walker??
		if(!list.item(i).getNodeName().equals("complexType")
			&&!list.item(i).getNodeName().equals("simpleType")
			&&!list.item(i).getNodeName().equals("attributeGroup"))//UNinAtb
				continue;	
		elem=(DOM_Element &) list.item(i);
		char *nombre=list.item(i).getNodeName().transcode();
		if(!elem.getAttribute("name").equals(""))
			char *elmnm=elem.getAttribute("name").transcode();
		if(elem.getAttribute("name").equals(type)|| elem.getAttribute("name").equals("")) 
		//if we are searching for an anonymous type, the atribute name is empty
			break;
		if(xsi_mode){
			if(compare_wo_nms(elem.getAttribute("name"),type)) break; //if the type is without prefix, it could not be found
		}

	}

	if(i==list.getLength()){
		//the type is probably a simple type defined in the XML Schema specification 
		//(string,integer,ID,...), so the best is to create a new node under schema and assign
		//it as the the type of the xml element. The type is created without childs, so the
		//node_lists tableA and tableB of the type will be empty
		DOM_Element simple=schema.createElement("simpleType");
		simple.setAttribute("name",type);
		char *instype=type.transcode();
		schema.getLastChild().appendChild(simple);
		elem=simple;
	}

	char *nombre4=list.item(i).getNodeName().transcode();
	char *nmnm=((DOM_Element &)list.item(i)).getAttribute("name").transcode();
	//if the type is a simpleType we don't do anything
	
	if(list.item(i).getNodeName().equals("complexType")||
		list.item(i).getNodeName().equals("attributeGroup")){//UNinAtb
		DOM_Element elem=(DOM_Element &)list.item(i);
		DOM_TreeWalker walker=schema.createTreeWalker(elem,0x00000003,NULL,1);

		bool b1=elem.getAttribute("base")!=NULL;
		bool b2=false;
		bool b3=false;
		if(!walker.firstChild().isNull()){
			b2=walker.getCurrentNode().getNodeName().equals("complexContent");
			b3=walker.getCurrentNode().getNodeName().equals("simpleContent");
		}
		if(b1||b2||b3){	
			//extension/restriction in the old and in the new schema!!!
			DOMString base=elem.getAttribute("base");	//old schema	
			if(base==NULL)		//new schema
				base=((DOM_Element&)walker.firstChild()).getAttribute("base");
			char *nombre=base.transcode();

			schema_type *pointer=search_type(base,false);//recursive function !!!				
			//we add the childs we have found in the recursive calls 
			if(pointer->type_definition.getNodeName().equals("simpleType")){
				DOM_Element content=schema.createElement("content");
				content.setAttribute("name","simple_content");
				content.setAttribute("type",base);
				char *nombre=content.getAttribute("name").transcode();
				types[types_read].elem_childs.add_node(content);
			}
			else
				types[types_read].elem_childs=pointer->elem_childs;
			
		}
		walker.setCurrentNode(list.item(i));
		walker.firstChild();
		char *nombre=walker.getCurrentNode().getNodeName().transcode();
		while(walker.getCurrentNode()!=list.item(i)){
			do{
				DOMString name=walker.getCurrentNode().getNodeName();
				//delete_namespace(name);
				char *nombre=name.transcode();
				if(name.equals("element")||name.equals("attribute")){
					char *nombre=((DOM_Element &)walker.getCurrentNode()).getAttribute("name").transcode();
					types[types_read].elem_childs.add_node(walker.getCurrentNode(), true);
					break;
				}
				if(name.equals("attributeGroup")){//UNinAtb
					DOMString AttGrp=((DOM_Element &)walker.getCurrentNode()).getAttribute("ref").transcode();
					//DOMString prefix=delete_namespace(AttGrp);
					char *attgp=AttGrp.transcode();
					schema_type *pointer=search_type(AttGrp,false);
					for(unsigned int u=0;u<pointer->elem_childs.get_length();u++)
						types[types_read].elem_childs.add_node(pointer->elem_childs.item(u));
				}
			}while(!walker.firstChild().isNull());
			while(walker.nextSibling().isNull()){
				if(walker.getCurrentNode()==list.item(i)) 
					break;
				walker.parentNode();
			}
		}
	}

	if(elem.getAttribute("name").equals(""))//if an anonymous type is inserted, the name is empty
		types[types_read].name=type;
	else
		types[types_read].name=elem.getAttribute("name");
	char *insertedname=types[types_read].name.transcode();
	report<<"inserted type"<<insertedname<<endl;

	types[types_read].type_definition=list.item(i);
	path.item(path.depth).type=&types[types_read];
	unsigned int longitud2=path.item(path.depth).type->elem_childs.get_length(); 
	//is there any way to delete the node???
	return (&types[types_read++]);
}


void search_declaration(DOMString type=NULL){	
	unsigned int i,num_subst_elem;
	node_list list;
	SubstGroup *ptr_SubstGroup;
	
	if (path.depth==0){	//root node!!
		DOM_Element elem;
				
		DOM_NodeList list=schema.getLastChild().getChildNodes();
		for(i=0;i<list.getLength();i++){
			char *nodnm=list.item(i).getNodeName().transcode();

			if (!list.item(i).getNodeName().equals("element"))
				continue;	
			elem=(DOM_Element &) list.item(i);
			if(elem.getAttribute("name").equals(path.item(0).name)) 
				break;
		}
		if(i==list.getLength()){
			for(i=0;i<list.getLength();i++){
				char *nodnm=list.item(i).getNodeName().transcode();
				if (!list.item(i).getNodeName().equals("element"))
					continue;	
				elem=(DOM_Element &) list.item(i);
				if(compare_wo_nms(elem.getAttribute("name"),path.item(0).name)) 
					break;
			}
		}
		path.item(0).element_declaration=elem;


		if(elem.hasChildNodes()){	//anonymous type
			DOMString type="Anonymous";
			type.appendData(elem.getAttribute("name"));
			path.item(0).type=search_type(type,false,list.item(i));
		}
		
		else{
			DOMString type=elem.getAttribute("type");
			//delete_namespace(type);
			path.item(0).type=search_type(type,false);
		}
	}

	else{
		list=path.item(path.depth-1).type->elem_childs;
		char *nombre=path.item(path.depth-1).type->name.transcode();
		DOM_Element elem;
		unsigned int listlength=list.get_length();
		for(i=0 ; i<listlength; i++){
			elem=(DOM_Element &) list.item(i);
			char *nombre=elem.getAttribute("name").transcode();
			char *nombre2=path.item(path.depth).name.transcode();
			/*if(elem.getAttribute("name").equals(path.item(path.depth).name)
			   ||elem.getAttribute("ref").equals(path.item(path.depth).name)) 
				break;*/
			if(compare_wo_nms(elem.getAttribute("name"),path.item(path.depth).name)||
				compare_wo_nms(elem.getAttribute("ref"),path.item(path.depth).name))
				break;
		}

		if(i==listlength){
			for(i=0;i<listlength;i++){
				elem=(DOM_Element &) list.item(i);
				DOMString elm_name=elem.getAttribute("name");
				if(elm_name.equals("")) elm_name=elem.getAttribute("ref");
				if(ptr_SubstGroup=is_head_of_substitution_group(elm_name)){
					num_subst_elem=search_substGroup_elem(ptr_SubstGroup,path.item(path.depth).name.transcode());				
				}else
					num_subst_elem=0;
				if(num_subst_elem){
					elem=retrieve_substgroup_elem(ptr_SubstGroup,num_subst_elem);
					break;
				}
			}
		}
		path.item(path.depth).element_declaration=elem;
//		char *namedecl=elem.getAttribute("name").transcode();

		if(elem.getAttribute("ref")!=NULL){	//reference!!
			DOMString ref=elem.getAttribute("ref");
			char *nombre=ref.transcode();
			DOM_NodeList list=schema.getLastChild().getChildNodes();
			for(i=0;i<list.getLength();i++){  
				if (!list.item(i).getNodeName().equals("element")
					&& !list.item(i).getNodeName().equals("attribute"))
					continue;	
				elem=(DOM_Element &) list.item(i);
				char *nombre=list.item(i).getNodeName().transcode();
				char *nombre2=elem.getAttribute("name").transcode();
				if(elem.getAttribute("name").equals(ref)) 
					break;
				if(compare_wo_nms(elem.getAttribute("name"),ref))
					break;
			}
		}

		if (type!=NULL)		//polymorphism!!
			path.item(path.depth).type=search_type(type,false);

		else if(elem.getAttribute("type").equals("")){	//anonymous type!!
			DOMString name="Anonymous";
			name.appendData(elem.getAttribute("name"));

			if(elem.getParentNode().getParentNode()!=schema){
				name.appendData("_in_");
				name.appendData(path.item(path.depth-1).type->name);
			}
			char *nombre=name.transcode();
			path.item(path.depth).type=search_type(name,false,elem);
		}
		else{ 
			DOMString type=elem.getAttribute("type");
			char *nombre=type.transcode();
			//DOMString prefix=delete_namespace(type);
			nombre=type.transcode();
			path.item(path.depth).type=search_type(type,false);
		}
	}
}

void navigate_xml_tree(){
	unsigned int i;
	int num_call=1;
	DOM_Node node;
	DOM_NamedNodeMap list;
	DOM_TreeWalker walker=doc.createTreeWalker(doc.getLastChild(),0x00000003,NULL,1);
	char nms_prefix[100];
	char qual_nms_prefix[200];

	node=walker.getCurrentNode();
	char *nombre=node.getNodeName().transcode();
	while(1){

		for(node=walker.getCurrentNode();!node.isNull();node=walker.firstChild()){
			report<<"Vamos al nodo "<<node.getNodeName().transcode()<<endl;
			char *nombre=node.getNodeName().transcode();
			if(!strcmp(nombre,"Fixed"))
				report<<"at breakpoint"<<endl;

			path.item(path.depth).name=node.getNodeName();
			path.item(path.depth).position=look_position(walker);	//aqui se pierde mucho tiempo
			path.item(path.depth).node=node;
			short tipo=node.getNodeType();
			walker.setCurrentNode(node);
			DOM_Element elem=(DOM_Element &) node;
			
			search_declaration();
			if(elem.getAttribute("xsi:type")!=""){
				DOMString type=elem.getAttribute("xsi:type");
				//delete_namespace(type);
				path.item(path.depth).type=search_type(type,true);
			}
			path.depth++;
			//we codify the attributes
			list=node.getAttributes();
			for(i=0;i<list.getLength();i++){
				path.item(path.depth).name=list.item(i).getNodeName();
				if(path.item(path.depth).name.equals("xsi:type")) continue;
				if(!strncmp(path.item(path.depth).name.transcode(),"xmlns",5)) continue;
				if(!strncmp(path.item(path.depth).name.transcode(),"xsi:schemaLocation",16)) continue;
				char *nombre=path.item(path.depth).name.transcode();
				path.item(path.depth).node=list.item(i);
				path.item(path.depth).position=1;
				search_declaration();
				if(has_nms_prefix(nombre)){
					int u=0;
					while(nombre[u]!=':') nms_prefix[u]=nombre[u++];
					nms_prefix[u]=0;
					char *glob_nms=local_nms_top.retrieve_nms(nms_prefix);
					char *full_qualified_nms=global_nms.retrieve_exp_nms(glob_nms);
					strcpy(qual_nms_prefix,full_qualified_nms);
					strcat(qual_nms_prefix,&nombre[u]);
					path.item(path.depth).name=path.item(path.depth).name.transcode(qual_nms_prefix);
					char *test_gen_str=path.item(path.depth).name.transcode();
				}
				report<<"codificamos el atributo "<<nombre<<endl;
				path.depth++;
				write_path(num_call);
				num_call++;
				path.depth--;
			}
		}
		//now we are in a leaf, so we codify
		report<<"estamos en la hoja "<<path.item(path.depth-1).name.transcode()<<", codificamos"<<endl;
		char *nombre=path.item(path.depth-1).name.transcode();
		
		if(path.item(path.depth-1).type!=NULL){
			if(path.item(path.depth-1).type->elem_childs.get_length()>0){
			for(unsigned int z=0;z<path.item(path.depth-1).type->elem_childs.get_length();z++)
				char *childname =path.item(path.depth-1).type->elem_childs.item(z).getNodeName().transcode();
		
			}
		}
		if(path.item(path.depth-1).type!=NULL 
		   && path.item(path.depth-1).type->elem_childs.get_length()>0
		   && path.item(path.depth-1).type->elem_childs.item(0).getNodeName().equals("content")){
			report<<"pero tiene un hijo simple_content!!!"<<endl;
			path.item(path.depth).name="simple_content";
			path.item(path.depth).node=path.item(path.depth-1).node.getLastChild();
			//we take the textual node where the simple content is written
			path.item(path.depth).element_declaration=path.item(path.depth-1).type->elem_childs.item(0);
			path.item(path.depth).type=NULL;
			path.depth++;
			write_path(num_call);
			num_call++;
			path.depth--;
		}
		else if(path.item(path.depth-1).node.hasChildNodes()){
			//we only want to encode the leaf if it has content!!!
			write_path(num_call);
			num_call++;
		}
		path.depth--;
		node=walker.nextSibling();
		if(node.isNull()){	// doesn't exist a brother
			while(walker.nextSibling().isNull()){
				node=walker.parentNode();
				char *nombre=walker.getCurrentNode().getNodeName().transcode();
				if (node.isNull()) 
					return;	//we are in the root node!!
				path.depth--;
			}
		}
	}
}

void delete_text_nodes(DOM_Node node){
	unsigned int i;
	DOM_NodeList list=node.getChildNodes();
	int numero=list.getLength();
	
	for(i=0;i<list.getLength();i++){
		short type=list.item(i).getNodeType();
		if(type==DOM_Node::TEXT_NODE || type==DOM_Node::COMMENT_NODE){
			node.removeChild(list.item(i));
			i--;	// there's a child less!!	
		}
	}
	int numero2=list.getLength();
}



void main(int argc, char *argv[]){ 
//navigation <xml_file> <xml_schema_file> <mode>

	/*
	possible values for mode
		absolute 1
		relative 2	
	*/

	int i;
	no_of_path=1;
	char *xml_file;
	char *sch_file;
	char text_length[10];
	char *p_tl;
	char name_of_results[40];
	char *name_of_descr;
	char *char_p;
	


	xml_file=argv[1];
	sch_file=argv[2];
	mode=atoi(argv[3]);

	printf("\ncall of interface_textual with parameters: %s %s %i\n\n",xml_file,sch_file,mode);

	if(mode==3){
		open_logs();
		write_statistic(sch_file);//write statisic of TBC-tables
		close_logs();
		exit(0);
	}
	strcpy(name_of_results,"results_");
	if(name_of_descr=strrchr(xml_file,'/')) name_of_descr++;
	else if(name_of_descr=strrchr(xml_file,'\\')) name_of_descr++;
	else name_of_descr=xml_file;

	strcat(name_of_results,name_of_descr);
	if(char_p=strrchr(name_of_results,'.')) *char_p=0;
	if(mode==ABS_ROOT) strcat(name_of_results,"_ABS");
	if(mode==REL) strcat(name_of_results,"_REL");

	strcat(name_of_results,".txt");
	results.open(name_of_results);
	report.open("report.txt");

	in_sch_log.open("in_sch_log_inft.txt");
	
	char path_modes[5][10]={"ABS_ROOT","ABS_ROOT","ABS_TOP","ABS_TOP","REL"};
	report<<"**** File "<<xml_file<<" ****"<<endl;
	report<<"**** Mode "<<path_modes[mode-1]<<" ****"<<endl<<endl;

	try{
		XMLPlatformUtils::Initialize();
	}
	catch(const XMLException& toCatch){
		cerr<<"Error\n"<<toCatch.getMessage()<<endl;
	}
	
	schema=parseFile(sch_file,true);
	global_nms.print_nms();
	extract_SubstGroups(schema);
	print_all_substgroups();

	delete_text_nodes(schema.getLastChild());
	rewrite_XML_file(schema);

	text_file.open("textual.txt");
	report<<"** Creating textual path!! **"<<endl<<endl;
	doc=parseFile(xml_file,false);
	navigate_xml_tree();
	report<<"** Textual path finished!! **"<<endl<<endl;
	text_file.close();

	read_text.open("textual.txt");

	char buffer[1500], *bits;
	char previous_element[1500];
	char *prev_pointer;
	char *decoded;
	char *actual_element;

	read_text.getline(buffer,1500);
	i=1;
	int not_ok=0;
	int bitsize=0;
	open_logs();

	while(buffer[0]!='\n'){
		results<<"path, no: "<<i<<endl;

		if(mode==REL){
			prev_pointer=buffer;
			while(*prev_pointer!=':') prev_pointer++;
			prev_pointer++;
			strcpy(previous_element,prev_pointer);
			read_text.getline(buffer,1500);
		}else
			strcpy(previous_element,"/");

		actual_element=buffer;
		while(*actual_element!=':') actual_element++;
		actual_element++;
		results<<actual_element;


		bits=encode_path(previous_element,actual_element,sch_file,mode,CPP_PATH_MAIN,i);
		results<<endl<<bits<<endl;
		decoded=decode_path(previous_element,bits,sch_file,mode,CPP_PATH_MAIN);
		
		results<<decoded<<endl;
		p_tl=text_length;

		while(*decoded!='%'){
			*p_tl++=*decoded++;
		}
		decoded++;
		*p_tl='\0';

		if(atoi(text_length)!=(int)strlen(bits)){
			results<<"length NOT OK!"<<endl;
			not_ok++;
		}else
			results<<"length OK!"<<endl;

		bitsize+=atoi(text_length);

		//results<<"compare:"<<endl;
		//results<<decoded<<endl;
		//results<<actual_element<<endl;
		if(strcmp(actual_element,decoded)){
			results<<"compare paths: "<<"NOT OK"<<endl<<endl;
			not_ok++;
		}
		else
			results<<"compare paths: "<<"OK"<<endl<<endl;

	
		read_text.getline(buffer,1500);
		if(++i==no_of_path) break;
		//if(i==1000) break;//for test purpose: only do 1000 paths;
	}
	results<<"NOT OK, total: "<<not_ok<<endl;
	results<<"bitsize:     "<<bitsize;    
	results.close();
	in_sch_log.close();
	close_logs();

}


DOMBiMErrorHandler::DOMBiMErrorHandler() :

    fSawErrors(false)
{
}

DOMBiMErrorHandler::~DOMBiMErrorHandler()
{
}

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

