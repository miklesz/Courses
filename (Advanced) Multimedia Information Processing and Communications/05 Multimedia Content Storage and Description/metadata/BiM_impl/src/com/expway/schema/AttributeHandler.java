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

import com.expway.schema.instance.Attribute;
import com.expway.schema.instance.DataType;
import com.expway.schema.instance.Annotation;
import com.expway.schema.instance.DataTypeRef;
import com.expway.schema.instance.SchemaException;

import com.expway.util.LocalHandler;
import com.expway.util.HandlerException;


/**
 * AttributeHandler
 *
 *
 * Created: Wed Aug 09 13:10:44 2000
 *
 * @author 
 * @version
 */
 

public class AttributeHandler extends StaticLocalHandler {

    Attribute attribute;

    public AttributeHandler () {
 
    }
 
    //implementation de com.expway.schema.LocalHandler


    public  void informEnd(LocalHandler son) throws HandlerException{
             // dans le cas d'un default Handler$
        if (son.getCreation()== null)
                return; 

        String className= son.getClass().getName();
        // inner definition of a subtype simpleType
        if (className.equals(SchemaRegister.CLASS_ELT_ANNOTATION))
            attribute.setAnnotation((Annotation)son.getCreation()); 
        else
            attribute.setDatatype((DataType)son.getCreation());
    }
    
    public  void init(String uri, String local, String raw, Attributes attrs) throws HandlerException{
        //NAME  || Ref
        try{
            String name= attrs.getValue(SchemaSymbols.ATT_NAME);
            if (name!=  null)
                attribute = new Attribute(name);
            else {
                attribute = new Attribute();
                attribute.setRefName(getResolvedName(attrs.getValue(SchemaSymbols.ATT_REF)));
            }
            attribute.setApplicationObject(NAME_LOCATOR,new LocatorImpl(locator));
            // REQUIRED
            String useString = attrs.getValue(SchemaSymbols.ATT_USE);
            if (useString == null) 
                //a verifier la valeur par defaut optional|required
                attribute.setUse(SchemaSymbols.ATTVAL_OPTIONAL); 
            else 
                attribute.setUse(useString);
            // Valeur par defaut:
            attribute.setValue(attrs.getValue(SchemaSymbols.ATT_VALUE));
            // Valeur fixe
            attribute.setValue(attrs.getValue(SchemaSymbols.ATT_FIXED));
            // DataTypeRef
            String typeString = getResolvedName(attrs.getValue(SchemaSymbols.ATT_TYPE));
            if (typeString != null){
                DataTypeRef adtr = new DataTypeRef(typeString);
                attribute.setDatatypeRef(adtr);
                adtr.setApplicationObject(NAME_LOCATOR,new LocatorImpl(locator));
            }
        }catch (SchemaException e){                     //TODO
            throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_ERROR);
        }
    }
    public void reset(){} 

    public void end(){}
    /**
     * return the object created
     * @return the value of the attribute.
     * @see com.expway.schema.instance.Attribute
     */
    public Object getCreation(){ 
        return attribute;
    }

    public  void characters(char[] ch, int start, int length){
        //TODO envoyer une erreur
    }

}// AttributeHandler
 
