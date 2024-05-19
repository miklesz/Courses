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

import java.io.*;
import java.util.*;

import com.expway.tools.automata.FiniteStateAutomata;
import com.expway.tools.automata.FiniteStateAutomataInterface;

import com.expway.tools.compression.CompressionFiniteStateAutomata;
import com.expway.tools.compression.DecompressionFiniteStateAutomata;
import com.expway.tools.compression.ComplexTypeInstance;
import com.expway.tools.compression.TypeInstance;
import com.expway.tools.compression.TypeEncoder;
import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.Chunk;

import com.expway.tools.utils.MethodsBag;

import com.expway.tools.io.BitToBitDataInputStream;

import com.expway.util.URIRegistry;

import com.expway.binarisation.CodingParameters;
import com.expway.binarisation.CodingContext;

abstract public class ComplexTypeDefinition extends TypeDefinition {

    boolean         isAbstract = false;

    private boolean attributesHaveBeenCollected = false;
    OccurrenceNode  definitionAttributes = null;
    OccurrenceNode  attributes = null;

    Comparable      attributesSignature = null;

    // Pour le cache d'automate

    CompressionFiniteStateAutomata   attributesFSA = null;
    DecompressionFiniteStateAutomata decompressionAttributesFSA = null;
    boolean attributesFSA_Used = false;
    boolean decompressionAttributesFSA_Used = false;

    public boolean NO_PARTIAL_INSTATIATION_ALLOWED_IN_THIS_SCHEMA=true;
    
    // ------------------------------------------------------------

    public ComplexTypeDefinition(String name){super(name);}

    // ------------------------------------------------------------

    public void     setAbstract(boolean b) { isAbstract = true; }
    public boolean  isAbstract()           { return isAbstract;}

    // ------------------------------------------------------------

    public void           setDefinitionAttributesNode(OccurrenceNode tn) {definitionAttributes = tn;}
    public OccurrenceNode getDefinitionAttributesNode()                  {return definitionAttributes;}
    
    public boolean hasAttributes()           { return attributes!=null; }
    public boolean hasDefinitionAttributes() { return definitionAttributes!=null; }
    
    public OccurrenceNode getAttributesNode() { 
        if (!attributesHaveBeenCollected)  
            flattenAttributes();
        return attributes;
    }

    FiniteStateAutomata getAttributesFSA()       { return attributesFSA;}
    public String       getAttributesSignature() { return (String) attributesSignature;}

    // ------------------------------------------------------------
    // le realize ne se pose pas de question et aplati tout le rws

    public void realize(TypeDefinitions tds) throws DefinitionException {
        // avnt tout on realize papa (qui va se charger notamment de créer les liens
        // de l'héritage complet, traitement dont on a besoin par la suite pour
        // applatir l'heritage
        super.realize(tds);

        // Collecte l'arbre syntaxique des attributs (si nécessaire)
        flattenAttributes();
        
        // Realize les attributs s'il y en a
        if (attributes!=null) attributes.realize(tds);

		
    }
    
    // si lastCodedType = null, on fournit tout jusqu'a la racine
    public OccurrenceNode getMissingAttributes(TypeDefinition lastCodedType){
        if (lastCodedType == null)
            return getMissingAttributes((String)null);
        return getMissingAttributes(lastCodedType.getName());
    }

    public OccurrenceNode getMissingAttributes(String lastCodedType){
        OccurrenceNode result = null;
        OccurrenceNode result2 = new OccurrenceNode(new ATTRNode(),1,1);
        ATTRNode atn = (ATTRNode)result2.getOccurrenceChild();

        ComplexTypeDefinition currentTD = this;

        while (currentTD != null){
            if (currentTD.getDerivationMethod() != RESTRICTION){ // soit extension soit type du haut 

                if (currentTD.hasDefinitionAttributes()){
                    if (result == null) result=result2; // tout ca pour renvoyer null s'il n'y a pas de CM
                    atn.adoptChildrenOf(currentTD.getDefinitionAttributesNode().getOccurrenceChild());                
                }
                
                TypeDefinition superTD = currentTD.getSuperTypeDefinition();
                
                if ((superTD instanceof ComplexTypeDefinition) && lastCodedType!=null && !superTD.getName().equals(lastCodedType))
                    currentTD = (ComplexTypeDefinition)superTD;
                else
                    currentTD = null;
            }
            else 
                currentTD = null;
        }
        return result;
    }

