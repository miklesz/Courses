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




import java.util.Hashtable;

import com.expway.schema.instance.Type;

import com.expway.schema.instance.Element;
import com.expway.schema.instance.Schema;
import com.expway.schema.instance.Attribute;
import com.expway.schema.instance.AttributeList;
import com.expway.schema.instance.GlobalGroup;
import com.expway.schema.instance.SchemaException;
import org.xml.sax.helpers.NamespaceSupport;


import com.expway.util.LocalHandler;
import com.expway.util.HandlerException;

/**
 * SchemaHandler
 *
 *
 * Created: Wed Aug 09 13:10:44 2000
 *
 * @author Claude Seyrat & Cedric Thienot
 * @version 1.0
 */
 

public class SchemaHandler extends StaticLocalHandler {

    Schema schema ;

    public SchemaHandler () {
 
    }
 
    public Schema getSchema(){return schema;}
    //implementation de com.expway.schema.LocalHandler

    
    public  void informEnd(LocalHandler son) throws HandlerException{
             // dans le cas d'un default Handler$
        if (son.getCreation()== null)
                return; 

        String sonClassName = son.getClass().getName();
        try{
            if (sonClassName.equals(SchemaRegister.CLASS_ELT_SIMPLETYPE))
                schema.addType((Type)son.getCreation());
            else if (sonClassName.equals(SchemaRegister.CLASS_ELT_COMPLEXTYPE))
                schema.addType((Type)son.getCreation());
            else if (sonClassName.equals(SchemaRegister.CLASS_ELT_ELEMENT))
                schema.addElement((Element)son.getCreation());
            else if (sonClassName.equals(SchemaRegister.CLASS_ELT_ATTRIBUTE))
                schema.addAttribute((Attribute)son.getCreation());
            else if (sonClassName.equals(SchemaRegister.CLASS_ELT_ATTRIBUTEGROUP))
                schema.addAttributeGroup((AttributeList)son.getCreation());
            else if (sonClassName.equals(SchemaRegister.CLASS_ELT_GLOBALGROUP))
                schema.addGroup((GlobalGroup)son.getCreation());
            else if (sonClassName.equals(SchemaRegister.CLASS_ELT_IMPORT))
                schema.setImport((String[])son.getCreation());
            else if (sonClassName.equals(SchemaRegister.CLASS_ELT_INCLUDE))
                schema.setInclude((String)son.getCreation());
        } catch (SchemaException e){
            throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_ERROR);
        }         
    }
    
    public  void init(String uri, String local, String raw, Attributes attrs) 
        throws HandlerException {
        try{
            SchemaRegister.register();
            schema = new Schema();
            schema.initialize();
            // the targetNamespace value
            String value = attrs.getValue(SchemaSymbols.ATT_TARGETNAMESPACE);
            if (value == null) {//TODO
                //                System.out.println("NO TARGET Namespace");
                value ="";
            }
            schema.setTargetNamespace(value);
            schema.setTargetNamespacePrefix(ns.getPrefix(value));
            value = attrs.getValue(SchemaSymbols.ATT_ELEMENTFORMDEFAULT);
            schema.setElementFormDefault(value);
            value = attrs.getValue(SchemaSymbols.ATT_ATTRIBUTEFORMDEFAULT);
            schema.setAttributeFormDefault(value);
        }catch (SchemaException e){                     //TODO
            throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_ERROR);
        }
    }

    public void setSchemaFileName(String fileName){schema.setFileName(fileName);}    
    public void reset(){} 

    //TODO
    public void characters(char[] ch, int start, int length){};
    
    public void end(){
    };

    public Object getCreation(){return this;}

    /**
     * @deprecated
     */
    /*    public void setNameSupport(NamespaceSupport myNamespaceSupport){
        schema.setNameSupport(myNamespaceSupport);
    }
    */
}// SchemaHandler
 
