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

// ============================================================
// COMPRESSION CONTEXT

class DecompressionContext extends CompressionApplicationContext {

    // utilisé aussi dans CompressionActivityToken
    static final public int INFINITE_INTEGER_THRESHOLD = 16;

    BitToBitDataInputStream theInputStream = null;

    String theElement =null;
    String theAttribute =null;
    int theNextKey = -1;


    public DecompressionContext(BitToBitDataInputStream dis){
        theInputStream = dis;
    }

    public int getNextKey(int length) throws IOException{
        if (theNextKey == -1){
            //System.out.print(" Lecture de " + length + " bits sur le flux = ");
            if (length == -1 || length>INFINITE_INTEGER_THRESHOLD)
                theNextKey = (int)theInputStream.readInfiniteLong();
            else
                theNextKey = theInputStream.readInt(length);
            //System.out.println("    read key " + theNextKey);
            return theNextKey;
        }
        else {
            //System.out.println("                         reutilisation des " + length + " bits deja lus");
            return theNextKey;
        }
    }

    public void setCurrentElementName(String s) {
        //System.out.println("                          context setCurrentElement "+s );
        theElement = s;
    }

    public String getCurrentElementName() {
        return theElement;
    }

    /*public void setCurrentAttributeName(String s) {
        //System.out.println("                         context setCurrentAttribute "+s );
        theAttribute = s;
    }

    public String getCurrentAttributeName() {
        return theAttribute;
        }*/

    public void clear(){
        super.clear();
        theNextKey = -1;
        theElement = null;
    }
}


