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

import java.util.*;

import com.expway.schema.instance.*;

import com.expway.util.LocalHandler;
import com.expway.util.HandlerException;

/**
 * SimpleTypeHandler
 *
 *
 * Created: Wed Aug 09 13:10:44 2000
 *
 * @author 
 * @version
 */
 

public class SimpleTypeHandler extends StaticLocalHandler {


    
    DataType simpleType;
    String name;
    String abstractString;
    Annotation annotation;

    //    static java.util.Map classHandlerMap;
    public SimpleTypeHandler () { 
    }


    public  void informEnd(LocalHandler son) throws HandlerException {
        if (son.getCreation()== null) 
            return; //in case of Defautl LocalHandler

        String className= son.getClass().getName();
        if (className.equals(SchemaRegister.CLASS_ELT_ANNOTATION)){
            annotation= (Annotation)son.getCreation();
            return; 
        }
        if (className.equals(SchemaRegister.CLASS_ELT_RESTSIMPLE)){
            simpleType = (DataType)son.getCreation();
            initSimpleType();
            return;
        }
        if (className.equals(SchemaRegister.CLASS_ELT_LIST)){
            simpleType=(DataType)son.getCreation();
            initSimpleType();
            return;
        }
        if (className.equals(SchemaRegister.CLASS_ELT_UNION)){
            simpleType = (DataType)son.getCreation();
            initSimpleType();
            return;
        }
    }

    public void initSimpleType() throws HandlerException {
        try{
            simpleType.setName(name);
            simpleType.setAbstract(abstractString);
        }catch (SchemaException e){                     //TODO
            throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_ERROR);
        }
    }

    public  void init(String uri, String local, String raw, Attributes attrs) throws HandlerException{
         name = attrs.getValue(SchemaSymbols.ATT_NAME);
         // abstract
         abstractString =  attrs.getValue(SchemaSymbols.ATT_ABSTRACT);
    }

    public void reset(){
        simpleType = null; 
    } 

    public void end() throws HandlerException {
    }

    public Object getCreation(){
        simpleType.setAnnotation(annotation);
        return simpleType;}

    public  void characters(char[] ch, int start, int length){
        //TODO envoyer une erreur
    }

}// SimpleTypeHandler
 
