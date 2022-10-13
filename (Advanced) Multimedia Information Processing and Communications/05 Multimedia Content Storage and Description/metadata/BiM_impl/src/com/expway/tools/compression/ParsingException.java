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

package com.expway.tools.compression;

import org.xml.sax.Locator;             //juste pour ecrire l'erreur

public class ParsingException extends Exception {

    Locator theLocator;
    
    /**
       * Get the value of theLocator.
       * @return Value of theLocator.
       */
    public Locator getLocator() {return theLocator;}
    
    /**
       * Set the value of theLocator.
       * @param v  Value to assign to theLocator.
       */
    public void setLocator(Locator  v) {this.theLocator = v;}

    // 

    public ParsingException()            { super();}
    public ParsingException(String str)  { super(str);}

    // 

    public String getMessage(){
        StringBuffer str = new StringBuffer(20);

        if (theLocator != null){            //TODO to be removed
            str.append(theLocator.getSystemId());
            str.append(" L");
            str.append(theLocator.getLineNumber());
            str.append(" C");
            str.append(theLocator.getColumnNumber());
            str.append(" : ");
        } else str.append (" \n Unknown position : ");
        str.append(super.getMessage());
        str.append("\n");

        return str.toString();
    }
}
