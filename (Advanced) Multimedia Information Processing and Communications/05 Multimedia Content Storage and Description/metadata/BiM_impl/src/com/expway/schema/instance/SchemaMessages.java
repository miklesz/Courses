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

import java.util.ListResourceBundle;

/**
 * SchemaMessages.java
 *
 *
 * Created: Thu Sep 07 11:36:47 2000
 *
 * @author 
 * @version
 */

public class SchemaMessages extends ListResourceBundle {
    
    public Object[][] getContents() {
        return contents;
    }
    static final Object[][] contents = {
        {"undefinedIn","The element {0} is not defined in {1}."},
        {"noAttribute","The element {0} has no attributes."},
        {"noPCData","The element {0} has no PCData."},
        {"onlySimpleType","Only simple type are accepted for {0}. {1} is not simple."},
        {"duplicate definition", "Duplicate definition for {0}."},
        {"no schema", "No schema associated with the targetnamespace {0}."},
        {"undefine element", "The element {0} is not defined."},
        {"undefine", "The {0} {1} is not defined."},
        {"empty group", "The group {0} is empty."},
        {"invalid value","Invalid value ({0}) for {1}."},
        {"invalid restriction","Invalid restricion for {0}."},
        {"invalid targetNamespace","The schema {0} has a different targetNamespace, expected value {1}."},
        {"undefine type","No type is defined in {0}."},
        {"undefined Attribute in restriction","the attribute {0} has not been defined."},
        {"inconsistencty","The object is not consistent."},
        {"simple Complex with a model","the complex Type named {0} can not derive from a simple type and have sub element at the same time."},
        {"derive without superType","The object {0} doesn't have defined its superType."},
        {"simple Anonymous Complex with a model","A complex Type can not derive from a simple type and have sub element at the same time."}
    };

    static final SchemaMessages newInstance(){
        if (anInstance == null)
            anInstance = new SchemaMessages();
        return anInstance;
    }

    static  SchemaMessages anInstance;

    // Greg (pour libérer de la place mémoire)
    static public void freeStatic() {
        anInstance=null;
    }

    static final String getMessages(String key){
        return (String)SchemaMessages.newInstance().handleGetObject(key);
    }



    
    
    
}// SchemaMessages
