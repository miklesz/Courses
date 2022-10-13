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

import org.xml.sax.Attributes;

import com.expway.util.LocalHandler;
import com.expway.util.HandlerException;
import com.expway.schema.instance.Documentation;
import com.expway.schema.instance.AppInfo;

/**
 * AppinfoHandler.java
 *
 *
 * Created: Wed Aug 09 18:56:06 2000
 *
 * @author 
 * @version
 */

public class AppinfoHandler extends StaticLocalHandler {

    AppInfo aAI = new AppInfo();

    public AppinfoHandler () {
        
    }
    public  void informEnd(LocalHandler son)throws HandlerException{
        if (son.getCreation()== null)
                return; 
        String className= son.getClass().getName();
        if (className.equals(SchemaRegister.CLASS_ELT_HFP)){
            String [] re = (String[])son.getCreation();
            if (re[0].equals("hasFacet"))
                aAI.addFacet(re[1]);
            if (re[0].equals("hasProperty"))
                aAI.addProperty(re[1],re[2]);

        }
        
    }
    
    public void init(String uri, String local, String raw, Attributes attrs){
    }
    
    public void reset(){} 

    //TODO
    public void end(){}
    //TODO
    public Object getCreation(){ 
        return aAI;
    }

    //TODO
    public  void characters(char[] ch, int start, int length){
    }

}// AppinfoHandler
