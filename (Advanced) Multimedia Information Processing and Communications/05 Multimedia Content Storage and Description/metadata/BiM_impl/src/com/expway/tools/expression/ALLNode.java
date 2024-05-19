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

import com.expway.tools.automata.*;
import com.expway.tools.compression.*;
import com.expway.tools.utils.MethodsBag;
import java.util.*;
import java.io.*;

public class ALLNode extends GroupNode {

    TreeSet tempTreeSet = new TreeSet();

     public Comparable generateSignature(){

        tempTreeSet.clear();

        for (Iterator e=children();e.hasNext();){
            TreeNode tempn=(TreeNode)e.next();
            tempTreeSet.add(tempn.generateSignature());
        }

        String mySig = "ALL";
        for (Iterator i=tempTreeSet.iterator();i.hasNext();){
            mySig += (String)i.next();
        }

        setSignature(mySig);
        return mySig;
    }

       public Collection generateFirstAcceptedTokens(){
        Collection c = new ArrayList();
        Iterator e=children();
        while(e.hasNext())
            c.addAll(((TreeNode)e.next()).generateFirstAcceptedTokens());
        return c;
    }

    public CompressionFiniteStateAutomata getFullFSA(boolean longName,int deep){
        //System.out.println("getFullFSA ALLNode " + this);

        // Création de l'état initial
        CompressionState realStart = new CompressionState();
        realStart.setStartState(true);
        CompressionState realFinal = new CompressionState();
        realFinal.setFinalState(true);

        // LE NOUVEL AUTOMATE a creer
        CompressionFiniteStateAutomata fsa = new CompressionFiniteStateAutomata();
        fsa.addState(realStart);
        fsa.addState(realFinal);

        // LA SUITE
        // pour tous les automates fils
        Object [] objects = getChildren().toArray();
        TreeNode[] elements = new TreeNode[objects.length];
        for (int t=0;t<elements.length;t++)
            elements[t]=(TreeNode)objects[t];

        attachALLFSA(elements,elements.length,fsa,realStart,realFinal,longName,deep);

        return fsa;
    }

    private boolean attachALLFSA(TreeNode[] theElements, int numOfValidElements,
                                 CompressionFiniteStateAutomata theFSA, State theState, State realFinal,
                                 boolean longName,int deep){

        State   lastFinalState   = null;
        int     lastElementIndex = 0;
        boolean shouldBeShunted  = true;

        for (int t=0;t<theElements.length;t++){
            if (theElements[t] != null){
                lastElementIndex = t;
                // on va chercher la definition de l'élément
                ItemNode in = (ItemNode)((OccurrenceNode)theElements[t]).getOccurrenceChild();
                //System.out.println("." + in);
                // on recupere son automate
                CompressionFiniteStateAutomata childFsa = in.getFullFSA(longName,deep);
                State childStart = childFsa.getFirstStartState();
                childStart.setStartState(false);
                State childFinal = childFsa.getFirstFinalState();
                childFinal.setFinalState(false);

                lastFinalState = childFinal;
                // on fusionne les deux automates
                theFSA.merge(childFsa);
                // on fusionne les deux etats
                //              theFSA.merge(theState,childStart);
                
                KeyTransition kt = new KeyTransition(null,theState,childStart);
                kt.setKey(in.getSignature());
                //kt.setAcceptedTokens(in.getFirstAcceptedTokens());
                kt.bind();

                // on recurse si necessaire (en enlevant l'element avant)
                if (numOfValidElements>1) {
                    TreeNode temp = theElements[t];
                    theElements[t] = null;
                    //System.out.println(" --> " + (numOfValidElements-1));
                    boolean shuntedResult = attachALLFSA(theElements,numOfValidElements-1,theFSA,childFinal,realFinal,longName,deep);
                    //System.out.println(" <-- " + (numOfValidElements-1));                    
                    //System.out.println("ShuntedResult="+shuntedResult);
                    if (shuntedResult==false) shouldBeShunted = false;
                    theElements[t]=temp;
                }
            }
        }
        
        //System.out.println("ShouldbeShunted = " + shouldBeShunted + " - numOfValidElements = " + numOfValidElements);
        // si c'est le dernier element on cree un lien epsilon entre le dernier etat et le reel etat de fin
        if (numOfValidElements==1){
            Transition t = new CompressionTransition(null,lastFinalState,realFinal);
            t.bind();
            if (((OccurrenceNode)theElements[lastElementIndex]).getMinOccurs()==0){
                ShuntTransition st = new ShuntTransition(null,theState,realFinal);
                st.bind();
                return true;
            }
            return false;
        }
        
        else if (shouldBeShunted){
            //System.out.println("shunt triggered");
            ShuntTransition st = new ShuntTransition(null,theState,realFinal);
            st.bind();
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------
    
    // ------------------------------------------------------------

    public String toString(){
        return "ALL";
    }

}