    //

    public OccurrenceNode getCompatibleAttributes(TypeDefinition[] tds) throws DefinitionException {
        OccurrenceNode ret = new OccurrenceNode(new SEQNode(),1,1);
        SEQNode sn = (SEQNode)ret.getOccurrenceChild();
        String lastCodedType = null;
        for (int n=0;n<tds.length;n++){
            // on ne s'occupe pas des simple types qui peuvent etre au debut lorsque des complextypes derivent de simple type
            if (tds[n] instanceof SimpleTypeDefinition) continue;
            OccurrenceNode man = ((ComplexTypeDefinition)tds[n]).getMissingAttributes(lastCodedType);
            sn.adoptAsChild(new OccurrenceNode(new CompatNode(tds[n].getName(),lastCodedType, man), 1,1));
            lastCodedType = tds[n].getName();
        }
        return ret;
    }

    //

    private void flattenAttributes(){
        flattenAttributes(null);
    }

    private void flattenAttributes(String stopType){
        // les attributs ont ils été déjà collectés ?
        if (!attributesHaveBeenCollected){
            // 1- on collecte les attributs provenant de l'héritage 
            //    on les place dans le champs "attributes"
            if (attributes == null) {
                // Dans le cas ou il y a restriction, le schema analyser 
                // de cédric se charge de fournir (dans le rws) tous les attributs avec le bon namespace
                if (derivationMethod == EXTENSION) {
                    TypeDefinition theSuperT = getSuperTypeDefinition();
                    if (theSuperT != null && !(theSuperT instanceof SimpleTypeDefinition)){
                        OccurrenceNode on = ((ComplexTypeDefinition)getSuperTypeDefinition()).getAttributesNode();
                        if (on != null) // des attributs proviennent de l'heritage
                            {
                                attributes = new OccurrenceNode(new ATTRNode(),1,1);
                                attributes.getOccurrenceChild().adoptChildrenOf(on.getOccurrenceChild());
                            }
                    }
                }
            }
        
        // 2- on adopte les attributs définit localement
        //    si c'est une restriction les attributs définitions 
        //    sont recopiés (est ce nécessaire ?)
        if (definitionAttributes != null) {
            
            // si on a hérité de rien ou si c'est une restriction aucun 
            // attribut n'est rapatrié de l'héritage
            if (attributes == null) attributes = new OccurrenceNode(new ATTRNode(),1,1);
            
            // on adopte les attributs qui sont dans le rws
            attributes.getOccurrenceChild().adoptChildrenOf(definitionAttributes.getOccurrenceChild());
        }
        // On marque que les attributs ont ete collectés.
        // la prochaine fois ca ne sera plus la peine d'en faire autant
        attributesHaveBeenCollected = true;
        }
    }
        
        public boolean isItUsedHere(String s){
            if (attributes != null ){
                return attributes.isItUsedHere(s);
            }
            return false;
        }

    // ============================================================
    // AUTOMATA

    // ------------------------------------------------------------
    // la signature (trouver ou elle est utilisée)

    public void generateSignature(){
        if (attributes != null){
            attributesSignature = attributes.getSignature();
        }
    }

    // ------------------------------------------------------------
    // un tout petit cache de fsa

    /** demande un nouvel automate pour les attributs */

    // Libere le cache automate
    public void reset() {
        super.reset();
        attributesFSA=null;
        attributesFSA_Used=false;
        decompressionAttributesFSA_Used=false;
        decompressionAttributesFSA=null;
    }

