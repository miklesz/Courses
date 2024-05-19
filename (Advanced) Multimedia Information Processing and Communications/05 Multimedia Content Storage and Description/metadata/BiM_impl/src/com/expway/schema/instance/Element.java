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

package com.expway.schema.instance;

import java.io.Writer;
import java.io.IOException;
import java.io.CharArrayWriter;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;



/**
 * Element.java
 *
 *
 * Created: Mon Aug 28 11:33:22 2000
 *
 * @author Cedric Thienot & Claude Seyrat
 * @version 1.0
 */

public class Element extends GroupElement 
implements CrossReferencable {

    public Element (String aN) {
        super(aN);        
    }
    
    String typeRef;    
    Type type;

    // if the element is defined using ref
    /**
     * the name of the reference
     */
    String refName;
    /**
     * the reference it self
     */
    Element refElement;

    String substitutionName;
    ///////////////////////////////////////////////////////////
    // ACCESSING
    ///////////////////////////////////////////////////////////

    boolean global = false;
    
    /**
     * Get the value of global.
     * @return value of global.
     */
    public boolean isGlobal() {
        return global;
    }
    
    /**
     * Set the value of global.
     * @param v  Value to assign to global.
     */
    public void setGlobal(boolean  v) {
        this.global = v;
    }
    
    
    /**
     * override of name of the element 
     * it could be either the name of the element or the name of the referenced element
     */
    public String getName(){
        if (getRefName()!=null) return getRefName();
        return super.getName();
    }
    /**
     * Get the value of refElement.
     * @return Value of refElement.
     */
    public Element getRefElement() {return refElement;}
    
    /**
     * Set the value of refElement.
     * @param v  Value to assign to refElement.
     */
    public void setRefElement(Element  v) {this.refElement = v;}
    
    /**
     * Get the value of refName.
     * @return Value of refName.
     */
    public String getRefName() {return refName;}
    
    /**
     * Set the value of refName.
     * @param v  Value to assign to refName.
     */
    public void setRefName(String  v) {
        if (v==null) return;
        isAReference = true;
        this.refName = v;}
    
    /**
     * Get the value of type.
     * @return Value of type.
     */
    public Type getType() {
        if (getRefElement()!=null) return getRefElement().getType();
        return type;
    }
    
    /**
     * Set the value of type.
     * @param v  Value to assign to type.
     */
    public void setType(Type  v) {this.type = v;}
    
    
    /**
     * Get the value of typeRef.
     * @return Value of typeRef.
     */
    public String getTypeRef() {return typeRef;}
    
    /**
     * Set the value of typeRef.
     * @param qName  Value to assign to typeRef.
     */
    public void setTypeRef(String  qName) {this.typeRef = qName;}

    public boolean isTypeOfElement(){
        return true;
    }








    
    ///////////////////////////////////////////////
    // SUBSTITUTION
    //////////////////////////////////////////////

    Collection substituableElements;
    
    /**
       * Get the value of substituableElement.
       * @return Value of substituableElement.
       */
    public Collection getSubstituableElements() {
        if (substituableElements == null) substituableElements = new ArrayList();
        return substituableElements;
    }

    /**
       * Get the value of all substituableElement.
       * @return Value of all substituableElement.
       */

    public Collection getAllSubstituableElements() {
        if (substituableElements == null) 
            return new ArrayList();
        ArrayList resultat = new ArrayList();
        resultat.addAll(substituableElements);
        Iterator aI= getSubstituableElements().iterator();
        while (aI.hasNext())
            resultat.addAll(((Element)aI.next()).getAllSubstituableElements());

        return resultat;
    }
    
    /**
     * add a substituableElement 
     * @param v  Value to add to substituableElements.
     */
    public void addSubstituableElement(Element e) {
        this.getSubstituableElements().add(e);
    }
    
    public boolean couldBeSubstituate(){
        if (substituableElements == null) return false;
        return !(substituableElements.isEmpty());
    }

    Element subsitutionElement;
    
    /**
     * Get the value of subsitutionElement.
     * @return Value of subsitutionElement.
     */
    public Element getSubsitutionElement() {return subsitutionElement;}
    
    /**
     * Set the value of subsitutionElement.
     * @param v  Value to assign to subsitutionElement.
     */
    public void setSubsitutionElement(Element  v) {this.subsitutionElement = v;}
    
    
    /**
     * Get the value of substitutionName.
     * TODO remove anciennement equiv Clas
     * @return Value of substitutionName.
     */
    public String getSubstitutionName() {return substitutionName;}
    
    /**
     * Set the value of substitutionName.
     * TODO remove anciennement equiv Clas
     * @param v  Value to assign to substitutionName.
     */
    public void setSubstitutionName(String  v) {
        this.substitutionName = v;}

    /**
     * true if the element can substitute an other
     */
   public boolean couldSubstituate(){return getSubstitutionName()!=null;}


    //////////////////////////////////////////////////////////////////
    //   REALIZE
    //////////////////////////////////////////////////////////////////


    //TODO
    public void realize(Schema aS) throws SchemaException {
        
        if (isAlreadyRealized) return;
        if (aS.affiche) System.out.println("on realize "+ this);
        isAlreadyRealized = true;
        // realize substituiton or EquivClass
        if (couldSubstituate()){
            setSubsitutionElement(aS.getRElement(getSubstitutionName()));
            if (getSubsitutionElement() == null)
                throw new SchemaException(this,
                                          SchemaMessages.getMessages("undefine element"),
                                          new String[] {this.getSubstitutionName()});
            getSubsitutionElement().realize(aS);
        }
        // realize when the type is a reference
        if (getTypeRef() != null){
            // try to find the type defined by the reference
            Type aType;
            try{
                aType = aS.getRType(typeRef);
            } catch (SchemaException e){
                e.setSource(this);
                throw e;
            }
            if (aType != null)
                setType(aType);
             else 
                throw new SchemaException(this,
                                          SchemaMessages.getMessages("undefine element"),
                                          new String[] {this.typeRef});
        }
        // realize the type of the element
        // supprimer si l'element n'est pas anonyme
        if (getType()!=null && getType().getName()==null){
            getType().realize(aS);
        }
        // realize when the element is a reference to another element
        if (getRefName() != null){
            Element aE;
            try{
                aE = aS.getRElement(getRefName());
            } catch (SchemaException e){
                e.setSource(this);
                throw e;
            }
            if (aE == null)
                throw new SchemaException(this,
                                          SchemaMessages.getMessages("undefine element"),
                                          new String[] {this.getRefName()});
            setRefElement(aE);
            getRefElement().realize(aS);
            setType(getRefElement().getType());
            //            if (!getRefElement().isCoherent) 
            //            getRefElement().realize(aS);
        }
        // prend la valeur par defaut de l'element qu'il peut substituer
        
        if ((getType()== null) && couldSubstituate()){
            setType(getSubsitutionElement().getType());
        }
        // TODO 
        //ici il ya une erreur car element a un type par defaut qui est any cf DTD
        if (getType()==null){
            //   System.out.println("ppppppourquoi le type est null ????" + getName() + location());
            
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("undefine type"),
                                      new String[] {getName()});
            
        }
        //
        // realize substituiton or EquivClass
        if (couldSubstituate()){
            //verify if it is a subtype
            if (!getType().isSubTypeOf(getSubsitutionElement().getType()))
                throw new SchemaException(this,
                                          SchemaMessages.getMessages("dfgdfsgds"), //TODO
                                          new String[] {this.getSubstitutionName()});
            // 
            getSubsitutionElement().addSubstituableElement(this);
        }
        if (getName() != null && getType() != null) //
            isCoherent = true;                
        if (getSchema()== null)
            setSchema(aS);
        if (aS.affiche) System.out.println("fin de realization de "+ this);
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    //      RAW SCHEMA
    //////////////////////////////////////////////////////////////////////////////////////////

    public void writeWithoutOccurenceRawSchema(AnonymousTypeRawSchemaConstructor anct)
        throws SchemaException {
        writeWithConditionnalOccurrenceRawSchema(anct,false);
    }
    /**
     * create the rawSchema with the occurence
     */
    public void writeRawSchema(AnonymousTypeRawSchemaConstructor anct) throws SchemaException{
        writeWithConditionnalOccurrenceRawSchemaAndSubstitution(anct,true);
    }

    private void writeWithConditionnalOccurrenceRawSchemaAndSubstitution(AnonymousTypeRawSchemaConstructor anct
                                                           , boolean withOccurence)
        throws SchemaException{
        if (getRefElement() == null)  
            writeWithConditionnalOccurrenceRawSchema(anct,true);
        else if (!getRefElement().couldBeSubstituate())
            writeWithConditionnalOccurrenceRawSchema(anct,true);
        else{
            if (!getRefElement().isCoherent) 
                throw new SchemaException(getRefElement(),
                                          SchemaMessages.getMessages("inconsistencty"));
            anct.write("(CHOICE" /*+ type.toRawSchema()*/
                   + (withOccurence ? getRawOccurence():"")
                   + " ");
            writeWithConditionnalOccurrenceRawSchema(anct,true);
            Iterator i = getRefElement().getAllSubstituableElements().iterator();
            while (i.hasNext()){
                Element e = (Element)i.next();
                e.writeWithConditionnalOccurrenceRawSchema(anct,true);  
            }
            anct.write(")");
        }
    }
    private void writeWithConditionnalOccurrenceRawSchema (AnonymousTypeRawSchemaConstructor anct
                                                           , boolean withOccurence) 
        throws SchemaException{
        if (!isCoherent) 
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("inconsistencty"));
        //        System.out.println(getName());

        // remplace getLocalRawName par getRawName
        anct.write((!isAReference()?getRawName():getRefElement().getRawName()) /*+ type.toRawSchema()*/
                   + (withOccurence ? getRawOccurence():"")
                   + "{");
        writeRawType(anct);
        anct.write("} ");
    }

    // give the type and generate one if it doesn't exist.
    void writeRawType(AnonymousTypeRawSchemaConstructor anct) throws SchemaException {
        //  if (getType().getName()== null && !getType().isSimple()) vieux truc
        if (isAReference()) 
            getRefElement().writeRawType(anct);
        else
            if (getType().getName()== null)
                // ecrit un nom generer
                // on utilise le getRawPrefix ne prenant pas en compte le schema de restriction
                anct.write(getOriginalRawPrefix()+anct.constructRawSchema(getType(),(isGlobal()?getName():null)));
            else
                anct.write(getType().getRawName());                                       
    }

    ///////////////////////////////////////////////////////
    // merge in case of restriction
    ///////////////////////////////////////////////////////


    public void mergeWith(GroupElement aGE) throws SchemaException {
        // le meme nom
                   
        if (!aGE.getName().equals(getName()))
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("invalid restriction"),
                                      new String[] {this.getName()});
        setRestrictionSchema(aGE.getRestrictionSchema());
    }

    

         

 
}// Element
