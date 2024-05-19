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

package com.expway.tools.compression;

import com.expway.tools.automata.*;
import com.expway.tools.expression.*;

import java.io.*;
import java.io.StringWriter;
import com.expway.tools.io.*;

import com.expway.util.URIRegistry;

import com.expway.binarisation.CodingParameters;
import com.expway.binarisation.CodingContext;


// COMPLEX COMPLEX TYPE

public class ComplexComplexTypeInstance extends ComplexTypeInstance  {

    CompressionFiniteStateAutomata myCodingFSA = null;
    DecompressionFiniteStateAutomata myDecodingFSA = null;

    //

    public ComplexComplexTypeInstance(ComplexComplexTypeDefinition td){
        super(td);
    }

    public boolean isEmpty(){
        return !((ComplexComplexTypeDefinition)myDefinition).hasContent();
    }

    // ------------------------------------------------------------
    // ENCODING

    public void startEncoding() throws ParsingException {
        if (ENCODEDEBUG) System.out.println("");
        if (ENCODEDEBUG) System.out.println("===> StartEncoding of " + this);

        super.startEncoding();

        if (CodingParameters.bAllowsCompatibility && getCodingContext().isSchemaMultiple())
            {
                try {
                    myCodingFSA    = (CompressionFiniteStateAutomata) 
                        ((ComplexComplexTypeDefinition)myDefinition).newCompatibleContentModelFSA(getCodingContext());
                } catch(DefinitionException de){
                    throw new ParsingException("Exception during compatible FSA generation : " + de.getMessage());
                }
                if (ENCODEDEBUG) System.out.println("     codingFSA    = " + (myCodingFSA!=null?"available":"none") + " for " + this);
                if (myCodingFSA != null)
                    if (ENCODEDEBUG) System.out.println("        - CompatibleCoding chosen");
            }
        else 
            {
                myCodingFSA = (CompressionFiniteStateAutomata) 
                    ((ComplexComplexTypeDefinition)myDefinition).newContentModelFSA();
                if (ENCODEDEBUG) System.out.println("     codingFSA    = " + (myCodingFSA!=null?"available":"none") + " for " + this);

            }
        
        resetContentModelFSA();
    }

    private void resetContentModelFSA() throws ParsingException {
        
        if (myCodingFSA!=null){
            myCodingFSA.resetMode();
            myCodingFSA.setApplicationContext(new CompressionContext());
            try {
                myCodingFSA.reset(new CompressionActivityToken());
            } catch(AutomataException ae) {
                throw new ParsingException("Error during content model automata initialization");
            }
        }
    }

    //

    public TypeEncoder encodeElement(String name) throws ParsingException{
        
        if (!isAttributeCodingFinished()){
            endAttributeEncoding();
            if (ENCODEDEBUG) System.out.println("---> Start Content Coding of " + this);
        }
        
        if (ENCODEDEBUG) System.out.println(" ===> encodeElement " + name + " " + this);
        
        if (myCodingFSA == null)
            throw new ParsingException("No element expected");
        
        try {
            myCodingFSA.consume(name);
        } catch(AutomataException ae){
            throw new ParsingException("Unexpected element "+name+" in "+this);
        }
        
        return ((CompressionContext)myCodingFSA.getApplicationContext()).getCurrentTypeInstance();
    }
    

    public void endEncoding() throws ParsingException {

        if (!isAttributeCodingFinished()){
            endAttributeEncoding();
        }

        if (CodingParameters.bAllowsCompatibility && getCodingContext().isSchemaMultiple())
            endCompatibleEncoding();
        else
            endSimpleEncoding();
    }
    
    // ------------------------------------------------------------
    // COMPATIBLE

