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

import org.xml.sax.Locator;             //juste pour ecrire l'erreur

/**
 * HandlerException.java
 *
 * can handle an exception generate during the parsing process
 *
 * @author Cedric thienot & Claude Seyrat
 *
 */

public class HandlerException  extends Exception {

    public static final String TYPE_FATAL = "Fatal";
    public static final String TYPE_ERROR = "Error";
    public static final String TYPE_WARNING = "Warning";
    
    String myType = TYPE_FATAL;

    public boolean isFatal(){ return myType.equals(TYPE_FATAL);}
    public boolean isError(){ return myType.equals(TYPE_ERROR);}
    public boolean isWarning(){ return myType.equals(TYPE_WARNING);}

    // exception
    Exception localException;

    /**
     * the type of the exception can be FATAL, ERROR or WARNING
     */

    public HandlerException(String e, String type) {
        super(e);	
        myType = type;
    }
    
    public HandlerException(Exception exc,String e, String type) {
        super(e);
        localException = exc;
        myType= type;
    }

    public HandlerException(Exception exc,String e, String[] args, String type){
        this (exc,java.text.MessageFormat.format(e,args), type);
    }

    public Exception getException(){return localException;}
    

    /*    public String getParseError(){
          StringBuffer str = new StringBuffer(20);
          Locator l = (Locator)theSource.getApplicationObject(LocalHandler.NAME_LOCATOR);
          str.append(getMessage());
          if (l != null){            //TODO to be removed
          str.append("\n ");
          str.append(l.getSystemId());
          str.append(" ligne :");
          str.append(l.getLineNumber());
          str.append(" col :");
          str.append(l.getColumnNumber());
          }
          return str.toString();
          }
    */
} // HandlerException
