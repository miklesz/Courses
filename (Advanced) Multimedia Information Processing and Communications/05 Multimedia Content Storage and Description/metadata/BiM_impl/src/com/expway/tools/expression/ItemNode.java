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

import com.expway.util.URIRegistry;

import java.awt.*;
import javax.swing.*;

import java.util.*;
import java.io.*;

public class ItemNode extends TreeNode {

    String theItem = null;
    String theContentModelName = null;
    TypeDefinition theContentModelDefinition = null;
    
    boolean mixed = false;

    // 
    public void setMixed() throws DefinitionException {
        mixed = true;
    }

    public String getItemName(){return theItem;}
    public String getTypeName(){return theContentModelName;}
    public TypeDefinition getTypeDefinition(){return theContentModelDefinition;}
    
    // 
    
    public void realize(SetOfDefinitions theDefinitions) throws DefinitionException {

        if (theContentModelName == null)
            throw new DefinitionException("No content model name for " + this);

        theContentModelDefinition = theDefinitions.getTypeDefinition(theContentModelName);
        if (theContentModelDefinition == null) {
            throw new DefinitionException("Referenced content model " + theContentModelName + " not found");
        }

        //          if (theContentModelDefinition == null) {
        //              // On a peut être une chance de le trouver plus tard, on le stocke dans une pile temporaire
        //              if (theDefinitions.getSetOfDefinitions()!=null) {
        //                  theDefinitions.getSetOfDefinitions().addToBeRealizedItemNode(this);
        //              }
        //              else throw new DefinitionException("Referenced content model " + theContentModelName + " not found");
        //          }
    }

    //      static Object alwaysAcceptedToken = new Object() {
    //              public boolean equals(Object token){return true;}
    //          };
    
    public ItemNode(String s,String cm){

        theItem = s;
        theContentModelName = cm;
    }
   
    /** crée sa signature et la retourne */
    public Comparable generateSignature(){ setSignature(theItem); return theItem;}

    /** retourne les token possible pour entrer dans ce groupe */
    public Collection generateFirstAcceptedTokens(){
        Collection c = new ArrayList();
        c.add(theItem);
        return c;
    }
    
    /** Construction de l'automate */
    public CompressionFiniteStateAutomata getFullFSA(boolean longName,int deep){
        //System.out.println("getFullFSA ItemNode " + this);
        if (mixed) return getMixedFSA();

        // un nouvel automate a etat
        CompressionFiniteStateAutomata fsa = new CompressionFiniteStateAutomata();
        // etat initial = simple
        CompressionState s1=new CompressionState();
        s1.setStartState(true);

        // etat final = TypeState car il va déclencher la compression des éléments
        // contenus
        TypeState s2=new TypeState(theContentModelDefinition);
        s2.setFinalState(true);

        // la transition est une cle // TODO : est ce vraiment une clé ?
        //ANCIENNEMENT : KeyTransition tt = new KeyTransition(theItem,s1,s2);
        ItemTransition tt = new ItemTransition(theItem,s1,s2);
        tt.bind();
        
        // ajout des etats
        fsa.addState(s1);
        fsa.addState(s2);

        // retourne le FSA
        return fsa;
    }

    private CompressionFiniteStateAutomata getMixedFSA(){
        // un nouvel automate a etat
        CompressionFiniteStateAutomata fsa = new CompressionFiniteStateAutomata();
        // etat initial = simple
        CompressionState s1=new CompressionState();
        s1.setStartState(true);
        // etat final = TypeState car il va déclencher la compression des éléments contenus
        TypeState s2=new TypeState(theContentModelDefinition);
        s2.setFinalState(true);
        // intermediaire mixed
        CompressionState is = new CompressionState();
        // intermediaire item
        CompressionState ms = new CompressionState();
        // type string
        TypeState mixedTS = new TypeState(SimpleTypeDefinition.MIXED);

        ShuntTransition st = new ShuntTransition(null,s1,ms); // Always 0
        st.bind();
        MixedTransition pcdata = new MixedTransition(SimpleTypeDefinition.MIXED_KEY,ms,mixedTS);
        pcdata.bind();
        KeyTransition kt = new KeyTransition(null,s1,is);
        kt.setKey("MIXED"); // bidon
        kt.bind();
        ItemTransition tt = new ItemTransition(theItem,is,s2);
        tt.bind();
        CompressionTransition back = new CompressionTransition(null,mixedTS,is);
        back.bind();

        // ajout des etats
        fsa.addState(s1);
        fsa.addState(s2);
        fsa.addState(ms);
        fsa.addState(is);
        fsa.addState(mixedTS);

        // retourne le FSA
        return fsa;
    }

    public boolean isItUsedHere(String theTypeName){
        return theTypeName.equals(theContentModelName);
    }


       
}



