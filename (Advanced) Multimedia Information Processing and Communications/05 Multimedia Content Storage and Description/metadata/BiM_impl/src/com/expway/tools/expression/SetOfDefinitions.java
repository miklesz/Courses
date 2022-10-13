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

import java.util.Hashtable;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.TreeSet;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import com.expway.schema.SchemaSymbols;
import com.expway.schema.xml.XMLSchemaInstance;
import com.expway.schema.GeneralSchemaHandler;
import com.expway.util.URIRegistry;

import org.xml.sax.helpers.NamespaceSupport;

// Cette classe gère un ensemble de TypeDefinitions pour le support des Namespaces
// Un TypeDefinitions <=> Un Namespace, caractérisé par son URI
public class SetOfDefinitions extends Hashtable {
    final static boolean DEBUG=false;
    

    // Table d'association URI <-> SchemaLocation
    Hashtable hURISchemaLocation;

    URIRegistry uriRegistry;
    boolean bIsRealized=false;

    // ------------------------------------------------------------
    // CONSTRUCTEUR

    public SetOfDefinitions() {
        super();
        uriRegistry=new URIRegistry();
        bIsRealized=false;
        unknownRegisterURI(SchemaSymbols.URI_SCHEMAFORSCHEMA);
        hURISchemaLocation=new Hashtable();
    }

    // ------------------------------------------------------------

    public void saveDefinitionForDecoderConfiguration(File f,String sFirstURI) throws IOException {
        DataOutputStream fos=new DataOutputStream(new FileOutputStream(f));
        int num = uriRegistry.numberOfRegisteredURI();

        fos.writeUTF(sFirstURI);

        fos.writeInt(num-1);
        for (int t=1;t<num;t++) {
            fos.writeUTF(uriRegistry.getURI(t));
            String schemaLocation=(String)hURISchemaLocation.get(uriRegistry.getURI(t));
            if (schemaLocation==null) schemaLocation="";
            fos.writeUTF(schemaLocation);
        }
    }
    
    // Le .decoderCondig donne :
    // - la première URI de la description
    // - le tableau (uri,schemalocation)
    // Le sInputFile est necessaire pour retrouver les schemas, souvent à côté du fichier Input 
    public String loadDefinitionForDecoderConfiguration(File f,String sInputFile) throws Exception {
        DataInputStream fis=new DataInputStream(new FileInputStream(f));

        String sFirstURI=fis.readUTF();

        int num = fis.readInt();
        for (int t=0;t<num;t++){
            String uri = fis.readUTF();
            String location=fis.readUTF();

            unknownRegisterURI(uri);

            if (location.length()>0) {
                locateAndLoadSchema(uri,location,sInputFile);
            }
        }
        return sFirstURI;
    }

    // ------------------------------------------------------------
    // ACCESSEURS

    // LES TYPES / LES DEFINITIONS

    // ajoute un type et le redirige vers le bon TypeDefinitions
    public void putTypeDefinition(TypeDefinition td){
        getDefinitionsFromCompactName(td.getName()).putTypeDefinition(td);
    }

    // ajoute un element et le redirige vers le bon TypeDefinitions
    public void putElementDefinition(ElementDefinition ed){
        getDefinitionsFromCompactName(ed.getName()).putElementDefinition(ed);
   
    }

    public TypeDefinition getTypeDefinition(String contentModelName){
        String uri=getURIRegistry().getURIFromCompactName(contentModelName);
        if (uri!=null) {
            TypeDefinitions tds=getDefinitions(uri);
            if (tds!=null) return tds.getTypeDefinition(contentModelName);
        }
        return null;
    }

    public TypeDefinitions getDefinitions(String sUri) {return (TypeDefinitions)get(sUri);}

    public TypeDefinitions getDefinitionsFromCompactName(String compactName) {
        String uri=getURIRegistry().getURIFromCompactName(compactName);
        TypeDefinitions td = (TypeDefinitions)get(uri);
        return td; 
    }

    TypeDefinitions getFirstTypeDefinitions() {return (TypeDefinitions)values().iterator().next();}

    // LES URIS

    public URIRegistry getURIRegistry() {return uriRegistry;}

    // DES ITERATEURS

    public Iterator typeDefinitions() {return values().iterator();}
    public Iterator allTypes() {return new TypesIterator(this);}

    // ------------------------------------------------------------
    // GESTION DES NAMESPACES

    public void unknownRegisterURI(String suri) {
        if (!uriRegistry.containsURI(suri)) {
            //System.out.println(" New URI and typeDefinitions requested : "+suri);
            uriRegistry.unknownRegisterURI(suri);

            // Ajout dans le SetOfDefinitions
            put(suri,new TypeDefinitions(this,suri));
        }
        else {
            //System.out.println(" URI and TypeDefinitions already exists : "+suri);
        }
    }

    // Libere le caches de tous les TypeDefinitions
    public void reset() {
        for (Iterator i=allTypes();i.hasNext();) {
            TypeDefinition td=(TypeDefinition)i.next();
            td.reset();
        }
    }

    // ------------------------------------------------------------
    // CHARGEMENT

