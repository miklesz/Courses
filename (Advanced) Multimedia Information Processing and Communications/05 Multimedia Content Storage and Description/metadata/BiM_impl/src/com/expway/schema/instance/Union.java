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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashSet;




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


public  class  Union extends DataType {

    // collection of direct sub class
    protected List collectionType = new ArrayList();

   final static String[] FACET_UNION = {
        "enumeration"};


    
    public Union (String aN) {
        super(aN);
        initialisePossibleFacet();
    }

    public void initialisePossibleFacet() {
        possibleFacetNames = new HashSet();
        for (int i = 0; i< FACET_UNION.length; i++)
            possibleFacetNames.add(FACET_UNION[i]);
    }
    /**
     * on ajoute une reference a un type globale 
     * peut etre jamais utilise TODO
     */
    public void addType(String name){
        DataType aDT = new DataType(name);
        aDT.setRefName(name);
        collectionType.add(aDT);
    }
    /**
     * return a list of all the possible type
     */
    public List getTypes(){return collectionType;}
    public List getUnionTypes(){
        return getTypes();
        }


    /**
     * on ajoute un type qu'il faudra realiser ulterieurement
     */
    public void addType(DataType aT) {
        collectionType.add(aT);
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


    /**
     * realize
     */

    public void realize(Schema aS) throws SchemaException{
        if (isAlreadyRealized) return;
        isAlreadyRealized = true;
        Iterator i = collectionType.iterator();
        while (i.hasNext())
            ((DataType)i.next()).realize(aS);;
        isCoherent = true;
    }
    
    
    public String facetToHTML(){return "";}
    // RAW SCHEMA
    ////////////////////////////////////////////////////////////

    void writeContentToRawSchema(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException{
        
        String s =" ";    
        Iterator i = collectionType.iterator();
        s += "(base";
        while (i.hasNext())
            //            s += ((DataType)i.next()).getRawName()+" "; 
            s += " "+getRawType(atrsc,(DataType)i.next());
        atrsc.write(s+")");
    }

    String getRawType(AnonymousTypeRawSchemaConstructor atrsc, DataType adt) throws SchemaException{
        return ((adt.getName() == null)?getLocalRawPrefix()+atrsc.constructRawSchema(adt,null):adt.getRawName());
    }    

                   


}
