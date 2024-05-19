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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.altkom.video.*;

// Specific binariser Wrapper for Visual types (Binariser)

public class SpecificTypeInstance extends TypeInstance {
    Binariser binariser;
    boolean DEBUG=false;

    // ------------------------------------------------------------ 

    public SpecificTypeInstance(TypeDefinition td,Binariser b){
        super(td);
        binariser=b;
        binariser.takeDescriptor();
        if (DEBUG) System.out.println("SpecificTypeInstance instanciation : binariser="+binariser);
    }

    public String getBinariserType() {
        return binariser.toString();
    }

    // ------------------------------------------------------------ 

    public TypeEncoder encodeAttribute(String name) throws ParsingException{
        throw new ParsingException("no attributes are expected in a specific type");
    }
    
    public TypeEncoder encodeElement(String name) throws ParsingException{
        throw new ParsingException("no elements are expected in a specific type");
    }
    
    public void endAttributeEncoding() throws ParsingException{
        throw new ParsingException("no attributes are expected in a specific type");
    }

    // ------------------------------------------------------------ 

    public void setValue(String value) throws ParsingException {
        throw new ParsingException("setvalue is not expected in a specific type");
    }
    
    public void endEncoding() {
        setCoding(binariser.getChunkWriter());
        if (DEBUG) System.out.println("SpecificTypeInstance endEnconding : binariser="+binariser);
    }

    // --- Minimal SAX Dispatcher to Visual Binariser classes
    public void characters(char[] ch, int start, int length) throws SAXException {
        binariser.characters(ch,start,length);
    }

    public void startElement(String uri, String local, String raw, Attributes attrs) throws SAXException {
        binariser.startElement(uri,local,raw,attrs);
    }

    public void endElement(String uri, String local, String raw) throws SAXException {
        binariser.endElement(uri,local,raw);
    }

    // =============================================================================================
    // DECODAGE

    public void startDecoding(BitToBitDataInputStream dis)throws DecodingException {        
    }

    public TypeEncoder decode(BitToBitDataInputStream dis) throws DecodingException {
        if (DEBUG) System.out.println("SpecificTypeInstance decode : binariser="+binariser);
        binariser.decode(dis,getElementWriter(),getElementName(),null);
        return null;
    }

    public void endDecoding() throws DecodingException {
    }
    
}


