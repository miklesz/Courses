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

import java.util.*;
import org.xml.sax.Locator;             //juste pour ecrire l'erreur
import com.expway.util.LocalHandler;    //juste pour ecrire l'erreur

import com.expway.schema.utils.ApplicationObjectContainer;


/**
 * General class 
 *
 * @author Cedric thienot & Claude Seyrat
 */



abstract public class SchemaObject 
    implements ApplicationObjectContainer,SchemaObjectInterface{

    String name;
    boolean isCoherent = false;
    // true if it is realized even if send an error
    boolean isAlreadyRealized = false;

    Schema schema;

    // could be null
    /**
     * return the schema 
     * if the object is a reference it return the schema of the reference
     */
    public Schema getSchema(){
        return schema;
    }

    public void setSchema(Schema aS){schema = aS;}


    Schema restrictionSchema;
    
    /**
       * Get the value of restrictionSchema. if == null get the schema
       * @return Value of restrictionSchema.
       */
    public Schema getRestrictionSchema() {
        if (restrictionSchema != null)
            return restrictionSchema;
        return getSchema();
    }
    
    /**
       * Set the value of restrictionSchema. in case of restriction of a type containing this element
       * @param v  Value to assign to restrictionSchema.
       */
    public void setRestrictionSchema(Schema  v) {this.restrictionSchema = v;}


    public SchemaObject() {}
   
    public SchemaObject(String aName) {
        name = aName;
    }
   
    HashMap theMap = new HashMap();
    public Object  getApplicationObject(String name)          { return theMap.get(name); }
    public void    setApplicationObject(String name,Object o) { theMap.put(name,o); }
    public boolean containsApplicationObject(String name)     { return theMap.containsKey(name); }
   
    /***********************************************/
    /*               ACCESSING                     */
    /***********************************************/

    public boolean isAnonymous(){return name==null;}

    Annotation annotation;
    
    /**
       * Get the value of annotation.
       * @return Value of annotation.
       */
    public Annotation getAnnotation() {return annotation;}
    
    /**
       * Set the value of annotation.
       * @param v  Value to assign to annotation.
       */
    public void setAnnotation(Annotation  v) {this.annotation = v;}
    
    public String getQName(){
        if (isAnonymous()) return null;
        if (getSchema()==null) return getName();
        return getSchema().getTargetNamespace()+"/"+getName();
    }

    /**
     * return mpeg7:Name
     */
    public String getPrefixName(){
        if (isAnonymous()) return null;
        if (getSchema()==null) return getName();
        // in case of unprefix schema xmlns="URI"
        if (getSchema().getTargetNamespacePrefix()== null) return getName();
        return getSchema().getTargetNamespacePrefix()+":"+getName();
    }


    public String getName(){return name;}
    public String getNameNonNull(){
        if (name == null) return "unnamed element";
        return name;
        
    }
    public void setName(String name){
        this.name = name;
    }
   
    // RAW
    // c'est horrible mais c'est pour avoir le nom local du schema dans un contexte particulier
    public String getLocalRawPrefix(){
        return Schema.CURRENTSCHEMA.getProvisionalRawSchemaKey()+"\"";
    }

    public String getLocalRawName(){
        return getLocalRawPrefix()+getNameWithoutPrefix();

    }

    public String getRawName(){
        return getRawPrefix()+getNameWithoutPrefix();
    }

    // on supprime le prefix
    public String getNameWithoutPrefix(){
        if (getName() == null) return null;
        int index = getName().lastIndexOf(":");
        return getName().substring(index+1);
    }

    // sasn prendre en compte la presence d'une restriction utilise pour les types anonymes
    public String getOriginalRawPrefix(){
        if (schema == null) return getLocalRawPrefix();
        return schema.getProvisionalRawSchemaKey()+"\"";
    }

    public String getRawPrefix(){
        if (getRestrictionSchema() == null) return getLocalRawPrefix();
        return getRestrictionSchema().getProvisionalRawSchemaKey()+"\"";
    }

    // FIN deRAW

    public abstract void realize(Schema s) throws SchemaException;
    public boolean isConsistent() {return isCoherent;};

    //
    //REFERENCE

    boolean isAReference = false;
    /**
     * to implemented by each class who could be a reference
     */
    public SchemaObject getReference(){
        return this;
    }

    /**
     * return if the element is a reference or the element it self
     */

    public boolean isAReference(){
        return isAReference;
    }

    public String location(){
        StringBuffer str = new StringBuffer();
        Locator l = (Locator)getApplicationObject(LocalHandler.NAME_LOCATOR);
        if (l != null){            //TODO to be removed
            str.append(" ");
            str.append((l.getSystemId()!=null?l.getSystemId():""));
            str.append(" ligne :");
            str.append(l.getLineNumber());
            str.append(" col :");
            str.append(l.getColumnNumber());
        }
        return str.toString();
    }

    // HTML
    public static String HTMLStyle="txtblack";
   
} // SchemaObject


















