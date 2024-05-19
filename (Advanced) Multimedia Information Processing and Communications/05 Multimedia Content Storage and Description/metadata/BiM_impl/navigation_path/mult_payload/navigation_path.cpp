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

#include "navigation_path.h"
#include "global_header.h"
#include "codec.h"
#include "bitstream.h"
#include "parse_file.h"
#include "polymorphism.h"
#include "textual_path.h"
#include "position_code.h"
#include "SubstGrp.h"
#include "namespaces.h"
#include <fstream.h>

#include "textual_path_list.h" // ISO/IEC 15938-1 7.5.5.6

#ifdef WRITE_DEBUG_INFO
ofstream report;
ofstream poly_log;
ofstream type_log;
ofstream rep_sbstGrp;
ofstream in_sch_log;
ofstream comp_log;
#endif


//global variables

//variables, that shall be persistent over many calls of the encode_path
//and decode_path functions

unsigned int mode;
schema_type build_TBCs::types[MAX_NUMBER_OF_TYPES];
unsigned int build_TBCs::types_read;

name_space global_nms;				//table, that maps URIs to global identifiers (e.g. "http://...."<-> "g0")
DOM_Document parse_file::doc;
DOM_Document parse_file::schema;	//DOMTree of the XML-document and thee schema
node_list parse_file::elements;		//list of all the elements defined under schema
node_list parse_file::attributes;	//list of all the attribute defined under schema
node_list parse_file::extensions;	//list of all the extensions of the NAMED types
SubstGroup *FirstSubstGroup=NULL;        //Pointer to the first SubstGroup
bool parse_file::new_schema; 
//------------------------------------------------------------------------

//the path-structures, that contain the elements and datatypes, that shall be 
//processed by the codec 
struct path_type codec::path, codec::rel_path, codec::prev_path;
//------------------------------------------------------------------------

//the bitstream, that's used by encoder and decoder to process the binary path
char bitstream::txt_bin_path[10000];//textual form of binary path (i.e. '1' and '0')
unsigned int bitstream::bitpointer;//pointer to the actual position in the bitstream
unsigned char bitstream::bin_xml[1000];//binary path
//------------------------------------------------------------------------


char position_code::ext_stack[10][MAX_NAME_LENGTH];
int position_code::ext_stack_depth;

node_list polymorphism::childs;
DOM_Document polymorphism::poly_tree;

char textual_path::txt_path[1000];

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


JNIEXPORT jbyteArray JNICALL Java_com_expway_util_Path_encode_1path
	(JNIEnv *env, jobject obj, jstring j_schema, jstring j_previous_element, 
		jstring j_current_element, jbyte j_mode){

	const char *previous_element;
	const char *current_element;
	const char *schema;
	jbyteArray j_byte_array;
	int mode;
	unsigned int i;
	jbyte byte_buf;

#ifdef WRITE_DEBUG_INFO
	report.open("report_enc.txt");
#endif

	mode=(int) j_mode;
	
	previous_element= env->GetStringUTFChars(j_previous_element, 0);
	current_element= env->GetStringUTFChars(j_current_element, 0);

	schema= env->GetStringUTFChars(j_schema, 0);
#ifdef WRITE_DEBUG_INFO
	report<<"call encode_path with parameters: "<<endl;
	report<<"previous element= "<<previous_element<<endl;
	report<<"current element= "<<current_element<<endl;
	report<<"schema= "<<schema<<endl;
	report<<"mode= "<<mode<<endl<<endl;
	report<<flush;
#endif
	encode_path(previous_element,current_element,schema,mode,JAVA_MAIN,0);
	
	//length of array is lenth of bitpointer + 2 bytes for length info

	j_byte_array=env->NewByteArray((jsize)((bitstream::get_bitpointer()-1)/8+3));

#ifdef WRITE_DEBUG_INFO
	report<<"bitpointer is: "<<bitstream::get_bitpointer()<<endl<<flush;
#endif
	
	//write length info in path
	if(bitstream::get_bitpointer()<65536){
		byte_buf=bitstream::get_bitpointer()%256;
		env->SetByteArrayRegion(j_byte_array,0,(jsize)1,&byte_buf);
		byte_buf=bitstream::get_bitpointer()/256;
#ifdef WRITE_DEBUG_INFO
		report<<"bin_xml[0]="<<bitstream::get_bitpointer()%256<<endl;
		report<<"bin_xml[1]="<<bitstream::get_bitpointer()/256<<endl;
#endif
		env->SetByteArrayRegion(j_byte_array,1,(jsize)1,&byte_buf);
	}
	else{
#ifdef WRITE_DEBUG_INFO
		report<<"binary path too long"<<endl<<flush;
#endif
		return 0;
	}
		

	for(i=0;i<(bitstream::get_bitpointer()-1)/8+1;i++){
		byte_buf=(jbyte)bitstream::get_bin_xml(i);//this converts unsigned char to signed char!
#ifdef WRITE_DEBUG_INFO
		report<<"bin_xml["<<i+2<<"]="<<(int)bitstream::get_bin_xml(i)<<endl<<flush;
#endif
		env->SetByteArrayRegion(j_byte_array,i+2,(jsize)1,&byte_buf);
	}

    env->ReleaseStringUTFChars(j_previous_element, previous_element);
	env->ReleaseStringUTFChars(j_current_element, current_element);
	env->ReleaseStringUTFChars(j_schema, schema);

#ifdef WRITE_DEBUG_INFO
	report.close();
#endif

	return j_byte_array;
}

