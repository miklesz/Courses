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

import com.expway.tools.automata.*;
import com.expway.tools.expression.*;
import java.io.*;
import com.expway.tools.io.*;

import com.expway.util.URIRegistry;

// ============================================================
// SIMPLE TYPE

public class SimpleTypeInstance extends TypeInstance {

    String theValue = null;

    // ------------------------------------------------------------ 

    public SimpleTypeInstance(TypeDefinition td){
        super(td);
    }

    // ------------------------------------------------------------ 

    public TypeEncoder encodeAttribute(String name) throws ParsingException{
        throw new ParsingException("no attributes ("+URIRegistry.getWithoutPrefix(name)+") are expected in a simple type");
    }
    
    public TypeEncoder encodeElement(String name) throws ParsingException{
        throw new ParsingException("no elements are expected in a simple type");
        }
    
    public void endAttributeEncoding() throws ParsingException{
        throw new ParsingException("no attributes are expected in a simple type");
    }

    // ------------------------------------------------------------ 

    public void setValue(String value) throws ParsingException {
        theValue = value;
        //TODO : myDefinition.checkValue(value);
    }
    
    public void endEncoding() throws ParsingException {
        /* A REMETTRE POUR LA BINARISATION */
        ChunkWriter cw = new ChunkWriter(); // TODO POOL
        //System.out.println(" enencoding of " + this);
        ((SimpleTypeDefinition)myDefinition).encodeItIntoContextAware(theValue,cw);
        setCoding(cw);
        if (ENCODEDEBUG) System.out.println("     encoding of " + this + " value = " + theValue + " length = " + cw.sizeInBits() + " bits");
    }

    // =============================================================================================
    // DECODAGE

    // l'attribut en cours de decodage

    private String myAttribute = null;

    public void setAttributeName(String s) { myAttribute = s; }
    public String getAttributeName()       { return myAttribute; }

    public void startDecoding(BitToBitDataInputStream dis)throws DecodingException{
        if (DECODEDEBUG) System.out.println(" --->  Start decoding SimpleType \"" + this + "\"");
        
        if (!STUPIDPARSING){
            try {
                String elementName=getElementName();
                String attributeName=getAttributeName();
                            
                if (getElementName() != null && !(getElementName().equals(SimpleTypeDefinition.MIXED_KEY))) {
                    getElementWriter().write("\n<"+elementName+">");
                }
                if (getAttributeName() != null) {
                    getElementWriter().write(" "+attributeName+"=\"");
                }
            } catch(IOException ioe){
                throw new DecodingException(ioe.getMessage());
            }
        }

        ((SimpleTypeDefinition)myDefinition).decodeContextAware(dis,getElementWriter());
    }

    public TypeEncoder decode(BitToBitDataInputStream dis)throws DecodingException {return null;}

    public void endDecoding() throws DecodingException {

        if (!STUPIDPARSING){
            try {
                String elementName=getElementName();

                if (getElementName() != null && !(getElementName().equals(SimpleTypeDefinition.MIXED_KEY))) {
                    getElementWriter().write("</"+elementName+">");
                }
                if (getAttributeName() != null) {
                    getElementWriter().write("\"");
                }
            } catch(IOException ioe){
                throw new DecodingException(ioe.getMessage());
            }
        }

        //if (DECODEDEBUG) System.out.println(" --->  End decoding SimpleType = " + this);
    }
    
}


