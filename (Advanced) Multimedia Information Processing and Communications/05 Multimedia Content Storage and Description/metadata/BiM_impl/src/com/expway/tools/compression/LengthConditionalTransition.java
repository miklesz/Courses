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
import java.util.Collection;

/** refuse une transition si l'activité (qui doit être une CompressionActivityToken)
    a un compteur plus grand que prévu */

abstract public class LengthConditionalTransition extends LengthTransition {
    
    public LengthConditionalTransition(String accept, State fromS, State toS){
        super(accept,fromS,toS);
    }
    
    protected int min=0, max=OccurrenceNode.UNBOUNDED;
    
    /** met en place une transition conditionnelle, la valeur retournée
        par l'activité doit etre comprise entre min et max */ 
    public void setLengthTransitionConditions(int min,int max){
        this.min=min;
        this.max=max;
    }
    
    /** 
     * appelé pour savoir si la conjonction événement et activité est 
     * susceptible d'être acceptée par la transition le token ne doit
     * pas être modifié par la méthode 
     */
    
    public boolean epsilonAccept(ActivityToken at,Object applicationContext){
        //System.out.println("EPSILONACCEPT of " + this + " on cat " + ((CompressionActivityToken)at).name() );
        //((CompressionActivityToken)at).log(this + " (EPSILONACCEPT)");
        int l = ((CompressionActivityToken)at).getCpt();
        if (l<min) {
            //System.out.println(" refused because length("+l+") < min ("+min+")");
            return false;
        }
        else if (l>=max && max!=OccurrenceNode.UNBOUNDED){
            //System.out.println(" refused because  length("+l+") >= max ("+max+")");
            return false;
        }
        return true;
    }
    
    /*public String getLabel(){
        return super.getLabel() + ", " + min + "< Cpt <" + (max==OccurrenceNode.UNBOUNDED?"*":""+max);
        }*/

}
