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

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;
import com.expway.tools.compression.TypeInstance;

import java.io.*;

class DoubleDataType extends NumberDataType {

    double min=Double.MIN_VALUE,max=Double.MAX_VALUE;
    boolean constrained = false;
    
    public DoubleDataType(String name,String primitive){super(name,primitive);}
    
    public void setSimpleFacetValue(String name,String value){}

    public void encodeItInto(String value, ChunkWriter cw){
        if (isEnumerated()) super.encodeItInto(value,cw);
        else {
            try { 
                double v = Double.parseDouble(value); 
                cw.writeDouble(v);
            } catch(NumberFormatException nfe){ throw new RuntimeException(nfe.getMessage());}
        }
    }

    public void decode(BitToBitDataInputStream bis,Writer w){
        if (isEnumerated()) super.decode(bis,w);
        else {
            try { 
                double v = bis.readDouble();
                if (TypeInstance.DECODEDEBUG) System.out.println("                                Double read... "+v);
                w.write(""+v);
            } catch(IOException ioe){ throw new RuntimeException(ioe.getMessage());}
        }
    }

    
}

