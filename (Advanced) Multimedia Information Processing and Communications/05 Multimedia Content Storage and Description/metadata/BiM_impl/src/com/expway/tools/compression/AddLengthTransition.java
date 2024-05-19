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
import java.util.Collection;
import java.io.*;

public class AddLengthTransition extends LengthTransition {

    private int codingLength = -1;
    private int minOccurs = 0;
    
    public AddLengthTransition(String accept, State fromS, State toS){
        super(accept,fromS,toS);
    }

    /** pour decider de la taille du codage du compteur, -1 = taille 
     *  indefini codage entier infini ASN1/BPF */
    public void setCptCodingLength(int t){
        codingLength = t;
    }
    
    /** pour decider de la taille du codage du compteur, -1 = taille 
     *  indefini codage entier infini ASN1/BPF */
    public void setCptMinOccurs(int mo){
        minOccurs = mo;
    }
    
    public boolean accept(ActivityToken at,Object evenement,Object applicationContext) {
        return super.accept(at,evenement,applicationContext);
    }

    public void cross(ActivityToken at,Object applicationContext) throws RejectionException{
        if (mode == CompressionMode.COMPRESSION){
            ((CompressionActivityToken)at).addCpt(minOccurs,codingLength);
            ((CompressionActivityToken)at).incrCpt(); // +1 car il y aura a coup sûr un element
        }        
        else if (mode == CompressionMode.DECOMPRESSION){
            try {
                int i = 0;
                if (codingLength != 0) // on recupere l'occurrence codee sur codingLength bits (=0 si minOccurs == maxOccurs)
                    {
                        //System.out.println(" read number of occurrences on " + codingLength + " bits");
                        i = ((DecompressionContext)applicationContext).getNextKey(codingLength);
                    }
                
                ((DecompressionActivityToken)at).addCpt(i+minOccurs);
                ((DecompressionActivityToken)at).decrCpt();  // -1 car il y aura a coup sur un element

            } catch(IOException ioe){
                throw new RuntimeException("IO Error " + ioe.getMessage());
            }
        }
    }

    public String getLabel(){
        return super.getLabel() + ", AddCpt";
    }

}
