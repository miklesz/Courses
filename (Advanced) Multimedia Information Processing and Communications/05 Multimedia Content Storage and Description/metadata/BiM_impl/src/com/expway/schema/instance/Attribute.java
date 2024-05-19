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

import com.expway.schema.SchemaSymbols;

/**
 * SimpleType.java
 *
 *
 * Created: Mon Aug 28 11:33:22 2000
 *
 * @author 
 * @version
 */

public class Attribute 
    extends SchemaObject 
    implements RawSchemaAble{


    //Constructor

    public Attribute (String aN) {
        
        super(aN);
    }
    public Attribute(){}    
    // in case of a ref to an attribute
    String refName;
    Attribute refAttribute;
    
    /**
       * Get the value of refAttribute.
       * @return Value of refAttribute.
       */
    public Attribute getRefAttribute() {return refAttribute;}

    
    /**
       * Set the value of refAttribute.
       * @param v  Value to assign to refAttribute.
       */
    public void setRefAttribute(Attribute  v) {
        isAReference = true;
        this.refAttribute = v;}
    

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


    String value;
    
    /**
     * Get the value of value.
     * @return value of value.
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Set the value of value.
     * @param v  Value to assign to value.
     */
    public void setValue(String  v) {
        this.value = v;
    }
    
    // attribute Required

    
    /**
       * Get the value of required.
       * @return Value of required.
       */
    public boolean required() {
        return use.equals(SchemaSymbols.ATTVAL_REQUIRED);
    }

    public boolean isOptional() {
        return use.equals(SchemaSymbols.ATTVAL_OPTIONAL);
    }

    public boolean isProhibited() {
        return use.equals(SchemaSymbols.ATTVAL_PROHIBITED);
    }
    
    /**
     * deprecated
       * Set the value of required.
       * @param v  Value to assign to required.
       */
    public void setRequired(boolean b){
        if (b) use=SchemaSymbols.ATTVAL_REQUIRED;}
    


    String use = SchemaSymbols.ATTVAL_OPTIONAL;
    
    /**
       * Get the value of use.
       * @return Value of use.
       */
    public String getUse() {return use;}
    
    /**
     * TODO verifier qu'on envoie les bonne valeur
       * Set the value of use.
       * @param v  Value to assign to use.
       */
    public void setUse(String  v) throws SchemaException {
        if (!(v.equals(SchemaSymbols.ATTVAL_OPTIONAL) ||
             v.equals(SchemaSymbols.ATTVAL_PROHIBITED)||
             v.equals(SchemaSymbols.ATTVAL_REQUIRED)))
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("invalid value"),
                                      new String[] {v,SchemaSymbols.ATT_USE});
        this.use = v;
    }

    DataType datatype;
    
    /**
       * Get the value of datatype.
       *          either directly using the datatype
       * @return Value of datatype, null if it is not defined.
       */
    public DataType getDatatype() {
        if (datatype != null)
            return datatype;
        if (datatypeRef != null)
            return (DataType)datatypeRef.getTarget();
        return null;
    }
    
    /**
       * Set the value of datatype.
       * @param v  Value to assign to datatype.
       */
    public void setDatatype(DataType  v) {this.datatype = v;}
    
 

    DataTypeRef datatypeRef;
    
    /**
       * Get the value of datatypeRef.
       * @return Value of datatypeRef.
       */
    public DataTypeRef getDatatypeRef() {return datatypeRef;}
    
    /**
       * Set the value of datatypeRef.
       * @param v  Value to assign to datatypeRef.
       */
    public void setDatatypeRef(DataTypeRef  v) {this.datatypeRef = v;}
    


    //TODO
    public void realize(Schema aS) throws SchemaException{
        if (isAlreadyRealized) return;
        isAlreadyRealized = true;
        if (refName != null){
            try{
                setRefAttribute(aS.getRAttribute(refName));
            } catch (SchemaException e){
                e.setSource(this);
                throw e;
            }
            if (refAttribute == null) 
                throw new SchemaException(this,
                                          SchemaMessages.getMessages("undefine"),
                                          new String[] {"attribute",this.refName});
            refAttribute.realize(aS);
            if (!refAttribute.isCoherent) return;
        }

      // on realize le datatype
        else {
            //solve the refe
            if (datatypeRef != null) 
                datatypeRef.realize(aS);            
            if (getDatatype() == null)
                setDatatype(aS.get_AnySimpleType());
                /*             throw new SchemaException(this,
                               SchemaMessages.getMessages("undefine type"),
                               new String[] {getNameNonNull()});
                */
            getDatatype().realize(aS); 
        }
        if (getSchema()== null)
            setSchema(aS);
        if (getName()!= null) isCoherent = true; //TODO ne marche pas dans le cas des restrictions
    }

    public void writeRawSchema(AnonymousTypeRawSchemaConstructor atsc) throws SchemaException{
        if (!isCoherent) 
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("inconsistencty"));
        else {
            atsc.write(" " 
                       + (!isAReference()?getRawName():getRefAttribute().getRawName()) 
                       + (required()? "[1,1]":"[0,1]")
                       + "{"+getRawType(atsc)+"}");
        }
    }
    // give the type and generate one if it doesn't exist.
    String getRawType(AnonymousTypeRawSchemaConstructor anct) throws SchemaException {
        //in case of refAttribute
        if (refAttribute != null) return refAttribute.getRawType(anct);
        if (getDatatype().getName()== null)

            return getOriginalRawPrefix()+anct.constructRawSchema(getDatatype(),null);
        return getDatatype().getRawName();                                       
    }


         
    ///////////////////////////////////////
    // RESTRICTION
    ///////////////////////////////////////

    public void mergeWith(Attribute a) throws SchemaException {
        // c'est a qui est le plus contraint et self la valeur original
        //        System.out.println("on merge "+a.getQName() + " avec "+ getQName());
        
        if (getDatatype()==null) {
            this.setDatatype(a.getDatatype());
            this.setDatatypeRef(a.getDatatypeRef());
            isCoherent = true;
        }
        setRestrictionSchema(a.getRestrictionSchema());
     //TODO fair ela verification de tous ce qui est possible
    }


      

    public String getTypeToString(){
        if (getDatatypeRef() != null) 
            return getDatatypeRef().toString();
        if (getDatatype() != null) 
            return getDatatype().toString();
        return "unknown";
    }
}// Attribute
