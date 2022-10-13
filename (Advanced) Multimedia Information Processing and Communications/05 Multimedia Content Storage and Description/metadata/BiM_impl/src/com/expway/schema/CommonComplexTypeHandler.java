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

import java.util.Collection;
import java.util.ArrayList;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.LocatorImpl;

import com.expway.schema.instance.Attribute;
import com.expway.schema.instance.AttributeList;
import com.expway.schema.instance.ComplexType;
import com.expway.schema.instance.SchemaException;
import com.expway.schema.instance.GroupElement;
import com.expway.schema.instance.Annotation;

import com.expway.util.LocalHandler;
import com.expway.util.HandlerException;

/**
 * CommonComplexTypeHandler
 *
 *
 * Created: Wed Aug 09 13:10:44 2000
 *
 * @author 
 * @version
 */
 

abstract public class CommonComplexTypeHandler extends StaticLocalHandler {
   
    ComplexType complexType;

    public CommonComplexTypeHandler () {
 
    }

    //in case of Enumeration
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


    public  void informEnd(LocalHandler son) throws HandlerException{
        try{
        if (son.getCreation()== null) 
            return; //in case of Defautl LocalHandler

            String className= son.getClass().getName();
            if (className.equals(SchemaRegister.CLASS_ELT_ANNOTATION))
                complexType.setAnnotation((Annotation)son.getCreation()); 
            // in case of facet 
            else if (className.equals(SchemaRegister.CLASS_ELT_FACET)){
                String [] res = (String[])son.getCreation();
                if (res[0].equals(SchemaSymbols.ELT_ENUMERATION))
                    getListOfPossibleValue().add(res[1]);
                else //others cases
                    complexType.addFacet(res[0],res[1]);
            }
            // attribute 
            else if (className.equals(SchemaRegister.CLASS_ELT_ATTRIBUTE))
                complexType.add((Attribute)son.getCreation());
            else if (className.equals(SchemaRegister.CLASS_ELT_ATTRIBUTEGROUP))
                complexType.addAttributeGroup((AttributeList)son.getCreation());
            else if (className.equals(SchemaRegister.CLASS_ELT_ELEMENT) 
                     || className.equals(SchemaRegister.CLASS_ELT_GROUP)
                     || className.equals(SchemaRegister.CLASS_ELT_ANY)
                     || className.equals(SchemaRegister.CLASS_ELT_GLOBALGROUP)){
                
                complexType.addGroupElement((GroupElement)son.getCreation());
            }
            else if (son.getCreation()== null)
                return; // dans le cas d'un default Handler$
            else {
                System.out.println("illegal adding in complexType of "+ className);
                System.out.println(locatorToString());
            }
        }catch (SchemaException e){                     
            throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_ERROR);
        }
    }
    
    public void reset(){} 

    //TODO
    public void end() throws HandlerException {
        try{
            // in case of Enumeration
            if (listOfPossibleValue != null)
                complexType.addEnumerationFacet(getListOfPossibleValue());                
        }catch(SchemaException e){
            throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_ERROR);
        }
    }
        
    public Object getCreation(){ 
        return complexType;
    }

    //TODO
    public  void characters(char[] ch, int start, int length){
        //TODO envoyer une erreur
    }

 
}// CommonComplexTypeHandler
 
