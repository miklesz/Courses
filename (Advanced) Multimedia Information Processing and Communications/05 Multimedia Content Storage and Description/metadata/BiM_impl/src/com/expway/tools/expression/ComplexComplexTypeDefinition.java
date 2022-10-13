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

import com.expway.tools.automata.State;
import com.expway.tools.automata.FiniteStateAutomata;
import com.expway.tools.automata.FiniteStateAutomataInterface;

import com.expway.binarisation.CodingParameters;

import com.expway.tools.compression.TypeEncoder;
import com.expway.tools.compression.TypeInstance;
import com.expway.tools.compression.ComplexComplexTypeInstance;
import com.expway.tools.compression.SpecificTypeInstance;

import com.expway.tools.compression.CompressionFiniteStateAutomata;
import com.expway.tools.compression.DecompressionFiniteStateAutomata;

import com.expway.tools.compression.CompressionTransition;
import com.expway.tools.compression.KeyTransition;
import com.expway.tools.compression.ShuntTransition;
import com.expway.tools.compression.MixedTransition;

import com.expway.tools.compression.CompressionState;
import com.expway.tools.compression.TypeState;

import com.expway.util.URIRegistry;

import com.expway.binarisation.CodingParameters;
import com.expway.binarisation.CodingContext;

// For SpecificInstances (Video types)
import com.altkom.video.*;

import java.io.*;
import java.util.*;

public class ComplexComplexTypeDefinition extends ComplexTypeDefinition {

    boolean mixed = false;

    boolean contentHasBeenCollected=false;
    OccurrenceNode content = null;
    OccurrenceNode definitionContent = null;
    
    Comparable contentModelSignature = null;
    
    // un tout petit cache de FSA (une seule case)
    DecompressionFiniteStateAutomata decompressionContentModelFSA = null;
    CompressionFiniteStateAutomata contentModelFSA = null;

    boolean contentModelFSA_Used = false;
    boolean decompressionContentModelFSA_Used = false;

    // ------------------------------------------------------------
    // CONSTRUCTEURS

    public ComplexComplexTypeDefinition(String name){
        super(name);
    }

    // ------------------------------------------------------------
    // TESTS

    void setMixed(boolean t){
        mixed = t;
    }

    public boolean isMixed(){
        return mixed;
    }

    //

    public boolean isEmpty(){
        return !hasContent() && !hasAttributes();
    }

    //

    public boolean hasContent(){
        return content!=null;
    }

    public boolean hasDefinitionContent(){
        return definitionContent != null;
    }
    
    // ------------------------------------------------------------
    // ACCESSEURS

    public void setDefinitionContentModelNode(OccurrenceNode tn){
        definitionContent = tn;
    }
    
    public OccurrenceNode getDefinitionContentModelNode(){
        return definitionContent;
    }
    
    public OccurrenceNode getContentModelNode(){
        if (!contentHasBeenCollected) flattenContent();
        return content;
    }

    FiniteStateAutomataInterface getContentModelFSA(){
        return contentModelFSA;
    }

    public String getContentModelSignature(){
        return (String)contentModelSignature;
    }

    public void realize(TypeDefinitions tds) throws DefinitionException {
        super.realize(tds);

        flattenContent();

        if (content!=null){
            //print();
            content.realize(tds.getSetOfDefinitions());
            if (isMixed())
                content.setMixed();
        }
    }

    // ------------------------------------------------------------
    // COMPATIBILITY

    // si lastCodedType = null, on fournit tout jusqu'a la racine
    public OccurrenceNode getMissingContent(TypeDefinition lastCodedType){
        if (lastCodedType == null)
            return getMissingContent((String)null);
        return getMissingContent(lastCodedType.getName());
    }
    
	
    public boolean hasMissingContent(TypeDefinition lastCodedType){
        if (getMissingContent(lastCodedType)==null) return false;
        return true;
    }

