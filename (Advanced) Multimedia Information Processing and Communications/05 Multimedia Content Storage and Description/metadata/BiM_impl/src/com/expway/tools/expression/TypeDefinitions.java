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
import com.expway.tools.automata.*;
import com.expway.binarisation.CodingParameters;
import com.expway.tools.utils.MethodsBag;
import com.expway.util.URIRegistry;

public class TypeDefinitions {

    Hashtable     theTypeDefinitions    = new Hashtable();
    TreeMap       theElementDefinitions = new TreeMap();
    TreeSet       theTypeDefinitionNames = new TreeSet();

    // Le contenant de tous les TypesDefinitions
    // 1 URI <=> 1 TypeDefinitions
    SetOfDefinitions setOfDefinitions = null;
    String theURI = null;

    // Qualifieurs
    boolean elementFormQualified=true;
    boolean attributeFormQualified=true;
    
    public TypeDefinitions(SetOfDefinitions s,String uri){
        setOfDefinitions=s;
        theURI = uri;
    }

    public void setElementFormQualified(boolean state) {
        elementFormQualified=state;
    }

    public void setAttributeFormQualified(boolean state) {
        attributeFormQualified=state;
    }

    public boolean isElementFormQualified() {
        return elementFormQualified;
    }

    public boolean isAttributeFormQualified() {
        return attributeFormQualified;
    }
    
    // Renvoie le contenant de ce TypeDefinitions, pour accéder aux autres TypeDefinitions
    public SetOfDefinitions getSetOfDefinitions() {
        return setOfDefinitions;
    }

    public void setTargetNamespace(String uri){
        theURI = uri;
    }

    public String getTargetNamespace(){
        return theURI;
    }
    
    public URIRegistry getURIRegistry(){
        return setOfDefinitions.getURIRegistry();
    }

    /**
     *  @deprecated : One should use (@link #qualifyName(String)), because this methods allows the user to do the concatenation himself.
     */
    public String getPrefix(){
        return getURIRegistry().getPrefixOfURI(theURI);
    }

    public void setSetOfDefinitions(SetOfDefinitions s) {
        setOfDefinitions=s;
    }
    
    /**
     * Qualifies the input typeName with the prefix of this TypeDefinitions.
     * Returns null if there is a problem.
     */
    public String qualifyName(String typeName) {
        try {
            return getURIRegistry().getCompactName(getTargetNamespace(),typeName);
        } catch(Exception e) {
            return null;
        }
    }
    
    /**
     * Returns the typeDefinition corresponding to the qualified input name contentModelName.
     * If your input name is not qualified, please use qualifyName.
     * @return null si le type n'existe pas 
     */
    public TypeDefinition getTypeDefinition(String contentModelName){
        TypeDefinition td=(TypeDefinition)theTypeDefinitions.get(contentModelName);

        // Si pas trouvé, peut-être est-il dans un autre TypeDefinitions ?
        if (td==null) {
            // On cherche l'URI correcte du type
            if (setOfDefinitions!=null) {
                
                String uri=getURIRegistry().getURIFromCompactName(contentModelName);
                if (uri!=null) {
                    TypeDefinitions tds=setOfDefinitions.getDefinitions(uri);
                    // On rerecherce dans le bon TypeDefinitions, différent de celui-là
                    if (tds!=null && tds!=this) {
                        td=tds.getTypeDefinition(contentModelName);
                    }

                    // On n'a toujours pas trouvé... il faudra realiser ce noeud plus tard
                    // si le schéma auquel il appartient n'a pas été encore trouvé
                }
            }
        }
 
        return td;
    }

    public void putTypeDefinition(TypeDefinition td){
        // System.out.println("putTypeDefinition " + td + " in NS " + theURI);
        if (theTypeDefinitions.contains(td.getName())){
            System.out.println("[Warning] " + td.getName() + " is already defined in namespace " +
                               getSetOfDefinitions().getURIRegistry().getURIFromCompactName(td.getName()));
        } else {
            theTypeDefinitions.put(td.getName(),td);
            theTypeDefinitionNames.add(td.getName());
        }
    }

    public TreeSet typeDefinitionNames(){return theTypeDefinitionNames;}

    /** @return null si le type n'existe pas */
    public ElementDefinition getElementDefinition(String elementName){
        return (ElementDefinition)theElementDefinitions.get(elementName);
    }