    // Charge le schema et l'ajoute dans SetOfDefinitions
    // Le champ sInput sert à aider loadSchema à trouver le bon fichier à côté du fichier sInput
    public void locateAndLoadSchema(String sURI, String sFilename, String sInput) throws Exception {
        try {
            File f=new File(sFilename);
            System.out.println("Loading schema "+sFilename+" for URI "+sURI+"... ");
			
            if (uriRegistry.containsURI(sURI) && sURI.length()!=0 && uriRegistry.knowsVersion(uriRegistry.getIndex(sURI))) {
                System.out.println("Using cached schema for URI "+sURI);
                // On resette quand même, qui libère les pools d'automates
                reset();
                return;
            }
            
            // Le fichier existe t'il ?
            if (f.exists()) {
                String sAbsoluteFilename=f.getAbsolutePath();         
                loadSchema(sURI,sAbsoluteFilename);               
            } else {
                // Deuxième tentative - Est-il à côté du fichier d'entrée ?
                File fInput=new File(sInput);
                String sPath="";
            
                if (fInput!=null) {
                    sPath=fInput.getParent()+"\\";                
                }
            
                f=new File(sPath+sFilename);
                if (f.exists()) {
                    loadSchema(sURI,sPath+sFilename);               
                } else {
                    uriRegistry.unknownRegisterURI(sURI);
                    System.out.println("File " + sFilename + " not found. Continuing...");
                }
            }
            System.out.println("Loading schema OK");
        } catch (Exception e) {
            System.out.println("Loading schema FAILED : " + e.getMessage());
            throw e;
        }
    }   

    // Charge un schéma quand il n'y pas de namespace
    public void loadSchema(String sFilename) throws Exception {
        loadSchema("",sFilename);
    }

    public void loadSchema(String sUri, String sFilename) throws Exception {
        boolean bRemoveRWSFile=false;
        String sOriginalName=sFilename;

        String sRWSFile=null;

        if (sFilename.toUpperCase().endsWith("XSD")) {
            // Création d'un fichier RWS temporaire
            String sXSDName=sFilename;
            sRWSFile=sXSDName+".rws";
            bRemoveRWSFile=true;
            GeneralSchemaHandler.generateSchema(sXSDName,sRWSFile,false,false);
            GeneralSchemaHandler.freeStatic();            
            sFilename=sRWSFile;
        }

        // Est-ce un fichier RWS ?
        if (sFilename.toUpperCase().endsWith("RWS")) 
            loadRWSSchema(sFilename);
        else 
            throw new Exception("Schema file "+sFilename+" must be an .xsd or .rws file");

        uriRegistry.registerURI(sUri);
        hURISchemaLocation.put(sUri,sOriginalName);
        
        if (bRemoveRWSFile) {
            File file=new File(sFilename);
            
                file.delete();
        }
    }

    private void loadRWSSchema(String sFilename) throws Exception {
        // Appel spécial, avec réference à SetOfDefinitions (this) pour résoudre
        // le SUPERTYPEOF
        TypeDefinitionInstantiator atdi = new TypeDefinitionInstantiator(this);

        File f=new File(sFilename);
        try {atdi.parseRawSchema(f);}
        catch(IOException e) {
            System.out.println(e);
            throw e;
        }
    }
      
    // ------------------------------------------------------------
    // REALIZATION
	
    public void realize() throws DefinitionException {
        if (bIsRealized) {
            System.out.println("SetOfDefinitions already realized");
            return;
        }

        // Realizations de l'héritage des TypesDefinitions
        for (Iterator i=values().iterator();i.hasNext();) {
            TypeDefinitions td=(TypeDefinitions)i.next();
            td.realizeInheritance();
        }

        // Realizations des TypesDefinitions
        for (Iterator i=values().iterator();i.hasNext();) {
            TypeDefinitions td=(TypeDefinitions)i.next();
            td.realize();
        }

        

        bIsRealized=true;
    }

    // Pour le décodage, écriture des xmlns:...
    // Si il existe un namespace vide, on suppose que l'on est en train de manipuler
    // un fichier sans namespace => on n'écrit rien
    public void writeXMLNSStuff(Writer writer) throws IOException {
        // Si le namespace vide n'existe pas...
        if (uriRegistry.getIndex("")==0) {
            writer.write(" xmlns:xsi=\""+XMLSchemaInstance.sURI+"\"\n");
            for (int i=1;i<uriRegistry.numberOfRegisteredURI();i++) {
                writer.write("xmlns:"+uriRegistry.getPrefixOfURI(uriRegistry.getURI(i))+"=\""+
                             uriRegistry.getURI(i)+"\"\n");
            }
            writer.write("xsi:schemaLocation=\"");
            for (int i=1;i<uriRegistry.numberOfRegisteredURI();i++) {
                String uri=uriRegistry.getURI(i);
                String schemaLocation=(String)hURISchemaLocation.get(uri);
                if (schemaLocation!=null) {
                    writer.write(uri+" "+schemaLocation+" ");
                }                
            }
            writer.write("\"\n");
        }
    }

    
}



class TypesIterator implements Iterator {

    Iterator allTypeDefinitions;
    Iterator currentTypeDefinitions;
    boolean finish = false;

    public TypesIterator(SetOfDefinitions sod){
        allTypeDefinitions = sod.values().iterator();
        if (allTypeDefinitions.hasNext())
            currentTypeDefinitions = ((TypeDefinitions)allTypeDefinitions.next()).types();
        else 
            finish = true;
    }
    
    // test if there is more type definition
    // moreover set the currentTypeDefinitions if necessary
    public boolean hasNext(){
        // already decided it is finished
        if (finish) return false;

        if (currentTypeDefinitions.hasNext()) return true;
        // no more typeDefinition in this iterator

        // is there any other TypeDefinitions in the SetOfDefinitions
        if (allTypeDefinitions.hasNext()) 
            {
                // set the new TypeDefinition iterator 
                currentTypeDefinitions = ((TypeDefinitions)allTypeDefinitions.next()).types();
            }
        // nothing more, definitively finished
        else 
            {
                finish = true;
                return false;
            }
        
        return currentTypeDefinitions.hasNext();
    }
    
    public Object next(){
        // important: test and ALSO SET the currentTypeDefinitions
        if (!hasNext()) throw new NoSuchElementException();
        return currentTypeDefinitions.next();
    }

    public void remove() {throw new UnsupportedOperationException();}

    //

}
