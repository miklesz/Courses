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


package com.expway.tools.expression;

import com.expway.tools.automata.*;
import com.expway.tools.compression.*;

import com.expway.util.URIRegistry;

import java.util.*;
import java.io.*;

public class ATTRNode extends SEQNode {
    
    public void sort(){
        //System.out.println("**** Sort attributes:");  
        //print("","");
        Collections.sort(getChildren(),AttributeComparator.anAttributeComparator);
        //System.out.println("   ----- Sorting result");
        //print("","");
    }

    public void realize(SetOfDefinitions theDefinitions) throws DefinitionException {
        super.realize(theDefinitions);
        sort();
    }
   
        
}


class AttributeComparator implements Comparator {

    static AttributeComparator anAttributeComparator = new AttributeComparator();

    public int compare(Object o1,Object o2){
        ItemNode i1 = (ItemNode)(((OccurrenceNode)o1).getOccurrenceChild());
        ItemNode i2 = (ItemNode)(((OccurrenceNode)o2).getOccurrenceChild());
        return URIRegistry.getWithoutPrefix(i1.getItemName()).compareTo(URIRegistry.getWithoutPrefix(i2.getItemName()));
    }

    public boolean equals(Object obj){return false;}
}
