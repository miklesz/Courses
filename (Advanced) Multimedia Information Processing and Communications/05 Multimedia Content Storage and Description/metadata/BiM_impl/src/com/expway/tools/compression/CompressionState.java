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

import com.expway.tools.compression.*;
import com.expway.tools.automata.*;

import java.util.*;

public class CompressionState extends SimpleState  {
    
    int levelForPrint = 0;
    int codingLength = 0;

    int mode = 0;

    // ------------------------------------------------------------

    public CompressionState(String s){ 
        super(s);
        setAcceptMultipleActivities(true);
   }
    
    /** true if compression */
   public CompressionState(){
       super();
       setAcceptMultipleActivities(true);
   }

    // ------------------------------------------------------------

    public void setMode(int a){
        mode = a;
    }

    public void resetMode(int m){
        setMode(m);
        TransitionLinkedListEnumeration tre = transitions();
        while(tre.hasMoreElements()){
            Transition t = tre.nextElement();
            if (t instanceof CompressionTransition)
                ((CompressionTransition)t).setMode(m);
        }
    }

    // ------------------------------------------------------------

    /** la realization a ici pour but d'affecter un codage à chaque key transition */
    public void realize(){
        
        TreeMap codingTransitions = new TreeMap();
        ShuntTransition theShunt = null;

        TransitionLinkedListEnumeration tre = transitions();
        while(tre.hasMoreElements()){
            Transition t = tre.nextElement();

            if (t instanceof ShuntTransition){

                if (theShunt == null)
                    theShunt = (ShuntTransition)t;
                else
                    throw new RuntimeException("Plusieurs shunts ??");

            } else if (t instanceof KeyTransition){
                codingTransitions.put(((KeyTransition)t).getKey(),t);
                //System.out.println(" Transition added :" + t + " " + t.getLabel());
            }
            else
                ;//System.out.println(" Transition refused :" + t + " " + t.getLabel());
            
        }
        
        int nposs = codingTransitions.size();

        if (theShunt != null) nposs++;

        if (nposs!=0){
            codingLength = (int)Math.ceil(Math.log((double)nposs)/Math.log(2.0));
            
            Set s = codingTransitions.entrySet();
            Iterator i = s.iterator();
            int transitionValue = 0;
            if (theShunt != null) {
                transitionValue = 1;
                theShunt.setCoding(0,codingLength);
            }
            
            while (i.hasNext()){
                KeyTransition kt = (KeyTransition)((Map.Entry)i.next()).getValue();
                kt.setCoding(transitionValue,codingLength);
                transitionValue++;
            }
            
        }
    }

    // Va plus vite si retourne true mais plante dans certains cas !
    // public boolean allowsDangerousEpsilonPropagationMode(){ return true;}

    
}