    private void endCompatibleEncoding() throws ParsingException {
        if (ENCODEDEBUG) System.out.println("         end Multiple Encoding ");
        // Récupération du codage attribut (est placé avec setCompressionActivityToken())
        CompressionActivityToken cattr = getCompressionActivityToken();
        CompressionActivityToken ccont = null;
        Chunk ccont_chunk = null;
        Chunk cattr_chunk = null;

        // Récupération du codage content
        if (myCodingFSA != null){
            try{

                ccont = (CompressionActivityToken)myCodingFSA.end();
                if (ENCODEDEBUG) System.out.println("");
                if (ENCODEDEBUG) System.out.println("         Récupération du CAT content = " + ccont + " of this "+this);
                if (ENCODEDEBUG) System.out.println("");
                
                // release de l'automate
                ((ComplexComplexTypeDefinition)myDefinition).releaseContentModelFSA(myCodingFSA);
                myCodingFSA = null;
                
            } catch(AutomataException ae){
                throw new ParsingException("elements are missing when parsing complexType " + this);
            }
        }

        // Merge des deux CATs
        
        if (cattr == null && ccont == null){
            if (ENCODEDEBUG) System.out.println("         Element with no attributes and no content : no chunk");
        }
        else if (cattr == null) {
            if (ENCODEDEBUG) System.out.println("         Uniquement element chunk");
            if (ENCODEDEBUG) System.out.println("");
            if (ENCODEDEBUG) System.out.println("===> EndEncoding of " + this + " length = " + getCoding().sizeInBits());
            
            // crée le chunk
            ccont_chunk = ccont.generateChunk();
            // Compte les bits de structure
            addBitsOfStructure(ccont.getBitsOfStructure());
        }
        else if (ccont == null) {
            if (ENCODEDEBUG) System.out.println("          Uniquement attribute chunk");
            if (ENCODEDEBUG) System.out.println("");
            if (ENCODEDEBUG) System.out.println("===> EndEncoding of " + this + " length = " + getCoding().sizeInBits());
         	
            // crée le chunk
            cattr_chunk = cattr.generateChunk();
            // Compte les bits de structure
            addBitsOfStructure(cattr.getBitsOfStructure());
        }
        else {
            // Merge des deux chunks 
            if (ENCODEDEBUG) System.out.println("          Merge des chunks attributs et content"); 
            if (ENCODEDEBUG) System.out.println("");
            
            //    ON LES METS MERGE et on l'écrit
            cattr.appendInCompatibilitySpaces(ccont);

            if (ENCODEDEBUG) System.out.println("           CAT après merge = " + cattr);
            
            // puis on cree le chunk
            ChunkWriter result = new ChunkWriter();
            cattr.generateChunk().writeYourselfInto(result);
            setCoding(result);
            // Compte les bits de structure
            addBitsOfStructure(cattr.getBitsOfStructure());
            
            if (ENCODEDEBUG) System.out.println("===> EndEncoding of " + this + " length = " + result.sizeInBits() + " bits");
        }
        setCodingFinished();
    }
    
    // ------------------------------------------------------------
    // SIMPLE

    private void endSimpleEncoding() throws ParsingException {
        if (ENCODEDEBUG) System.out.println("  end Simple Encoding of " + this);
        // recuperation du codage attribut (est place avec setCoding())
        
        Chunk ccont = null;
        Chunk cattr = getCoding(); // celui des attributs et du sous type

        // RECUPERATION DU CODAGE CONTENT
        if (myCodingFSA != null){
            try{

                CompressionActivityToken cat = (CompressionActivityToken)myCodingFSA.end();
                if (ENCODEDEBUG) System.out.println("");
                if (ENCODEDEBUG) System.out.println("         Creation du chunk elements  = " + cat + " of this "+this);
                if (ENCODEDEBUG) System.out.println("");
                
                ccont = cat.generateChunk(); // celui du contenu
                // calcul des bits de structure
                addBitsOfStructure(cat.getBitsOfStructure());

                // release de l'automate
                ((ComplexComplexTypeDefinition)myDefinition).releaseContentModelFSA(myCodingFSA);
                myCodingFSA = null;
                
            } catch(AutomataException ae){
                throw new ParsingException("elements are missing when parsing complexType " + this);
            }
        }
            // GENERATION DU CODING FINAL

        if (cattr == null && ccont == null){
            if (ENCODEDEBUG) System.out.println("         Element with no attributes and no content : no chunk");
        }
        else if (cattr == null) {
            if (ENCODEDEBUG) System.out.println("         Uniquement element chunk");
            if (ENCODEDEBUG) System.out.println("");
            setCoding(ccont);
            if (ENCODEDEBUG) System.out.println("===> EndEncoding of " + this + " length = " + ccont.sizeInBits());
        }
        else if (ccont == null) {
            if (ENCODEDEBUG) System.out.println("         Uniquement attribute chunk");
            if (ENCODEDEBUG) System.out.println("");
            setCoding(cattr);
            if (ENCODEDEBUG) System.out.println("===> EndEncoding of " + this + " length = " + cattr.sizeInBits());
        }
            else {
                // merge des deux chunks
                if (ENCODEDEBUG) System.out.println("         Merge des chunks attributs et content"); 
                if (ENCODEDEBUG) System.out.println("");
                    
                ChunkWriter result = new ChunkWriter();
                cattr.writeYourselfInto(result);
                ccont.writeYourselfInto(result);
                setCoding(result);
                if (ENCODEDEBUG) System.out.println("===> EndEncoding of " + this + " length = " + result.sizeInBits() + " bits");
            }
            setCodingFinished();
    }

