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

import com.expway.binarisation.CodingContext;

import java.io.*;
import java.util.*;

// Note:
// FromTypeName : le plus bas dans la hierarchie des types 
// ToTypeName : le plus haut dans la hierarchie des types
// Le fils de CompatNode peut etre a null (lors de restriction par exemple)

public class CompatNode extends CompositionNode  {

    private String theFromTypeName = null;
    private String theToTypeName = null;
    private TypeDefinition theFromTypeDefinition = null;
    private TypeDefinition theToTypeDefinition = null;

    private IntegerCoding versionCoding = null;
    private IntegerCoding typeCoding = null;

   // ------------------------------------------------------------
   
    public CompatNode(String fromType, String toType, TreeNode on){
        addChild(on);
        theFromTypeName = fromType;
        theToTypeName = toType;
   }

   // ------------------------------------------------------------

    /** accesseur du fiston (a utiliser plutot que les methodes de treenode */
    public TreeNode getCompatibilityChild(){return (TreeNode)getFirstChild();}

    /** une occurrence n'a pas de signature (ou plutot elle a celle de son fils) */
    public Comparable generateSignature(){
        TreeNode child = getCompatibilityChild();
        if (child != null)
            setSignature(child.generateSignature());
        else
            setSignature("__NoChild");
        return getSignature();
    }

    public Collection generateFirstAcceptedTokens(){
        //          TreeNode child = getCompatibilityChild();
        //          if (child != null)
        //              return child.generateFirstAcceptedTokens();
        return null;
    }
    
    // ------------------------------------------------------------
    // REALIZATIONS

    public void realize(SetOfDefinitions sod) throws DefinitionException {
        // Les types definitions
        if (theFromTypeName == null) throw new DefinitionException("From type == null ??");
        theFromTypeDefinition = sod.getTypeDefinition(theFromTypeName);
        if (theToTypeName != null) 
            theToTypeDefinition = sod.getTypeDefinition(theToTypeName);

        // On realize le fils
        TreeNode tn = getCompatibilityChild();
        if (tn!=null) tn.realize(sod);
    }

    public void dynamicRealize(CodingContext cc) throws DefinitionException {

        // le numero du schema
        versionCoding = cc.getURIRegistry().getVersionCoding(theFromTypeName);

        // Génération du codage du type
        if (theToTypeDefinition != null) // lorsque c'est le tout premier morceau
            typeCoding = theToTypeDefinition.getSubtypeEncoding(theFromTypeName,cc.getSetOfDefinitions());
        else // le numero du type dans le namespace fonction du dernier type encodé (encode le 'from' connaissant le 'to')
            {
                TreeSet subtypeNS = cc.getSetOfDefinitions().getDefinitionsFromCompactName(theFromTypeName).typeDefinitionNames();
                typeCoding = new IntegerCoding(MethodsBag.getPosition(theFromTypeName,subtypeNS),
                                               MethodsBag.getCodingLength(subtypeNS.size()));
            }
        
        
        TreeNode tn = getCompatibilityChild();
        if (tn!=null) tn.dynamicRealize(cc);
        
        //          System.out.println("             CompatNode " + theFromTypeName + " => " + theToTypeName);
        //          System.out.println("               - VersionCoding = " + versionCoding);
        //          System.out.println("               - TypeCoding = " + typeCoding);
    }

    // ------------------------------------------------------------

    public CompressionFiniteStateAutomata getFullFSA(boolean longName, int deep){
        TreeNode child = getCompatibilityChild();
        CompressionFiniteStateAutomata childFsa = null;
        // pas de fiston (par exemple dans le cas d'une restriction ou s'il n'y a rien de nouveau
        // entre les deux types)
        if (child == null) {
            childFsa = new CompressionFiniteStateAutomata();
            // un nouveau start
            CompressionState s1=new CompressionState();
            s1.setStartState(true);
            childFsa.addState(s1);
            // un nouveau end
            CompressionState s2=new CompressionState();
            s2.setFinalState(true);
            childFsa.addState(s2);
            
            // une Compatibility Transition
            CompatibilityTransition ct = new CompatibilityTransition(null,s1,s2);
            ct.setCodings(versionCoding,typeCoding);
            ct.bind();
        }else{
            childFsa = child.getFullFSA(longName,deep++);
            // par construction l'automate ne contient qu'un etat de depart et un etat de fin 
            // donc pas de probleme
            State childStart = childFsa.getFirstStartState();
            childStart.setStartState(false);
        
            // un nouveau start
            CompressionState s1=new CompressionState();
            s1.setStartState(true);
            childFsa.addState(s1);

            // une Compatibility Transition
            CompatibilityTransition ct = new CompatibilityTransition(null,s1,childStart);
            ct.setCodings(versionCoding,typeCoding);
            ct.bind();
        }
        return childFsa;
    }

    
}








