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

#include <dom/DOM.hpp>
#include <util/PlatformUtils.hpp>
#include <parsers/DOMParser.hpp>
#include <dom/DOM_TreeWalker.hpp>
#include <dom/DOM_Document.hpp>
#include <stdlib.h>
#include <fstream.h>
#include <stdio.h>
#include <string.h>
#include "com_expway_util_Path.h"

//functions
_declspec(dllexport)
char *encode_path(const char *previous_element,const char *actual_element,const char *sch_file, 
				  int nav_mode, int select_main, int no_of_path);

_declspec(dllexport)
char *decode_path(const char *previous_element,char *txt_bin_path, const char *sch_file, 
				  int nav_mode, int select_main);

JNIEXPORT jbyteArray JNICALL Java_com_expway_util_Path_encode_1path
	(JNIEnv *env, jobject obj, jstring j_schema, jstring j_previous_element, 
		jstring j_current_element, jbyte j_mode);

JNIEXPORT jstring JNICALL Java_com_expway_util_Path_decode_1path
(JNIEnv *env, jobject, jstring j_schema, jstring j_previous_element, jbyteArray j_bin_path, jbyte j_mode);

/* MPEG-7 Systems (ISO/IEC 15938-1) 7.5.5.6 Multiple Payload Mode */
_declspec(dllexport)
char *encode_path_multi(
				  const char *previous_element,
				  char **actual_element_list,
				  const char *sch_file, 
				  int nav_mode,
				  int select_main,
				  int no_of_path
				  );

_declspec(dllexport)
char **decode_path_multi(
				  const char *previous_element,
				  char *txt_bin_path,
				  const char *sch_file,				  
				  int nav_mode,
				  int select_main
				  );
