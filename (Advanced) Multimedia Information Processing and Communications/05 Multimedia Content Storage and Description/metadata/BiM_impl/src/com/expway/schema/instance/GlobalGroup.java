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

import java.io.Writer;
import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;



import com.expway.schema.SchemaSymbols;

/**
 * GlobalGroup.java
 *
 *
 * Created: Mon Aug 28 11:33:22 2000
 *
 * @author 
 * @version
 */

public class GlobalGroup    
    extends  Group
    implements CrossReferencable {



    public GlobalGroup (String aN) {
        super(aN);

    }
    
    ////////////////////////////////////////
    // accessing
    /////////////////////////////////
    /**
     * the name of the reference
     */
    String refName;
    
    /**
     * Set the value of refName.
     * @param v  Value to assign to refName.
     */
    public void setRefName(String  v) {this.refName = v;}
    
    /**
     * Get the value of refName.
     * @return Value of refName.
     */
    public String getRefName() {return refName;}


    GlobalGroup refGroup;
    
    /**
     * Get the value of refGroup.
     * @return Value of refGroup.
     */
    public GlobalGroup getRefGroup() {return refGroup;}
    
    /**
     * Set the value of refGroup.
     * @param v  Value to assign to refGroup.
     */
    public void setRefGroup(GlobalGroup  v) {this.refGroup = v;}
    
    public boolean isAReference(){return (refGroup != null);}    

    /**
     * Get the value of groupElements.
     * if it is a reference, you get the value of the RefGroup.
     * @return Value of groupElements.
     */
    public GroupElementList getGroupElements(){
        if (isAReference())
            return getRefGroup().getGroupElements();
        return super.getGroupElements();
    }
            
    /**
     * Get the value of name.
     * if it is a reference, you get the value of the RefGroup.
     * @return Value of name.
     */
    public String getName(){
        if (isAReference())
            return getRefGroup().getName();
        return super.getName();
    }
        


    public void realize(Schema aS) throws SchemaException {
        if (isAlreadyRealized) return;
        isAlreadyRealized = true;
        //realize the reference 
        if (getRefName() != null){
            try{
                setRefGroup(aS.getRGroup(getRefName()));
            } catch (SchemaException e){
                e.setSource(this);
                throw e;
            }

            if (getRefGroup() == null)
                throw new SchemaException(this,
                                          SchemaMessages.getMessages("undefine element"),
                                          new String[] {this.getRefName()});
            // realize the element point at TODO une variable already realized
            
        }
        getGroupElements().realize(aS);
        isCoherent = true;
    }

    /*
      public GroupElement reductiona(){
      if (isAReference())
      return getRefGroup().reductiona();
      return super.reductiona();
      }
    */
    public GroupElement reductiona(){return this;}

    public void writeRawSchema(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException{
        if (!isCoherent) 
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("inconsistencty"));
        // on encapsule le groupe dans une sequence
        atrsc.write("("+SEQUENCE_NAME+getRawOccurence()+" ");
        if (isEmpty()) 
            getGroupElements().writeRawSchema(atrsc);
        atrsc.write(")");
    }

     

         

}// Group

