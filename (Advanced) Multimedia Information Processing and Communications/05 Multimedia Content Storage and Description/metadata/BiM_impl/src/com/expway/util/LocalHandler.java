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

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import com.expway.util.MyNamespaceSupport;

import java.util.Map;

/**
 * il s'agit de l'interface pour tous les autres Handler
 * un handler a soit un dictionnaire définit dans le handlerManager ou pas de dictionnaire
 */
public abstract class LocalHandler {
    static public final String NAME_LOCATOR = "locator";

    HandlerExceptionHandler handlerExceptionHandler;
    LocalHandler father;
    protected Locator locator;

    // Gestionnaire des namespaces, utiliser setMyNamespaceSupport pour le mettre à jour
    protected MyNamespaceSupport ns;
    
    // Pour savoir si on a changé de namespace dans ce LocalHandler
    boolean bNamespaceSwitched=false;

    public LocalHandler(){
        ns=null;
        bNamespaceSwitched=false;
    }
    
    public final LocalHandler startElement(String uri, String local, String raw, Attributes attrs) 
        throws HandlerException {

        try {            
            LocalHandler currentSon = getSonHandler(uri,local,raw);
            currentSon.xmlName = getWithoutPrefix(raw);
            currentSon.setFather(this);
            currentSon.setLocator(locator);
            currentSon.setMyNamespaceSupport(getMyNamespaceSupport());
            currentSon.setErrorHandler(handlerExceptionHandler);

            // Changement de namespace
            if (uri!=null) {
                if (uri.length()!=0) {                   
                    currentSon.switchNamespace(uri);
                }
            }

            currentSon.init(uri,local,raw,attrs);            
            return currentSon;
        } catch (HandlerException aHE){
            handlerExceptionHandler.handlerError(aHE);
            LocalHandler aDf = new DefaultLocalHandler();
            aDf.setFather(this);
            return aDf; // TODO a verifier

               // SAXParseException(aHE.getMessage(),locator,aHE);
        }
    }

    public void setErrorHandler(HandlerExceptionHandler a){handlerExceptionHandler = a;}

    public final LocalHandler endElement(String uri, String local, String raw)
        throws HandlerException {
        try{
            if (bNamespaceSwitched) ns.popURI();

            this.end(uri,local,raw);

            // On revient dans le bon namespace, si on avait changé

            if (father ==null) return this; // TODO le premier element
            
            father.informEnd(this);
            if (hasPoolHoldByHandlerManager())
                HandlerManager.release(this);
            return father;
        } catch (HandlerException aHE){
            handlerExceptionHandler.handlerError(aHE);
            return (father != null ? father : this); // TODO a verifier

            //            throw new SAXParseException(aHE.getMessage(),locator,aHE);
        }
    }
    

    public Locator getLocator(){
        return locator;
    }
    public void setLocator(Locator l){
        locator = l;
    }


    public abstract boolean hasPoolHoldByHandlerManager();

    public abstract LocalHandler getSonHandler(String uri, String local, String raw) throws HandlerException;
    // instantiation pour un schéma fixe
    //   LocalHandler currentSon = HandlerManager.getSonHandler(getClass().getName(),raw);
 
    private void setFather(LocalHandler af){father = af;}

    /**
     * envoyer quand le fils fini
     *
     * @param son le fils qui a fini son instantiation
     */
    public  abstract void informEnd(LocalHandler son) throws HandlerException;

    /**
     * intitialize the object created by the handler
     */
    
    public abstract void init(String uri, String local, String raw, Attributes attrs)
        throws HandlerException;

    /**
     * Reset the object before it goes in the pool
     */

    public abstract void reset() ;

    /**
     * inform the Handler that its antiation has just finished
     */
    public abstract void end() throws HandlerException;

    public void end(String uri, String local, String raw) throws HandlerException {
        end();
    }

    /**
     * get character
     */
    public abstract void characters(char[] ch, int start, int length) throws HandlerException;

    /**
     * the object created
     */
    public abstract Object getCreation() throws HandlerException;

    String xmlName;
    
    /**
     * Get the value of xmlName.
     * @return value of xmlName.
     */
    public String getXmlName() {
        return xmlName;
    }

    public void setMyNamespaceSupport(MyNamespaceSupport n) {
        ns=n;
    }

    protected MyNamespaceSupport getMyNamespaceSupport() {
        return ns;
    }

    // On change de namespace, s'ils sont supportés,
    // Indispensable pour commencer
    public void switchNamespace(String uri) {
        if (ns!=null) {
            bNamespaceSwitched=true;
            ns.pushURI(uri);
        }
    }

    // Enlève le préfixe d'un nom qualifié, indépendamment du contexte
    public String getWithoutPrefix(String qName) {
        return URIRegistry.getWithoutPrefix(qName);
    }

    // Renvoie l'URI d'un nom qualifié, dépend du contexte à un instant donné via MyNamespaceSupport
    // Ne fonctionne qu'à l'instant du parsing    
    public String getURI(String qName) {
        if (ns!=null) return ns.getURI(qName);
        else {
            System.out.println("LocalHandler.getURI() : No namespace support !!! Please setMyNamespaceSupport !");
            return null;
        }
    }

    // Copie de NS
    public String getURIInternal(String qName) {
        if (ns!=null) return ns.getURIInternal(qName);
        else {
            System.out.println("LocalHandler.getURI() : No namespace support !!! Please setMyNamespaceSupport !");
            return null;
        }
    }

    // Résolut le préfixe dans le nom qualifié et le remplace par son URI 
    // Ne fonctionne qu'à l'instant du parsing
    // Uniquement utilisé par Cédric
    public String getResolvedName(String qName) throws HandlerException {
        if (qName==null) return null;

        String uqName=getWithoutPrefix(qName);
        String uriName=getURIInternal(qName);

        if (uriName==null) throw new HandlerException("getResolvedName: can't resolve prefix for "+qName,HandlerException.TYPE_ERROR);
        if (uriName.equals("")) return uqName;

        return uriName+":"+uqName;
    }

    // Renvoie le nom compact avec une URI registrée sous la forme q1:Mpeg7 q12:Bloub...
    // Ne fonctionne qu'à l'instant du parsing
    public String getCompactName(String qName) throws HandlerException {
        if (qName==null) return null;

        if (ns==null) {
            System.out.println("LocalHandler.getURI() : No namespace support !!! Please setMyNamespaceSupport !");
            return "";
        }
        
        try {
            String uri=getURI(qName);
            String element=getWithoutPrefix(qName);
            return ns.getURIRegistry().getCompactName(uri,element);
        } catch(Exception e) {
            throw new HandlerException(e.getMessage(),HandlerException.TYPE_ERROR);
        }
    }

}