    public void setValue(String value) throws ParsingException {
        if (((ComplexComplexTypeDefinition)myDefinition).isMixed()){
            
            if (ENCODEDEBUG) System.out.println("  - encodePCDATA of mixed " + this + "\"" + value + "\"");
            
            if (myCodingFSA == null)
                throw new ParsingException("No mixed allowed");
            
            try {  myCodingFSA.consume(SimpleTypeDefinition.MIXED_KEY);} 
            catch(AutomataException ae){
                System.out.println("Error in automata of " + this);
                ae.printStackTrace();
                throw new ParsingException("Unexpected PCDATA in "+this);
            }
            
            TypeEncoder te=((CompressionContext)myCodingFSA.getApplicationContext()).getCurrentTypeInstance();
            te.startEncoding();
            te.setValue(value);
            te.endEncoding();
        }
        else
            throw new ParsingException("No string expected in ComplexComplexType " + this + "(not mixed) : send string =\""+value+"\"");
        //myCoding = myDefinition.getChunk(value);
    }

    // ============================================================
    // DECODING

    public void startDecoding(BitToBitDataInputStream dis) throws DecodingException {
        if (DECODEDEBUG) System.out.print(" ===>  Start decoding ComplexComplexType \"" + this + "\" - Element \"" + getElementName() + "\" in");

        if (CodingParameters.bAllowsCompatibility && getCodingContext().isSchemaMultiple()) {
            if (DECODEDEBUG) System.out.println(" compatible mode");
            super.startDecoding(dis); // should be done after setOuputWriter because it should
        } else {
            if (DECODEDEBUG) System.out.println(" non-compatible mode");
            super.startDecoding(dis);
            myDecodingFSA = (DecompressionFiniteStateAutomata)((ComplexComplexTypeDefinition)myDefinition).newDecompressionContentModelFSA(); 
            if (myDecodingFSA!=null)
                {
                    myDecodingFSA.resetMode();
                    myDecodingFSA.setApplicationContext(new DecompressionContext(dis));
                    try {
                        myDecodingFSA.reset(new DecompressionActivityToken());
                    }catch(AutomataException ae){
                        throw new RuntimeException("Error during content model automata initialization");
                    }
                }
        }
    }
    
    public TypeEncoder decode(BitToBitDataInputStream dis) throws DecodingException {
        if (CodingParameters.bAllowsCompatibility && getCodingContext().isSchemaMultiple()) 
            return compatibleDecode(dis);
        else 
            return simpleDecode(dis);
    }

    // ------------------------------------------------------------
    // COMPATIBLE

    private TypeEncoder compatibleDecode(BitToBitDataInputStream dis) throws DecodingException {
        
        while(true) // HERE WE DECODE MANY CompatiblePieces 
            {
				
                TypeEncoder te = null;
                if (!isAttributeDecodingFinished()){
                    te = super.decode(dis);
                    if(te!=null){
                        ((TypeInstance)te).setElementWriter(getElementWriter());
                        return te; // besoin d'empiler un decoder
                    }
                }
                
                // now we have to deal with content model automata
                while (te == null){ 
                    try {
                
                        if (myDecodingFSA == null) break;  // no content defined in this CompatiblePiece
                        if (myDecodingFSA.consume()) break; // final state reached
                
                        // get the type encoder returned by the automata (if null, continue decoding the token is moving !)
                        te = ((DecompressionContext)myDecodingFSA.getApplicationContext()).getCurrentTypeInstance();
                        // set the element name associated to the type encoder received
                        if (te!=null) {
                            String element = ((DecompressionContext)myDecodingFSA.getApplicationContext()).getCurrentElementName();
                                                                                    
                            if (element!=null) {
                                // Dequalification, si nécessaire
                                if (!getTypeDefinition().isElementFormQualified()) element=URIRegistry.getWithoutPrefix(element);
                                ((TypeInstance)te).setElementName(element);
                            } else
                                ((TypeInstance)te).setElementName(SimpleTypeDefinition.MIXED_KEY);

                            ((TypeInstance)te).setElementWriter(getContentWriter());
                        }
                    } catch(AutomataException ae){
                        System.out.println("error : " + ae.getMessage() + " in type " + this + " in element " + getElementName());
                        ae.printStackTrace();
                        throw new DecodingException();
                    }
                }
        
                if (te!=null) return te; // l'automate a retourné un nouveau type decoder 
  
                // HERE THE DECODING OF THE AUTOMATA IS FINISHED, 
                // is there any new Compatible Piece header. If so decode it and restart automaton !
                // We stop when there is no more compatible piece (return null)
                TypeDefinition newTD = readCompatibilityHeader(dis);
                if (newTD == null) return null; // NO MORE PCH
                // Start for a new compatible piece
                reinitCompatibleDecoding(dis,newTD); // change FSAs
                changeTypeDefinition(newTD); // the new TD
                withdrawSetAttributeDecodingFinished(); // new attributes are expected
                // NOW WE ARE IN A NEW PC .. restart the process at the beginning
            }
    }

