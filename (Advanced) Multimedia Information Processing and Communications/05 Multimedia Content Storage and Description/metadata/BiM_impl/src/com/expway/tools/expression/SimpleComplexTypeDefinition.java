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
import com.expway.tools.automata.FiniteStateAutomataInterface;

import com.expway.tools.compression.CompressionFiniteStateAutomata;
import com.expway.tools.compression.SimpleComplexTypeInstance;
import com.expway.tools.compression.TypeInstance;
import com.expway.tools.compression.SimpleTypeInstance;
import com.expway.tools.compression.TypeEncoder;
import com.expway.tools.io.ChunkWriter;
import com.expway.util.URIRegistry;

import com.expway.tools.io.*;

import java.io.*;
import java.util.*;


public class SimpleComplexTypeDefinition extends ComplexTypeDefinition {

    static private int unid = 1;

    SimpleTypeDefinition theSimpleTypeDefinition = null;
    String               theSimpleTypeName = null;
    
    public SimpleComplexTypeDefinition(String name,String primitive){
        super(name);
        theSimpleTypeName = name;
        //@@ SCTAA - SIMPLECOMPLEXTYPE A AMELIORER
        theSimpleTypeDefinition = SimpleTypeDefinition.newSimpleTypeDefinition("internalSimpleType_" + unid++,primitive);
    }

    public boolean ignoreWhiteSpace() {return theSimpleTypeDefinition.ignoreWhiteSpace();}

    public void setFacetValue(String name,String value){
        theSimpleTypeDefinition.setFacetValue(name,value);
    }
   
    public TypeEncoder newInstance(){
        return new SimpleComplexTypeInstance(this);
    }

    public TypeEncoder newSimpleInstance(){
        return new SimpleTypeInstance(theSimpleTypeDefinition);
    }
    
    public void realize(TypeDefinitions tds) throws DefinitionException {
        super.realize(tds);
        theSimpleTypeDefinition.realize(tds);
    }

    public boolean isItUsedHere(String s){
        if (super.isItUsedHere(s))
            return true;
        
        if (theSimpleTypeName.equals(s)) return true;
        return false;
    }

    // ------------------------------------------------------------
    // Encodage / Décodage

    public TypeEncoder getContentEncoder(){
        return theSimpleTypeDefinition.newInstance();
    }

    public void encodeItInto(String value, ChunkWriter cw){
        theSimpleTypeDefinition.encodeItIntoContextAware(value,cw);
    }

    public void decode(BitToBitDataInputStream bis, Writer w){
        theSimpleTypeDefinition.decodeContextAware(bis,w);
    }

    
}



