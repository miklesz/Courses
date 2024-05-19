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

package com.expway.ref;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileReader;

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

import com.expway.binarisation.GeneralBinaryHandler;

public class AccessUnitParser extends DefaultHandler  {
    final static int ACCESS_UNIT_MODE=0;
    final static int NAVIGATION_PATH_MODE=1;
    final static int VALUE_MODE=2;
    final static int NO_MODE=3;

    int mode;
    int iValueModeDepth;
    String auName;

    AccessUnit au;
 
    GeneralBinaryHandler subtreeParser;

    AccessUnitParser(String auname,String schema) {
        auName=auname;
        if (BiMEncoder.bAccessUnitAware) {
            mode=NO_MODE;
        } else {
            mode=VALUE_MODE;
        }
        subtreeParser=new GeneralBinaryHandler();
        subtreeParser.setInput(auname);
        subtreeParser.initialise();
        au=new AccessUnit(schema);
        iValueModeDepth=0;
    }

    public void writeOutput(String outputFilename) throws Exception {
        au.writeOutput(outputFilename);

        // Ecriture des fichiers URI et DecoderConfig
        subtreeParser.writeDecoderConfigFile(outputFilename);
    }

    public void printReport() {
        System.out.println("Input textual filesize (bits):"+new File(auName).length()*8);
        System.out.print("Output binary filesize (bits):"+au.getTotalSizeInBits());
        System.out.println(" (path:"+au.getPathSizeInBits()+
                           " header:"+au.getHeaderSizeInBits()+
                           " payload:"+(au.getSubtreeSizeInBits()-au.getHeaderSizeInBits())+")");
    }

    public void parse() throws Exception {        
        SAXParserFactory factory = null;

        try {
            factory = SAXParserFactory.newInstance();
        } catch (javax.xml.parsers.FactoryConfigurationError e) {
            System.err.println("Something went wrong in the parser configuration: " + e);
            System.err.println("The exception is " + e.getException());
            System.err.println("Stack trace: " );
            e.printStackTrace();
        }

        XMLReader parser = factory.newSAXParser().getXMLReader();

        parser.setFeature( "http://xml.org/sax/features/validation", false);
        parser.setFeature( "http://xml.org/sax/features/namespaces", true);
        parser.setFeature( "http://apache.org/xml/features/validation/schema", false);
        parser.setContentHandler(this);
        parser.setErrorHandler(this);
        parser.parse(new InputSource(auName));
        if (mode==NO_MODE) {
            System.out.println("[Warning] While in access_unit mode, no first tag <MPEG7_AccessUnit> was encountered");
        } else {
            au.setSubtreeChunk(subtreeParser.getSubtreeChunk());
            au.setHeaderSizeInBits(subtreeParser.getHeaderBitsize());
        }
    }

    // implementation of org.xml.sax.ContentHandler interface
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch(mode) {
        case ACCESS_UNIT_MODE:
            break;
        case NAVIGATION_PATH_MODE:
            au.setPath(new String(ch,start,length));
            break;
        case VALUE_MODE:
            subtreeParser.characters(ch,start,length);
            break;
        }
    }

    public void endDocument() throws SAXException {
        switch(mode) {
        case ACCESS_UNIT_MODE:
            break;
        case NAVIGATION_PATH_MODE:
            break;
        case VALUE_MODE:
            subtreeParser.endDocument();
            break;
        }
    }

    public void endElement(String uri, String local, String raw) throws SAXException {
        switch(mode) {
        case ACCESS_UNIT_MODE:
            break;
        case NAVIGATION_PATH_MODE:
            if (raw.equals("location")) mode=ACCESS_UNIT_MODE;
            break;
        case VALUE_MODE:
            iValueModeDepth--;
            if (raw.equals("value") && iValueModeDepth==0) mode=ACCESS_UNIT_MODE;
            else subtreeParser.endElement(uri,local,raw);
            break;
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        switch(mode) {
        case ACCESS_UNIT_MODE:
            break;
        case NAVIGATION_PATH_MODE:
            break;
        case VALUE_MODE:
            subtreeParser.endPrefixMapping(prefix);
            break;
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        switch(mode) {
        case ACCESS_UNIT_MODE:
            break;
        case NAVIGATION_PATH_MODE:
            break;
        case VALUE_MODE:
            subtreeParser.ignorableWhitespace(ch,start,length);
            break;
        }
    }

    public void processingInstruction(String param1, String param2) throws SAXException {
        switch(mode) {
        case ACCESS_UNIT_MODE:
            break;
        case NAVIGATION_PATH_MODE:
            break;
        case VALUE_MODE:
            subtreeParser.processingInstruction(param1,param2);
            break;
        }       
    }

    public void setDocumentLocator(Locator param1) {
        switch(mode) {
        case ACCESS_UNIT_MODE:
            break;
        case NAVIGATION_PATH_MODE:
            break;
        case VALUE_MODE:
            subtreeParser.setDocumentLocator(param1);
            break;
        }
    }

    public void skippedEntity(String param1) throws SAXException {
        switch(mode) {
        case ACCESS_UNIT_MODE:
            break;
        case NAVIGATION_PATH_MODE:
            break;
        case VALUE_MODE:
            subtreeParser.skippedEntity(param1);
            break;
        }
    }

    public void startDocument() throws SAXException {
        switch(mode) {
        case ACCESS_UNIT_MODE:
            break;
        case NAVIGATION_PATH_MODE:
            break;
        case VALUE_MODE:
            subtreeParser.startDocument();
            break;
        }
    }

    public void startElement(String uri, String local, String raw, Attributes attrs) throws SAXException {     
        switch(mode) {
        case ACCESS_UNIT_MODE:
            if (raw.equals("location")) mode=NAVIGATION_PATH_MODE;
            if (raw.equals("value")) mode=VALUE_MODE;
            break;
        case NAVIGATION_PATH_MODE:
            break;
        case VALUE_MODE:
            iValueModeDepth++;

            // On precise le type à l'encodeur en mode AccessUnit
            if (BiMCommandLine.bAccessUnitAware) {
                subtreeParser.setFirstTypeName(au.getPathLastType());
            }

            subtreeParser.startElement(uri,local,raw,attrs);
            break;
        case NO_MODE:
            if (raw.equals("MPEG7_AccessUnit")) mode=ACCESS_UNIT_MODE;
            break;
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        switch(mode) {
        case ACCESS_UNIT_MODE:
            break;
        case NAVIGATION_PATH_MODE:
            break;
        case VALUE_MODE:
            subtreeParser.startPrefixMapping(prefix,uri);
            break;
        }
    }

    // implementation of org.xml.sax.ErrorHandler interface
    public void warning(SAXParseException ex) {
        switch(mode) {
        case ACCESS_UNIT_MODE:
            break;
        case NAVIGATION_PATH_MODE:
            break;
        case VALUE_MODE:
            subtreeParser.warning(ex);
            break;
        }
    }

    public void error(SAXParseException ex) {
        switch(mode) {
        case ACCESS_UNIT_MODE:
            break;
        case NAVIGATION_PATH_MODE:
            break;
        case VALUE_MODE:
            subtreeParser.error(ex);
            break;
        }
    }

    public void fatalError(SAXParseException ex) throws SAXException {
        switch(mode) {
        case ACCESS_UNIT_MODE:
            break;
        case NAVIGATION_PATH_MODE:
            break;
        case VALUE_MODE:
            subtreeParser.fatalError(ex);
            break;
        }
    }
}
