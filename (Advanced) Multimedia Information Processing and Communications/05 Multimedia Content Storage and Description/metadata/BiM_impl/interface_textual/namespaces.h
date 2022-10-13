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

#ifndef NAMESPACES
#define NAMESPACES


typedef struct nms_table{
	char *source_nms;	//pointer to source namespace
	char *target_nms;	//pointer to string, the source nms is mapped to
	struct nms_table *next; //pointer to next entry in table;
}NMSTable;

class name_space{	//"namespace" is a keyword
	NMSTable *first_entry;
public:
	name_space();
	~name_space();
	void insert_nms_save(char *nms);
	void insert_nms_save(char *nms,char *gbl_sh);
	char *retrieve_nms(char *nms);
	char *retrieve_exp_nms(char *nms);
	void print_nms();
	void order_alpha_nms();
	int is_alpha_order_correct(char *glob_short1,char *glob_short2);
	void transform_path(const char *path, char *transf_path);
	void retransform_path(const char *path, char *transf_path);
	operator=(name_space& nms);
};

#endif