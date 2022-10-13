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

import java.util.TreeSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.io.Writer;
import java.io.IOException;

import com.expway.schema.SchemaSymbols;
import com.expway.schema.xml.XMLSchemaInstance;

import com.expway.tools.compression.IntegerCoding;
import com.expway.tools.utils.MethodsBag;

// Dictionnaire biindexé des URI

public class URIRegistry {
    int iRegisteredURI=1;
    
    Hashtable hURIKey,hIndexKey;
    TreeSet unknownURI;

    public URIRegistry() {
        hURIKey    = new Hashtable();
        hIndexKey  = new Hashtable();
        unknownURI = new TreeSet();
    }

    public int numberOfRegisteredURI(){
        return iRegisteredURI;
    }

    // REGISTER SI SCHEMA CONNU (SI INCONNU AVANT, IL DEVIENT CONNU)
    // On enregistre la nouvelle URI si elle n'est pas déjà dans la table
    public void registerURI(String uri) {   
        //System.out.println("===> register uri " + uri + " URIRegistry:"+this);
        if (!hURIKey.containsKey(uri)) {
            Integer i=new Integer(iRegisteredURI);
            hURIKey.put(uri,i);
            hIndexKey.put(i,uri);
            iRegisteredURI++;
        }
        if (unknownURI.contains(uri)) {
            unknownURI.remove(uri);
        }
    }
    
    // REGISTER SI SCHEMA INCONNU
    public void unknownRegisterURI(String uri){
        registerURI(uri);
        if (!unknownURI.contains(uri))
            unknownURI.add(uri);
    }

    public boolean containsURI(String uri){
        return hURIKey.containsKey(uri);
    }
    
    public String getURI(int iIndex) {
        if (iIndex==0) return "";

        Object o=hIndexKey.get(new Integer(iIndex));
        if (o!=null) return (String)o;
        else return "";        
    }

    // Renvoie 0 si l'URI est inconnue
    public int getURIIndexFromCompactName(String cname) {
        return getIndex(getURIFromCompactName(cname));
    }
    
    // Renvoie 0 si l'URI est inconnue
    public int getIndex(String uri) {
        if (uri==null) return 0;

        Object o=hURIKey.get(uri);
        if (o!=null) {
            Integer i=(Integer)o;
            return i.intValue();
        } else return 0;
    }

    // Noms sans les deux petits points
    static public String getFileCompliantName(String name){
        return name.replace(':','_');
    }

    // Noms interne utilisé dans les automates
    public String getCompactName(String uri,String element) throws Exception {
        // XMLSCHEMA_AS_A_TYPEDEFINITION modifié par claude pour faire de XMLSchema un NS a a part
        // Pas de compact name qX:y si X réfère à l'uri de XMLSchema ou à une URI vide
        // if (uri.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA)) return element;
        if (uri.length()==0) return element;

        

        return getPrefixOfURI(uri)+":"+element;
    }

    // Noms interne utilisé dans les automates
    public String getPrefixOfURI(String uri) {
        int iURI=getIndex(uri);
        return "q"+iURI;
    }

    // Noms interne utilisé dans les automates
    // Si il y a des namespaces, il y a un q devant chaque nom
    public String getURIFromCompactName(String cName) {
        if (cName==null) return null;

        if (cName.charAt(0)=='q') {
            // Retire le 'q'
            cName=cName.substring(1);

            int iDollar=cName.lastIndexOf(':');
            if (iDollar!=-1) {
                String sIndex=cName.substring(0,iDollar);
                Integer iIndex=new Integer(sIndex);
                int index=iIndex.intValue();
            
                return getURI(index);
            }
        }

        // Sinon, on renvoie le namespace par défaut, vide
        return "";
    }

    // Enlève le préfixe d'un nom qualifié, indépendamment du contexte
    static public String getWithoutPrefix(String qName) {
        if (qName==null) return null;

        int iLastColon=qName.lastIndexOf(':');

        // Pas de deux points dans le qName ? On le renvoie tel quel
        if (iLastColon==-1) return qName;

        // Sinon, on enlève toute la partie avant le deux points, y compris celui-ci (+1)
        return qName.substring(iLastColon+1);
    }
    
    // test si le nom a un préfixe
    static public boolean hasPrefix(String qName) {
        if (qName==null) return false;
        int iLastColon=qName.lastIndexOf(':');
        // Pas de deux points dans le qName ? On le renvoie tel quel
        if (iLastColon==-1) return false;
        return true;
    }
    
    // 
    static public String getPrefix(String qName) {
        if (qName==null) return null;
        int iLastColon=qName.lastIndexOf(':');
        // Pas de deux points dans le qName ? On le renvoie tel quel
        return qName.substring(0,iLastColon);
    }

    public void dumpURIs(){
        for (int i=0;i<iRegisteredURI;i++) {
            System.out.print(getPrefixOfURI(getURI(i))+" = \""+getURI(i)+"\"");
            if (knowsVersion(i))
                System.out.println(" (known)");
            else
                System.out.println(" (unknown)");
        }
    }

    // Pour le RWS ecriture des Raw
    public String toTargetNamespace() throws IOException {
        String result="(TARGETNAMESPACE ";
        for (int i=1;i<iRegisteredURI;i++) {
            result+=(" \""+getURI(i)+"\"");
        }
        return result+")\n";
    }

 	
    public int getVersionNumber(String compactName){
        return getURIIndexFromCompactName(compactName);
    }
    
 	
    public int getVersionSize(){
        //System.out.println("GETVERSIONSIZE="+numberOfRegisteredURI());
        return numberOfRegisteredURI();
    }
    
 	
    public int getVersionDecodingSize(){
        return getVersionCodingSize();
    }
    
    
    public boolean knowsVersion(int version){
        // regarde dans la table de mapping numero de version <=> URI qui a été décodé au début de l'AU
        // regarde si cette uri est connue
        String s = getVersionURI(version);
        return containsURI(s) && !unknownURI.contains(s);
    }

    /** Renvoie vrai si l'URI est registered et chargée en mémoire
     */
    public boolean isSchemaLoadedForURI(String uri) {
        return containsURI(uri) && !unknownURI.contains(uri);
    }
    
    //@@COMPAT
    // modifier avec la table de mapping
    public String getVersionURI(int read){
        return getURI(read);
    }

    //@@COMPAT
    public int getVersionCodingSize(){
        return MethodsBag.getCodingLength(getVersionSize());
    }
    
    //@@COMPAT
    public IntegerCoding getVersionCoding(String compactName){
        return new IntegerCoding(getVersionNumber(compactName),getVersionCodingSize());
    }



}
