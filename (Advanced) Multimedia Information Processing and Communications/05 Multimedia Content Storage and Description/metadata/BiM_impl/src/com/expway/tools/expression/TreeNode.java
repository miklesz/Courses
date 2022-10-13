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

import java.util.*;
import java.io.*;

import java.awt.*;
import javax.swing.*;

import com.expway.tools.automata.*;
import com.expway.tools.compression.*;
import com.expway.tools.expression.BasicTreeNode;

import com.expway.binarisation.CodingContext;

abstract public class TreeNode extends BasicTreeNode implements Cloneable {

    protected boolean BIGSHUNT_ALLOWED = false;
    
    static int uidcpt = 0;
    int uid = 0;

    // Variables d'instance

    private Comparable signature = null;
    private Collection firstAcceptedTokens = null;

    //------------------------------------------------------------
    // CONSTRUCTEURS

    /** constructeur */
    public TreeNode() { uid = uidcpt++; }

    /** Signature */
    protected void setSignature(Comparable c){
        signature = c;
    }
 
    protected Comparable getSignature(){
        if (signature == null)
            generateSignature();
        return signature;
    }
 
    protected Collection getFirstAcceptedTokens(){
        if (firstAcceptedTokens == null)
            firstAcceptedTokens = generateFirstAcceptedTokens();
        return firstAcceptedTokens;
    }    


    public boolean isItUsedHere(String theTypeName){
        for (Iterator e=children();e.hasNext();){
            TreeNode tn = (TreeNode)e.next();
            if (tn.isItUsedHere(theTypeName)) return true;
        }
        return false;
    }
 
    //------------------------------------------------------------

    public void dynamicRealize(CodingContext cc) throws DefinitionException {
        for (Iterator e=children();e.hasNext();){
            TreeNode tn=(TreeNode)e.next();
            tn.dynamicRealize(cc);
        }
    }

    /**@deprecated use realize(SetOfDefinitions) */
    public void realize(TypeDefinitions sods) throws DefinitionException {
        realize(sods.getSetOfDefinitions());
    }

    public void realize(SetOfDefinitions sods) throws DefinitionException {
        for (Iterator e=children();e.hasNext();){
            TreeNode tn=(TreeNode)e.next();
            tn.realize(sods);
        }
    }
    
    public void setMixed() throws DefinitionException {
        for (Iterator e=children();e.hasNext();){
            TreeNode tn=(TreeNode)e.next();
            tn.setMixed();
        }
    }

    //------------------------------------------------------------
    // METHODES ABSTRAITES 

    /** crée sa signature et la retourne */
    abstract public Comparable generateSignature();

    

    /** construction des Full FSA */
    abstract public CompressionFiniteStateAutomata getFullFSA(boolean longName,int deep);

    /** construction des first possible token */
    abstract public Collection generateFirstAcceptedTokens();   

    //------------------------------------------------------------
    // UTILS

    // construction de fichiers XML
    

    //------------------------------------------------------------
    // NOMMAGES

    // Unique names
    public String getUName() {return "node" + uid;}
    public int getUID()      {return uid;}
}







