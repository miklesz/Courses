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

import java.util.Collection;
import java.util.ArrayList;

import com.expway.schema.instance.*;

import com.expway.util.LocalHandler;
import com.expway.util.HandlerException;

/**
 * SimpleTypeListHandler
 *
 *
 * Created: Wed Aug 09 13:10:44 2000
 *
 * @author 
 * @version
 */
 

public class SimpleTypeListHandler extends StaticLocalHandler {


    
    ListType simpleType;

    //    static java.util.Map classHandlerMap;
    public SimpleTypeListHandler () {
    }
 

    Collection listOfPossibleValue;
    
    /**
     * Get the value of listOfPossibleValue.
     * @return Value of listOfPossibleValue.
     */
    public Collection getListOfPossibleValue() {
        if (listOfPossibleValue == null)
            listOfPossibleValue = new ArrayList();
        return listOfPossibleValue;
    }
    
    /**
     * Set the value of listOfPossibleValue.
     * @param v  Value to assign to listOfPossibleValue.
     */
    public void setListOfPossibleValue(Collection  v) {this.listOfPossibleValue = v;}
    

    //implementation de com.expway.schema.LocalHandler


    public  void informEnd(LocalHandler son) throws HandlerException {
        String className= son.getClass().getName();
        if (className.equals(SchemaRegister.CLASS_ELT_ANNOTATION))
                return; //TODO
        if (className.equals(SchemaRegister.CLASS_ELT_SIMPLETYPE))
            simpleType.setType((DataType)son.getCreation());
    }

    public  void init(String uri, String local, String raw, Attributes attrs) throws HandlerException{
        String base=  attrs.getValue(SchemaSymbols.ATT_ITEMTYPE);
        //        String name = attrs.getValue(SchemaSymbols.ATT_NAME);

        // ## ADDED BY CLAUDE

        // ici on considere les simple types derives par list comme des listes ayant une facet base

        simpleType = new ListType();
        simpleType.setAsAList();
        if ( base != null) 
            simpleType.setType(getResolvedName(base));

        simpleType.setApplicationObject(NAME_LOCATOR,new LocatorImpl(locator));
        // ## FIN ADDED BY CLAUDE
    }

    public void reset(){
        simpleType = null; 
    } 

    public void end() throws HandlerException {
    }

    public Object getCreation(){return simpleType;}

    public  void characters(char[] ch, int start, int length){
        //TODO envoyer une erreur
    }

}// SimpleTypeListHandler
 
