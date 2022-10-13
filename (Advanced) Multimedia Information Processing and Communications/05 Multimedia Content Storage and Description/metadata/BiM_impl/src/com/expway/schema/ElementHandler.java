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

import java.util.Hashtable;

import com.expway.schema.instance.Element;
import com.expway.schema.instance.Type;
import com.expway.schema.instance.SchemaException;
import com.expway.schema.instance.Annotation;

import com.expway.util.LocalHandler;
import com.expway.util.HandlerException;

/**
 * ElementHandler
 *
 *
 * Created: Wed Aug 09 13:10:44 2000
 *
 * @author Claude Seyrat & Cedric Thienot
 * @version 1.0
 */
 

public class ElementHandler extends StaticLocalHandler {

    Element element;

    public ElementHandler () {
 
    }
 
    public boolean hasPoolHoldByHandlerManager(){return true;}
    //implementation de com.expway.schema.LocalHandler


    public  void informEnd(LocalHandler son) throws HandlerException{
        // dans le cas d'un default Handler$
        if (son.getCreation()== null)
                return; 
        String className= son.getClass().getName();
        if (className.equals(SchemaRegister.CLASS_ELT_ANNOTATION))
            element.setAnnotation((Annotation)son.getCreation()); 
        //  type
        else if (className.equals(SchemaRegister.CLASS_ELT_COMPLEXTYPE) || 
                 className.equals(SchemaRegister.CLASS_ELT_SIMPLETYPE))     
            element.setType((Type)son.getCreation());
        else {
            System.out.println("illegal adding in element of "+ className);
            System.out.println(locatorToString());
        }
    }
    
    public  void init(String uri, String local, String raw, Attributes attrs) throws HandlerException {
        String s;
        try{
            // the name, it could be null
            element = new Element(attrs.getValue(SchemaSymbols.ATT_NAME));
            // the typeRef it could be null
            element.setTypeRef(getResolvedName(attrs.getValue(SchemaSymbols.ATT_TYPE)));
            // the element can be defined with ref
            element.setRefName(getResolvedName(attrs.getValue(SchemaSymbols.ATT_REF)));
            // the equivclass
            s = getResolvedName(attrs.getValue(SchemaSymbols.ATT_SUBSTITUTION));
            if (s != null)
                element.setSubstitutionName(s);
            // the min Occur
            String value = attrs.getValue(SchemaSymbols.ATT_MINOCCURS);
            if (value != null) 
                element.setMinOccur(value);
            value = attrs.getValue(SchemaSymbols.ATT_MAXOCCURS);
            if (value != null) 
                element.setMaxOccur(value);
            // set the locator
            element.setApplicationObject(NAME_LOCATOR,new LocatorImpl(locator));
        }catch (SchemaException e){                     //TODO
            throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_ERROR);
        }
    }

    public void reset(){} 
    //TODO
    public void end()throws HandlerException{}

    public Object getCreation()throws HandlerException{ 
        return element;
    }

    //TODO
    public  void characters(char[] ch, int start, int length)throws HandlerException{
        //TODO envoyer une erreur
    }


}// ElementHandler
 
