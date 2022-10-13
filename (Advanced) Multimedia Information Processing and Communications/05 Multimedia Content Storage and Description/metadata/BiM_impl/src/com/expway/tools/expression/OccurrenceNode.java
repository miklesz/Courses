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

import java.awt.*;
import javax.swing.*;

import com.expway.tools.automata.*;
import com.expway.tools.compression.*;
import com.expway.tools.utils.MethodsBag;
import java.io.*;
import java.util.*;

public class OccurrenceNode extends CompositionNode  {

    private int minOccurs = 0;
    private int maxOccurs = 0;
    static final public int UNBOUNDED = -1; 

    //
   
    public OccurrenceNode(TreeNode tn, int min,int max){
        addChild(tn);
        minOccurs = min;
        maxOccurs = max;
    }

    /** accesseur du fiston (a utiliser plutot que les methodes de treenode */
    public TreeNode getOccurrenceChild(){return (TreeNode)getFirstChild();}

    public int getMinOccurs(){return minOccurs;}
    public int getMaxOccurs(){return maxOccurs;}
    
    /** une occurrence n'a pas de signature (ou plutot elle a celle de son fils) */
    public Comparable generateSignature(){
        setSignature(getOccurrenceChild().generateSignature());
        return getSignature();
    }

    public Collection generateFirstAcceptedTokens(){
        // le meme que le fils
        return getOccurrenceChild().generateFirstAcceptedTokens();
    }

    public CompressionFiniteStateAutomata getFullFSA(boolean longName, int deep){
        //if (BIGSHUNT_ALLOWED)
        
        CompressionFiniteStateAutomata cfsa = getFullFSA_BigShunt(longName,deep);       
        //catch(IOException e){}
        return cfsa;
        //else
        //return getFullFSA_NoShunt(longName,deep);
    }

    /** Construction de l'automate Full return null s'il n'y a pas lieu de créer un automate */
    public CompressionFiniteStateAutomata getFullFSA_BigShunt(boolean longName, int deep){
        //System.out.println("getFullFSA OccurenceNode " + this);

        CompressionFiniteStateAutomata childFsa = getOccurrenceChild().getFullFSA(longName,deep++);
        if (childFsa == null) return null;
        // par construction l'automate ne contient qu'un etat de depart et un etat de fin 
        // donc pas de probleme
        State childStart = childFsa.getFirstStartState();
        State childFinal = childFsa.getFirstFinalState();

        // cas très simple ou on ne fait rien on passe le fiston a son grand pere
        if (minOccurs==1 && maxOccurs==1) return childFsa;
        if (minOccurs==0 && maxOccurs==1) {
            // par construction l'automate ne contient qu'un etat de depart et un etat de fin 
            // donc pas de probleme
            Transition childShuntTransition0 = childStart.getFirstSimilarTransitionsTo(ShuntTransition.SHUNTTRANSITION_REFERENCE,
                                                                                       childFinal);
            // s'il y a deja un shunt pas la peine d'en refaire un
            if (childShuntTransition0!=null)  return childFsa;
            // sinon oui
            ShuntTransition st = new ShuntTransition(null,childStart,childFinal);
            st.bind();
            return childFsa;
        }

        // de nouveaux etats initiaux et finaux sont prévus
        childStart.setStartState(false);
        childFinal.setFinalState(false);

        // recuperation de la transition de shunt si elle existe
        // par construction on a qu'une seule transition de shunte
        Transition childShuntTransition = childStart.getFirstSimilarTransitionsTo(ShuntTransition.SHUNTTRANSITION_REFERENCE,childFinal);

        // les nouveaux etats
        CompressionState s1=new CompressionState();
        s1.setStartState(true);
        childFsa.addState(s1);
        CompressionState s2 = new CompressionState();
        s2.setFinalState(true);
        childFsa.addState(s2);
            
        // les transitions
        AddLengthTransition alt = new AddLengthTransition(null,s1,childStart);
        int nbits = -1;
        if (maxOccurs != UNBOUNDED){
            nbits = MethodsBag.getCodingLength(minOccurs,maxOccurs);
            alt.setCptCodingLength(nbits);
        }
        else 
            alt.setCptCodingLength(UNBOUNDED);
        //System.out.println(" CODAGE ["+minOccurs+","+maxOccurs+"] sur " + nbits + " bits ");
        alt.setCptMinOccurs(minOccurs);
        alt.bind();

        RemLengthTransition rlt = new RemLengthTransition(null,childFinal,s2);
        rlt.setLengthTransitionConditions(minOccurs,UNBOUNDED); // min,max
        rlt.bind();

        IncrLengthTransition ilt = new IncrLengthTransition(null,childFinal,childStart);
        ilt.setLengthTransitionConditions(0,maxOccurs); // min,max
        ilt.bind();

        //  on rebalance le shunt vers le haut
        if (childShuntTransition != null){
            childShuntTransition.fullUnbind();
            childShuntTransition.setFromState(s1);
            childShuntTransition.setToState(s2);
            childShuntTransition.bind();
            // l'occurence minimale a changée
            rlt.setLengthTransitionConditions(0,UNBOUNDED); // min,max
        } else if (minOccurs == 0) {
            ShuntTransition st = new ShuntTransition(null,s1,s2);
            st.bind();
        }

        // ajout des etats
        childFsa.addState(s1);
        childFsa.addState(s2);

        // retourne le FSA
        return childFsa;
    }
    
    
}



