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

public class IncrLengthTransition extends LengthConditionalTransition {
     public IncrLengthTransition(String accept, State fromS, State toS){
         super(accept,fromS,toS);
     }

    public boolean accept(ActivityToken at,Object evenement,Object applicationContext) {
        //System.out.println("ACCEPT1");
        // en mode decompression on ne laisse passer le token que le compteur qui
        // se trouve au sommet de sa pile est different de 0
        if (mode == CompressionMode.DECOMPRESSION){
            if (((DecompressionActivityToken)at).getCpt()>0) return true;
            else {
                //System.out.println("                  transition rejected because cpt("+((DecompressionActivityToken)at).getCpt()+") <= 0");
                return false;
            }
        }
        return super.accept(at,evenement,applicationContext);
    }
    
    public void cross(ActivityToken at,Object applicationContext) throws RejectionException{
        //System.out.println("CROSS1");
        //((CompressionActivityToken)at).log(this + " (CROSS1)");

        super.cross(at,applicationContext);
        if (mode == CompressionMode.COMPRESSION)
            ((CompressionActivityToken)at).incrCpt();
        else if (mode == CompressionMode.DECOMPRESSION)
            ((DecompressionActivityToken)at).decrCpt();
        //          else 
        //              throw new RuntimeException("Ne devrait pas arriver !!");
    }


    public String getLabel(){
        return super.getLabel() + ", OK if Cpt < " + (max==com.expway.tools.expression.OccurrenceNode.UNBOUNDED?"*":""+max) + ", Cpt++";
    }

}
