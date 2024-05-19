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

import java.io.*;

class IntegerDataType extends NumberDataType {
    long min=Integer.MIN_VALUE,max=Integer.MAX_VALUE;
    static final private int HUGE_VALUE = 1124748361;

    private boolean minconstrained = false, maxconstrained = false;
    private int codingLength = 64;

    public IntegerDataType(String name,String primitive){
        super(name,primitive);
    }

    
    
    public void setSimpleFacetValue(String name,String value){

        if (name.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)){
            try {  max = Long.parseLong(value)-1; } catch(NumberFormatException nfe){ }
            maxconstrained = true;
        }
        else if (name.equals(SchemaSymbols.ELT_MINEXCLUSIVE)){
            try {  min = Long.parseLong(value)+1; } catch(NumberFormatException nfe){ }
            minconstrained = true;
        }
        else if (name.equals(SchemaSymbols.ELT_MAXINCLUSIVE)){
            try {  max = Long.parseLong(value); } catch(NumberFormatException nfe){ }
            maxconstrained = true;
        }
        else if (name.equals(SchemaSymbols.ELT_MININCLUSIVE)){
            try {  min = Long.parseLong(value); } catch(NumberFormatException nfe){ }
            minconstrained = true;
        }
        
        if (maxconstrained && minconstrained){
            double range=(double)(max-min+1);
            // codingLength est dans le pire des cas, 1 bit trop grand puisqu'on utilise ceil
            codingLength = (int)Math.ceil(Math.log(range)/Math.log(2.0));

            // est-ce que codingLength-1 aurait suffit ?
            if (range<=Math.pow(2,codingLength-1)) codingLength--;
        }
    }
    
    public void encodeItInto(String value, ChunkWriter cw){
        if (isEnumerated()) super.encodeItInto(value,cw);
        else {
            try {
                long v = Long.parseLong(value); 
                //System.out.println("real coding of " + value + " = " + v);
                if (maxconstrained && minconstrained){
                    //System.out.println("coding " + this + " = " + (v-min) + " over " + codingLength + " bits");
                    cw.writeLong(v-min,codingLength);
                }
                else { // un int
                    cw.writeLong(v,32);
                }
            } catch(NumberFormatException nfe){ throw new RuntimeException("IntegerDataType : NumberFormatException "+nfe.getMessage());}
        }
    }

    public void decode(BitToBitDataInputStream bis,Writer w){
        if (isEnumerated()) super.decode(bis,w);
        else {
            try { 
                if (maxconstrained && minconstrained){
                    int v = bis.readInt(codingLength);
                    v += min;
                    if (TypeInstance.DECODEDEBUG) System.out.println("                                Int read... "+v+ " " + codingLength + " bits");
                    w.write(""+v);
                }
                else {
                    int v = bis.readInt();
                    if (TypeInstance.DECODEDEBUG) System.out.println("                                Int read... "+v);
                    w.write(""+v);
                }
            } catch(IOException ioe){ ioe.printStackTrace();throw new RuntimeException(ioe.getMessage());}
        }
    }

    
}

