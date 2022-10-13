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

#include "bitstream.h"
#include "global_header.h"
#include <iostream.h>
#include <fstream.h>

#ifdef WRITE_DEBUG_INFO 
extern ofstream report;
#endif

bitstream::bitstream(){
	bitpointer=0;//constructor
}

unsigned int bitstream::bit_length(unsigned int num){
	int i=0;
	while(num){
		i++;
		num>>=1;
	}
	return i;
}

void bitstream::write_bits(int value,unsigned int bits){
	unsigned int i,filter=1<<(bits-1);
	unsigned char offset;
	int address;
	//MSB is written first in byte

	if(value<0){
#ifdef WRITE_DEBUG_INFO
		report<<"write value<0!!!"<<endl<<flush;
		report<<"abort..."<<endl<<flush;
#endif
	}

#ifdef WRITE_DEBUG_INFO
	report<<"bitpointer: "<<bitpointer;
	report<<", write_bits("<<value<<","<<bits<<")"<<endl<<flush;
#endif

	for(i=0;i<bits;i++){
		address=bitpointer/8;
		offset=(bitpointer%8);
		bitpointer++;
		if(!offset) bin_xml[address]=0;
		if(value&filter){
			bin_xml[address]+=1<<(7-offset);
		}
		filter>>=1;	
	}	
}

int bitstream::read_bits(int num_bits){	
	int i,value=0;
	int address;
	unsigned char offset;
#ifdef WRITE_DEBUG_INFO
	report<<"bitpointer: "<<bitpointer;
#endif
	for(i=0;i<num_bits;i++){
		address=bitpointer/8;
		offset=1<<(7-bitpointer%8);
		if(bin_xml[address]&offset) 
			value+=(1<<(num_bits-i-1));
		bitpointer++;
	}
#ifdef WRITE_DEBUG_INFO
	report<<", take "<<num_bits<<" bits, value="<<value<<endl<<flush;
#endif
	return value;
}

void bitstream::write_var_len_int(unsigned int value){
	int num_chunks=0;
	unsigned int test_length=value;
	int address;
	unsigned char offset;
	unsigned int filter;
	int i;
#ifdef WRITE_DEBUG_INFO
	report<<"bitpointer: "<<bitpointer<<" write_var_len_int("<<value<<")"<<endl<<flush;
#endif
	//detect number of 4 bit chunks, necessary to represent the integer
	while(test_length){
		num_chunks++;
		test_length>>=4;
	}

	//write Ext bits field
	for(i=0;i<num_chunks;i++){
		address=bitpointer/8;
		offset=bitpointer%8;
		bitpointer++;

		if(!offset) bin_xml[address]=0; //initialize new byte with zero
		if(i!=num_chunks-1) bin_xml[address]+=1<<(7-offset);//last bit of Ext bits is null!
	}

	//write integer itself, MSB first
	filter=1<<(num_chunks*4-1);
	for(i=0;i<num_chunks*4;i++){
		address=bitpointer/8;
		offset=bitpointer%8;
		bitpointer++;

		if(!offset) bin_xml[address]=0; //initialize new byte with zero
		if(value&filter) bin_xml[address]+=1<<(7-offset);
		filter>>=1;
	}
}

unsigned int bitstream::read_var_len_int(){
	int i;
	int address;
	unsigned char mask;
	int length=1; // minimum length of integer is one chunk of 4 bits
	unsigned int value=0;

#ifdef WRITE_DEBUG_INFO
	report<<"bitpointer: "<<bitpointer;
#endif
	for(;;){
		address=bitpointer/8;
		mask=1<<(7-bitpointer%8);
		bitpointer++;
		if(bin_xml[address]&mask) length++;
		else break;	//zero in bitstream indicates end of "Ext bits" field
	}

	length=length*4; //convert number of chunks to number of bits
	for(i=0;i<length;i++){
		address=bitpointer/8;
		mask=1<<(7-bitpointer%8);
		bitpointer++;
		if(bin_xml[address]&mask) value+=1<<(length-1-i);
	}
#ifdef WRITE_DEBUG_INFO
	report<<" read_var_len_int, value: "<<value<<endl<<flush;
#endif
	return value;
}

void bitstream::read_txt_bin_path(char *txt_bin_path){

 for(int i=0;txt_bin_path[i];i++){
	if(i%8==0) bin_xml[i/8]=0;
	if(txt_bin_path[i]=='1') bin_xml[i/8]+=(1<<(7-i%8));
 }
}

void bitstream::convert_bin_to_txt(){
	unsigned int i;
	for(i=0;i<bitpointer;i++){
		if(bin_xml[i/8]&(1<<(7-i%8))) txt_bin_path[i]='1';
		else txt_bin_path[i]='0';
	}
	txt_bin_path[i]=0;
#ifdef WRITE_DEBUG_INFO
	report<<"encoded path: "<<endl;
	for(i=0;txt_bin_path[i];i++) report<<txt_bin_path[i];
	report<<endl;
#endif
}

unsigned int bitstream::get_bitpointer(){
	return bitpointer;
}

unsigned char bitstream::get_bin_xml(unsigned int i){
	return bin_xml[i];
}

void bitstream::set_bin_xml(unsigned int i, unsigned char value){
	bin_xml[i]=value;
}

char *bitstream::get_txt_bin_path(){
	return txt_bin_path;
}

