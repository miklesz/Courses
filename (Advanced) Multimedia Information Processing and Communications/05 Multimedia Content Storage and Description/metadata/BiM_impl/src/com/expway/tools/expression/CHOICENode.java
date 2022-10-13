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

import java.awt.*;
import javax.swing.*;

public class CHOICENode extends GroupNode {
    
    TreeSet tempTreeSet = new TreeSet();

    /** crée sa signature et la retourne */
    public Comparable generateSignature(){

        tempTreeSet.clear();

        for (Iterator e=children();e.hasNext();){
            TreeNode tempn=(TreeNode)e.next();
            tempTreeSet.add(tempn.generateSignature());
        }

        String mySig = "CHOICE";
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
        //System.out.println("getFullFSA CHOICENode " + this);

        // mes fils
        TreeNode childNode = null;
        CompressionFiniteStateAutomata childFSA = null;

        // variables temporaires
        Transition childShuntTransition = null;
        State childStart = null,childFinal = null;
        boolean needsToBeShunted = false;

        // crée deux nouveaux etats (start et final)
        CompressionState realStart = new CompressionState();
        realStart.setStartState(true);
        CompressionState realFinal = new CompressionState();
        realFinal.setFinalState(true);

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
            // on part de l'automate du premier fils 
            if (fsa == null) {
                fsa = childFSA;
                fsa.addState(realStart);
                fsa.addState(realFinal);
            } 
            else
                fsa.merge(childFSA); 

            // ensuite on ajoute une transition "accepte tout" codante
            // entre l'entree du choice et l'entree du nouveau petit
            KeyTransition t = new KeyTransition(null,realStart,childStart);
            t.setKey(childNode.getSignature());
            //t.setAcceptedTokens(childNode.getFirstAcceptedTokens());
            t.bind();

            // ensuite on ajoute une transition "accepte tout" non-codante
            // entre la sortie du nouveau et la sortie du choice
            Transition tt = new CompressionTransition(null,childFinal,realFinal);
            tt.bind();

            // Y a t il un shunt entre les etats start et fin de l'automate du fils ??
            childShuntTransition = childStart.getFirstSimilarTransitionsTo(ShuntTransition.SHUNTTRANSITION_REFERENCE,childFinal);

            // S'il y a un shunt chez les fils
            if (childShuntTransition != null) {
                // on le vire
                childShuntTransition.unbind();
                // l'automate complet doit être shunté
                needsToBeShunted = true;
            }
        }
        
        if (needsToBeShunted){
            ShuntTransition st = new ShuntTransition(null,realStart,realFinal);
            st.bind();
        }
        
        return fsa;
    }
    
    
}
