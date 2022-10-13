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

public class AppInfo extends SchemaObject {

    ArrayList listOfFacets;

    public ArrayList getFacets(){
        if (listOfFacets == null)
            listOfFacets = new ArrayList();
        return listOfFacets;
    }

    /**
     * add a has facet from the schema of schema
     */

    public void addFacet(String facet){
        getFacets().add(facet);
    }

    ArrayList listOfProperties;

    public ArrayList getProperties(){
        if (listOfProperties == null)
            listOfProperties = new ArrayList();
        return listOfProperties;
    }

    /**
     * add a property from the schema of schema
     */

    public void addProperty(String property, String value){
        String[] re = {property,value};
        getProperties().add(re);
    }
    ////////////////////////////////////
    // ERALIZE
    public void realize(Schema aS){}
    
 
}// Element
