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

package com.expway.binarisation;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import com.expway.util.URIRegistry;
import com.expway.util.LocalHandler;
import com.expway.util.HandlerException;

import com.expway.tools.expression.TypeDefinitions;
import com.expway.tools.expression.TypeDefinition;
import com.expway.tools.expression.SetOfDefinitions;
import com.expway.tools.expression.ComplexTypeDefinition;
import com.expway.tools.compression.TypeEncoder;
import com.expway.tools.compression.SpecificTypeInstance;
import com.expway.tools.compression.ParsingException;

import com.expway.schema.xml.XMLSchemaInstance;

import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

public class SpecificHandler extends LocalHandler{    
    private final static boolean DEBUG=false;
    
    SpecificTypeInstance specificTypeInstance;
    SetOfDefinitions setOfDefinitions;
    int bitsOfStructure = 0;
    LocalHandler first;

    // Le premier
    public SpecificHandler(SpecificTypeInstance sti,SetOfDefinitions sd){
        super();
        setOfDefinitions=sd;
        specificTypeInstance=sti;
        first=this;
    }

    // Copie
    public SpecificHandler(SpecificHandler sh) {
        super();
        specificTypeInstance=sh.specificTypeInstance;
        first=sh.first;
    }

    private String getCodecType() {
        return specificTypeInstance.getBinariserType();
    }

    public int getBitsOfStructure(){return bitsOfStructure;}

    public void reset() {
    }
    
    public void informEnd(LocalHandler son) throws HandlerException {        
    }
      
    public void init(String uri, String local, String raw, Attributes attrs) throws HandlerException {
        if (DEBUG) System.out.println("specifichandler_init this="+this+" raw="+raw);
        try {
            specificTypeInstance.startElement(uri,local,raw,attrs);
        } catch (Exception e) {
            throw new HandlerException(e,"Error in SpecificCodec of type "+getCodecType()+" : "+
                                       e,HandlerException.TYPE_FATAL);
        }
    }
    
    public LocalHandler getSonHandler(String uri, String local, String raw) throws HandlerException {
        if (DEBUG) System.out.println("specifichandler_getson this="+this+" raw="+raw); 
        return new SpecificHandler(this);
    }

    public void end(String uri, String local, String raw) throws HandlerException {
        if (DEBUG) System.out.println("specifichandler_end this="+this);

        try {
            specificTypeInstance.endElement(uri,local,raw);
        } catch (Exception e) {
            throw new HandlerException(e,"Error in SpecificCodec of type "+getCodecType()+" : "+
                                       e,HandlerException.TYPE_FATAL);
        }

        if (this==first) {
            specificTypeInstance.endEncoding();
        }
    }

    public void end() throws HandlerException {
        throw new HandlerException(null,"specific_handler end() should not be called !",HandlerException.TYPE_FATAL);
    }
    
    public void characters(char[] ch, int start, int length) throws HandlerException {
        if (DEBUG) System.out.println("specifichandler_chars this="+this);
        try {
            specificTypeInstance.characters(ch,start,length);
        } catch (Exception e) {
            throw new HandlerException(e,"Error in SpecificCodec of type "+getCodecType()+" : "+
                                       e,HandlerException.TYPE_FATAL);
        }
    }

    public boolean hasPoolHoldByHandlerManager(){return false;}

    public Object getCreation() throws HandlerException {return specificTypeInstance;}
}