_declspec(dllexport)
char *encode_path(const char *previous_element,const char *actual_element,const char *sch_file, int nav_mode, int select_main, int no_of_path){
	char schema_file[100];
	char previous_transformed[1000];
	char actual_transformed[1000];

	mode=nav_mode;

#ifdef WRITE_DEBUG_INFO
	if(select_main!=JAVA_MAIN) report.open("report_enc.txt");
#endif
	
	try{
		XMLPlatformUtils::Initialize();
	}
	catch(const XMLException& toCatch){
		cerr<<"Error\n"<<toCatch.getMessage()<<endl;
	}

	strcpy(schema_file, sch_file);


	if(parse_file::get_schema().isNull()){

#ifdef WRITE_DEBUG_INFO	
		in_sch_log.open("include_log.txt");
#endif
		parse_file::set_schema(parse_file::parseFile(schema_file));
		global_nms.order_alpha_nms();
		global_nms.print_nms();

#ifdef WRITE_DEBUG_INFO	
		in_sch_log.close();
#endif
		parse_file::delete_text_nodes(parse_file::get_schema().getLastChild());
		extract_SubstGroups(parse_file::get_schema());
		print_all_substgroups();
		parse_file::rewrite_XML_file(parse_file::get_schema());
		//exit(0);
	}
	global_nms.transform_path(previous_element,previous_transformed);
	global_nms.transform_path(actual_element,actual_transformed);

#ifdef WRITE_DEBUG_INFO
	report<<"no_of_path: "<<no_of_path<<endl;
	report<<"prev_elem: "<<previous_element<<endl;
	report<<"actu_elem: "<<actual_element<<endl;
	report<<"prev_transformed: "<<previous_transformed<<endl;
	report<<"actu_transformed: "<<actual_transformed<<endl;

	report<<"file parsed"<<endl<<flush;
#endif	

	if(parse_file::get_extensions().get_length()==0)
		parse_file::prepare_schema(parse_file::get_schema().getLastChild()); //create polyinfo

	if(polymorphism::get_poly_tree().isNull()){
		polymorphism::create_poly_tree();
	}
#ifdef WRITE_DEBUG_INFO
	report<<"** Encode **"<<endl<<endl<<flush;
#endif
	if(mode==REL){
#ifdef WRITE_DEBUG_INFO
		report<<"read textual path of prev_path"<<endl;
#endif
		textual_path::read_textual_path(PREV_PATH,previous_transformed,ABS_ROOT);  //the previous_path is coded in absolute mode
	}
#ifdef WRITE_DEBUG_INFO
	report<<"read textual path "<<endl<<flush;
#endif
	textual_path::read_textual_path(PATH,actual_transformed,mode);
#ifdef WRITE_DEBUG_INFO
	report<<"encode binary path"<<endl<<endl<<flush;
#endif
	codec::encode_path(mode,POSCODE);
	if(select_main!=JAVA_MAIN) bitstream::convert_bin_to_txt();

#ifdef WRITE_DEBUG_INFO
	report<<"** Encoding finished! **"<<endl<<endl;	
	if(select_main!=JAVA_MAIN) report.close();
#endif

	return bitstream::get_txt_bin_path();
}

