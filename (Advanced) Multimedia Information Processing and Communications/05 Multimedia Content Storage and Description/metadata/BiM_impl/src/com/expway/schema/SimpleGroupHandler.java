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
import org.xml.sax.helpers.LocatorImpl;

import com.expway.schema.instance.Group;
import com.expway.schema.instance.GroupElement;
import com.expway.schema.instance.SchemaException;
import com.expway.schema.instance.Annotation;

import com.expway.util.LocalHandler;
import com.expway.util.HandlerException;

/**
 * SimpleGroupHandler
 *
 *
 * Created: Wed Aug 09 13:10:44 2000
 *
 * @author 
 * @version
 */
 

public class SimpleGroupHandler extends GroupHandler {

    Group group;   
    
    public  void init(String uri, String local, String raw, Attributes attrs) throws HandlerException {
        try{
            group = new Group(raw);
            String value = attrs.getValue(SchemaSymbols.ATT_MINOCCURS);
            if (value != null) 
                group.setMinOccur(value);
            value = attrs.getValue(SchemaSymbols.ATT_MAXOCCURS);
            if (value != null) 
                group.setMaxOccur(value);
            // set the locator
            group.setApplicationObject(NAME_LOCATOR,new LocatorImpl(locator));
       
        }catch (SchemaException e){                     //TODO
             throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_ERROR);
        }
 
    }

   public Object getCreation(){ 
        return group;
    }
    public  void informEnd(LocalHandler son) throws HandlerException{
        //TOD
        if (son.getCreation()== null) 
            return; //in case of Defautl LocalHandler
        String className= son.getClass().getName();
        if (className.equals(SchemaRegister.CLASS_ELT_ANNOTATION))
            group.setAnnotation((Annotation)son.getCreation()); 

        else
            group.addGroupElement((GroupElement)son.getCreation());
    }
}// GroupHandler
 
