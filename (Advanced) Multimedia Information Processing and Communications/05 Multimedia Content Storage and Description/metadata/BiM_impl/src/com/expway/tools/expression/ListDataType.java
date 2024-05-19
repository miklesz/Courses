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

import java.io.Writer;
import java.io.IOException;
import java.util.StringTokenizer;

class ListDataType extends SimpleTypeDefinition {
    
    SimpleTypeDefinition theBaseTypeDefinition = null;
    String theBaseTypeName = null; 

    int codingLength = 64;
    int minlength = 0, maxlength = -1;
    // Par défaut, la taille minimale d'une liste est contrainte à 0 
    boolean minlengthconstrained = true, maxlengthconstrained = false;

    public ListDataType(String name, String baseTypeName){
        super(name,baseTypeName);
    }
       
    public void internalSetFacetValue(String name, String value){        
        if (name.equals(FAKE_BASETYPEFACET)){
            theBaseTypeName = value;
        }
        else if (name.equals(SchemaSymbols.ELT_MAXLENGTH)){
            maxlength = Integer.parseInt(value);   
            maxlengthconstrained = true;
        }
        else if (name.equals(SchemaSymbols.ELT_MINLENGTH)){
            minlength = Integer.parseInt(value);        
            minlengthconstrained = true;
        }
        else if (name.equals(SchemaSymbols.ELT_LENGTH)){
            maxlength = Integer.parseInt(value);        
            minlength = maxlength;
            maxlengthconstrained = true;
            minlengthconstrained = true;
        }
        
        if (maxlengthconstrained && minlengthconstrained)
            codingLength = (int)Math.ceil(Math.log((double)(maxlength-minlength+1))/Math.log(2.0));
    }

    public void realize(TypeDefinitions tds) throws DefinitionException {
        super.realize(tds);

        if (theBaseTypeName!=null)
            theBaseTypeDefinition = (SimpleTypeDefinition)tds.getTypeDefinition(theBaseTypeName);
    }

    public void encodeItInto(String value, ChunkWriter cw){
        try { 
            StringTokenizer st =new StringTokenizer(value," \t\n\r\f,;");
            int v = st.countTokens();
            
            if (maxlengthconstrained && minlengthconstrained){
                //System.out.println("coding " + this + " = " + (v-minlength) + " over " + codingLength + " bits");
                cw.writeInt(v-minlength,codingLength);
            }
            else {
                cw.writeInfiniteLong(v);
            }
            
            //
            while(st.hasMoreTokens()){
                String s = st.nextToken();
                //System.out.println(" ======= coding " + s);
                theBaseTypeDefinition.encodeItIntoContextAware(s,cw);
            }
            
        } catch(NumberFormatException nfe){ System.out.println("numberformatexception");throw new RuntimeException(nfe.getMessage());}
     }

    public void decode(BitToBitDataInputStream bis,Writer w){
        try { 

            long l = 0;
            if (maxlengthconstrained && minlengthconstrained){
                l = bis.readInt(codingLength) + minlength;
            }
            else {
                l = bis.readInfiniteLong();
            }
            
            if (l>0) {
                theBaseTypeDefinition.decodeContextAware(bis,w);
                for (int t=1;t<l;t++){
                    w.write(" ");
                    theBaseTypeDefinition.decodeContextAware(bis,w);
                }
            }
        } catch(IOException ioe){ throw new RuntimeException(ioe.getMessage());}
    }

    
}