    //

    protected void reinitCompatibleDecoding(BitToBitDataInputStream dis, TypeDefinition newType) throws DecodingException {
        super.reinitCompatibleDecoding(dis,newType);

        try {
            DecompressionFiniteStateAutomata fsa = null;
            fsa = (DecompressionFiniteStateAutomata) 
                ((ComplexComplexTypeDefinition)newType).newCompatibleDecompressionContentModelFSA(getCodingContext(),myDefinition);
        
            if (fsa!=null)
                {
                    myDecodingFSA = fsa;
                    myDecodingFSA.resetMode();
                    myDecodingFSA.setApplicationContext(new DecompressionContext(dis));
                    try {
                        myDecodingFSA.reset(new DecompressionActivityToken());
                    }catch(AutomataException ae){
                        throw new RuntimeException("Error during content model automata initialization");
                    }
                }
        } catch (DefinitionException de){
            de.printStackTrace();
            throw new DecodingException(de.getMessage());
        }

    }

    // ------------------------------------------------------------
    // SIMPLE

    private TypeEncoder simpleDecode(BitToBitDataInputStream dis) throws DecodingException {

        if (!isAttributeDecodingFinished()){
            TypeEncoder te = super.decode(dis);
            if(te!=null) {
                ((TypeInstance)te).setElementWriter(getElementWriter());
                return te; // besoin d'empiler un decoder
            }
            outputEndAttributeCoding();
        }
        
        TypeEncoder te = null;
        while (te == null){
            try {
                
                if (myDecodingFSA == null) {return null;}
                if (myDecodingFSA.consume()) {return null;} // etat final atteint
                
                te = ((DecompressionContext)myDecodingFSA.getApplicationContext()).getCurrentTypeInstance();
                if (te!=null) {
                    String element = ((DecompressionContext)myDecodingFSA.getApplicationContext()).getCurrentElementName();
                    if (element!=null) {
                        // Dequalification, si nécessaire
                        if (!getTypeDefinition().isElementFormQualified()) element=URIRegistry.getWithoutPrefix(element);
                        ((TypeInstance)te).setElementName(element);
                    } else
                        ((TypeInstance)te).setElementName(SimpleTypeDefinition.MIXED_KEY);

                    ((TypeInstance)te).setElementWriter(getContentWriter());
                }
            } catch(AutomataException ae){
                System.out.println("error : " + ae.getMessage() + " in type " + this + " in element " + getElementName());
                ae.printStackTrace();
                throw new DecodingException();
            }
        }
        return te;
    }

    public void endDecoding() throws DecodingException {
        if (DECODEDEBUG) System.out.println(" ===>  End decoding ComplexComplexType = " + this);

        if (CodingParameters.bAllowsCompatibility && getCodingContext().isSchemaMultiple()) {
            if (DECODEDEBUG) System.out.println("        (compatible codec)");
            // on a vraiment fini de coder l'element (on ferme le >)
            outputEndAttributeCoding();
            // on ouput le content
            outputContent();
        }
        else 
            if (DECODEDEBUG) System.out.println("        (simple codec)");

        // on ferme l'élément
        super.endDecoding();

        ((ComplexComplexTypeDefinition)myDefinition).releaseDecompressionContentModelFSA(myDecodingFSA);
        myDecodingFSA = null;

    }

    
}


