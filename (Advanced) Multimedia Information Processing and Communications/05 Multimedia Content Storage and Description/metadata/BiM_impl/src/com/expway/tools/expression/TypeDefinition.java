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

import com.expway.tools.automata.FiniteStateAutomata;

import com.expway.tools.compression.CompressionFiniteStateAutomata;
import com.expway.tools.compression.ComplexTypeInstance;
import com.expway.tools.compression.TypeInstance;
import com.expway.tools.compression.TypeEncoder;
import com.expway.tools.compression.IntegerCoding;
import com.expway.tools.utils.MethodsBag;

import com.expway.util.URIRegistry;

import java.io.*;
import java.util.*;

abstract public class TypeDefinition implements Comparable {
    // Derivation mode
    static final public int RESTRICTION = 1;
    static final public int EXTENSION   = 2;

    // ------------------------------------------------------------
    // CHAMPS

    // le nom du type (qualified)
    String name = null;

    // le super type (qualified) : null = no super type
    String sSuperType=null;
    TypeDefinition supertypeDefinition = null;
    short derivationMethod = 0;

    /** the set of direct named subtypes (anonymous type are omitted) */ 
    TreeSet directSubtypes=null;

    // Qualifieurs
    boolean elementFormQualified;
    boolean attributeFormQualified;
    
    // some flags

    // ------------------------------------------------------------
    // CONSTRUCTEUR

    public TypeDefinition(String name){
        this.name = name;
    }

    // ------------------------------------------------------------
    // ABSTRAITES 

    abstract public TypeEncoder newInstance();
    abstract public boolean isItUsedHere(String s);

    // ------------------------------------------------------------
    // GESTION DES ESPACES

    public boolean ignoreWhiteSpace() {return false;}
    
    // ------------------------------------------------------------
    // ACCESSEURS

    public String getName(){ return name; }

    // L'heritage

    public boolean hasSubtypes(boolean bInNamespace){return getSubtypesIterator(bInNamespace).hasNext();}

    /**
     * Get an iterator over the subtypes of this type.
     * bInNamespace should be true if we want consider only the subtypes in the namespace of this type. 
     */
    public Iterator getSubtypesIterator(boolean bInNamespace) { return new SubtypesIterator(this,bInNamespace);}

    /**
     * Get the index of a subtype. Returns -1 if subtype is not a subtype of this type.
     * bInNamespace should be true if we want consider this namespace 
     */
    public int getSubtypeIndex(String subtype,boolean bInNamespace) {
        int index=0;

        for(Iterator i=getSubtypesIterator(bInNamespace);i.hasNext();) {
            TypeDefinition td=(TypeDefinition)i.next();
            if (td.getName().equals(subtype)) return index;
            index++;
        }
        
        return -1;
    }

    /**
     * Get the number of subtypes of a type.
     * bInNamespace should be true if we want consider only the subtypes in the namespace of this type. 
     */
    public int getNumberOfSubtypes(boolean bInNamespace) {
        int nbtypes=0;

        for(Iterator i=getSubtypesIterator(bInNamespace);i.hasNext();) {
            i.next();
            nbtypes++;
        }

        return nbtypes;
    }

    /**
     * Get a subtype by its index among all possible subtypes. Returns null if index is out of bounds. 
     * bInNamespace should be true if we want consider only the subtypes in the namespace of this type. 
     */
    public TypeDefinition getSubtype(int index, boolean bInNamespace) {
        try {
            Iterator i=getSubtypesIterator(bInNamespace);
            for (int j=0;j<index;j++) i.next();
            return (TypeDefinition)i.next();
        } catch(Exception e) {
            return null;
        }
    }

    /**
     * Is this type is a super type of s ?
     */
    public boolean isSuperTypeOf(String s){
        if (getSubtypeIndex(s,false)!=-1) return true;
        else return false;
    }

