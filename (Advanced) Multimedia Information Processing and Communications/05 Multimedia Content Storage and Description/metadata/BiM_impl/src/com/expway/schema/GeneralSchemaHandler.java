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

package com.expway.schema;

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

import org.xml.sax.helpers.NamespaceSupport;


import java.io.IOException;
import java.io.FileReader;
import java.io.File;

import com.expway.util.LocalHandler;
import com.expway.util.HandlerException;
import com.expway.util.HandlerExceptionHandler;
import com.expway.util.MyNamespaceSupport;

import com.expway.schema.instance.SchemaException;
import com.expway.schema.instance.SchemaObject;
import com.expway.schema.instance.Schema;
import com.expway.schema.instance.SchemaMessages;
import com.expway.schema.utils.ErrorHandler;
import com.expway.schema.utils.StringMap;



import com.expway.util.URIRegistry;

/**
 * general comments j'ai changer tous les raw par des local pour avoir directement les elements
 * il faut peut être le faire dans les handler
 * ici i.e curentHandler.startElement(uri,local,local);
 */

public class GeneralSchemaHandler 
    extends DefaultHandler 
    implements HandlerExceptionHandler , ErrorHandler {
    public GeneralSchemaHandler(String fileName){
        super();
        inputFileName = fileName;
    }
    //
    // Constants
    //

    /** Default parser name. */
    protected static final String 
        DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";


    // true si une erreur fatal est apparu
    protected boolean FATAL = false;

    protected    LocalHandler currentHandler;
    Locator locator;
    boolean DEBUG = false;
    MyNamespaceSupport myNamespaceSupport = new MyNamespaceSupport();

    //le nom du fichier a parser
    String inputFileName;

    //le nom du fichier RWS
    protected String outputRWS = "";

    // le schema
    Schema schema;

    public Schema getSchema(){return schema;}


    protected void setOutputRWS(String outputRWSFileName){
        outputRWS = outputRWSFileName;
    }



    // implementation of org.xml.sax.ContentHandler interface

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @exception org.xml.sax.SAXException <description>
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (FATAL) return;
        try{
            if (ignorableCharacters (ch,start,length)) return;
            
            if (currentHandler!=null) 
                currentHandler.characters(ch,start,length);
            
        } catch (HandlerException exp){
            handlerError(exp);
        }
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
    
    /**
     *
     * @exception org.xml.sax.SAXException <description>
     */
    public void endDocument() throws SAXException {
        if (FATAL) return;
        try{

        } 
        catch (Exception e){}
        /*catch (SchemaException e){
          } catch (IOException e){e.printStackTrace();}*/
        // TODO: implement this org.xml.sax.ContentHandler method
    }
    
    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @exception org.xml.sax.SAXException <descripton>
     */
    public void endElement(String uri, String local, String raw) throws SAXException {
        if (FATAL) return;
        if (DEBUG)  System.out.println("***** END"+locator.getLineNumber() +"uri " +uri + " local " +local +" raw "+raw+" *****\n");
        try{
            if (currentHandler!=null) 
                currentHandler = currentHandler.endElement(uri,local,local);
        } catch (HandlerException e){
            handlerError(e);
        }

    }

    /**
     *
     * @param param1 <description>
     * @exception org.xml.sax.SAXException <description>
     */
    public void endPrefixMapping(String param1) throws SAXException {
        myNamespaceSupport.endPrefixMapping(param1);
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @exception org.xml.sax.SAXException <description>
     */
    public void ignorableWhitespace(char[] param1, int param2, int param3) throws SAXException {
        if (FATAL) return;
        // TODO: implement this org.xml.sax.ContentHandler method
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @exception org.xml.sax.SAXException <description>
     */
    public void processingInstruction(String param1, String param2) throws SAXException {
        if (FATAL) return;
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
        if (FATAL) return;
        // TODO: implement this org.xml.sax.ContentHandler method
    }

    /**
     *
     * @exception org.xml.sax.SAXException <description>
     */
    public void startDocument() throws SAXException {
        if (FATAL) return;
        // TODO: implement this org.xml.sax.ContentHandler method
    }

    /**
     *
     * @param uri <description>
     * @param local <description>
     * @param raw <description>
     * @param attrs <description>
     * @exception org.xml.sax.SAXException <description>
     */
    public void startElement(String uri, String local, String raw, Attributes attrs) 
        throws SAXException {
        if (FATAL) return;
        try{
            if (DEBUG)  
                System.out.println( "\n**************************************************************"
                                    +"\n*****         START          "+raw + " " +local+" " +uri
                                    +"\n**************************************************************");
            if (currentHandler!=null)        
                // TODO changer local for raw partout dans le handler
                currentHandler = currentHandler.startElement(uri,local,local,attrs);
            else if (local.equals(SchemaSymbols.ELT_SCHEMA)){
                
                SchemaHandler schemaHandler = new SchemaHandler();
                schemaHandler.setLocator(locator);
                schemaHandler.setMyNamespaceSupport(myNamespaceSupport);
                schemaHandler.setErrorHandler(this);
                schemaHandler.init(uri,local,local,attrs);
                myNamespaceSupport.pushURI(uri);
                schemaHandler.setSchemaFileName(inputFileName);
                //             schemaHandler.setNameSupport(myNamespaceSupport);
                currentHandler = schemaHandler;
                schemaHandler.schema.setErrorHandler(this);
                schema = schemaHandler.getSchema();
            } else throw (new SAXNotRecognizedException(local + " is not recognize"));              
        } catch (HandlerException exp){
            handlerError(exp);
        }
    }
    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @exception org.xml.sax.SAXException <description>
     */
    public void startPrefixMapping(String param1, String param2) throws SAXException {
        myNamespaceSupport.startPrefixMapping(param1,param2);
    }


    //
    // ErrorHandler methods
    //
    // traitement du handler Exception
    void handlerErrorEncapsulate(SAXException e) throws SAXException {
        HandlerException exp = (HandlerException)e.getException();
        if (exp != null)
            handlerError(exp);
    }

    public void handlerError(HandlerException exp) {
       
        if (exp.isFatal()){
            System.err.println("[FATAL] "+
                               exp.getMessage());
            //            System.exit(0);
        }


        if (exp.isError()){
            System.err.println("[Error] "+
                               exp.getMessage());

        }

    }
    // creation d'une SAXParse Exception qui encapsule l'erreur;
    SAXParseException createSAXParesException(HandlerException e){
        return new SAXParseException(e.getMessage(),new LocatorImpl(locator),e);
    }

    // Interface MPEG7ErrorHandler 
    public void schemaError(SchemaException e) throws SchemaException{
        System.out.println("in "+ ((e.getSource()!= null && e.getSource().getName()!=null)
                                   ? e.getSource().getName() 
                                   :"an anonymous object"));
        System.out.println("       "+e.getMessage()+"\n");//TODO       
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
    }

    /** Fatal error. */
    public void fatalError(SAXParseException ex) throws SAXException {
        System.err.println("[Fatal Error] "+
                           getLocationString(ex)+": "+
                           ex.getMessage());
        throw ex;
    }

    /** Returns a string of the location. */
    protected String getLocationString(SAXParseException ex) {
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

 
    

    // crer le schema
    static public Schema getSchemaWithoutRealize(String uri) throws Exception {
        //        if (Schema.READSCHEMA.containsKey(uri))
        //            return (Schema)Schema.READSCHEMA.get(uri);
        GeneralSchemaHandler handler = new GeneralSchemaHandler(uri);
        System.out.println("Reading schema : "+uri);

        XMLReader parser = (XMLReader)Class.forName(DEFAULT_PARSER_NAME).newInstance();
        try {
            parser.setFeature( "http://xml.org/sax/features/validation", false);
            parser.setFeature( "http://xml.org/sax/features/namespaces", true);
            parser.setFeature( "http://apache.org/xml/features/validation/schema", false);
            /* a conserver pour voir les features possibles
               
               String[] features = ((org.apache.xerces.parsers.SAXParser)parser).getFeaturesRecognized();
               for (int j = 0; j<features.length;j++)
               try {
               System.out.println(features[j]+" "+parser.getFeature(features[j]));
               } catch (SAXNotRecognizedException e){
               System.out.println(features[j]+ " NOT RECOGNIZED");
                
               }
            */

            parser.setContentHandler(handler);
            parser.setErrorHandler(handler);
            //        parser.parse(new InputSource(new FileReader(uri)));
            parser.parse(new InputSource((uri)));
            Schema.READSCHEMA.put(uri,handler.getSchema());
            return handler.getSchema();
        }catch(SAXException sae){
            System.out.println("SAX EXCEPTION (" + sae + ")");
            System.out.println(" Embedded Exception = " + sae.getException());
            sae.getException().printStackTrace();
            throw sae;
        } catch(Exception e){
            System.out.println("ERRORXXXX=" + e);
            throw e;
        } finally {
            handler.currentHandler=null;
            handler.schema=null;
            handler=null;
            parser=null;
        }

    }

    static public Schema getSchema(String uri, boolean bVerbose) throws Exception{
        Schema localschema1;
        try {            
            if (bVerbose) 
                System.out.println("on parse le fichier "+uri);
            localschema1= GeneralSchemaHandler.getSchemaWithoutRealize(uri);        
            // handler.setOutputRWS(outputFile);      
            if (bVerbose) {
                System.out.println("#######################################################");
                System.out.println("##                   REALIZE                         ##");
            }
            localschema1.realize();
            return localschema1;
        } finally {
            localschema1=null;
        }
    }

    static public Schema getAndRawSchema(String uri, String outputFile, boolean bVerbose) 
        throws Exception {
        Schema localschema2= null;
        try{ 
            localschema2 = getSchema(uri,bVerbose);
            if (!localschema2.isValid()){
                if (bVerbose) 
                    System.out.println("#################       FIN        ###############");
                return localschema2;
                }
                   
            if (bVerbose) {
                System.out.println();
                System.out.println("######################################################");
                System.out.println("##                  RAw schema                       ##");
                System.out.println();
            }
            localschema2.toRawSchema(outputFile);
            return localschema2;
        } catch(Exception ee) {
            if (bVerbose)
                ee.printStackTrace(System.err);
            else throw ee;
        } finally {
            localschema2=null;
        }

        return null;
    }

    // Greg (pour libérer de la place mémoire)
    static public void freeStatic() {
        StringMap.freeStatic();
        HandlerManager.freeStatic();
        Schema.freeStatic();
        SchemaMessages.freeStatic();
    }

    static public void generateSchema(String uri, String outputFile, boolean bdoc, boolean bVerbose) 
        throws Exception {
        Schema localschema3 = getAndRawSchema(uri,outputFile,bVerbose);
        try {
            

            

            if (bVerbose) {
                System.out.println("#################       FIN        ###############");
                System.out.println("FIN");
            }
        } catch(Exception ee) {
            if (bVerbose)
                ee.printStackTrace(System.err);
            else throw ee;
        } finally {
            localschema3=null;
        }
    }
}