    public void putElementDefinition(ElementDefinition td){
        theElementDefinitions.put(td.getName(),td);
    }

    public int getNumberOfElement(){
        return theElementDefinitions.size();
    }

    public int getElementNumber(String elementname){
        int t=0;
        for (Iterator e=theElementDefinitions.keySet().iterator();e.hasNext();){
            String key = (String)e.next();
            if (key.equals(elementname))
                return t;
            t++;
        }
        return -1;
    }

    public int getTypeNumber(String typename) {
        int t=0;
        for (Iterator e=types();e.hasNext();){
            TypeDefinition td=(TypeDefinition)e.next();
            if (td.getName().equals(typename))
                return t;
            t++;
        }
        return -1;
    }

    public String getElementName(int elementnumber){
        Iterator e = theElementDefinitions.keySet().iterator();
        String key = null;
        for (int t=0;t<=elementnumber;t++){
            key = (String)e.next();
        }
        return key;
    }
    
    // retourne un iterateur sur la liste des types 
    // qui n'ont pas de super type
    public Iterator rootTypes(){
        ArrayList ar = new ArrayList();
        Iterator i=types();
        while(i.hasNext()){
            TypeDefinition td =(TypeDefinition)i.next();
            if (!td.hasSuperType()){
                ar.add(td);
            }
        }
        return ar.iterator();
    }

    public Iterator types(){
        return theTypeDefinitions.values().iterator();
    }

    // Libere les caches de tous les TypesDefinitions
    public void reset() {
        for (Iterator i=types();i.hasNext();) {
            TypeDefinition td=(TypeDefinition)i.next();
            td.reset();
        }
    }
    
    public Iterator typeNames(){
        return theTypeDefinitions.keySet().iterator();
    }
      
    /**
     *  <code>generateSignatures</code> 
     *
     */
    public void generateSignature(){
        for (Enumeration e=theTypeDefinitions.keys();e.hasMoreElements();){
            String key = (String)e.nextElement();
            TypeDefinition et=getTypeDefinition(key);
            if (et instanceof ComplexComplexTypeDefinition){
                ComplexComplexTypeDefinition cet = (ComplexComplexTypeDefinition)et;
                cet.generateSignature();
            }
        }
    }    

    /**
     *  <code>generateFSAs</code> 
     *
     */
    public void generateFullFSA() {
        for (Enumeration e=theTypeDefinitions.keys();e.hasMoreElements();){
            String key = (String)e.nextElement();
            TypeDefinition et=getTypeDefinition(key);
            if (et instanceof ComplexComplexTypeDefinition){
                System.out.println("Generate FSA of " + key);
                ComplexComplexTypeDefinition cet = (ComplexComplexTypeDefinition)et;
                cet.generateFullFSA();
            }
            //System.out.println(et.getContentModelFSA());
        }
    }

    public void generateFullFSA(String key){
        TypeDefinition et=getTypeDefinition(key);
        if (et instanceof ComplexComplexTypeDefinition){
            ComplexComplexTypeDefinition cet = (ComplexComplexTypeDefinition)et;
            cet.generateFullFSA();
        }
    }


    /**
     *  <code>realizeInheritance</code> de tous les types contenus dans le
     *  TypeDefinitions.
     *
     */
    public void realizeInheritance() throws DefinitionException {
        for (Enumeration e=theTypeDefinitions.keys();e.hasMoreElements();){
            String key = (String)e.nextElement();
            TypeDefinition et=getTypeDefinition(key);
            et.realizeInheritance(this);
        }
    }


    /**
     *  <code>realize</code> de tous les types et elements contenus dans le
     *  TypeDefinitions. Doit se faire après le realize inheritance. 
     */
    public void realize() throws DefinitionException {
        for (Enumeration e=theTypeDefinitions.keys();e.hasMoreElements();){
            String key = (String)e.nextElement();
            TypeDefinition et=getTypeDefinition(key);
            et.realizeInheritance(this);
            
        }

        

        for (Enumeration e=theTypeDefinitions.keys();e.hasMoreElements();){
            String key = (String)e.nextElement();
            TypeDefinition et=getTypeDefinition(key);
            et.realize(this);
        }

        for (Iterator e=theElementDefinitions.keySet().iterator();e.hasNext();){
            String key = (String)e.next();
            ElementDefinition et=getElementDefinition(key);
            et.realize(this);
        }
    }



    
}

