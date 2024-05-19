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

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import java.io.Writer;
import java.io.IOException;

import com.expway.schema.SchemaSymbols;

/**
 * ComplexType.java
 *
 * complexType
 * <complexType name="">
 * Created: Mon Aug 28 11:33:22 2000
 *
 * @author Claude Seyrat & Cedric Thienot
 * @version 1.0
 */

public class ComplexType
    extends  Type {

    public ComplexType (String aN) {
        super(aN);
        
    }
    /** generate le fameux type any qui est le pere de tous les type
     */
    static ComplexType GenerateANYTYPE(){
        ComplexType ct=new ComplexType("anyType");
        ct.isCoherent=true;
        ct.setAbstract(true);
        // TODO a voir
        return ct;
    }

    /////////////////////////////////////////
    // Variable
    /////////////////////////////////////////

    //DEBUGGING
    final boolean DEBUG = false;
    //reduction d'arbre
    static final boolean REDUCTION = false;

    static int indent =0;
    static String giveIndent(){
        String in ="";
        for (int i = 0; i<indent; i++)
            in +=" ";
        return in;
    }

    //collection of Attribute group
    //    Collection attributeGroupList;

    // list of the attributes
    AttributeList attributes;

    // list of the every attributes
    AttributeList attributesInh;

    // hold the root group i.e. a sequence
    Group mainGroup = new Group(SchemaSymbols.ELT_SEQUENCE);

    // hold the root group using the derivation
    Group mainGroupInh;

    // true if it is derived by extension, false otherwise
    boolean isDerivedByExtension = false;

    // true if it is derived by restriction, false otherwise
    boolean isDerivedByRestriction = false;

    // if it is a simpleType with attribute 
    // the datatype is hold by the variable
    DataType datatype;

    // hold the value of the base type, it could be null;
    Type baseType;

    // become true if it is derived from a simpleType.
    boolean simpleComplex = false;

    ////////////////////////////////////////
    // accessing
    ////////////////////////////////////////




    public    DataType getDatatype() {
        // if the datatype is not created then it create one
        if (datatype == null) 
            datatype = new DataType("",getBase());
        return datatype;
    }


    /**
     * Get the value of attributes.
     * @return Value of attributes.
     */
    public AttributeList getAttributes() {
        if (attributes==null)
            attributes = new AttributeList();
        return attributes;
    }

    /**
     * Get the value of attributes define alone or in a Group.
     * this list could be empty
     * error if two attribute has the same name
     * @return Value of attributes.
     */
    public AttributeList getAllAttributes()  {
        /*
          System.out.println(("on contruit les attr"));
        
          AttributeList result = new AttributeList();
          result.addAttributes(getAttributes());
          if (attributeGroupList != null){
          Iterator i =attributeGroupList.iterator();
          while (i.hasNext()){
          AttributeList al = (AttributeList)i.next();
          result.addAttributes(al);
          }
          }
          return result;
        */
        return getAttributes();
    }

    /**
     * Get the list  of attributes from the type and its super types.
     * @return Value of attributes.
     */
    public AttributeList getAttributesInh() {
        if (attributesInh == null) return getAttributes();
        return attributesInh;
    }
    
    /**
     * Set the value of attributes.
     * @param v  Value to assign to attributes.
     */
    public void setAttributes(AttributeList  v) {this.attributes = v;}

    /**
     * Get the collection of attributes groups.
     * @return Value of attributes groups.
     */
    /*
    Collection getAttributeGroupList() {
        if (attributeGroupList==null)
            attributeGroupList = new ArrayList();
        return attributeGroupList;
    }
    */
    /**
     * add an attribute
     */
    public void add(Attribute att) throws SchemaException{
        getAttributes().addAttribute(att);
    }

    /**
     * add an attribute List
     */
    
    public void addAttributeGroup(AttributeList atl) throws SchemaException{
        getAttributes().addAttributeGroup(atl);
        //        getAttributeGroupList().add(atl);
    }
    


    // group
    
    /**
     * Get the value of mainGroup.
     * @return Value of mainGroup.
     */
    public Group getMainGroupInh() {
        if (mainGroupInh== null)
            return getMainGroup();
        return mainGroupInh;
    }
    /**
     * Get the value of mainGroup.
     * @return Value of mainGroup.
     */
    public Group getMainGroup() {
        if (mainGroup== null){
            //on initialise aussi la premiere fois qu'on ajoute
            mainGroup = new Group(SchemaSymbols.ELT_SEQUENCE);
            // on pourra peut etre supprimer
            mainGroup.setApplicationObject(com.expway.util.LocalHandler.NAME_LOCATOR,
                                           (org.xml.sax.Locator)getApplicationObject(com.expway.util.LocalHandler.NAME_LOCATOR));
        }
        return mainGroup;
    }
    
    /**
     * Set the value of mainGroup.
     * @param v  Value to assign to mainGroup.
     */
    public void setMainGroup(Group  v) {this.mainGroup = v;}
    

    public void addGroupElement(GroupElement age) {
                           
        // on initialise si age n'est pas un group
        if(mainGroup == null)
            if (!age.isAgroup()){
                // normalement ce cas ne devrait pas arriver a verifier dans la doc d'XML schema
                mainGroup = new Group(SchemaSymbols.ELT_SEQUENCE);
                getMainGroup().addGroupElement(age);
            } else mainGroup = (Group) age;
        else 
            getMainGroup().addGroupElement(age);
    }

   
    
    /**
     * Get the value of baseType.
     * @return Value of baseType.
     */
    public Type getType() {return baseType;}

    
    /**
     * Set the value of the base baseType.
     * @param v  Value to assign to type.
     */
    public void setType(Type  v) {
        // add this to the collection of subtype of v
        v.collectionOfSubtype.add(this);
        this.baseType = v;
    }

    public Type getSuperType() {return getType();}
    /**
     * return false
     */
    public boolean isSimple(){return false;}

    /**
     * if it is a simpleComplexType return true
     * either return false 
     */
    public boolean isSimpleComplex(){return simpleComplex;}

    /**
     * set the deivation type it could be either by extension or by restriction
     */
    public void setDerived(String derivationType) throws SchemaException{
        if (derivationType == null) return;
        if (derivationType.equals(SchemaSymbols.ATTVAL_EXTENSION))
            isDerivedByExtension = true;
        else if (derivationType.equals(SchemaSymbols.ATTVAL_RESTRICTION))
            isDerivedByRestriction = true;
        else {
            isAlreadyRealized = true;
            isCoherent = false;
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("invalid value"),
                                      new String[] {derivationType, SchemaSymbols.ATT_DERIVEDBY});
        }
    }
    public boolean isDerived(){ return getDerived()!=null;}

    public String getDerived(){
        if (isDerivedByRestriction) 
            return SchemaSymbols.ATTVAL_RESTRICTION;
        if (isDerivedByExtension) 
            return SchemaSymbols.ATTVAL_EXTENSION;
        return null;
    }
 
   ////////////////////////////////////////
    ///////  Data type <--> ComplexType

    public void addFacet(String facetName, String facetValue) throws SchemaException{
        getDatatype().addFacet(facetName,facetValue);
    }

    public void addEnumerationFacet(Collection c) throws SchemaException{
        getDatatype().addEnumerationFacet(c);
    }

    /////////////////////////////////////////////////////////////
    // REALIZE
    ////////////////////////////////////////////////////////////


    public void realize(Schema aS) throws SchemaException{
        if (isAlreadyRealized) return;
        isAlreadyRealized = true;       
        // DEBUG
        if (DEBUG) {
            if (indent == 0) 
                System.out.println("\nnewType");
            System.out.println(ComplexType.giveIndent()
                               +"on realize "
                               + (getName()==null?"anonymous":getName()) 
                               +" "+location());
            indent ++;
        }
        if (getBase() != null){
            // try for a datatype
            Type aType;
            try{
                aType = aS.getRType(getBase());
            } catch (SchemaException e){
                e.setSource(this);
                throw e;
            }
            if (aType != null){
                // realize the superType
                aType.realize(aS);
                setType(aType);
                if (aType.isSimple())
                    simpleComplex = true;
                else if (((ComplexType)aType).isSimpleComplex())
                    simpleComplex = true;
                // on peut peut être trouver une solution plus intelligent
                // c'est la cas ou un complexType derive d'un simple Type mais ne fait
                // qu'ajouter des attributs
                if (isSimpleComplex() && datatype == null)
                    // le super type est un datatype
                    if (getType().isSimple())                        
                        datatype = (DataType)getType();
                //le super type est un complex Type
                    else datatype = ((ComplexType)getType()).datatype;
            }
            else {
                System.out.println(getBase());

                throw new SchemaException(this,
                                          SchemaMessages.getMessages("undefine element"),
                                          new String[] {getBase()});
            }
        }
        // coherence avec simple complex
        if (isSimpleComplex() && (mainGroup != null))
            if (!mainGroup.isEmpty()){
                System.out.println(mainGroup+" "+mainGroup.isEmpty());
                throw new SchemaException(this,
                                          SchemaMessages.getMessages("simple Anonymous Complex with a model"),
                                          new String[] {});
        }
        // realize Groupe of Attribute.
        /*
        if (attributeGroupList != null){
        Iterator i =attributeGroupList.iterator();
        while (i.hasNext())
        ((AttributeList)i.next()).realize(aS);
        }
        */

        if (datatype != null)
            datatype.realize(aS);
        if ((isDerivedByRestriction || isDerivedByExtension)&& baseType ==null)
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("derive without superType"),
                                      new String[] {getNameNonNull()});
        //        realizeInheritance(aS);

        // realize attribbute
        // inutile dans le cas de restriction car la realization se fait quand in merge les attributs
        // a voir les attributs sont il vraiment realise dans le cas d'un restriction
        if (!isDerivedByRestriction && attributes != null)
            //        if ( attributes != null) // maintenant c'est utile
            attributes.realize(aS);   
        if (isDerivedByRestriction && attributes != null)
            //        if ( attributes != null) // maintenant c'est utile
                attributes.realizeWithoutException(aS);   
            

        attributesInh = new AttributeList();
        attributesInh.addAttributes(getAllAttributes());

        
        // realize group
        if (mainGroup!= null){
            mainGroup.realize(aS);
            if (REDUCTION){
                GroupElement ag = mainGroup.reductiona();
                if (ag.isAgroup())
                    mainGroup=(Group) ag;
                else{
                mainGroup.removeElement();
                mainGroup.addGroupElement(ag);
                }
            }
            
        }
        
        //dans le cas de type anonyme on ne refait pas la deuxieme passe de realization
        if (isAnonymous() && isDerived()) {
            realizeInheritance(aS);
            // realize group
            if (mainGroupInh!= null){    
                getMainGroupInh().realize(aS);
            }
            //            isCoherent=true;
        }
        isCoherent=true;

        if (DEBUG) {
            System.out.println(ComplexType.giveIndent()
                               +"fin de "
                               + (getName()==null?"anonymous":getName()) 
                               + " " 
                               +location());
            indent --;
        }
        
    }


    // in heritance realization$
    private boolean isAlreadyRealizedInheritance = false;
    // a utiliser en premier
    public void realizeWithInheritance(Schema aS) throws SchemaException{
        // if (!isCoherent) return;
        if (isAlreadyRealizedInheritance) return;
        isAlreadyRealizedInheritance = true;
        if (getType() != null)
            getType().realizeWithInheritance(aS);
        realizeInheritance(aS);
        //        realizeWithInheritance(aS);

        // realize group
        if (mainGroupInh!= null){    
            mainGroupInh.realize(aS);
        }
        isCoherent = true;
    }       
    // en deuxieme
    void realizeInheritance(Schema aS) throws SchemaException{
        if (isDerived() && (getType()!=null))
            if (isDerivedByRestriction)
                realizeInheritanceByRestriction(aS);
            else {
                attributesInh = new AttributeList();
                attributesInh.addAttributes(getAllAttributes());
                if (isDerivedByExtension)
                    realizeInheritanceByExtension(aS);
            }
    }

    //TODO
    void realizeInheritanceByExtension(Schema aS) throws SchemaException{
         if (!getType().isSimple()) {
             if(!((ComplexType)getType()).isAlreadyRealizedInheritance)
                 ((ComplexType)getType()).realizeWithInheritance(aS);
             if (((ComplexType)getType()).getAttributesInh() == null)
               System.out.println("**** Mauvaise réalisation des attributs pour "+((ComplexType)getType()).getName());
           else {
               // construction of attributesInh
               attributesInh.addAttributesComplete(((ComplexType)getType()).getAttributesInh());
           }
             // construction of mainGroupInh
             mainGroupInh = new Group(SchemaSymbols.ELT_SEQUENCE);
             mainGroupInh.addGroupElements(((ComplexType)getType()).getMainGroupInh().getGroupElements());
             mainGroupInh.addGroupElements(getMainGroup().getGroupElements());
             mainGroupInh.isCoherent=true;
         }

    }
    

    void realizeInheritanceByRestriction(Schema aS) throws SchemaException{
        if (DEBUG) System.out.println("derivation par restriction de "+this.getName());
        if (!getType().isSimple()) {
            // verification des attributs
            //on la merge avec les anciens
            if (!getAllAttributes().isEmpty()){
                
                if(((ComplexType)getType()).getAttributesInh()==null)
                    System.out.println("**** Mauvaise réalisation des attributs pour "+((ComplexType)getType()).getName());
                else{
                    attributes = getAllAttributes().mergeAttributesWith(((ComplexType)getType()).attributesInh);

                    //                    attributes = getAllAttributes().mergeAttributesWith(((ComplexType)getType()).getAttributesInh());
                    attributesInh = attributes;
                }
            } else {
                attributes = ((ComplexType)getType()).getAttributesInh();
                attributesInh = attributes;
            }
            // en cas de restriction le schema des objets doit être celui du superType
            // on va merger
            try {
                if (!Schema.RWS_NON_REALIZE) 
                    mainGroup.mergeWith(((ComplexType)getType()).mainGroup);
            } catch (SchemaException e){
                e.setSource(this);
                throw e;
           }
        }
        //TODO
    }
         
    /////////////////////////////////////////////////////////////
    // RAW SCHEMA
    ////////////////////////////////////////////////////////////


    AttributeList getAttributesInhAndxsiType(){
        /* vieux code a supprimer
           AttributeList aTL = new AttributeList();
           try{
           if (getAttributesInh() != null)
           aTL.addAttributes(getAttributesInh());
           /* supprimer 
              finalement on ajoute pas xsi type dans les attributs du Raw schema
              if (xsiTypeAttribute != null)
              aTL.addAttribute(xsiTypeAttribute);
        
              }catch (SchemaException e){}

              return aTL;
        */
        return getAttributesInh();

    }

    public void writeRawSchema(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException{
        writeRawSchemaWithName(atrsc,getName());
    }

    void writeContentToRawSchema(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException{
        writeAbstract(atrsc);
        writeMixed(atrsc);
        writeXSIType(atrsc);
        if (!Schema.RWS_NON_REALIZE) {
            if (getAttributesInhAndxsiType() != null)
                getAttributesInhAndxsiType().writeRawSchema(atrsc);
            if (!getMainGroupInh().isEmpty())
                getMainGroupInh().writeRawSchema(atrsc);
        } else {
            if (getAllAttributes() != null)
                getAllAttributes().writeRawSchema(atrsc);
            if (!getMainGroup().isEmpty())
                getMainGroup().writeRawSchema(atrsc);
        }
    }

    void writeRawSchemaWithName(AnonymousTypeRawSchemaConstructor atrsc, String name)
        throws SchemaException{
        if (!isCoherent) 
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("inconsistency"));
        // in case of complexType
        if (!isSimpleComplex()){
            atrsc.write("{" + getRawPrefix()+name+"}");
            writeContentToRawSchema(atrsc);
            return;
        }
        //in case of simple complexType
        else {
            atrsc.write("[" + getRawPrefix()+name+ "]");
            datatype.writePrimitiveType(atrsc);
            datatype.writeContentToRawSchema(atrsc);
            writeContentToRawSchema(atrsc);
        }
    }
    // FINALEMENT ce n'est plus XSI type qu'on ecrit mais superTyye Of
    void writeXSIType(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException {
        /*
          if (xsiTypeAttribute == null) return;
          atrsc.write("(XSITYPE ");
          xsiTypeAttribute.datatype.writeOnlyEnumerationToRawSchema(atrsc);
          atrsc.write(")");
        */

        if (isDerived())
            if (!getType().getQName().equals(SchemaSymbols.URI_SCHEMAFORSCHEMA+"/"+SchemaSymbols.ANYTYPE)){
                if (Schema.RWS_NON_REALIZE) 
                    atrsc.write(((isDerivedByExtension)?"(EXTENSION ":"(RESTRICTION "));
                else
                    atrsc.write("(SUPERTYPEOF ");
                atrsc.write(getType().getRawName());      
                atrsc.write(")");
            }
                
    }
    void writeAbstract(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException {
        if (isAbstract())
            atrsc.write(" (ABSTRACT) ");
    }

    void writeMixed(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException {
        if (isMixed())
            atrsc.write(" (MIXED) ");
    }

    
        

}// ComplexType
