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

package com.expway.schema.xml;

import java.util.Stack; 
import java.util.StringTokenizer; 
import org.xml.sax.Attributes;

public class XMLSchemaInstance {
    static public final String sURI = "http://www.w3.org/2001/XMLSchema-instance";
    
    static final String [] sATTR={
        "noNamespaceSchemaLocation",
        "schemaLocation",
        "type",
        "null" };
    
    // Renvoie une pile (uri,filename,uri,filename...) des positions des schemas
    // en fonction de schemaLocation et noNamespaceSchemaLocation
    static public Stack processSchemaLocation(Attributes attrs) throws Exception {
        Stack sResults=new Stack();

        // Processing noNamespaceSchemaLocation
        String sValue=attrs.getValue(sURI,sATTR[0]);
        if (sValue!=null) {
            sResults.push(""); // URI vide pour un noNamespace
            sResults.push(sValue);
        }
        
        // Processing schemaLocation
        sValue=attrs.getValue(sURI,sATTR[1]);
        if (sValue!=null) {
            StringTokenizer st=new StringTokenizer(sValue);
            int iNbTokens=st.countTokens();
            if ((iNbTokens%2)!=0 || iNbTokens<2) throw new Exception("schemaLocation attribute value malformed");
            while (st.hasMoreTokens()) {
                String sURI=(String)st.nextToken();
                String sFilename=(String)st.nextToken();
                sResults.push(sURI);
                sResults.push(sFilename);
            }
        }

        return sResults;
    }

    // Traitement du xsi:type
    // Traitement spécial dans le cas où il y a n'a pas de namespace, on vérifie seulement "xsi:type"
    // sans regarder l'URI associé au préfixe xsi
    static public String processType(Attributes attrs) {
        String s=attrs.getValue(sURI,sATTR[2]);
        if (s!=null) return s;
        else {
            int index=attrs.getIndex("xsi:type");
            if (index!=-1) {
                if (attrs.getURI(index).length()==0) {
                    return attrs.getValue(index);
                }
            }
        }
        return null;
    }

    // Cet attribut est-il reservé à XSI ?
    static public boolean isReserved(Attributes attrs,int index) {
        if (attrs.getURI(index).equals(sURI)) return true;

        // Cas spécial si il n'y a pas de namespaces
        if (attrs.getQName(index).equals("xsi:type") && attrs.getURI(index).length()==0) return true;

        return false;
    }
}
