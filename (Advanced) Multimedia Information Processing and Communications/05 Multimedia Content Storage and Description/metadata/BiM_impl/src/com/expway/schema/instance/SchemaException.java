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

package com.expway.schema.instance;

import org.xml.sax.Locator;             //juste pour ecrire l'erreur
import com.expway.util.LocalHandler;

/**
 * SchemaException.java
 *
 * @author Cedric thienot & Claude Seyrat
 *
 */

public class SchemaException  extends Exception{
    
    //////////////////////////////
    // ERROR

    //    final static String DUPLICATE = "Duplicate definition for ";
    //    final static String UNDEFINEDELEMENT = "No definition for ";
    //    final static String INVALIDVALUEMIN ="Invalid value for min Occurence ";
    //    final static String UNDEFINETYPE ="No type define for ";
    //    final static String UNEXPECTEDVALUE ="is an unexpected value ";

    //    final static String INCONSISTENT = "the object is not consistent ";

    SchemaObjectInterface theSource = null;

   public SchemaException(String e) {
      super(e);	
   }

   public SchemaException(SchemaObjectInterface source,String e) {
	super(e);
	setSource(source);
   }

    public SchemaException(SchemaObjectInterface source,String e, String[] args){
        this (source,java.text.MessageFormat.format(e,args));
    }
    public SchemaException(String e, String[] args){
        this (null,java.text.MessageFormat.format(e,args));
    }

    public void setSource(SchemaObjectInterface s){theSource = s;}

    public SchemaObjectInterface getSource(){return theSource;}
    

    public String getMessage(){
        StringBuffer str = new StringBuffer(20);
        str.append(theSource);
        str.append((super.getMessage()!=null?super.getMessage()+"\n":""));
        if (theSource!=null){
            Locator l = (Locator)theSource.getApplicationObject(LocalHandler.NAME_LOCATOR);
            if (l != null){            //TODO to be removed
                str.append("in ");
                str.append((l.getSystemId()!=null?l.getSystemId():""));
                str.append(" ligne :");
                str.append(l.getLineNumber());
                str.append(" col :");
                str.append(l.getColumnNumber());
                str.append("\n");
            }
            
        }
        return str.toString();
    }
} // SchemaException
