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
import java.util.*;
import java.io.*;

public class SEQNode extends GroupNode {
    
    /** crée sa signature et la retourne */
    public Comparable generateSignature(){
        String mySig = "SEQ";
        for (Iterator e=children();e.hasNext();){
            TreeNode tempn=(TreeNode)e.next();
            mySig += (String)tempn.generateSignature();
        }

        setSignature(mySig);
        return mySig;
    }
    
    public Collection generateFirstAcceptedTokens(){
        Collection c = new ArrayList();
        Iterator e=children();
        while(e.hasNext()){
            OccurrenceNode oc = (OccurrenceNode)e.next();
            if (oc.getMinOccurs()!=0)
                c.addAll(oc.generateFirstAcceptedTokens());
            else break;
        }
        return c;
    }

    // ------------------------------------------------------------

    public CompressionFiniteStateAutomata getFullFSA(boolean longName,int deep){
        CompressionFiniteStateAutomata cfsa = null;
        
        

        cfsa = getFullFSA_NoShunt(longName,deep);

        return cfsa;
    }
    
    public CompressionFiniteStateAutomata getFullFSA_NoShunt(boolean longName,int deep){
        //System.out.println("getFullFSA SEQNode " + this);

        // mes fils
        TreeNode childNode = null;
        CompressionFiniteStateAutomata childFSA = null;

        // variables temporaires
        Transition childShuntTransition = null;
        State childStart = null,childFinal = null;
        State lastFinalState = null;

        // le tout premier
        State veryFirstChild = null, veryLastChild = null;

        // pour construire tous les shunts possibles
        CompressionState shuntDeparture = null;

        // crée deux nouveaux etats (start et final)
        CompressionState realStart = new CompressionState();
        realStart.setStartState(true);
        CompressionState realFinal = new CompressionState();
        realFinal.setFinalState(true);

        shuntDeparture = null;
        lastFinalState =  realStart;

        // LE NOUVEL AUTOMATE a creer
        CompressionFiniteStateAutomata fsa = null;

        // LA SUITE
        // pour tous les automates fils
        Iterator e=children();
        while(e.hasNext()){
            childNode = (TreeNode)e.next();
            // recupere l'automate imbrique de l'element courant
            childFSA = childNode.getFullFSA(longName,deep+1);
            // ce fils est bidon
            if (childFSA == null) continue;

            // recupère le start de l'automate ; par construction un seul start 
            childStart = childFSA.getFirstStartState();
            childStart.setStartState(false);
            // Récupère le end de l'automate ; par construction un seul end
            childFinal = childFSA.getFirstFinalState();
            childFinal.setFinalState(false);

            // Allez on importe l'automate du fils
            // pour eviter de creer inutilement un autre automate
            if (fsa == null) {
                fsa = childFSA;
                fsa.addState(realStart);
                fsa.addState(realFinal);
            } 
            else
                fsa.merge(childFSA); 

            // Y a t il un shunt entre les etats start et fin de l'automate du fils ??
            childShuntTransition = childStart.getFirstSimilarTransitionsTo(ShuntTransition.SHUNTTRANSITION_REFERENCE,childFinal);

            // S'il y a un shunt chez le fils
            if (childShuntTransition != null) {
                // rajoutons un nouvel etat de depart
                CompressionState aNewStart = new CompressionState();
                fsa.addState(aNewStart);
                
                KeyTransition st = new KeyTransition(null,aNewStart,childStart);
                //st.setAcceptedTokens(childNode.getFirstAcceptedTokens());
                st.setKey(childNode.getSignature());
                st.bind();

                childStart = aNewStart;

            }
 
            // ensuite on ajoute une transition "accepte tout" codante
            // entre la sortie du sous automate précédent et l'entrée du sous automate courant
            KeyTransition t = new KeyTransition(null,lastFinalState,childStart);
            //t.setAcceptedTokens(childNode.getFirstAcceptedTokens());
            t.setKey(childNode.getSignature());
            t.bind();

            // construisons le shunt
            if (shuntDeparture != null){
                ShuntTransition st = new ShuntTransition(null,shuntDeparture,childStart);
                //                  KeyTransition st = new KeyTransition(null,shuntDeparture,childStart);
                //                  st.setAcceptedTokens(childNode.getFirstAcceptedTokens());
                //                  st.setKey(childNode.getSignature());
                st.bind();
            }
            
            // le prochain shunt
            if (childShuntTransition == null){
                // Les anciens departs des shunts ne sont plus valables car il FAUT a tout prix passer par ici
                shuntDeparture = null;
            } else {
                // memorisons le shuntdepartures
                shuntDeparture = (CompressionState)childStart;
                childShuntTransition.unbind();
                // childShuntTransition.release();
            }
            
            lastFinalState = childFinal;

        }
        
        
        Transition t = new CompressionTransition(null,lastFinalState,realFinal);
        t.bind();
        
        // construisons le dernier shunt possible
        if (shuntDeparture!=null){
            ShuntTransition st = new ShuntTransition(null,shuntDeparture,realFinal);
            st.bind();
        }       
        
        return fsa;
    }

    
}


