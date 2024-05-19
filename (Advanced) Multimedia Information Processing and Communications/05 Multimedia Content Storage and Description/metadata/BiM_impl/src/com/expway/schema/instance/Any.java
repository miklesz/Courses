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


import java.util.Map;
import java.util.Iterator;
import com.expway.schema.utils.StringMap;

/**
 * Any.java
 *
 *
 * Created: Tue Sep 19 15:29:14 2000
 * TODO ANY avec les namespace
 * @author 
 * @version
 */

public class Any extends GroupElement {

    public Any(String a){
        super(a);
    }
    // contain all the possible elements
    
    private Map elements = new StringMap();



    public void writeRawSchema(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException{
        if (!isCoherent) 
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("inconsistencty"));
        atrsc.write("(" 
                    + RawSchemaAble.CHOICE_NAME // TODO  que le raw schema accepte ANY_NAME
                    + getRawOccurence()+" ");
        Iterator aI = elements.values().iterator();
        while (aI.hasNext())
            ((Element) aI.next()).writeRawSchema(atrsc);
        atrsc.write(")");
    }

    public void realize(Schema aS) throws SchemaException{
        elements= aS.getElements();
        isCoherent = true;
    }    
         
}// Any