    // si lastCodedType = null, on fournit tout jusqu'a la racine
    public OccurrenceNode getMissingContent(String lastCodedType){
        //System.out.println("*************************");
        //System.out.println("getMissingContent of " + getName() + " lastCodedType = " + lastCodedType);
        OccurrenceNode result = null;
        OccurrenceNode result2 = new OccurrenceNode(new SEQNode(),1,1);
        SEQNode atn = (SEQNode)result2.getOccurrenceChild();

        ComplexComplexTypeDefinition currentTD = this;
        
        while (currentTD != null){
            if (currentTD.getDerivationMethod() != RESTRICTION){ // soit extension soit type du haut 
                //System.out.println("=> currentTD="+currentTD.getName());
                if (currentTD.hasDefinitionContent()){
                    if (result == null) result=result2; // tout ca pour renvoyer null s'il n'y a pas de CM
                    //System.out.println("   -> adoptChildren ");
                    //currentTD.getDefinitionContentModelNode().print("       ","");
                    atn.adoptChildrenOf(currentTD.getDefinitionContentModelNode().getOccurrenceChild());                
                }
                
                TypeDefinition superTD = currentTD.getSuperTypeDefinition();
                
                if ((superTD instanceof ComplexComplexTypeDefinition) && lastCodedType!=null && !superTD.getName().equals(lastCodedType))
                    currentTD = (ComplexComplexTypeDefinition)superTD;
                else
                    currentTD = null;
            }
            else 
                currentTD = null;
        }
        //System.out.println("return " + result);
        //System.out.println("*************************");
        return result;
    }
    
	
    public OccurrenceNode getCompatibleContent(TypeDefinition[] tds){
        OccurrenceNode ret = new OccurrenceNode(new SEQNode(),1,1);
        SEQNode sn = (SEQNode)ret.getOccurrenceChild();
        String lastCodedType = null;
        for (int n=0;n<tds.length;n++){
            //System.out.println(" adoptChild. from=" + tds[n].getName() + " to=" + lastCodedType);
            sn.adoptAsChild(new OccurrenceNode(new CompatNode(tds[n].getName(), //from
                                                              lastCodedType, //to
                                                              ((ComplexComplexTypeDefinition)tds[n]).getMissingContent(lastCodedType)),
                                               1,1));
            lastCodedType = tds[n].getName();
        }
        //ret.print("","");
        return ret;
    }

    private void flattenContent(){
        //System.out.println("flatten ContentModel of " + getName());
        // le content a t il été déjà collecté ?
        if (!contentHasBeenCollected){
            // 1- on collecte le content provenant de l'héritage 
            //    on le place dans le champs content
            if (content == null){
                //System.out.println(" content = null");
                if (derivationMethod == EXTENSION){
                    OccurrenceNode on = ((ComplexComplexTypeDefinition)getSuperTypeDefinition()).getContentModelNode();
                    if (on != null) // du content provient de l'heritage
                        {
                            content = new OccurrenceNode(new SEQNode(),1,1);
                            content.getOccurrenceChild().adoptChildrenOf(on.getOccurrenceChild());
                        }
                }
            }
           	
            if (definitionContent != null) {
                
                if (content == null) // si par contre on a hérité de rien (ou d'une restriction)
                    content = new OccurrenceNode(new SEQNode(),1,1);
                
                content.getOccurrenceChild().adoptChildrenOf(definitionContent.getOccurrenceChild());
            }
            
            // On marque que le content a deja ete collecté.
            // la prochaine fois ca ne sera plus la peine d'en faire autant
            contentHasBeenCollected = true;
        }
    }

    // ============================================================
    // GESTION DES FSAs

    public FiniteStateAutomata newCompatibleContentModelFSA(CodingContext cc) throws DefinitionException {
        //System.out.println("** NEWCOMPATIBLECONTENTMODELFSA of " + this.getName());
        OccurrenceNode on = getCompatibleContent(getCompatibilityCodings());
        //System.out.println("Avant realization");
        //on.print(" ","");
        on.realize(cc.getSetOfDefinitions());
        //System.out.println("Après realization");
        //on.print(" ","");
        on.dynamicRealize(cc);
        //System.out.println("Après realization dynamique");
        //on.print(" ","");
        //System.out.println("");
        //System.out.println("");
        CompressionFiniteStateAutomata ret = on.getFullFSA(false,0);
        ret.realize();
        ret.reset();
        return ret;
    }

    public FiniteStateAutomata newCompatibleDecompressionContentModelFSA(CodingContext cc,TypeDefinition to) throws DefinitionException {
        //System.out.println("** NEWCOMPATIBLECONTENTMODELFSA of " + this.getName());
        OccurrenceNode on = getMissingContent(to);
        //System.out.println("Avant realization");
        //on.print(" ","");
        on.realize(cc.getSetOfDefinitions());
        //System.out.println("Après realization");
        //on.print(" ","");
        on.dynamicRealize(cc);
        //System.out.println("Après realization dynamique");
        //on.print(" ","");
        //System.out.println("");
        //System.out.println("");
        DecompressionFiniteStateAutomata ret = new DecompressionFiniteStateAutomata(on.getFullFSA(false,0));
        ret.realize();
        ret.reset();
        return ret;
    }

    // ------------------------------------------------------------
    // UN TOUT PETIT CACHE DE FSA

    // Libere le cache
    public void reset() {
        super.reset();
        decompressionContentModelFSA=null;
        contentModelFSA=null;
        contentModelFSA_Used=false;
        decompressionContentModelFSA_Used=false;
    }

