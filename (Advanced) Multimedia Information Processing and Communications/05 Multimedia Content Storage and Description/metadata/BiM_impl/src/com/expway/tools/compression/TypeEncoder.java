/***********************************************************************
This software module was originally developed by Cédric Thiénot (Expway)
Claude Seyrat (Expway) and Grégoire Pau (Expway) in the course of 
development of the MPEG-7 Systems (ISO/IEC 15938-1) standard. 

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

Expway retains full right to use the code for his/her own purpose, 
assign or donate the code to a third party and to inhibit third parties 
from using the code for non MPEG-7 Systems (ISO/IEC 15938-1) conforming 
products. 

This copyright notice must be included in all copies or derivative works.

Copyright Expway © 2001.
************************************************************************/

package com.expway.tools.compression;

import java.io.*;
import com.expway.tools.io.*;
import com.expway.tools.expression.TypeDefinition;

public interface TypeEncoder {
 
    public int getBitsOfStructure();
    
    public void changeTypeDefinition(TypeDefinition td) ;
    public TypeDefinition getTypeDefinition();

    // POUR L'ENCODAGE
    public void         startEncoding() throws ParsingException;
    public TypeEncoder  encodeAttribute(String name) throws ParsingException;
    public void         endAttributeEncoding() throws ParsingException;
    public TypeEncoder  encodeElement(String name) throws ParsingException;
    public void         endEncoding() throws ParsingException;
    public boolean      isPartialInstantiated();
    public void         setPartialInstantiated(boolean bit);
    public String       getTypeName();

    public boolean isCodingFinished();
    public Chunk   getCoding();
    public Chunk getCodingWithContext(boolean bMode); // Coding spécial en cas de PI
    
    // pour les types simple et les mixed CM
    public void setValue(String value) throws ParsingException;

    // POUR LE DECODAGE
    public void startDecoding(BitToBitDataInputStream dis) throws DecodingException;
    public TypeEncoder decode(BitToBitDataInputStream dis) throws DecodingException;
    abstract public void endDecoding()throws DecodingException ;

	

}
