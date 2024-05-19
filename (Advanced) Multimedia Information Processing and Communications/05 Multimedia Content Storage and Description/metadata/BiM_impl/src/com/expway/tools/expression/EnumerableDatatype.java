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

import com.expway.tools.automata.FiniteStateAutomata;

import com.expway.tools.compression.CompressionFiniteStateAutomata;
import com.expway.tools.compression.SimpleTypeInstance;
import com.expway.tools.compression.TypeInstance;
import com.expway.tools.compression.TypeEncoder;
import com.expway.tools.io.ChunkWriter;

import com.expway.tools.utils.MethodsBag;

import com.expway.tools.io.BitToBitDataInputStream;

import com.expway.util.URIRegistry;

import com.expway.schema.SchemaSymbols;

import java.io.*;
import java.util.*;

public abstract class EnumerableDatatype extends SimpleTypeDefinition {
    TreeSet possibleValues = null;

    private int codingLength = -1;

    public EnumerableDatatype(String name,String primitive){
        super(name,primitive);
    }

    public Collection getSortedPossibleValues(){
        return possibleValues;
    }
    
    public boolean isEnumerated(){
        return possibleValues!=null;
    }

    public void realize(TypeDefinitions tds) throws DefinitionException {
        super.realize(tds);
      
        if (isEnumerated()){
            codingLength = MethodsBag.getCodingLength(possibleValues.size());
        }
    }
    
    public void encodeItInto(String value, ChunkWriter cw){
        int pos = MethodsBag.getPosition(value,possibleValues);
        //System.out.println("encoding of " + value + " in enumeration " +possibleValues + " " + pos + "[" + codingLength+"]");
        if (pos == -1)
            throw new RuntimeException("Value " + value + " not in enumeration");

        cw.writeInt(pos,codingLength);
    }

    public void decode(BitToBitDataInputStream bis,Writer w){
        try { 
            int pos = bis.readInt(codingLength);
            w.write(""+MethodsBag.getObjectAt(pos,possibleValues));
        } catch (IOException ioe){
            throw new RuntimeException(ioe.getMessage());
        }
    }
         
    final public void internalSetFacetValue(String name, String value) {
        if (name.equals(SchemaSymbols.ELT_ENUMERATION))
            {
                if (possibleValues==null)
                    possibleValues = new TreeSet();
                //System.out.println("add enumeration = " + value);
                possibleValues.add(value);
            }
        else
            setSimpleFacetValue(name,value);
    }
    
    

    abstract public void setSimpleFacetValue(String name, String value);
}
