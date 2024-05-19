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


#define ABS_ROOT 1
#define REL      2

/* MPEG-7 Systems (ISO/IEC 15938-1) 7.5.4 Context Mode */
#define ABS_ROOT_MULTI  ABS_ROOT	
#define REL_MULTI       REL
//#define ABS_ROOT_MULTI  3	/* 011b */
//#define REL_MULTI       4	/* 100b */

#define POSCODE    1
#define NO_POSCODE 0

#define TEXTUAL 0
#define ATTRIBUTE 1

//content models
#define ALL			1
#define CHOICE		2
#define SEQUENCE	3

//identifier for different types of tables (used in alphabetical_order)
#define TABLE_A		0
#define TABLE_B		1

#define MAX_NUMBER_OF_TYPES 600	//maximal nuimber of TBC tables, that can be handeled
#define MAX_MBG_DEPTH		15	//maximal nesting depth of content models (choice, group)
#define MAX_NODE_LIST_LEN   500	//maximal number of nodes in a node_list
#define MAX_NAME_LENGTH     150	//maximal number of charakters for a name

//main(), that call the encoder and decoder functions
#define JAVA_MAIN 0
#define CPP_PATH_MAIN 1
#define DEC_CONF_MAIN 2

//identifier for different types of paths
#define PATH 1
#define REL_PATH 2
#define PREV_PATH 3

//marker
//UNnms: namespace support
//NOABS: no abstract type 

#define WRITE_DEBUG_INFO //comment out this, if no debug info shall be written to file
#define FULL_QUALIFIED_NAMES //comment out this, if full  qualified names shall not be used 
//#define WRITE_POLY_INFO //comment out this, if no debug info related to polymorhism shall be written to file


