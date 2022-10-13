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

import com.expway.schema.SchemaSymbols;

import java.io.Writer;
import java.io.IOException;

/**
 * GroupElement.java
 *
 * SuperClass for Element and Group
 *
 * Created: Mon Aug 28 11:33:22 2000
 *
 * @author Claude Seyrat & Cedric Thienot
 * @version 1.0
 */


abstract public class GroupElement 
    extends SchemaObject 
    implements RawSchemaAble{

    // -2 is equivalent to undefined TODO CONSTANTE

    static final int UNDEFINED = -2;
    static final int UNBOUNDED = -1;

    int minOccur = UNDEFINED;
    int maxOccur = UNDEFINED;

    public GroupElement (String aN) {
        super(aN);        
    }    

    public boolean isAgroup(){return false;}

    public boolean isTypeOfElement(){
        return false;
    }
    public void setMinOccur(int min) {minOccur=min;}

    public void setMinOccur(String minOccurString) throws SchemaException{
        try{
            minOccur = new Integer(minOccurString).intValue();
        }catch (NumberFormatException e){
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("invalid value"),
                                      new String[] {minOccurString,SchemaSymbols.ATT_MINOCCURS});
        }
    }
    public void setMaxOccur(int max){maxOccur=max;}
    public void setMaxOccur(String maxOccurString) throws SchemaException{
        if (maxOccurString.equals(SchemaSymbols.ATTVAL_UNBOUNDED))
            maxOccur = UNBOUNDED;
        else
            try{
                maxOccur = new Long(maxOccurString).intValue();
            }catch (NumberFormatException e){
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("invalid value"),
                                      new String[] {maxOccurString,SchemaSymbols.ATT_MAXOCCURS});
            }
    }
   
    public int getMinOccur(){
        if (minOccur == UNDEFINED) return 1;
        return  minOccur;
    }
    /**
     * if the value is not defined maxOccur = minOccur;
     */
    public int getMaxOccur(){
        if (maxOccur == UNDEFINED) {
            if (minOccur ==0) return 1;
            return getMinOccur();
        }
        return maxOccur;
    }

    public String getRawOccurence(){
        return "["+getMinOccur()+","+ ((getMaxOccur()!=UNBOUNDED)? (""+getMaxOccur()) : "*")+"]"; 

    }
         

    /////////////////////////////////
    // TREE reduc

    public GroupElement reduc(){return this;}

    public GroupElement reductiona(){return this;}

    public void mergeWith(GroupElement aGE) throws SchemaException{}

    
}
