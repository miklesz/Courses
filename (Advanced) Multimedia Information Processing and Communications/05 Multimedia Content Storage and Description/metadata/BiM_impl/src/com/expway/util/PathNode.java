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

package com.expway.util;

import java.lang.Exception;
import java.util.Stack;
import java.util.EmptyStackException;
import java.util.Iterator;

public class PathNode {
    public String sType;
    public String sElement;
    public int iIndex;
    boolean bIsAttribute,bIsFather;

    // Noeud signifiant go to this Element
    public PathNode(String type, String element, int index) {
        sType=type;
        sElement=element;
        iIndex=index;
        bIsAttribute=false;
        bIsFather=false;
    }

    // Noeud signifiant go to this Attribute
    public PathNode(String type, String element) {
        sType=type;
        sElement=element;
        bIsAttribute=true;
        bIsFather=false;
    }

    // Noeud signifiant go to the Father
    public PathNode() {
        bIsFather=true;
    }

    // Construit un PathNode à partir de la chaîne X#Y#Z (Element) @X#Y (Attribut) ## (Father)
    public PathNode(String sPath) throws PathException {
        if (sPath.equals("##")) {
            bIsFather=true;
            bIsAttribute=false;
        } 
        else if (sPath.charAt(0)=='@') {
            try {
                int iT=sPath.lastIndexOf('#');
                String sT=sPath.substring(iT+1);
                sElement=new String(sT);
        
                int iTLast=iT;
                sT=sPath.substring(1,iTLast);
                sType=new String(sT);
                bIsAttribute=true;
                bIsFather=false;
            } catch (Exception e) {
                throw new PathException();
            }
        }
        else {
            try {
                int iT=sPath.lastIndexOf('#');
                String sT=sPath.substring(iT+1);
                iIndex=(new Integer(sT)).intValue();

                int iTLast=iT;
                iT=sPath.lastIndexOf('#',iT-1);
                sT=sPath.substring(iT+1,iTLast);
                sElement=new String(sT);
        
                iTLast=iT;
                sT=sPath.substring(0,iTLast);
                sType=new String(sT);
                bIsAttribute=false;
                bIsFather=false;
            } catch (Exception e) {
                throw new PathException();
            }
        }
    }

    public String toString() {
        if (bIsAttribute) return "@"+sType+"#"+sElement;
        if (bIsFather) return "##";
        return sType+"#"+sElement+"#"+iIndex;
    }
}
