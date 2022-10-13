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

import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Stack;
import java.util.Iterator;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.LocatorImpl;

import javax.xml.parsers.SAXParserFactory;  
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

//import org.apache.xerces.parsers.SAXParser;



import com.expway.schema.xml.XMLSchemaInstance;
import com.expway.schema.GeneralSchemaHandler;

import com.expway.tools.automata.ActivityTokenNode;
import com.expway.tools.automata.StateNode;
import com.expway.tools.automata.TransitionNode;

import com.expway.tools.expression.TypeDefinitionInstantiator;
import com.expway.tools.expression.TypeDefinitions;
import com.expway.tools.expression.TypeDefinition;
import com.expway.tools.expression.SetOfDefinitions;
import com.expway.tools.expression.ElementDefinition;
import com.expway.tools.expression.DefinitionException;

import com.expway.tools.compression.TypeEncoder;
import com.expway.tools.io.*;

import com.expway.tools.utils.MethodsBag;

import com.expway.util.MyNamespaceSupport;
import com.expway.util.LocalHandler;
import com.expway.util.HandlerException;
import com.expway.util.HandlerExceptionHandler;

public class GeneralBinaryHandler extends DefaultHandler implements HandlerExceptionHandler {
    

    // Cache de la chaine de characters() courante
    private String currentCachedString="";

    SetOfDefinitions setOfDefinitions; 
    MyNamespaceSupport myNamespaceSupport;
    int bitsOfStruture = 0;
    int sizeInBits = 0;
    int iHeaderBitsize=0;

    String sFirstTypeName=null;
    public void setFirstTypeName(String s) { sFirstTypeName=s; }
    boolean bFinished=false;

    public int getBitsOfStructure(){return bitsOfStruture;}
    public void addBitsOfStructure(int bs){bitsOfStruture += bs;}
    public void setFinalSizeInBits(int bs){sizeInBits += bs;}
    public int getFinalSizeInBits(){return sizeInBits;}
    public SetOfDefinitions getSetOfDefinitions() { return setOfDefinitions;}

    // ERROR
    public boolean FATAL = false;
    public boolean hasError(){return (myError != null);} 
    HandlerException myError;
    public HandlerException getLastError(){
        return myError;
    }

    // Constants
    static final boolean DEBUG = false;
    private static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
    static private final boolean WRITE_DECODER_CONFIG=true;

    protected String sInput=null ,sOutput = null;
    private String firstElementName = null;
    private String firstURI = null;
    int iTotalCharacters=0;

    LocalHandler currentHandler;
    protected Locator locator;

    // implementation of org.xml.sax.ContentHandler interface

    public GeneralBinaryHandler(String input, String output){
        initialise();
        sInput=input;
        sOutput=output;
    }

    public GeneralBinaryHandler(){
        initialise();
    }

    public void setInput(String input) {
        sInput=input;
    }

    public void setOutput(String output) {
        sOutput=output;
    }

    // Vidange des schemas
    public void clearSetOfDefinitions() {
        setOfDefinitions=null;
    }


    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @exception org.xml.sax.SAXException <description>
     */

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (FATAL) return;
        
        iTotalCharacters+=length;
        
