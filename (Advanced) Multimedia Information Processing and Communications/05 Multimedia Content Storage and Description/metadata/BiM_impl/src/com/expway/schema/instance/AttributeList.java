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

import java.util.Map;
import java.util.Iterator;

import com.expway.schema.utils.StringMap;

/**
 * AttributeList.java
 * this class hold the attribute defines in an attribute Group or in an complexType.
 *
 * Created: Wed Aug 30 10:37:41 2000
 *
 * @author 
 * @version
 */

public class AttributeList
    extends SchemaObject
    implements RawSchemaAble{

    public AttributeList (){super();}
    public AttributeList (String name){super(name);}
    String name;
    Map attributes = new StringMap();
    Map attributesComplete;

    Map attributesGroup;

    // in case of a ref to a group of attribute
    String refName;
    AttributeList refAttributeList;


    /**
     *
     */
    public boolean isAReference() {
        return (refName != null);
    }

    /**
       * Get the value of refAttributeList.
       * @return Value of refAttributeList.
       */
    public AttributeList getRefAttributeList() {return refAttributeList;}
    
    /**
       * Set the value of refAttributeList.
       * @param v  Value to assign to refAttributeList.
       */
    public void setRefAttributeList(AttributeList  v) {this.refAttributeList = v;}

    public  boolean isEmpty(){return getAttributes().isEmpty();}
    
    /**
       * Get the value of refName.
       * @return Value of refName.
       */
    public String getRefName() {return refName;}
    
    /**
       * Set the value of refName.
       * @param v  Value to assign to refName.
       */
    public void setRefName(String  v) {this.refName = v;}

    /**
     * override of name of the element 
     * it could be either the name of the element or the name of the referenced element
     */
    public String getName(){
        if (getRefName()!=null) return getRefName();
        return super.getName();
    }

    public Map getAttributes(){
        if (isAReference())
                return getRefAttributeList().getAttributes();
        if (attributesComplete != null )
            return attributesComplete;
        return attributes;
    }

    public Map getAttributesGroup(){
        if (attributesGroup == null)
            attributesGroup = new StringMap();
        return attributesGroup;
    }

    public Attribute getAttribute(String attributeName){
        if (isAReference())
            return getRefAttributeList().getAttribute(attributeName);
        return (Attribute)attributes.get(attributeName);
    }
    /**
     * add a list of attributes to the map.
     * TODO this method could be optimized perhaps
     */
    public void addAttributes(AttributeList ats) throws SchemaException{
        for (Iterator i=ats.getAttributes().values().iterator();i.hasNext();)
            addAttribute((Attribute)i.next());
    }
    /**
     * a utilise en cas d'heritage
     * ajoute directment a la vraie list des attributs apres la realisation
     */
    public void addAttributesComplete(AttributeList ats) throws SchemaException{
        for (Iterator i=ats.getAttributes().values().iterator();i.hasNext();)
            addAttributeComplete((Attribute)i.next());
    }

    public void addAttributeComplete(Attribute att) throws SchemaException{
        if (getAttributes().containsKey(att.getName()))
            throw new SchemaException(getAttribute(att.getName()),
                                      SchemaMessages.getMessages("duplicate definition"),
                                      new String[] {att.getNameNonNull()});
        getAttributes().put(att.getName(),att);
    }    

    public void addAttributeGroup(AttributeList ats) throws SchemaException{
        getAttributesGroup().put(ats.getName(),ats);
    }

    public void addAttribute(Attribute att) throws SchemaException{
        if (attributes.containsKey(att.getName()))
            throw new SchemaException(getAttribute(att.getName()),
                                      SchemaMessages.getMessages("duplicate definition"),
                                      new String[] {att.getNameNonNull()});
        attributes.put(att.getName(),att);
    }    

    public AttributeList mergeAttributesWith(AttributeList aAttributListSource) throws SchemaException {
        //        System.out.println("on merge "+this +" avec "+ aAttributListSource);
        Iterator aI = attributes.values().iterator();
        while (aI.hasNext()){
            Attribute a = (Attribute) aI.next();
            Attribute a1 = aAttributListSource.getAttribute(a.getName());
            // l'attribut n'a pas ete deja defini
            if (a1 == null)
                throw new SchemaException(a,
                                          SchemaMessages.getMessages("undefined Attribute in restriction"),
                                          new String[] {a.getName()});
            // puis on les merge tous les deux
            // en mofifiant a
            a.mergeWith(a1);
        }
        // on ajoute les attributs non modifies
        for(Iterator i=aAttributListSource.getAttributes().values().iterator();i.hasNext();){
            Attribute att = (Attribute)i.next();
            if (!attributes.containsKey(att.getName())){
                attributes.put(att.getName(),att);
            }
        }
        return this;
    }

    public void realizeWithoutException (Schema aS){
        try {realizeException(aS, false);
        }catch (SchemaException e){}
    }
    public void realize(Schema aS)throws SchemaException{
        realizeException(aS,true);
    }

    public void realizeException (Schema aS, boolean sendException)throws SchemaException{
        if (isAlreadyRealized) return;
        isAlreadyRealized = true;
        //in case  of reference to an attribute group
        if (getRefName() != null){
            AttributeList aAL = null;
            try{
                aAL = aS.getRAttributeGroup(getRefName());
            } catch (SchemaException e){
                e.setSource(this);
                if (sendException) throw e;
            }
            if (aAL == null && sendException)
                throw new SchemaException(this,
                                          SchemaMessages.getMessages("undefine element"),
                                          new String[] {getRefName()});
            setRefAttributeList(aAL);
            aAL.realize(aS);
        } else {
            if (getSchema()== null)
                setSchema(aS);
            Iterator aI;
            //in other case
            aI = attributes.values().iterator();
            while (aI.hasNext()){
                Attribute a = (Attribute) aI.next();
                if (a.getSchema()== null)
                    a.setSchema(getSchema());
                a.realize(aS);
            }
            attributesComplete = attributes;
            if (attributesGroup != null){
                aI = attributesGroup.values().iterator();
                while (aI.hasNext()){
                    AttributeList al = ((AttributeList) aI.next());
                    if (al.getSchema()== null)
                        al.setSchema(getSchema());
                    al.realize(aS);
                    Iterator aI2 = al.getAttributes().values().iterator();
                    while (aI2.hasNext()){
                        Attribute att = (Attribute)aI2.next();
                        if (att.getSchema()== null)
                            att.setSchema(getSchema());
                        if (attributesComplete.containsKey(att.getName()))
                            throw new SchemaException(getAttribute(att.getName()),
                                                      SchemaMessages.getMessages("duplicate definition"),
                                                      new String[] {att.getNameNonNull()});
                        attributesComplete.put(att.getName(),att);
                    }
                }
            }
        }
        isCoherent = true;
    }

    public void writeRawSchema(AnonymousTypeRawSchemaConstructor atsc) throws SchemaException {
        if (getAttributes().isEmpty()) {
            atsc.write( "");
            return;
        }
        //element
        Iterator aI = getAttributes().values().iterator();
        while (aI.hasNext()){
            Attribute aA =  ((Attribute)aI.next());
            if (!aA.isProhibited()){
                atsc.write("(ATTR");
                aA.writeRawSchema(atsc);
                while (aI.hasNext())
                    ((Attribute) aI.next()).writeRawSchema(atsc) ;
                atsc.write(")");
            }
        }
    }
               

}// AttributeList

