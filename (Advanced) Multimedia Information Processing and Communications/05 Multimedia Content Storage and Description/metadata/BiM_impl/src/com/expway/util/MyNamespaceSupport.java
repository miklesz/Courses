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

import java.util.Stack;
import java.io.File;
import java.io.IOException;

import com.expway.schema.GeneralSchemaHandler;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.NamespaceSupport;

// Cette classe gère les namespaces
// D'un point de vue LOCAL, lors du parsing

public class MyNamespaceSupport extends NamespaceSupport {

    private boolean bFatherToChildNamespacePropagation = false;

    static int iRegisteredURI=1;
    final static boolean DEBUG=false;
    Stack sURI;
    URIRegistry uriRegistry;

    // ------------------------------------------------------------

    // A ne pas utiliser... le URIRegistry ne doit pas être lié à cette classe !
    public MyNamespaceSupport() {
        sURI=new Stack();
        uriRegistry=new URIRegistry();
    }

    public MyNamespaceSupport(URIRegistry u) {
        super();
        sURI=new Stack();
        uriRegistry = u;
    }

    // ------------------------------------------------------------

    public URIRegistry getURIRegistry() {
        return uriRegistry;
    }

    // ------------------------------------------------------------

    public void pushURI(String uri) {
        if (DEBUG) System.out.println("switching into URI:"+uri+" prefix:"+uriRegistry.getIndex(uri));
          
        // La pile stocke les URI pères pour résoudre l'URI dans le cas d'élement locaux non qualifiés
        if (uri==null) return;
        if (uri.length()==0) return;
        
        sURI.push(uri);
    }

    public void popURI() {        
        // On dépile si on peut...
        try {
            sURI.pop();
        } catch(Exception e) {}

        String uri="";
        if (!sURI.empty()) uri=(String)sURI.peek();

        if (DEBUG) System.out.println("switching back to URI:"+uri+" prefix:"+uriRegistry.getIndex(uri));
    }
    

    // ------------------------------------------------------------

    public void startPrefixMapping(String prefix, String uri) {
        //System.out.println("start prefix:"+prefix+" uri:"+uri);
        pushContext();
        declarePrefix(prefix,uri);
        // On n'enregistre plus l'URI:
        //    le SetOfDefinition a la main mise sur l'URI Registry
        //    Il garde la coherence URI <=> TypeDefinitions
        // uriRegistry.registerURI(uri);
    }

    public void endPrefixMapping(String prefix) {
        popContext();
        //System.out.println("end prefix: "+prefix);
    }

    // ------------------------------------------------------------

    // ne fais que la conversion prefixe
    public String getURIInternal(String qName) {
        if (qName==null) return null;

        String parts[] = new String[3];       
        processName(qName,parts,false);

        String uri=parts[0];
        return uri;
    }

    // renvoie la derniere uri du pere
    
    public String getURI(String qName){
        String uri=getURIInternal(qName);
        // Pas de préfixe ? On renvoie la dernière URI du père si bOriginal.. (cf elementFormDefault)
        if (bFatherToChildNamespacePropagation)
            if (uri.length()==0 && sURI.size()>0 ) 
                return (String)sURI.peek();
        return uri;
    }

}
