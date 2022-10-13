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


import org.xml.sax.Attributes;
import org.xml.sax.helpers.LocatorImpl;

import com.expway.schema.instance.Attribute;
import com.expway.schema.instance.ComplexType;
import com.expway.schema.instance.SchemaException;

import com.expway.util.HandlerException;
import com.expway.util.LocalHandler;

/**
 * ComplexTypeHandler
 *
 *
 * Created: Wed Aug 09 13:10:44 2000
 *
 * @author 
 * @version
 */
 

public class ComplexTypeHandler extends CommonComplexTypeHandler{
   

    public ComplexTypeHandler () {
 
    }
    
    public  void init(String uri, String local, String raw, Attributes attrs)
        throws HandlerException {
            // the name, it could be null TODO
        try{
            complexType = new ComplexType(attrs.getValue(SchemaSymbols.ATT_NAME));
            complexType.setAbstract(attrs.getValue(SchemaSymbols.ATT_ABSTRACT));
            complexType.setMixed(attrs.getValue(SchemaSymbols.ATT_MIXED));
            complexType.setApplicationObject(NAME_LOCATOR,new LocatorImpl(locator));
        }catch (SchemaException e){                     //TODO
            throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_ERROR);
        }
    }
    // on recuper l'ancien complexType toutes ses valeurs et on met a jour le nouveau

    public void setComplexType (ComplexType aCT){
           aCT.setName(complexType.getName());
           aCT.setAbstract(complexType.isAbstract());
           complexType = aCT;
    }

    public  void informEnd(LocalHandler son) throws HandlerException{
        if (son.getCreation()== null)
                return; // dans le cas d'un default Handler$
       String className= son.getClass().getName();
       if (className.equals(SchemaRegister.CLASS_ELT_COMPLEXCONTENT)) {
           // on remplace le complex type par celui contenu et on met a jour le nom
           setComplexType((ComplexType)son .getCreation());
       }
       else if (className.equals(SchemaRegister.CLASS_ELT_SIMPLECONTENT)) {
           // on remplace le complex type par celui contenu et on met a jour le nom
           setComplexType( (ComplexType)son .getCreation());
       }
      else 
          super.informEnd(son);
    }
 
}// ComplexTypeHandler
 