JNIEXPORT jstring JNICALL Java_com_expway_util_Path_decode_1path
(JNIEnv *env, jobject, jstring j_schema, jstring j_previous_element, jbyteArray j_bin_path, jbyte j_mode){
	const char *previous_element;
	const char *schema;
	int nav_mode;
	int i;

	previous_element= env->GetStringUTFChars(j_previous_element, 0);
	schema=env->GetStringUTFChars(j_schema, 0);

	jbyte *bin_path= env->GetByteArrayElements(j_bin_path, 0);
   
//lenght of array can be selected by java module!
	jsize bin_length = env->GetArrayLength(j_bin_path);
	nav_mode=(int)j_mode;

#ifdef WRITE_DEBUG_INFO
	report.open("report_dec.txt");
	report<<"bin_length: "<<bin_length<<endl<<flush;
	report<<"call decode path with parameters: "<<endl;
	report<<"previous element= "<<previous_element<<endl;
	report<<"schema= "<<schema<<endl;
	report<<"mode= "<<mode<<endl<<endl;
	report<<flush;
#endif
	for(i=0;i<bin_length;i++){
		bitstream::set_bin_xml(i,(unsigned char)bin_path[i]);
	}
	decode_path(previous_element, (char *)NULL, schema, nav_mode, JAVA_MAIN);
	
	env->ReleaseStringUTFChars(j_previous_element, previous_element);
	env->ReleaseStringUTFChars(j_schema, schema);
	env->ReleaseByteArrayElements(j_bin_path, bin_path,0 );

#ifdef WRITE_DEBUG_INFO
	report<<endl<<"textual:"<<textual_path::get_textual_path()<<endl<<flush;
	report.close();
#endif

	return env->NewStringUTF(textual_path::get_textual_path());
}

_declspec(dllexport)
char *decode_path(const char *previous_element,char *txt_bin_path, const char *sch_file, int nav_mode, int select_main){
	char schema_file[100];
	char previous_transformed[1000];

#ifdef WRITE_DEBUG_INFO
	if(select_main==CPP_PATH_MAIN) 
		report.open("report_dec.txt");
#endif
	
	mode=nav_mode;	

	try{
		XMLPlatformUtils::Initialize();
	}
	catch(const XMLException& toCatch){
		cerr<<"Error\n"<<toCatch.getMessage()<<endl;
	}

	//initialize variables

	/*
	for(unsigned int i=0;i<types_read;i++){
		char *typenm=types[i].name.transcode();
		types[i].tableA.set_num_nodes(0);
		types[i].tableB.set_num_nodes(0);
		types[i].name="";
	}
	elements.set_num_nodes(0);
	attributes.set_num_nodes(0);	
	extensions.set_num_nodes(0);	
	types_read=0;
	ext_stack_depth=0;
    */

	strcpy(schema_file, sch_file);

	if(parse_file::get_schema().isNull()){
		parse_file::parseFile(schema_file);
		parse_file::delete_text_nodes(parse_file::get_schema().getLastChild());
		extract_SubstGroups(parse_file::get_schema());
	}

	if(parse_file::get_extensions().get_length()==0)
		parse_file::prepare_schema(parse_file::get_schema().getLastChild()); //create polyinfo

	if(polymorphism::get_poly_tree().isNull()){
		polymorphism::create_poly_tree();
	}
	global_nms.transform_path(previous_element,previous_transformed);

#ifdef WRITE_DEBUG_INFO
	report<<"** Decode **"<<endl<<endl;
	report<<"previous_element: "<<previous_element<<endl;
	report<<"previous_transformed: "<<previous_transformed<<endl;
#endif

	if(mode==REL&&select_main!=DEC_CONF_MAIN){//if main==DEC_CONF_MAIN, keep last encoded path in path variable
		textual_path::read_textual_path(PATH,previous_transformed,ABS_ROOT);  //the previous_path is coded in absolute mode
	}

	if(select_main==CPP_PATH_MAIN)
		bitstream::read_txt_bin_path(txt_bin_path);

	/*if(select_main==JAVA_MAIN||select_main==CPP_PATH_MAIN) 
		bitpointer=0;*/

	if(select_main==DEC_CONF_MAIN){
		codec::decode_path(mode,NO_POSCODE);
	}
	else codec::decode_path(mode,POSCODE);

	textual_path::write_textual_path(mode);

#ifdef WRITE_DEBUG_INFO	
	report<<endl<<"** Decoding finished:"<<endl;
	report<<textual_path::get_textual_path()<<endl<<flush;
	if(select_main==CPP_PATH_MAIN) 
		report.close();
#endif

	return textual_path::get_textual_path();	
}

_declspec(dllexport)
void open_logs(){
#ifdef WRITE_DEBUG_INFO
	poly_log.open("poly_log.txt");
	type_log.open("type_log.txt");
	rep_sbstGrp.open("SubstGrp_log.txt");
	comp_log.open("compare_log.txt");
#endif
}

