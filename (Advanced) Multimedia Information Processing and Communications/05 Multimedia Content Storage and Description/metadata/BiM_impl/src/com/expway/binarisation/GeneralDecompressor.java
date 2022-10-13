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

package com.expway.binarisation;

import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.Writer;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.util.Stack;

import com.expway.tools.io.BitToBitDataInputStream;
import com.expway.tools.compression.TypeInstance;
import com.expway.tools.compression.ComplexTypeInstance;
import com.expway.tools.compression.ComplexComplexTypeInstance;
import com.expway.tools.compression.SimpleTypeInstance;
import com.expway.tools.compression.SimpleComplexTypeInstance;
import com.expway.tools.compression.TypeEncoder;
import com.expway.tools.compression.DecodingException;
import com.expway.tools.compression.ParsingException;
import com.expway.tools.expression.SetOfDefinitions;
import com.expway.tools.expression.TypeDefinitions;
import com.expway.tools.expression.TypeDefinition;
import com.expway.tools.expression.ElementDefinition;
import com.expway.tools.utils.MethodsBag;



public class GeneralDecompressor {
    String sInput=null;
    String sOutput=null;
    String sFirstURI=null;
    String sFirstElementName=null;
    String sFirstTypeName=null;

    

    final static boolean DEBUG=false;
    SetOfDefinitions setOfDefinitions = null;

    // STACK 
    TypeEncoderNode last;

    public GeneralDecompressor() {
        setOfDefinitions=new SetOfDefinitions();
    }

    public GeneralDecompressor(SetOfDefinitions se){
        setOfDefinitions=se;
    }

    public void setInput(String input) {
        sInput=input;
    }

    public void setOutput(String output) {
        sOutput=output;
    }
    
    public void setFirstURI(String uri) {
        sFirstURI=uri;
    }

    public String getFirstURI() {
        return sFirstURI;
    }

    public void setFirstElementName(String s) {
        try {
            sFirstElementName=setOfDefinitions.getURIRegistry().getCompactName(sFirstURI,s);
        } catch (Exception e) {
            System.out.println("[Warning] Unable to create internal name with uri="+sFirstURI+" and elt="+s);
        }
    }

    public void setFirstTypeName(String s) {
        try {
        sFirstTypeName=setOfDefinitions.getURIRegistry().getCompactName(sFirstURI,s);
        } catch (Exception e) {
            System.out.println("[Warning] Unable to create internal name with uri="+sFirstURI+" and elt="+s);
        }
    }

    public void initialise() {
        CodingParameters.reset();
        
        last=null;

        // Réalisation des schémas
        try {
            setOfDefinitions.realize();
        } catch(Exception e) {
            System.out.println("Schemas realization exception:"+e);
        }
    }
   
    class TypeEncoderNode {
        TypeEncoderNode next = null;
        TypeEncoder theDecoder = null;
        
        public TypeEncoderNode(TypeEncoder te){
            theDecoder = te;
        }
        public TypeEncoder getTypeEncoder(){
            return theDecoder;
        }
    }

    TypeEncoder getLast(){
        return last.theDecoder;
    }

    void push(TypeEncoder c){
        TypeEncoderNode ten = new TypeEncoderNode(c);
        ten.next=last;
        last = ten;
    }
    
    TypeEncoder pop(){
        if (last == null) return null;
        TypeEncoderNode c = last;
        last = last.next;
        c.next = null;
        return c.getTypeEncoder();
    }

    public void loadExternalDecoderConfigFilesToLoadAndRealizeSchemas(String inputName) throws Exception {
        // Lecture du DecoderConfig
        if (new File(inputName+".decoderConfig").exists()) {            
            sFirstURI=setOfDefinitions.loadDefinitionForDecoderConfiguration(new File(inputName+".decoderConfig"),inputName); 
        }    

        // Les schémas sont réalisés ici
        initialise();
    }

    // Init à partir d'un fichier Input
    public void decompress() throws Exception {
        FileInputStream inputFile = new FileInputStream(sInput);
        Writer outputFile = new FileWriter(sOutput);

        // Teste et lit le .uri et le .decoderConfig
        // Le firstURI est lu ici, si le fichier .uri existe
        loadExternalDecoderConfigFilesToLoadAndRealizeSchemas(sInput);

        BitToBitDataInputStream dis = new BitToBitDataInputStream(inputFile);
        try {
            decompress(dis,outputFile);
        } catch(DecodingException de){
            System.out.println("Error while decoding : " + de.getMessage());
        } catch(ParsingException pe){
            System.out.println("Error while decoding : " + pe.getMessage());
        } catch(IOException ioe){
            System.out.println("Error while decoding : " + ioe.getMessage());
        } finally {
            // On remet le décompresseur à zéro
            initialise();
        }
        outputFile.close();
    }