    public FiniteStateAutomata newAttributesFSA(){
        
        if (attributes!=null){
            if (attributesFSA == null) {
                //System.out.println("attributesFSA of "+this+" not yet created");
                attributesFSA = attributes.getFullFSA(false,0);
                attributesFSA.realize(); // pour calculer toutes les valeurs des transitions
                attributesFSA.reset();
                attributesFSA_Used = true;
                return attributesFSA;
            } else if (attributesFSA_Used){
                //System.out.println("attributesFSA of "+this+" Used");
                CompressionFiniteStateAutomata compFSA = attributes.getFullFSA(false,0);
                compFSA.realize(); // pour calculer toutes les valeurs des transitions
                compFSA.reset();
                return compFSA;
            } else {
                //System.out.println("attributesFSA of "+this+" not used ========");
                attributesFSA_Used = true;
                return attributesFSA;
            }
        }

        return null;
     }

    /** demande un nouvel automate pour les attributs */
    public FiniteStateAutomata newDecompressionAttributesFSA(){
        
        if (attributes!=null){
            if (decompressionAttributesFSA == null) {
                //System.out.println("decompressionAttributesFSA of "+this+" not yet created");
                decompressionAttributesFSA = new DecompressionFiniteStateAutomata(attributes.getFullFSA(false,0));
                decompressionAttributesFSA.realize(); // pour calculer toutes les valeurs des transitions
                decompressionAttributesFSA.reset();
                decompressionAttributesFSA_Used = true;
                return decompressionAttributesFSA;
            } else if (decompressionAttributesFSA_Used){
                //System.out.println("decompressionAttributesFSA of "+this+" Used");
                DecompressionFiniteStateAutomata compFSA = new DecompressionFiniteStateAutomata(attributes.getFullFSA(false,0));
                compFSA.realize(); // pour calculer toutes les valeurs des transitions
                compFSA.reset();
                return compFSA;
            } else {
                //System.out.println("decompressionAttributesFSA of "+this+" not used ========");
                decompressionAttributesFSA_Used = true;
                return decompressionAttributesFSA;
            }
        }

        return null;
     }

    /** pour renvoyer l'automate dans le cache */
    public void releaseAttributesFSA(FiniteStateAutomata fsa){
        if (fsa == attributesFSA){
            attributesFSA_Used = false;
        }
    }

    public void generateFullFSA(){generateFullFSA(true);}
    
    public void generateFullFSA(boolean longName){
        if (attributes != null){
            attributesFSA = attributes.getFullFSA(longName,0);
            attributesFSA.realize();
        }
    }

    // OPTIM un cache !!
    public FiniteStateAutomata newCompatibleAttributesFSA(CodingContext cc) throws DefinitionException {
        // System.out.println("** NEWCOMPATIBLEATTRIBUTESFSA of " + this.getName());
        OccurrenceNode on = getCompatibleAttributes(getCompatibilityCodings());
        on.realize(cc.getSetOfDefinitions());
        on.dynamicRealize(cc);
        //System.out.println("attributes generated for:"+this);
        //on.print(" ","");
        CompressionFiniteStateAutomata ret = on.getFullFSA(false,0);
        ret.realize();
        ret.reset();
        return ret;
    }

    public FiniteStateAutomata newCompatibleDecompressionAttributesFSA(CodingContext cc,TypeDefinition to) throws DefinitionException {
        OccurrenceNode on = getMissingAttributes(to);
        if (on == null) return null;
        //System.out.println("Missing attributes:");
        //on.print("","");
        on.realize(cc.getSetOfDefinitions());
        on.dynamicRealize(cc);
        DecompressionFiniteStateAutomata ret = new DecompressionFiniteStateAutomata(on.getFullFSA(false,0));
        ret.realize();
        ret.reset();
        return ret;
    }
    
    
}




