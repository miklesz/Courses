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


/**
 * An object that refers to an mpeg7 object
 * A reference realized contains a valid handler
 *
 * @author Claude Seyrat & Cedric thienot
 */

abstract public class Ref extends SchemaObject {

   SchemaObject myHandler = null;
   
   public Ref(String aName) {
      super(aName);	
   }
    /**
    * Get the Object Handler referenced by it
    * @return the Handler. null if not realized.
    */
   final public SchemaObject getTarget() {return myHandler;}
   final public SchemaObject getTarget(Schema s) throws SchemaException {
      if (myHandler==null) realize(s);
      return myHandler;
   }
   
   /**
    * Set the value of the Handler.
    * @param v  Value to assign to myDSHandler.
    */
   final void setHandler(SchemaObject  v) {this.myHandler = v;}
   
} // Ref






