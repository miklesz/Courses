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

#ifndef BITSTREAM
#define BITSTREAM

class bitstream{
	static char txt_bin_path[10000];//textual form of binary path (i.e. '1' and '0')
	static unsigned char bin_xml[1000];//binary path
	static unsigned int bitpointer;

public:
    bitstream();
	static unsigned int bit_length(unsigned int num);
	static unsigned char get_bin_xml(unsigned int i);
	static void set_bin_xml(unsigned int i, unsigned char value);
    static char *get_txt_bin_path();
	static void write_bits(int value,unsigned int num_bits);
	static int read_bits(int num_bits);
	static void write_var_len_int(unsigned int value);
	static unsigned int read_var_len_int();
	static unsigned int get_bitpointer();
	static void read_txt_bin_path(char *txt_bin_path);
	static void convert_bin_to_txt();
};

#endif