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

import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;

import com.expway.schema.SchemaSymbols;


/**
 * Type.java
 *
 * type
 *
 * Created: Mon Aug 28 11:33:22 2000
 *
 * @author Claude Seyrat & Cedric Thienot
 * @version 1.0
 */

public abstract class Type extends SchemaObject
    implements RawSchemaAble,CrossReferencable {

    // 
    static boolean ABSTRACT_DEFAULT_VALUE = false;


    boolean anonymous = false;    
    // collection of direct sub class
    protected Collection collectionOfSubtype = new HashSet();
    
    public Type (String aN) {
        super(aN);
        if (aN==null) anonymous = true;
    }


    String anonymousID;
    
    /**
     * Get the value of anonymousID.
     * @return value of anonymousID.
     */
    public String getAnonymousID() {
        return anonymousID;
    }
    
    /**
     * Set the value of anonymousID.
     * @param v  Value to assign to anonymousID.
     */
    public void setAnonymousID(String  v) {
        this.anonymousID = v;
    }
    


    boolean mixed = false;
    
    /**
       * Get the value of mixed.
       * @return Value of mixed.
       */
    public boolean isMixed() {return mixed;}
    
    /**
       * Set the value of mixed.
       * @param v  Value to assign to mixed.
       */
    public void setMixed(boolean  v) {this.mixed = v;}
    
    /**
       * Set the value of mixed.
       * @param v  Value to assign to mixed.
       */
    public void setMixed(String v) throws SchemaException {
        if (v == null) return;
        if (v.equals("true")) 
            setMixed(true);
        else if (v.equals("false")) 
            setMixed(false);
        else
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("invalid value"),
                                      new String[] {v,SchemaSymbols.ATT_MIXED});
    }



    boolean abstractValue = ABSTRACT_DEFAULT_VALUE;
    
    /**
     * Get the value of abstract.
     * @return value of abstract.
     */
    public boolean isAbstract() {
        return abstractValue;
    }
    
    /**
     * Set the value of abstract.
     * @param v  Value to assign to abstract.
     */
    public void setAbstract(boolean  v) {
        this.abstractValue = v;
    }
    /**
     * Set the value of abstract.
     * could be null
     * @param v  Value to assign to abstract.
     */
    public void setAbstract(String v) throws SchemaException{
        if (v == null) return;
        if (v.equals("true")) 
            setAbstract(true);
        else if (v.equals("false")) 
            setAbstract(false);

        else
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("invalid value"),
                                      new String[] {v,SchemaSymbols.ATT_ABSTRACT});
    }


    /**
     * return a collection of all direct subtype
     */
    public Collection getSubTypes(){
        return collectionOfSubtype;
    }

    /**
     * return a collection of all the name of non-abstract subtypes 
     */
    public Collection getAllSubTypesNames(){
        Collection collectionOfAllNonAbstractSubTypeName = new HashSet();
        Iterator i = getAllSubTypes().iterator();
        while ( i.hasNext()){
            Type t = (Type)i.next();
            if (!t.isAbstract())
                collectionOfAllNonAbstractSubTypeName.add(t.getRawName());
        }
        return collectionOfAllNonAbstractSubTypeName;
    }

    /**
     * return a collection of all  subtypes
     */
    public Collection getAllSubTypes(){
        Collection collectionOfAllSubType = new HashSet();
        Iterator i = getSubTypes().iterator();
        while ( i.hasNext()){
            Type t = (Type)i.next();
            if (!t.isAnonymous()) 
                if (!collectionOfAllSubType.contains(t)){
                    collectionOfAllSubType.add(t);
                    collectionOfAllSubType.addAll(t.getAllSubTypes());
                }
        }
        return collectionOfAllSubType;
    }

    // Ne sert que pour le raw schema pourrait être supprimé
    Attribute xsiTypeAttribute;
    void createxsiTypeAttribute(Schema aS) throws SchemaException{
        Collection aC = getAllSubTypesNames();
        if (aC.isEmpty()) return;
        Attribute typeAttribute = new Attribute("xsi:type");
        // creation du type
        DataType aDT = new DataType(null,SchemaSymbols.URI_SCHEMAFORSCHEMA+":string");
        aDT.addEnumerationFacet(aC);
        //        System.out.println(aDT);
        typeAttribute.isCoherent = true;
        aDT.realize(aS);
        typeAttribute.setDatatype(aDT);
        xsiTypeAttribute = typeAttribute;
    }
    

    String base;

    final public boolean isAnonymous(){return (getName() == null);}

    /**
     * Get the value of base.
     *       this value could be null
     * @return Value of base.
     */

    final public String getBase() {return base;}
    
    /**
     * Set the value of base.
     * @param v  Value to assign to base.
     */
    public void setBase(String  v) {this.base = v;}
    
    /**
     * send true if the complexType has a super type
     */
    public boolean hasSuperType(){return (base!=null);}

    /**
     * set the derivedMode:
     *        in case of complexType  it could be restriction or extension
     *        in case of complexType  it could be restriction or extension or list
     */

    abstract public void setDerived(String name) throws SchemaException;

    abstract public String  getDerived();

    public abstract boolean isSimple();

    abstract void writeContentToRawSchema(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException;

    abstract void writeRawSchemaWithName(AnonymousTypeRawSchemaConstructor atrsc, String name)
        throws SchemaException;
    abstract public Type getSuperType();

    boolean isSubTypeOf(Type t){return true;}

    public boolean isAType(){return true;}


          

    /**
     * cette methode est surcharge dans complextype
     */
    public void realizeWithInheritance(Schema as) throws SchemaException{}

    // HTML
    public static String HTMLStyle="txtpurpleitalic";

}
