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
import java.util.HashSet;


/**
 * ListType.java
 *
 * type
 *
 * Created: Mon Aug 28 11:33:22 2000
 *
 * @author Claude Seyrat & Cedric Thienot
 * @version 1.0
 */


public class ListType extends DataType {

    // debugging
    final static public boolean DEBUG = false;

    final static String[] FACET_LIST = {
        "enumeration", "length", "maxLength", "minLength", "pattern", "whiteSpace"};




    // collection of direct sub class
    DataType base;    

    public ListType () {
        super();
        initialisePossibleFacet();
        setAsAList();
    }

    public void initialisePossibleFacet() {
        possibleFacetNames = new HashSet();
        for (int i = 0; i< FACET_LIST.length; i++)
            possibleFacetNames.add(FACET_LIST[i]);
    }

    /**
     * on ajoute une reference a un type globale
     */
    public void setType(String name){
        base = new DataType(name);
        base.setRefName(name);
    }
    /**
     * on ajoute labase
     */
    public void setType(DataType aDT){
        base = aDT;
    }
    /**
     * return a list of all the possible type
     */
    public DataType getType(){return base;}
    /**
     * return a list of all the possible type
     */

    public  DataType getListItem(){
        return getBaseType();}

    /**
     * return a list of all the possible type
     */

    public DataType getBaseType(){
        return getType();
    }


    /**
     * Do NOTHING
     */
    public void setDerived(String name) throws SchemaException {}

    /**
     * Do NOTHING
     */
    public String  getDerived(){return null;}
    /**
     * Do NOTHING
     */
    public Type getSuperType(){return null;}

    public boolean isSimple(){return true;}
    /////////////////////////////////////////////////////////////

    // RAW SCHEMA
    ////////////////////////////////////////////////////////////


    /**
     * realize
     */
    public void realize(Schema s) throws SchemaException {
        if (isAlreadyRealized) return;
        isAlreadyRealized = true;
        if (ListType.DEBUG) System.out.println("");
        if (ListType.DEBUG) System.out.print("====> REALIZE LIST " + getName());
        // realize my BaseType
 
        if (ListType.DEBUG) System.out.println("");
        
        // on realize la base de la list
        if (ListType.DEBUG) System.out.println("....> realize basetype of " + getName() + " : " + base.getName());
        try {
            base.realize(s);
            //          if (ListType.DEBUG) System.out.println("....> realize Inheritance of " + getName());
            ///            realizeInheritance(s);
            if (ListType.DEBUG) System.out.println("....> check facets of " + getName());
            checkPossibleFacets(s);
        }
        catch(SchemaException se){
            s.getErrorHandler().schemaError(new SchemaException(this,se.getMessage()));
        }
        if (ListType.DEBUG) System.out.println(this);
        isCoherent = true;
    }



    String getRawType(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException{
        return ((base.getName() == null)?getLocalRawPrefix()+atrsc.constructRawSchema(base,null):base.getRawName());
    }    

    void writeContentToRawSchema(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException{
        String s =" ";    
        s += "(base "+ getRawType(atrsc) +")";
        s +=writeContentToRawSchemaForList();
        atrsc.write(s);

    }  
    
    public boolean hasFacet(){
        if (myPrimitiveDataType != null)
            return !currentFacets.isEmpty();
        return false;
    }


}