    // Init capable de savoir ou non (en fonction de sFirstElementName et sFirstTypeName) 
    // s'il faut lire le numéro de l'element dans le bitstream
    // Avant de l'appeler, il faut que les schémas aient été chargés et réalisés en
    // mémoire (avec loadExternalDecoderConfigFilesToLoadAndRealizeSchemas() par exemple)
    public void decompress(BitToBitDataInputStream dis, Writer output) 
        throws DecodingException,ParsingException,IOException {
        try {
        
        // La première URI, doit être connue à ce niveau
        if (sFirstURI==null) throw new DecodingException("Unknown first URI : Unable to start decoding bitstream");
        
        TypeDefinitions theDefinitions=setOfDefinitions.getDefinitions(sFirstURI);

        if (theDefinitions==null) throw new DecodingException("No realized schema for the URI="+sFirstURI);

        // Si le nom de l'élément est inconnu, on doit lire dans le bitstream sous forme de nombre
        if (sFirstElementName==null) {
            int numberOfElements=theDefinitions.getNumberOfElement();
            int codingLength=MethodsBag.getCodingLength(numberOfElements);
            long elementIndex = dis.readInt(codingLength);
            sFirstElementName=theDefinitions.getElementName((int)elementIndex);

            if (sFirstElementName==null) throw new DecodingException("Unable to know the first element name");
        }

        if (DEBUG) System.out.println("First Element name = " + sFirstElementName);
            
        // Le premier décodeur
        ElementDefinition ef = theDefinitions.getElementDefinition(sFirstElementName);
        TypeDefinition tf;

        // Si l'élement n'est pas global, on ne peut connaitre son type que par sFirstTypeName
        if (ef==null) tf=theDefinitions.getTypeDefinition(sFirstTypeName);
        else tf=ef.getTypeDefinition();
        if (tf==null) throw new DecodingException("Unable to know the first type name for the element name="+sFirstElementName);

        // Création de l'instance
        TypeInstance ti = (TypeInstance)tf.newInstance();
        ti.setElementName(sFirstElementName);

        decompressMain(dis,output,ti,theDefinitions); 
        } finally {
            sFirstElementName=null;
            sFirstTypeName=null;
        }
    }

   private void decompressMain(BitToBitDataInputStream dis, Writer output, TypeInstance ti,TypeDefinitions theDefinitions) 
        throws DecodingException,ParsingException,IOException {

        // Lecture des 4 bits de CodingParameters
        CodingParameters.readHeaderFrom(dis);

        // La pile de CodingContext et initialisation
        CodingContext firstContext=new CodingContext(setOfDefinitions);
        CodingContext newContext=null;

        if (ti instanceof ComplexTypeInstance) {
            newContext=firstContext.readContext(dis);
            ((ComplexTypeInstance)ti).setCodingContext(newContext);
        }

        if (DEBUG) System.out.println("************ Decoder Configuration Mapping Table:");
        if (DEBUG) setOfDefinitions.getURIRegistry().dumpURIs();
        if (DEBUG) System.out.println("************");

        

        push(ti);
        
        ti.setElementWriter(output);
        ti.startDecoding(dis);
        if (CodingParameters.bWriteXMLNSInDecompressFile) setOfDefinitions.writeXMLNSStuff(output);

        while (last!=null){
            if (DEBUG) System.out.println("=> Current TypeInstance decoding : elementName="+((TypeInstance)getLast()).getElementName()+
                                          " type="+getLast().getTypeName());

            TypeEncoder te = getLast().decode(dis);
            
            if (te != null) {
                // Lecture du contexte
                if (te instanceof ComplexTypeInstance) {
                    ComplexTypeInstance tec=(ComplexTypeInstance)te;
                    CodingContext local=(CodingContext)(((ComplexTypeInstance)getLast()).getCodingContext());
                    newContext=local.readContext(dis);
                    tec.setCodingContext(newContext);
                }
                
                te.startDecoding(dis);
                
                if (!te.isPartialInstantiated())
                    push(te);
                else {
                    System.out.println(((TypeInstance)te).getElementName()+" partially instantiated !");
                    //output.write("<"+((TypeInstance)te).getElementName()+" "+
                    //             StreamConstants.PARTIALLY_INSTANTIATED_ATTRIBUTE+"=\"true\"/>\n");
                }
                
            } else {
                
                pop().endDecoding();                
            }
        }
        
    }
    
    // Ajoute un schéma, si celui-ci n'a pas été déclaré dans xsi:schemaLocation
    // Se débrouille entre XSD et RWS, et marche avec un path absolu ou regarde à côté 
    // du fichier d'entrée
    public void locateAndLoadSchema(String uri,String schemaName) throws Exception {
        setOfDefinitions.locateAndLoadSchema(uri,schemaName,sInput);        
    }

    public void locateAndLoadSchema(String schemaName) throws Exception {
        locateAndLoadSchema("",schemaName);
    }
    
    
}







