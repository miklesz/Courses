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

package com.expway.tools.compression;


public class CptNodeStack {

    CptNode last = null;
    //    private CptNode first = null;

    public CptNodeStack(){ }

    // recopie completement l'autre stack
    public CptNodeStack(CptNodeStack other){    
        CptNode otherLast = other.last;
        CptNode current = null, otherCurrent = null;

        // recopie du premier noeud
        if (otherLast == null)  return;
        last = new CptNode(otherLast);
        current = last;
        otherCurrent = otherLast;
        
        while(otherCurrent.next != null){
            current.next = new CptNode(otherCurrent.next);
            current = current.next;
            otherCurrent = otherCurrent.next;
        }
    }

    /** Attention, les deux Stacks deviennent identiques */
    void append(CptNodeStack cns){
        if (cns.last==null) return;
        CptNode cn = cns.last;
        while (cn.next !=null) cn = cn.next;
        cn.next = last;
        last = cns.last;
    }

    void bind(CptNodeStack cns){
        last = cns.last;
        //        first = cns.first;
    }

    public void decrLast(){
        last.decr();
    }

    public void incrLast(){
        last.incr();
    }

    public int lastValue(){
        return last.getValue();
    }
    
    void push(CptNode c){
        //          if (last == null) first = c;
        c.next=last;
        last = c;
    }

    void pushInFirstEmptyCpt(CptNode c){
        CptNode current = last;
        while (current != null){
            if (current.isEmpty()){
                current.setValue(c);
                return;
            }
            current = current.next;
        }
        throw new RuntimeException("Impossible to find an Empty node to put the cpt");
    }

    CptNode pop(){
        if (last == null) return null;
        CptNode c = last;
        last = last.next;
        c.next = null;
        //        if (last == null) first = null;
        return c;
    }

    public String toString(){
        CptNode temp = last;
        String s = "->";
        while(temp!=null){
            s = s + temp + "->";
            temp = temp.next;
        }
        return s;
    }

	
}