    public void setSuperType(String s,short deriv) {sSuperType=s;derivationMethod=deriv;}
    public boolean hasSuperType() {return sSuperType!=null;}
    public String getSuperType()           {return sSuperType;}
    public TypeDefinition getSuperTypeDefinition() {return supertypeDefinition;}
    public short getDerivationMethod(){return derivationMethod;}
    
    /*
      void addPossibleSubtype(String s){
      //System.out.println("   AddPossibleSubtype = " + s + " to type " + this.getName());
      // des soustypes
      if (possibleSubtypes == null) possibleSubtypes = new TreeSet();
      possibleSubtypes.add(s);
      // dans le meme NS (devrait être plutôt dans la même version non ???)
      if (URIRegistry.getPrefix(s).equals(URIRegistry.getPrefix(getName()))){
      if (possibleSubtypesInNamespace == null) possibleSubtypesInNamespace = new TreeSet();
      possibleSubtypesInNamespace.add(s);
      }
      }
    */

    /** direct subtypes are all types that are not anonymous */
    private void addDirectSubtype(TypeDefinition td) {
        if (directSubtypes==null) directSubtypes=new TreeSet();
        if (td.isAnonymous()) return;
        directSubtypes.add(td);
    }

    // Qualification locale de ce type ; donnée par le schéma auquel il appartient
    public boolean isElementFormQualified() { return elementFormQualified; }
    public boolean isAttributeFormQualified() { return attributeFormQualified; }

    /**
       returns true if the type is a local type, not a globally defined type 
       very dirty implementation using name - should be improved
    */
    public boolean isAnonymous(){
        String rname = URIRegistry.getWithoutPrefix(name);
        //System.out.print("is anonymous \'"+ rname+ "\' anonymous ?");
        if (rname.startsWith("AC") && rname.charAt(2) > '0' && rname.charAt(2) < '9'){
            //System.out.println(" yes");
            return true;
        }
        //System.out.println(" no");
        return false;
    }

    // ------------------------------------------------------------
    // REALIZATION

    public void realize(TypeDefinitions tds) throws DefinitionException {        
        // La qualification d'un type est donnée par le schéma qui définit ce type
        // Qualification of a type is given by the schema
        elementFormQualified=tds.elementFormQualified;
        attributeFormQualified=tds.attributeFormQualified;
    }

    /**
     *  Realize the subTypes superTypes fields of this object between the other
     *  TypeDefinitions contained in the TypeDefinitions it is belonging.
     */
    void realizeInheritance(TypeDefinitions tds) throws DefinitionException {
        if (sSuperType != null) {
            supertypeDefinition = tds.getTypeDefinition(sSuperType);
            if (supertypeDefinition==null) throw new DefinitionException("Unknown superType "+sSuperType+" of type "+getName());
            supertypeDefinition.addDirectSubtype(this);
        }
    }

    public int compareTo(Object o) {
        TypeDefinition td=(TypeDefinition)o;
        return getName().compareTo(td.getName());
    }

    // ------------------------------------------------------------
    // COMPATIBILITY
	
    TypeDefinition[] getCompatibilityCodings(){
        LinkedList ll = new LinkedList();
        
        TypeDefinition lastTD = this;
        TypeDefinition currentTD = this.getSuperTypeDefinition();
        ll.addFirst(this);
        while (currentTD != null){
            //System.out.println("CurrentTD = " +currentTD.getName());
            String currentURI = URIRegistry.getPrefix(currentTD.getName());
            String lastURI = URIRegistry.getPrefix(lastTD.getName());
            if (!currentURI.equals(lastURI)) {            // String.intern ??
                ll.addFirst(currentTD);
            }
            lastTD = currentTD;
            currentTD = currentTD.getSuperTypeDefinition();
        }

        // création du tableau
        TypeDefinition[] returnTDs = new TypeDefinition[ll.size()];
        int t=0;Iterator i=ll.iterator();
        while (i.hasNext()) 
            returnTDs[t++]=(TypeDefinition)i.next();

        return returnTDs;
    }

