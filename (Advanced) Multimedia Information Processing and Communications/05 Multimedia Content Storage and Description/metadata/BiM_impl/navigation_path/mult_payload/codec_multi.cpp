/* MPEG-7 Systems (ISO/IEC 15938-1) 7.5.5.6 Multiple Payload Mode */
#include <stdio.h>
#include <assert.h>
#include <dom/DOM.hpp>
#include "global_header.h"
#include "codec.h"
#include "bitstream.h"

bool look_if_unbounded (DOM_Node node);

/*******************************************************************
  NumberOfMultiOccurrenceLayer
*******************************************************************/
int codec::m_NumberOfMultiOccurrenceLayer;

/*******************************************************************
  encode_firstpath()

  encode:
	- SBCs
	- Substitution Codes
	- Type Codes
	- Initial Position Codes
*******************************************************************/
void codec::encode_firstpath(int mode, bool poscode){

	codec::encode_path(mode, poscode);
}

/*******************************************************************
  encode_increment()

  encode:
	- Incremental Position Codes (Increment)
*******************************************************************/
void codec::encode_increment(int mode, bool poscode){
	unsigned int	bits;
	unsigned int	layer;
	bool			flag;
	unsigned int	i;
	bool			unbounded;
	unsigned int	increment_code;

	/* calculate number of bits required for increment code */
	bits	= bitstream::bit_length(m_NumberOfMultiOccurrenceLayer + 2);

	layer	= 0;
	flag	= false;

	for(i=0; i<path.depth; i++){
		if(path.item(i).text_attrib == TEXTUAL){
			unbounded = look_if_unbounded(path.item(i).element_declaration);
		}else{
			unbounded = false;
		}

		if(unbounded){
			layer++;
			if((!flag) && (path.item(i).position != prev_path.item(i).position)){
				increment_code = layer;
				bitstream::write_bits(increment_code, bits);
				flag = true;
			}
		}
	}
}

/*******************************************************************
  encode_termination()

  encode:
	- Incremental Position Codes (Termination)
*******************************************************************/
void codec::encode_termination(void){
	unsigned int	bits;
	int				termination_code;

	/* calculate number of bits required for termination code */
	bits = bitstream::bit_length(m_NumberOfMultiOccurrenceLayer + 2);

	/* set termination code (all '1') */
	termination_code = (1<<bits) -1;

	/* write termination code to a bitstream */
	bitstream::write_bits(termination_code, bits);
}

/*******************************************************************
  encode_gap()

  encode:
	- Incremental Position Codes (Gap)
*******************************************************************/
void codec::encode_gap(void){
	unsigned int	bits;
	int				gap_code;

	/* calculate number of bits required for gap code */
	bits = bitstream::bit_length(m_NumberOfMultiOccurrenceLayer + 2);

	/* set gap code (all '0') */
	gap_code = 0;

	/* write gap code to a bitstream */
	bitstream::write_bits(gap_code, bits);
}

/*******************************************************************
  decode_firstpath()

  decode:
	- SBCs
	- Substitution Codes
	- Type Codes
	- Initial Position Codes
*******************************************************************/
void codec::decode_firstpath(int mode, bool poscode){

	codec::decode_path(mode, poscode);
}

/*******************************************************************
  decode_increment()

  decode:
	- Incremental Position Codes
*******************************************************************/
void codec::decode_increment(int val){
	int				value;
	unsigned int	i;
	bool			unbounded;
	DOM_Node		node;
	int				layer=0;

	// increment_code should be smaller than or equal to 
	// the number_of_multi_occurrence_layer
	assert(val<=m_NumberOfMultiOccurrenceLayer);

	value=val;
	for(i=0;i<path.depth;i++){
		node=path.item(i).element_declaration;

		if(node.getNodeName().equals("content")){
			unbounded=false; // simple_content
		}else{
			unbounded=look_if_unbounded(node);
		}

		if(unbounded){
			layer++;
			if(layer<value){
				/* higher layer */
				;
			}else if(layer==value){
				/* increment layer */
				path.item(i).position++;
			}else if(layer>value){
				/* lower layer */
				path.item(i).position=1;
			}
		}
	}
}

/*******************************************************************
  set_NumberOfMultiOccurrenceLayer()
	- calculate and set NumberOfMultiOccurrenceLayer of (current) path
*******************************************************************/
void codec::set_NumberOfMultiOccurrenceLayer(void){
	unsigned int	i;
	bool			unbounded;

	/* initialize the NumberOfMultiOccurrenceLayer */
	m_NumberOfMultiOccurrenceLayer = 0;

	/* check if the node is multiple occurence node or not */
	for(i=0; i<path.depth; i++){
		if(path.item(i).text_attrib == TEXTUAL){
			unbounded = look_if_unbounded(path.item(i).element_declaration);
		}else{
			unbounded = false;
		}

		if(unbounded){
			m_NumberOfMultiOccurrenceLayer++;
		}
	}
}

/*******************************************************************
  get_NumberOfMultiOccurrenceLayer()
	- get NumberOfMultiOccurrenceLayer of (current) path
*******************************************************************/
int codec::get_NumberOfMultiOccurrenceLayer(void){

	return m_NumberOfMultiOccurrenceLayer;
}

/*******************************************************************
  look_if_unbounded()
	- look if the node has unbounded attribute or not
*******************************************************************/
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
