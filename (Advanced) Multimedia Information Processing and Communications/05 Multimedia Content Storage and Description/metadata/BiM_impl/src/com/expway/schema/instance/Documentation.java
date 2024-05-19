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

package com.expway.schema.instance;

import java.util.*;


/**
 * Documentation 
 *
 * @author Cedric thienot & Claude Seyrat
 */



public class Documentation extends SchemaObject {

    //unusefull for the moment
    public  void realize(Schema s) throws SchemaException{}

    String value = "";
    
    /**
     * Get the value of value.
     * @return value of value.
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Set the value of value.
     * @param v  Value to assign to value.
     */
    public void setValue(String  v) {
        this.value = v;
    }
    
    String source;
    
    /**
       * Get the value of source.
       * @return Value of source.
       */
    public String getSource() {return source;}
    
    /**
       * Set the value of source.
       * @param v  Value to assign to source.
       */
    public void setSource(String  v) {this.source = v;}
    
   
} // Documentation


