    /** demande un nouvel automate pour le content */
    public FiniteStateAutomata newContentModelFSA(){
        if (content!=null){
            if (contentModelFSA == null) {
                //System.out.println("contentModelFSA of "+this+" not yet created");
                contentModelFSA = getContentFullFSA(false,0);
                contentModelFSA.realize(); // pour calculer toutes les valeurs des transitions
                contentModelFSA.reset();
                contentModelFSA_Used = true;
                return contentModelFSA;
            } else if (contentModelFSA_Used){
                //System.out.println("contentModelFSA of "+this+" Used");
                CompressionFiniteStateAutomata compFSA = getContentFullFSA(false,0);
                compFSA.realize(); // pour calculer toutes les valeurs des transitions
                compFSA.reset();
                return compFSA;
            } else {
                //System.out.println("contentModelFSA of "+this+" not used ========");
                contentModelFSA_Used = true;
                return contentModelFSA;
            }
        }
        return null;
    }

    /** pour renvoyer l'automate dans le cache */
    public void releaseContentModelFSA(FiniteStateAutomata fsa){
        
        //System.out.println("fsa of "+this+" released");
        if (fsa == contentModelFSA){
            //System.out.println("yes");
            contentModelFSA.reset();
            contentModelFSA_Used = false;
        }
    }
    
    /** demande un nouvel automate pour le content */
    public FiniteStateAutomata newDecompressionContentModelFSA(){

        if (content!=null){
            if (decompressionContentModelFSA == null) {
                //System.out.println("decompressionContentModelFSA of "+this+" not yet created");
                decompressionContentModelFSA = new DecompressionFiniteStateAutomata(getContentFullFSA(false,0));
                decompressionContentModelFSA.realize(); // pour calculer toutes les valeurs des transitions
                decompressionContentModelFSA.reset();
                decompressionContentModelFSA_Used = true;
                return decompressionContentModelFSA;
            } else if (decompressionContentModelFSA_Used){
                //System.out.println("decompressionContentModelFSA of "+this+" Used");
                DecompressionFiniteStateAutomata compFSA = new DecompressionFiniteStateAutomata(getContentFullFSA(false,0));
                compFSA.realize(); // pour calculer toutes les valeurs des transitions
                compFSA.reset();
                return compFSA;
            } else {
                //System.out.println("decompressionContentModelFSA of "+this+" not used ========");
                decompressionContentModelFSA_Used = true;
                decompressionContentModelFSA.reset();
                return decompressionContentModelFSA;
            }
        }

        return null;
    }

    /** pour renvoyer l'automate dans le cache */
    public void releaseDecompressionContentModelFSA(FiniteStateAutomata fsa){
        //System.out.println("fsa of "+this+" released");
        if (fsa == decompressionContentModelFSA){
            //System.out.println("yes");
            decompressionContentModelFSA_Used = false;
        }
    }

    // ------------------------------------------------------------
    // INSTANTIATION D'UN TYPE

    public TypeEncoder newInstance(){
        // Is there a special binariser for the current complex complex type ?
        if (CodingParameters.bSpecificVideoCodecs) {
            Binariser binariser=BinarisationConfig.getBinariserForType(URIRegistry.getWithoutPrefix(getName()));
            if (binariser!=null) return new SpecificTypeInstance(this,binariser);
        }
        return new ComplexComplexTypeInstance(this);
    }

    // ------------------------------------------------------------
    // GENERATIONS

    public void generateSignature(){
        super.generateSignature();

        if (content !=null){
            contentModelSignature = content.getSignature();
        }
    }
    
    public void generateFullFSA(){generateFullFSA(true);}
    
    public void generateFullFSA(boolean longName){
        super.generateFullFSA(longName);

        if (content !=null){
            contentModelFSA = getContentFullFSA(longName,0);
            contentModelFSA.realize();
        }
    }

    // si le complexcomplextype est mixed
    // on rajoute a la fin la possibilité d'encoder du texte
    private CompressionFiniteStateAutomata getContentFullFSA(boolean longname, int d){
        CompressionFiniteStateAutomata fsa = content.getFullFSA(longname,d);
        if (!mixed) return fsa;

        State childFinal = fsa.getFirstFinalState();
        childFinal.setFinalState(false);
        State s1 = new CompressionState();
        s1.setFinalState(true);
        KeyTransition kt = new KeyTransition(null,childFinal,s1);
        kt.setKey("end");
        kt.bind();

        // intermediaire item
        CompressionState ms = new CompressionState();
        // type string
        TypeState mixedTS = new TypeState(SimpleTypeDefinition.MIXED);

        ShuntTransition st = new ShuntTransition(null,childFinal,ms); // Always 0
        st.bind();
        MixedTransition pcdata = new MixedTransition(SimpleTypeDefinition.MIXED_KEY,ms,mixedTS);
        pcdata.bind();
        CompressionTransition back = new CompressionTransition(null,mixedTS,s1);
        back.bind();

        fsa.addState(s1);
        fsa.addState(ms);
        fsa.addState(mixedTS);
        
        return fsa;
    }

    public boolean isItUsedHere(String s){
        if (super.isItUsedHere(s))
            return true;
        
        if (content != null ){
            return content.isItUsedHere(s);
        }
        return false;
    }

    
}

