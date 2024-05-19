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
 * DataTypeRef.java
 *
 * @author Claud Seyrat & Cedric Thienot
 */

public class DataTypeRef extends Ref {
    
    public DataTypeRef(String name) {
       super(name);
       isAReference = true;
    }


    public DataTypeRef(DataType dth) {
       this(dth.getName());
       setHandler(dth);
       setSchema(dth.getSchema());
    }
   
   // establishing links

    public void realize(Schema s) throws SchemaException {
        Type tempdt;
        try{
            tempdt = s.getRType(getName());
        } catch (SchemaException e){
            System.out.println("tt "+ getName());
            
            e.setSource(this);
            throw e;
        }

        if (tempdt ==null)
              throw new SchemaException(this,
                                        SchemaMessages.getMessages("undefine element"),
                                        new String[] {getNameNonNull()});
        //in case of simpleComplex we swith to simple datatype
        if (!tempdt.isSimple())
            tempdt= ((ComplexType)tempdt).getDatatype();
        if (tempdt !=null && tempdt.isSimple())
            setHandler(tempdt);
        else
              throw new SchemaException(this,
                                        SchemaMessages.getMessages("undefine element"),
                                        new String[] {getNameNonNull()});
    }


    // HTML
    public static String HTMLStyle="txtpurpleitalic";


} // DataTypeRef
