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


package com.expway.tools.expression;


import com.expway.schema.SchemaSymbols;

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;
import com.expway.tools.compression.TypeInstance;
import com.expway.tools.utils.MethodsBag;

import java.io.Writer;
import java.io.IOException;
import java.util.*;


class UnionDataType extends SimpleTypeDefinition {
    
    List theBaseTypeDefinitions = null;
    List theBaseTypeNames = null;
    int numberOfTypes = 0;
    int typeInfoCodingLength = 0;

    public UnionDataType(String name, String baseTypeName){
        super(name,baseTypeName);
        theBaseTypeDefinitions = new ArrayList();
        theBaseTypeNames = new ArrayList();
    }
    
    public void internalSetFacetValue(String name, String value){ 
        if (name.equals(FAKE_BASETYPEFACET)){
            theBaseTypeNames.add(value);
            numberOfTypes++;
        }
    }
    
    public void realize(TypeDefinitions tds) throws DefinitionException { 
        super.realize(tds);

        Iterator i = theBaseTypeNames.iterator();
        while (i.hasNext()){
            String sTemp = (String)i.next();
            SimpleTypeDefinition stdTemp = (SimpleTypeDefinition)tds.getTypeDefinition(sTemp);
            if (stdTemp == null)
                throw new DefinitionException("Base type " + sTemp + " of union " + getName() + " not found");
            theBaseTypeDefinitions.add(stdTemp);
        }
        typeInfoCodingLength = MethodsBag.getCodingLength(numberOfTypes);
    }

    public SimpleTypeDefinition getMatchingType(String value,ChunkWriter cw) {
        Iterator i = theBaseTypeDefinitions.iterator();
        boolean error=true;
        SimpleTypeDefinition stdTemp=null;

        int t=0;
        while (i.hasNext()){
            ChunkWriter cwTemp = new ChunkWriter();
            stdTemp = (SimpleTypeDefinition)i.next();
            try {
                if (t==0)
                    cwTemp.writeInt(0,1);
                else {
                    cwTemp.writeInt(1,1);
                    cwTemp.writeInt(t,typeInfoCodingLength);
                }
                stdTemp.encodeItIntoContextAware(value,cwTemp);
                if (TypeInstance.ENCODEDEBUG) System.out.println("   encoded using " + stdTemp.getName());
                error=false;
                cwTemp.writeYourselfInto(cw);
                break;
            }
            catch(Exception e){}
            t++;
        }
        if (error) return null; 
        else return stdTemp;
    }

    public void encodeItInto(String value, ChunkWriter cw) {
        SimpleTypeDefinition stdTemp=getMatchingType(value,cw);
        
        if (stdTemp==null) 
            throw new RuntimeException("The value " + value + "does not match any of the member types of union " + getName());
    }
    
    public void decode(BitToBitDataInputStream bis,Writer w){
        if (TypeInstance.DECODEDEBUG) System.out.println("decoding an union");
        SimpleTypeDefinition toBeCalledForDecoding = null;
        try {
            if(bis.readInt(1)==0){
                toBeCalledForDecoding = (SimpleTypeDefinition)theBaseTypeDefinitions.get(0);
            } else {
                int index=bis.readInt(typeInfoCodingLength);
                try {toBeCalledForDecoding = (SimpleTypeDefinition)theBaseTypeDefinitions.get(index);}
                catch(IndexOutOfBoundsException ioobe){
                    throw new RuntimeException("Incorrect type info during  decoding of union " + getName() );
                }
            }
            toBeCalledForDecoding.decodeContextAware(bis,w);
        }
        catch(IOException ioe){ throw new RuntimeException(ioe.getMessage());}
    }

    
}

