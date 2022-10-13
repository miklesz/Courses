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

package com.expway.util;

import org.xml.sax.Attributes;

/**
 * DefaultLocalHandler.java
 *
 *
 * Created: Wed Aug 09 18:56:06 2000
 *
 * @author 
 * @version
 */

public class DefaultLocalHandler extends LocalHandler {
    public DefaultLocalHandler () {
        
    }
    public  void informEnd(LocalHandler son)
        throws HandlerException {}
    
    public void init(String uri, String local, String raw, Attributes attrs)
        throws HandlerException {}
    
    public LocalHandler getSonHandler(String uri, String local, String raw) 
        throws HandlerException {
        return new DefaultLocalHandler();
    }
    
    public boolean hasPoolHoldByHandlerManager() {return false;}
    
    public void reset() {} 

    //TODO
    public void end() throws HandlerException {}
    //TODO
    public Object getCreation() throws HandlerException { 
        return null;
    }

    //TODO
    public  void characters(char[] ch, int start, int length) throws HandlerException {
        //TODO envoyer une erreur
    }

}// DefaultLocalHandler
