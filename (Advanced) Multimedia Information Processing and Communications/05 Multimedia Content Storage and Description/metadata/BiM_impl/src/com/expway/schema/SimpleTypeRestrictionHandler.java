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
 * SimpleTypeRestrictionHandler
 *
 *
 * Created: Wed Aug 09 13:10:44 2000
 *
 * @author 
 * @version
 */
 

public class SimpleTypeRestrictionHandler extends StaticLocalHandler {


    
    DataType simpleType;

    //    static java.util.Map classHandlerMap;
    public SimpleTypeRestrictionHandler () {
 
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
        if (className.equals(SchemaRegister.CLASS_ELT_SIMPLETYPE)){
            simpleType = (DataType) son.getCreation();
            return;
        }
        if (className.equals(SchemaRegister.CLASS_ELT_LIST)){
            simpleType = (DataType) son.getCreation();
            return;
        }
        if (className.equals(SchemaRegister.CLASS_ELT_UNION)){
            simpleType = (DataType) son.getCreation();
            return;
        }
        if (son.getCreation() == null)
            return; // DefaultLocalHandler
        // format for an facet String[2] where [0] is the name of the facet
        //                                     [1] is the value of the facet
        String [] res = (String[])son.getCreation();
        try {
            // in case of Enumeration
            if (res[0].equals(SchemaSymbols.ELT_ENUMERATION))
                getListOfPossibleValue().add(res[1]);
            else //others cases
                simpleType.addFacet(res[0],res[1]);
        }catch(SchemaException e){
            throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_ERROR);
        }
    }
    public  void init(String uri, String local, String raw, Attributes attrs) throws HandlerException{
        String base=  getResolvedName(attrs.getValue(SchemaSymbols.ATT_BASE));    
        if (base==null) return; //TODO renoie une rreur
        simpleType = new DataType("anonymous",base);
        simpleType.setApplicationObject(NAME_LOCATOR,new LocatorImpl(locator));
        // ## FIN ADDED BY CLAUDE
    }

    public void reset(){
        simpleType = null; 
        listOfPossibleValue = null; // to be garbaged
    } 

    public void end() throws HandlerException {
        try{
        // in case of Enumeration
            if (listOfPossibleValue != null)
                simpleType.addEnumerationFacet(getListOfPossibleValue());                
        }catch(SchemaException e){
            throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_ERROR);          
        }
    }

    public Object getCreation(){return simpleType;}

    public  void characters(char[] ch, int start, int length){
        //TODO envoyer une erreur
    }

}// SimpleTypeRestrictionHandler
 
