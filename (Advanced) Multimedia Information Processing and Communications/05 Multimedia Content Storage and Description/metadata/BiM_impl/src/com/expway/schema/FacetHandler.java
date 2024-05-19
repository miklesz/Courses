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

package com.expway.schema;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import com.expway.util.LocalHandler;
import com.expway.util.HandlerException;


/**
 * FacetHandler
 *
 * the object created is a String[2]
 *              where [0] is the name of the facet
 *                and [1] is the value of the facet
 * Created: Wed Aug 09 13:10:44 2000
 *
 * @author 
 * @version
 */
 

public class FacetHandler extends StaticLocalHandler {
   
    String[] value = new String[2];

    public FacetHandler () {
 
    }
 
    //implementation de com.expway.schema.LocalHandler


    public  void informEnd(LocalHandler son){}
    
    public  void init(String uri, String local, String raw, Attributes attrs){

        value[0]= raw;
        value[1] = attrs.getValue(SchemaSymbols.ATT_VALUE);
        // TODO send a error
    }

    public void reset(){} 

    public Object getCreation(){return value;}

    public  void characters(char[] ch, int start, int length){
        //TODO envoyer une erreur
    }
    public void end(){};
 
 
}// FacetHandler
 