_declspec(dllexport)
void close_logs(){
#ifdef WRITE_DEBUG_INFO
	poly_log.close();
	type_log.close();
	rep_sbstGrp.close();
	comp_log.close();
#endif
}

/* MPEG-7 Systems (ISO/IEC 15938-1) 7.5.5.6 Multiple Payload Mode */
_declspec(dllexport)
char *encode_path_multi(
				  const char *previous_element,
				  char **actual_element_list,
				  const char *sch_file, 
				  int nav_mode,
				  int select_main,
				  int no_of_path
				  )
{
	char schema_file[100];
	char previous_transformed[1000];
	char actual_transformed[1000];

	char **list;				// ISO/IEC 15938-1 7.5.5.6
	char *actual_element;		// ISO/IEC 15938-1 7.5.5.6

	list = actual_element_list;	// ISO/IEC 15938-1 7.5.5.6
	actual_element = *list++;	// ISO/IEC 15938-1 7.5.5.6

	mode=nav_mode;

#ifdef WRITE_DEBUG_INFO
	if(select_main!=JAVA_MAIN) report.open("report_enc.txt");
#endif
	
	try{
		XMLPlatformUtils::Initialize();
	}
	catch(const XMLException& toCatch){
		cerr<<"Error\n"<<toCatch.getMessage()<<endl;
	}

	strcpy(schema_file, sch_file);


	if(parse_file::get_schema().isNull()){

#ifdef WRITE_DEBUG_INFO	
		in_sch_log.open("include_log.txt");
#endif
		parse_file::set_schema(parse_file::parseFile(schema_file));
		global_nms.order_alpha_nms();
		global_nms.print_nms();

#ifdef WRITE_DEBUG_INFO	
		in_sch_log.close();
#endif
		parse_file::delete_text_nodes(parse_file::get_schema().getLastChild());
		extract_SubstGroups(parse_file::get_schema());
		print_all_substgroups();
		parse_file::rewrite_XML_file(parse_file::get_schema());
		//exit(0);
	}
	global_nms.transform_path(previous_element,previous_transformed);
	global_nms.transform_path(actual_element,actual_transformed);

#ifdef WRITE_DEBUG_INFO
	report<<"no_of_path: "<<no_of_path<<endl;
	report<<"prev_elem: "<<previous_element<<endl;
	report<<"actu_elem: "<<actual_element<<endl;
	report<<"prev_transformed: "<<previous_transformed<<endl;
	report<<"actu_transformed: "<<actual_transformed<<endl;

	report<<"file parsed"<<endl<<flush;
#endif	

	if(parse_file::get_extensions().get_length()==0)
		parse_file::prepare_schema(parse_file::get_schema().getLastChild()); //create polyinfo

	if(polymorphism::get_poly_tree().isNull()){
		polymorphism::create_poly_tree();
	}
#ifdef WRITE_DEBUG_INFO
	report<<"** Encode **"<<endl<<endl<<flush;
#endif
	if(mode==REL){
#ifdef WRITE_DEBUG_INFO
		report<<"read textual path of prev_path"<<endl;
#endif
		textual_path::read_textual_path(PREV_PATH,previous_transformed,ABS_ROOT);  //the previous_path is coded in absolute mode
	}
#ifdef WRITE_DEBUG_INFO
	report<<"read textual path "<<endl<<flush;
#endif
//	textual_path::read_textual_path(PATH,actual_transformed,mode); // ISO/IEC 15938-1 7.5.5.6
#ifdef WRITE_DEBUG_INFO
	report<<"encode binary path"<<endl<<endl<<flush;
#endif
//	codec::encode_path(mode,POSCODE); // ISO/IEC 15938-1 7.5.5.6
	{ // ISO/IEC 15938-1 7.5.5.6
		textual_path::read_textual_path(PATH, actual_transformed, mode);
		codec::encode_firstpath(mode, POSCODE);
		textual_path::read_textual_path(PREV_PATH, actual_transformed, mode);
		codec::set_NumberOfMultiOccurrenceLayer();

		while(*list != NULL){
			actual_element = *list++;
			global_nms.transform_path(actual_element, actual_transformed);
			textual_path::read_textual_path(PATH, actual_transformed, mode);
			codec::encode_increment(mode, POSCODE);
			textual_path::read_textual_path(PREV_PATH, actual_transformed, mode);
		}

		codec::encode_termination();
	}

	if(select_main!=JAVA_MAIN) bitstream::convert_bin_to_txt();

#ifdef WRITE_DEBUG_INFO
	report<<"** Encoding finished! **"<<endl<<endl;	
	if(select_main!=JAVA_MAIN) report.close();
#endif

	return bitstream::get_txt_bin_path();
}

