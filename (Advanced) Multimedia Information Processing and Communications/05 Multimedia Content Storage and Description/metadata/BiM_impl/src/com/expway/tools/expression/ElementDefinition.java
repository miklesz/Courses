/***********************************************************************
This software module was originally developed by C�dric Thi�not (Expway)
Claude Seyrat (Expway) and Gr�goire Pau (Expway) in the course of 
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

Copyright Expway � 2001.
************************************************************************/

package com.expway.tools.expression;

import java.io.*;
import java.util.*;

public class ElementDefinition {

    String theElementName = null;
    String theTypeName = null;

    TypeDefinition theTypeDefinition= null;

    public ElementDefinition(String elementname,String typename){
        theElementName =elementname;
        theTypeName = typename;
    }
    
    public String getName(){
        return theElementName;
    }

    public TypeDefinition getTypeDefinition(){
        return theTypeDefinition;
    }

    public void realize(TypeDefinitions theDefinitions) throws DefinitionException {
        //System.out.println("realize ElementDefinition of elt name:" + theElementName + " and type:" + theTypeName);

        if (theTypeName == null)
            throw new DefinitionException("Referenced type " + theTypeName + " not found");

        theTypeDefinition = theDefinitions.getTypeDefinition(theTypeName);
        
        if (theTypeDefinition == null)
            throw new DefinitionException("Referenced type " + theTypeName + " not found");
    }

    public void print(){
        System.out.println("Element of type " + theTypeName + " " + (theTypeDefinition==null?"(not realized)":""));
    }

    public String toString(){return "Element Definition ( elt="+theElementName+" type="+theTypeName+")";}
}