    // Libere les caches automates
    public void reset() {
    }
	
	
    public IntegerCoding getSubtypeEncoding(String subtype,SetOfDefinitions sod){
        // le type n'a pas de sous types
        if (!hasSubtypes(false)) return new IntegerCoding(-2,-2);
        // le soustype c'est lui
        if (subtype.equals(getName())) return new IntegerCoding(-1,-1);
        // les deux types sont dans le même namespace
        if (URIRegistry.getPrefix(getName()).equals(URIRegistry.getPrefix(subtype))){
            if (hasSubtypes(true))
                return new IntegerCoding(getSubtypeIndex(subtype,true),
                                         MethodsBag.getCodingLength(getNumberOfSubtypes(true)));
            else
                return new IntegerCoding(0,0);
        }
        // Les deux types sont dans des namespaces différents
        TreeSet subtypeNS = sod.getDefinitionsFromCompactName(subtype).typeDefinitionNames(); // recupere le set des types du NS
        return new IntegerCoding(MethodsBag.getPosition(subtype,subtypeNS),
                                 MethodsBag.getCodingLength(subtypeNS.size())); 
    }

    // @@COMPAT passe à la version
    public int getSubtypeEncodingLength(String namespace, SetOfDefinitions sod){
        // si les deux types sont dans la même version (pour l'instant namespace)
        if (sod.getURIRegistry().getURIFromCompactName(getName()).equals(namespace)){
            if (hasSubtypes(true))
                return MethodsBag.getCodingLength(getNumberOfSubtypes(true));
            else
                return 0;
        }
        // si les deux types sont dans des namespaces différents (le plus probable)
        TreeSet subtypeNS = sod.getDefinitions(namespace).typeDefinitionNames(); // recupere le set des types du NS
        return MethodsBag.getCodingLength(subtypeNS.size()); 
    }

    // @@COMPAT passe à la version
    public TypeDefinition getSubtypeDefinition(int typeCode, String namespace, SetOfDefinitions sod){
        System.out.println("Look for subtype of " + this + " in namespace " + namespace);
        if (sod.getURIRegistry().getURIFromCompactName(getName()).equals(namespace))
            // s'il n' y a pas de sous type dans le NS ca ne devrait pas etre appele
            return getSubtype(typeCode,true); 
        // si les deux types sont dans des namespaces différents (le plus probable)
        TreeSet subtypeNS = sod.getDefinitions(namespace).typeDefinitionNames(); // recupere le set des types du NS
        return sod.getTypeDefinition((String)MethodsBag.getObjectAt(typeCode,subtypeNS));
    }

    // ------------------------------------------------------------
    // AFFICHAGE

    public String toString(){ return getName(); }

    
}

class SubtypesIterator implements Iterator {
    Stack iteratorStack;
    Iterator currentIterator;
    boolean inNamespace;
    
    SubtypesIterator(TypeDefinition td,boolean bInNamespace) {
        iteratorStack=new Stack();
        inNamespace=bInNamespace;
        if (td.directSubtypes==null) currentIterator=null;
        else currentIterator=td.directSubtypes.iterator();
    }

    private void popIteratorStack() {
        if (!iteratorStack.isEmpty()) {
            currentIterator=(Iterator)iteratorStack.pop();
        } else currentIterator=null;
    }

    private void pushIteratorStack (TypeDefinition td) {
        if (td.directSubtypes!=null) {
            iteratorStack.push(currentIterator);
            currentIterator=td.directSubtypes.iterator();
        }
    }

    public boolean hasNext() {
        if (currentIterator==null) return false;
        if (currentIterator.hasNext()) return true;
        else {
            popIteratorStack();
            return hasNext();
        }
    }

    public Object next() {
        if (!hasNext()) throw new NoSuchElementException();
        TypeDefinition td=(TypeDefinition)currentIterator.next();
        pushIteratorStack(td);
        return td;
    }

    public void remove() {throw new UnsupportedOperationException();}
}
