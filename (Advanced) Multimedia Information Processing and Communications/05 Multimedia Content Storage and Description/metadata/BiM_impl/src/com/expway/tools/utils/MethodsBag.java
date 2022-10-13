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

package com.expway.tools.utils;

import java.util.Collection;
import java.util.Iterator;

public class MethodsBag {
    
    static final public int getCodingLength(int minInclusif, int maxInclusif){
        return (int)Math.ceil(Math.log((double)((maxInclusif-minInclusif)+1))/Math.log(2.0));
    }
    
    static final public int getCodingLength(int maxNotInclusif){
        return (int)Math.ceil(Math.log((double)(maxNotInclusif))/Math.log(2.0));
    }

    static final public int getCodingLength(long maxNotInclusif){
        return (int)Math.ceil(Math.log((double)(maxNotInclusif))/Math.log(2.0));
    }

    static final public boolean isPowerOfTwo(int maxNotInclusif){
        return (getCodingLength(maxNotInclusif) != getCodingLength(maxNotInclusif+1));
    }

    /** first elt = 0 ; -1 not here */

    static final public int getPosition(Object searchedObject, Collection c){
        if (c == null || searchedObject == null){
            System.out.println("error in position c =" + c + " searchedObject = " + searchedObject);
            throw new NullPointerException("Object or Collecton is null");
        }
        Iterator i = c.iterator();
        if (!i.hasNext()) return -1;
        int t=0;
        while(i.hasNext()){
            Object o = i.next();
            if (searchedObject.equals(o)) return t;
            t++;
        }
        return -1;
    }

    static final public Object getObjectAt(int pos, Collection c){
        if (pos < 0 || pos >= c.size()){
            System.out.println("error in getObjectAt pos="+pos+" col=" + c);
            throw new ArrayIndexOutOfBoundsException();
        }
        
        if (c == null )
            {
                System.out.println("error in getObjectAt()");
                throw new NullPointerException("Object or Collecton is null");
            }
        
        Iterator i = c.iterator();
        if (!i.hasNext()) return null;

        int t=0;
        while(i.hasNext()){
            Object o = i.next();
            if (t==pos) return o;
            t++;
        }
        return null;
    }
    
}