_declspec(dllexport)
char **decode_path_multi(
				  const char *previous_element,
				  char *txt_bin_path,
				  const char *sch_file,				  
				  int nav_mode,
				  int select_main
				  )
{
	char schema_file[100];
	char previous_transformed[1000];

#ifdef WRITE_DEBUG_INFO
	if(select_main==CPP_PATH_MAIN) 
		report.open("report_dec.txt");
#endif
	
	mode=nav_mode;	

	try{
		XMLPlatformUtils::Initialize();
	}
	catch(const XMLException& toCatch){
		cerr<<"Error\n"<<toCatch.getMessage()<<endl;
	}

	//initialize variables

	/*
	for(unsigned int i=0;i<types_read;i++){
		char *typenm=types[i].name.transcode();
		types[i].tableA.set_num_nodes(0);
		types[i].tableB.set_num_nodes(0);
		types[i].name="";
	}
	elements.set_num_nodes(0);
	attributes.set_num_nodes(0);	
	extensions.set_num_nodes(0);	
	types_read=0;
	ext_stack_depth=0;
    */

	strcpy(schema_file, sch_file);

	if(parse_file::get_schema().isNull()){
		parse_file::parseFile(schema_file);
		parse_file::delete_text_nodes(parse_file::get_schema().getLastChild());
		extract_SubstGroups(parse_file::get_schema());
	}

	if(parse_file::get_extensions().get_length()==0)
		parse_file::prepare_schema(parse_file::get_schema().getLastChild()); //create polyinfo

	if(polymorphism::get_poly_tree().isNull()){
		polymorphism::create_poly_tree();
	}
	global_nms.transform_path(previous_element,previous_transformed);

#ifdef WRITE_DEBUG_INFO
	report<<"** Decode **"<<endl<<endl;
	report<<"previous_element: "<<previous_element<<endl;
	report<<"previous_transformed: "<<previous_transformed<<endl;
#endif

	if(mode==REL&&select_main!=DEC_CONF_MAIN){//if main==DEC_CONF_MAIN, keep last encoded path in path variable
		textual_path::read_textual_path(PATH,previous_transformed,ABS_ROOT);  //the previous_path is coded in absolute mode
	}

	if(select_main==CPP_PATH_MAIN)
		bitstream::read_txt_bin_path(txt_bin_path);

	/*if(select_main==JAVA_MAIN||select_main==CPP_PATH_MAIN) 
		bitpointer=0;*/

	if(select_main==DEC_CONF_MAIN){
		codec::decode_path(mode,NO_POSCODE);
	}
//	else codec::decode_path(mode,POSCODE); // ISO/IEC 15938-1 7.5.5.6

//	textual_path::write_textual_path(mode); // ISO/IEC 15938-1 7.5.5.6

	{ // ISO/IEC 15938-1 7.5.5.6
		int	nlayer;
		int	bits;
		int	value;
		char *textual_path;

		/* clear textual_path_list */
		textual_path_list::clear();

		/* decode first path */
		codec::decode_firstpath(mode, POSCODE);
		textual_path::write_textual_path(mode);

		/* add textual path to the textual_path_list */
		textual_path = textual_path::get_textual_path();
		textual_path_list::add(textual_path);

		/* calculate number of bits required for incremental position codes */
		codec::set_NumberOfMultiOccurrenceLayer();
		nlayer = codec::get_NumberOfMultiOccurrenceLayer();
		bits = bitstream::bit_length(nlayer + 2);

		/* read bits for next codes */
		value = bitstream::read_bits(bits);

		while(value != ((1<<bits)-1)){
			/* decode incremental position codes */
			codec::decode_increment(value);
			textual_path::write_textual_path(mode);

			/* add textual path to the textual_path_list */
			textual_path = textual_path::get_textual_path();
			textual_path_list::add(textual_path);

			/* read bits for next codes */
			value = bitstream::read_bits(bits);
		}
	}

#ifdef WRITE_DEBUG_INFO	
	report<<endl<<"** Decoding finished:"<<endl;
	report<<textual_path::get_textual_path()<<endl<<flush;
	if(select_main==CPP_PATH_MAIN) 
		report.close();
#endif

//	return textual_path::get_textual_path();	// ISO/IEC 15938-1 7.5.5.6
	return textual_path_list::get_list();		// ISO/IEC 15938-1 7.5.5.6
}