        if (ignorableCharacters (ch,start,length)) return;
        currentCachedString+=new String(ch,start,length);
    }
  
    boolean ignorableCharacters(char[] ch, int start, int length){
        if (FATAL) return true;
        int i=0;
        while (i<length){
            if (ch[start+i]!=' ' && ch[start+i]!='\n' && ch[start+i]!='\t')
                return false;
            i++;
        }
        return true;
    }

    // Pour gérer le cache de characters, qui peut être appelé 2 fois alors
    // qu'il n'y a qu'une seule string
    private void cacheCharacters() throws SAXException {
        int size=currentCachedString.length();
        if (size==0) return;

         try{
             if (currentHandler!=null) {
                 
                 char [] chars=new char[size];
                 currentCachedString.getChars(0,size,chars,0);
                 currentHandler.characters(chars,0,size);
             }
         } catch (HandlerException exp){
             handlerError(exp);
         }
         currentCachedString="";        
    }
    
    /**
     *
     * @exception org.xml.sax.SAXException <description>
     */
    public void endDocument() throws SAXException {
        bFinished=true;

        cacheCharacters();
        if (FATAL) return;

        // On ecrit le fichier si sOutput est non null
        if (sOutput!=null) {
            try {
                Chunk cw=getSubtreeChunk();

                BitOutputStream fos = new BitOutputStream(new FileOutputStream(sOutput));
                cw.writeYourselfInto(fos);
                fos.close();
            } catch (Exception ioe){
                handlerError(new HandlerException(ioe,"Error while saving "+sOutput+" : "+ioe,HandlerException.TYPE_FATAL));
            }
        }
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @exception org.xml.sax.SAXException <descripton>
     */
    public void endElement(String uri, String local, String raw) throws SAXException {
        cacheCharacters();
        if (FATAL) return;

        //          if (DEBUG)  System.out.println("***** END uri " + uri + 
        //                                         " local " +local +
        //                                         " raw "+raw+
        //                                       " +ligne + "+locator.getLineNumber()+"*****\n");

        try{
            if (currentHandler!=null) {
                LocalHandler lh = currentHandler;
                currentHandler = currentHandler.endElement(uri,local,raw);
                if (lh instanceof BinaryHandler) addBitsOfStructure(((BinaryHandler)lh).getBitsOfStructure());
            }
        } catch (HandlerException e){
            handlerError(e);
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        myNamespaceSupport.endPrefixMapping(prefix);
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @exception org.xml.sax.SAXException <description>
     */
    public void ignorableWhitespace(char[] param1, int param2, int param3) throws SAXException {
        // TODO: implement this org.xml.sax.ContentHandler method
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @exception org.xml.sax.SAXException <description>
     */
    public void processingInstruction(String param1, String param2) throws SAXException {
        // TODO: implement this org.xml.sax.ContentHandler method
    }

    /**
     *
     * @param param1 <description>
     */
    public void setDocumentLocator(Locator param1) {
        locator = param1;
    }

    /**
     *
     * @param param1 <description>
     * @exception org.xml.sax.SAXException <description>
     */

    public void skippedEntity(String param1) throws SAXException {
        // TODO: implement this org.xml.sax.ContentHandler method
        System.out.println("skipped:"+param1);
    }

    /**
     *
     * @exception org.xml.sax.SAXException <description>
     */
    public void startDocument() throws SAXException {
        // TODO: implement this org.xml.sax.ContentHandler method
    }

    // Charge dynamiquement un schema en fonction des attributs
    private void handleSchemaDynamicLoading(Attributes attrs) throws HandlerException {
        Stack s;
       
        try {
            s=XMLSchemaInstance.processSchemaLocation(attrs);
        } catch(Exception e) {
            throw new HandlerException(e,e.getMessage(),HandlerException.TYPE_WARNING);
        }    
       
        // Si il y a des couples (Schema, URI), on les lit et on realize le schéma
        if (!s.empty()) {
            while (!s.empty()) {
                String sFilename=(String)s.pop();
                String sURI=(String)s.pop();                
                locateAndLoadSchema(sURI,sFilename);                               
            }            
        }        
    }
    
    // Greg (pour libérer de la place mémoire)
    static public void freeStatic() {
        ActivityTokenNode.freeStatic();
        TransitionNode.freeStatic();
        StateNode.freeStatic();
        GeneralSchemaHandler.freeStatic();
    }    
   
    /**
     *
     * @param uri <description>
     * @param local <description>
     * @param raw <description>
     * @param attrs <description>
     * @exception org.xml.sax.SAXException <description>
     */
    public void startElement(String uri, String local, String raw, Attributes attrs) throws SAXException {
        cacheCharacters();
        if (FATAL) return;     

        try{
            if (DEBUG) System.out.println(" ===> Start element: "+local+" (local)       "+ raw+ " (raw)" + "   uri=" + uri);
            
            // on est deja en train de compresser quelque chose
            if (currentHandler!=null)
                {
                    currentHandler = currentHandler.startElement(uri,local,raw,attrs);
                } 
            // le tout debut - encore aucun handler
            else 
                {
                    // Tentative de chargement dynamique d'un schema grace à xsi:schemaLocation
                    // Elle ne sera faite qu'une fois, au premier tag ouvrant de la description
                    try {
                        handleSchemaDynamicLoading(attrs);
                    } catch (HandlerException exp){
                        handlerError(exp);
                    }
                    
                    // Réalisation du setOfDefinitions au premier element
                    try {
                        setOfDefinitions.realize();
                    }catch(DefinitionException de){
                        throw new HandlerException(de,de.getMessage(),HandlerException.TYPE_ERROR);
                    }

                    // Le schéma a bien été chargé ?
                    if (!setOfDefinitions.getURIRegistry().isSchemaLoadedForURI(uri))
                        throw new HandlerException(null,"No schema have been loaded for URI:"+uri,HandlerException.TYPE_ERROR);

                    // Composition du cName, compactedName, sous la forme q5:Mpeg7Main, par exemple
                    String cName;
                    try {
                        cName=setOfDefinitions.getURIRegistry().getCompactName(uri,local);
                    } catch(Exception e) {
                        throw new HandlerException(e,"No schema have been loaded for URI:"+uri,HandlerException.TYPE_ERROR); 
                    }

                    CodingContext initialContext=new CodingContext(setOfDefinitions);
                    
                    TypeDefinitions typeDefinitions=setOfDefinitions.getDefinitions(uri);                   

                    if (typeDefinitions!=null) {
                        ElementDefinition elt = typeDefinitions.getElementDefinition(cName);                        
                        TypeDefinition atd = null;
                        firstElementName = cName;
                        firstURI= uri;

                        // Est-ce que le premier élément est global ?
                        if (elt!=null)
                            atd = elt.getTypeDefinition();
                        else {
                            // Non, alors connait-on le type du premier élement ?
                            if (sFirstTypeName!=null) atd=typeDefinitions.getTypeDefinition(sFirstTypeName);
                        }
 
                        if (atd != null)
                            {
                                if (DEBUG)  System.out.println("First Element \"" + elt.getName() + "\" type \"" + atd.getName()+"\"");
                                if (DEBUG)  System.out.println("");                                
                                currentHandler = new BinaryHandler(atd.newInstance(),setOfDefinitions,initialContext);
                                currentHandler.setLocator(locator);
                                currentHandler.setMyNamespaceSupport(myNamespaceSupport);
                                myNamespaceSupport.pushURI(uri);
                                currentHandler.setErrorHandler(this);                             
                                currentHandler.init(uri,local,raw,attrs);
                            } 
                        else 
                            throw new HandlerException(null,"Unknown element "+cName+" in URI "+uri,HandlerException.TYPE_ERROR); 
                    }
                    else 
                        throw new HandlerException(null,"No schema for URI "+uri+" was loaded. Unknown prefix for element "+
                                                   raw,HandlerException.TYPE_ERROR);
                }
        } catch (HandlerException exp){
            handlerError(exp) ;
        }        
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @exception org.xml.sax.SAXException <description>
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        myNamespaceSupport.startPrefixMapping(prefix,uri);
        if (FATAL) return;
    }


    //
    // ErrorHandler methods
    //

    public void handlerError(HandlerException exp) {
        myError = exp;
        
        if (exp.isFatal()){
            System.err.println("[FATAL] "+ getLocationString() + exp.getMessage());
            //exp.printStackTrace();
            FATAL = true;
        }
        if (exp.isError()) {
            System.err.println("[Error] "+ getLocationString() + exp.getMessage());
            //exp.printStackTrace();
            FATAL = true; 
        }
        if (exp.isWarning()) {
            System.err.println("[Warning] "+ getLocationString() + exp.getMessage());
        } 
        
    }

    /** Warning. */
    public void warning(SAXParseException ex) {
        System.err.println("[Warning] "+
                           getLocationString(ex)+": "+
                           ex.getMessage());
      
    }

    /** Error. */
    public void error(SAXParseException ex) {
        System.err.println("[Error] "+
                           getLocationString(ex)+": "+
                           ex.getMessage());
        ex.printStackTrace();
    }

    /** Fatal error. */
    public void fatalError(SAXParseException ex) {
        System.err.println("[Fatal Error] "+
                           getLocationString(ex)+": "+
                           ex.getMessage());

        ex.printStackTrace();
    }

    private String getLocationString() {
        String s="";
        try {
            s="L"+locator.getLineNumber()+" ";
        } catch(Exception e) {
        }

        return s;
    }

    /** Returns a string of the location. */
    private String getLocationString(SAXParseException ex) {
        StringBuffer str = new StringBuffer();

        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1) 
                systemId = systemId.substring(index + 1);
            str.append(systemId);
        }
        str.append(':');
        str.append(ex.getLineNumber());
        str.append(':');
        str.append(ex.getColumnNumber());

        return str.toString();

    } // getLocationString(SAXParseException):String

    public void initialise() {
        currentHandler=null;
        
        currentCachedString="";

        if (setOfDefinitions==null) { setOfDefinitions=new SetOfDefinitions(); }
        myNamespaceSupport=new MyNamespaceSupport(setOfDefinitions.getURIRegistry());

        FATAL=false;
        bitsOfStruture=0;
        sizeInBits=0;
        bFinished=false;
        iHeaderBitsize=0;

        CodingParameters.reset();

        // Libere tout les trucs statiques du projet, les caches, les pools
        freeStatic();
    }

    public void binarise() throws SAXException {
        
        SAXParserFactory factory = null;
        try {
            factory = SAXParserFactory.newInstance();
        } catch (javax.xml.parsers.FactoryConfigurationError e) {
            System.err.println("Something went wrong in the parser configuration: " + e);
            System.err.println("The exception is " + e.getException());
            System.err.println("Stack trace: " );
            e.printStackTrace();
        }

        try {
            
            // create the actual parser 
            XMLReader parser = factory.newSAXParser().getXMLReader();

            parser.setFeature( "http://xml.org/sax/features/validation", false);
            parser.setFeature( "http://xml.org/sax/features/namespaces", true);
            parser.setFeature( "http://apache.org/xml/features/validation/schema", false);

            parser.setContentHandler(this);
            parser.setErrorHandler(this);

            System.out.println("File to encode : " + sInput);
            System.out.println("Encoded File   : " + sOutput);
            System.out.println("Parsing...");
            
            

            // parse the input...!
            parser.parse(new InputSource(sInput));

            

            System.out.println("RESULTS : ");
            int fileInLength=(int)new File(sInput).length();
            System.out.println("  Total input bits  = " +fileInLength*8+" ("+fileInLength+" bytes)");
            System.out.println("  Total output bits = " +sizeInBits+" ("+sizeInBits/8+" bytes) ratio="+
                               ((double)(fileInLength*8)/sizeInBits));
            System.out.println("  Total output bits of structure = " + getBitsOfStructure());

            writeDecoderConfigFile(sOutput);
        } 
        catch(SAXException sae){
            System.out.println("SAX EXCEPTION (" + sae + ")");
            System.out.println(" Embedded Exception = " + sae.getException());
            sae.getException().printStackTrace();
        }
        catch(IOException ioe){
            System.out.println("IO EXCEPTION (" + ioe + ")");
            ioe.printStackTrace();
        }        
        catch (ParserConfigurationException cfe) {
            System.out.println("Parser configuration error (" + cfe + ")");
        }
        finally {
            // Libere tout les trucs statiques du projet, les caches, les pools
            freeStatic();
            currentHandler=null;
        }
    }

    // Ecriture d'un fichier externe .decoderConfig avec
    // - La premiere URI
    // - L'ensemble des couples (uri,schema file)
    // Ecriture <=> structure et data sont présentes
    public void writeDecoderConfigFile(String fileOut) throws IOException {      
        if (!FATAL && bFinished && WRITE_DECODER_CONFIG) {
            if (CodingParameters.bWriteData && CodingParameters.bWriteStructure) {               
                if (DEBUG) System.out.println("************ Decoder Configuration Mapping Table:");
                if (DEBUG)  setOfDefinitions.getURIRegistry().dumpURIs();
                setOfDefinitions.saveDefinitionForDecoderConfiguration(new File(fileOut+".decoderConfig"),firstURI);
                if (DEBUG) System.out.println("************");
            }                
        }
    }

	
    public void locateAndLoadSchema(String uri,String schemaName) throws HandlerException {
        
        try {
            setOfDefinitions.locateAndLoadSchema(uri,schemaName,sInput); // sInput = fichier a encoder;
            
        } catch(Exception e) {
            System.out.println("Loading schema FAILED : " + e.getMessage());
            e.printStackTrace();  
            throw new HandlerException(e.getMessage(),HandlerException.TYPE_WARNING);
        } 
        
    }

    public void locateAndLoadSchema(String schemaName) throws HandlerException {
        locateAndLoadSchema("",schemaName);
    }

    // ------------------------------------------------------------

    public static void binarise(String input,String output) throws Exception {
        GeneralBinaryHandler gbh=new GeneralBinaryHandler();
        gbh.setInput(input);
        gbh.setOutput(output);
        gbh.binarise();
    }

    public int getHeaderBitsize() {
        return iHeaderBitsize;
    }

    public Chunk getSubtreeChunk() {
        try {
            if (bFinished==false) return null;

            ChunkWriter cw = new ChunkWriter();
            
            // Connait-on le type de façon extérieure ? Dans ce cas on le code pas.
            if (sFirstTypeName==null) {
                TypeDefinitions typeDefinitions=setOfDefinitions.getDefinitions(firstURI);
                int elementIndex=typeDefinitions.getElementNumber(firstElementName);
                int numberOfElements=typeDefinitions.getNumberOfElement();
                int codingLength=MethodsBag.getCodingLength(numberOfElements);

                if (elementIndex!=-1) {
                    cw.writeInt(elementIndex,codingLength);
                    addBitsOfStructure(codingLength);
                    iHeaderBitsize+=codingLength;
                } else {
                    System.out.println("[Error] Unkwown global element elt="+firstElementName+" without a textual_access_unit");
                }
            }

            // Ecriture des 4 bits de CodingParameters
            CodingParameters.writeHeaderInto(cw);
            iHeaderBitsize+=4;

            // Flush de tous les codecs
            CodingParameters.flushAllPendingCodecs();

            // Le subtree
            TypeEncoder theTopLevelEncoder = (TypeEncoder)currentHandler.getCreation();
            Chunk c = theTopLevelEncoder.getCodingWithContext(false);
            if (c!=null) c.writeYourselfInto(cw);
            setFinalSizeInBits((int)cw.sizeInBits());
            return cw;
        } catch (HandlerException e){
            handlerError(e);
            return null;
        }
    }

    
}
