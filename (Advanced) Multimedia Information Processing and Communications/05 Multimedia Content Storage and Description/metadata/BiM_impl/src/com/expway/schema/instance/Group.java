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
 * Group.java
 *
 *
 * Created: Mon Aug 28 11:33:22 2000
 *
 * @author 
 * @version
 */

public class Group 
    extends  GroupElement 
    implements RawSchemaAble {

    

    public Group (String aN) {
        super(aN);
        
    }
    
    ////////////////////////////////////////
    // accessing
    /////////////////////////////////

    GroupElementList groupElements;

    public boolean isAgroup(){return true;}

    public boolean isEmpty(){
        if (groupElements == null) return true;
        return groupElements.isEmpty();
    }

    public boolean containsOneElement(){
        if (groupElements == null) return false;
        return groupElements.containsOneElement();
    }

    /**
     * Get the value of groupElements.
     * create one if it has not been created.
     * @return Value of groupElements.
     */
    public GroupElementList getGroupElements() {
        if (groupElements==null)
            groupElements = new GroupElementList();
        return groupElements;
    }
    
    /**
     * Set the value of groupElements.
     * @param v  Value to assign to groupElements.
     */
    public void setGroupElements(GroupElementList  v) {this.groupElements = v;}
    
    public void removeElement(){groupElements = null;}
    /**
     * add an attribute
     */

    public void addGroupElement(GroupElement att) {
        getGroupElements().addGroupElement(att);
    }

    public void addGroupElements(GroupElementList agel) {
        getGroupElements().addGroupElements(agel);
    }

    //TODO
    public void realize(Schema aS) throws SchemaException{
        if (isAlreadyRealized) return;
        isAlreadyRealized = true;
        /*
          if (getGroupElements().isEmpty()){
          throw new SchemaException(this,
          SchemaMessages.getMessages("empty group"),
          new String[] {this.getName()});
          }
        */
        getGroupElements().realize(aS);
        isCoherent = true;
    }
    /////////////////////////////////
    // TREE reduc

    public GroupElement reduc(){
        return this;
    }

    public GroupElement reductiona(){
        if (groupElements == null) return this;
        if (groupElements.size()!=1) return this;

        GroupElement aGE = groupElements.getElement(0);
        aGE = aGE.reductiona();
        if (aGE.getMinOccur() >1) return this;

        aGE.setMinOccur(aGE.getMinOccur()*this.getMinOccur());
        if (aGE.getMaxOccur()==UNBOUNDED || this.getMaxOccur()== UNBOUNDED)
            aGE.setMaxOccur(UNBOUNDED);
        else
            aGE.setMaxOccur(aGE.getMaxOccur()*this.getMaxOccur());
        return aGE;
    }
    ////////////////////////////////////
    // RAW
    public void writeRawSchema(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException{
        if (!isCoherent) 
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("inconsistencty"));
        atrsc.write("(" 
                    +getRAWSchemaName()
                    + getRawOccurence()+" ");
        getGroupElements().writeRawSchema(atrsc);
        atrsc.write(")");
    }

    String getRAWSchemaName(){
        if (getName().equals(SchemaSymbols.ELT_SEQUENCE))
            return SEQUENCE_NAME;
        else if (getName().equals(SchemaSymbols.ELT_ALL))
            return ALL_NAME;
        else if (getName().equals(SchemaSymbols.ELT_CHOICE))
            return CHOICE_NAME;
        return getName();
    }

    



    ///////////////////////////////////////////////////////
    // merge in case of restriction
    ///////////////////////////////////////////////////////


    public void mergeWith(GroupElement aGE) throws SchemaException {
        // le meme nom
        if (!aGE.getName().equals(getName()))
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("invalid restriction"),
                                      new String[] {this.getName()});
        //        setRestrictionSchema(aGE.getRestrictionSchema());
        getGroupElements().mergeWith(((Group)aGE).getGroupElements());
        //System.out.println(getName());

    }

    ///////////////////////////////////////////////////////
    // Cross reference
    ///////////////////////////////////////////////////////

    boolean cut= false;
         

}// Group
