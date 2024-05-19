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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.Writer;
import java.io.IOException;



/**
 * GroupElmentList.java
 *
 *
 * Created: Wed Aug 30 10:37:41 2000
 *
 * @author 
 * @version
 */

public class GroupElementList 
    implements RawSchemaAble {

    List groupElements = new ArrayList();

    public int size(){return groupElements.size();}

    public boolean isEmpty(){return groupElements.isEmpty();}
    public boolean containsOneElement(){return groupElements.size()==1;}


    public GroupElement getElement(int index){
        if (index >= size()) return null;
        return (GroupElement)((groupElements.toArray())[index]);
    }

    public void addGroupElement(GroupElement age) { 
        groupElements.add(age);
    }    
    /**
     * return the list of the GroupElements
     */
    public List values(){ return groupElements;}
 
    public void addGroupElements(GroupElementList agel) {      
        if (!agel.groupElements.isEmpty())
            groupElements.addAll(agel.groupElements);
    }    

    public void  realize(Schema aS) throws SchemaException{
        Iterator aI = groupElements.iterator();
        while (aI.hasNext()){
            GroupElement agr = (GroupElement)aI.next();
            agr.realize(aS);        
        }
    }


    public void writeRawSchema(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException{
        //element
        Iterator aI = groupElements.iterator();
        while (aI.hasNext()){
            if (aI==null) atrsc.write("###################");  //TODO to be removed
            else ((GroupElement) aI.next()).writeRawSchema(atrsc);
        }
    }

    public void mergeWith(GroupElementList aGEL) throws SchemaException{
        //element
        Iterator aI = groupElements.iterator();
        Iterator aI2 = aGEL.groupElements.iterator();
        while (aI.hasNext()){
            if (!aI2.hasNext())
                throw new SchemaException(SchemaMessages.getMessages("invalid restriction"),
                                          new String[] {""});// fair le message
            ((GroupElement) aI.next()).mergeWith((GroupElement)aI2.next());
        }
    }

         

    

         
    
}// GroupElementList
