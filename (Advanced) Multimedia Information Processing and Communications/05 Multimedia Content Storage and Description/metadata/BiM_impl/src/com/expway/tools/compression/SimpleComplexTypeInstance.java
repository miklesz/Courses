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

import com.expway.binarisation.CodingParameters;

import com.expway.tools.automata.*;
import com.expway.tools.expression.*;
import java.io.*;
import com.expway.tools.io.*;

// ============================================================
// COMPLEX TYPE

public class SimpleComplexTypeInstance extends ComplexTypeInstance {
    
    String theValue = null;

    public void setValue(String value)throws ParsingException {
        theValue = value;
    }

    //

    public SimpleComplexTypeInstance(SimpleComplexTypeDefinition td){
        super(td);
    }

    //

    public void startEncoding() throws ParsingException {
        if (ENCODEDEBUG)  System.out.println("");
        if (ENCODEDEBUG) System.out.println("===> StartEncoding of " + this);

        super.startEncoding();
    }

    public TypeEncoder encodeElement(String name) throws ParsingException{
        throw new ParsingException("No element allowed in a simple complex Type");
    }

    //

    public void endEncoding() throws ParsingException {
        if (CodingParameters.bAllowsCompatibility && getCodingContext().isSchemaMultiple() ){
            endCompatibleEncoding();
        }
        else {
            endSimpleEncoding();
        }
    }

    private void endCompatibleEncoding() throws ParsingException {

        if (!isAttributeCodingFinished()){
            endAttributeEncoding();
        }

        // assert
        if (getCoding()!=null) throw new RuntimeException("Compatible Coding of simpleComplexType is not null !!");

        // RECUPERATION DU CODING DES ATTRIBUTS
        CompressionActivityToken cattr_CAT = getCompressionActivityToken(); // celui des attributs
        if (ENCODEDEBUG) System.out.println("         CAT attributs " + cattr_CAT); 
        
        // LE CONTENT EST UN SIMPLE TYPE
        TypeEncoder te = ((SimpleComplexTypeDefinition)myDefinition).getContentEncoder();
        te.startEncoding();
        te.setValue(theValue);
        te.endEncoding();

        // merge des deux chunks
        if (ENCODEDEBUG) System.out.println("         Merge du CAT attributs et simple content"); 
        if (ENCODEDEBUG) System.out.println("");
        cattr_CAT.appendSimpleTypeInFirstCompatibilitySpace(te); // ajouté dans la premiere pieceheader
        if (ENCODEDEBUG) System.out.println("         CAT attributs " + cattr_CAT); 
        
        // genere la sortie
        Chunk cattr = null;
        cattr=cattr_CAT.generateChunk();
        addBitsOfStructure(cattr_CAT.getBitsOfStructure());
        setCoding(cattr);

        if (ENCODEDEBUG) System.out.println("===> EndEncoding of " + this + " length = " + cattr.sizeInBits() + " bits");
        setCodingFinished();
    }



    private void endSimpleEncoding() throws ParsingException {

        if (!isAttributeCodingFinished()){
            endAttributeEncoding();
        }
        
        Chunk cattr = getCoding(); // celui des attributs
        
        // RECUPERATION DE CELUI DES ATTRIBUTS
    
        ChunkWriter ccont = new ChunkWriter();
        ((SimpleComplexTypeDefinition)myDefinition).encodeItInto(theValue,ccont);
        if (ENCODEDEBUG) System.out.println("     encoding of " + this + 
                                            " value = " + theValue + " length = " + ccont.sizeInBits() + " bits");
        
        // GENERATION DU CODING FINAL

        if (cattr == null && ccont == null)
            throw new RuntimeException("nothing in here : simplecomplextypeinstance");
        
        else if (cattr == null) {
            if (ENCODEDEBUG) System.out.println("         Uniquement simple chunk");
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
            if (ENCODEDEBUG) System.out.println("         Merge des chunks attributs et simple"); 
            if (ENCODEDEBUG) System.out.println("");
            
            ChunkWriter result = new ChunkWriter();
            cattr.writeYourselfInto(result);
            if (CodingParameters.bWriteData) ccont.writeYourselfInto(result);
            setCoding(result);
            if (ENCODEDEBUG) System.out.println("===> EndEncoding of " + this + " length = " + result.sizeInBits() + " bits");
        }
        setCodingFinished();
    }
    
    // ============================================================
    // DECODING

    boolean finished = false;

    public void startDecoding(BitToBitDataInputStream dis)throws DecodingException{
        if (DECODEDEBUG) System.out.println(" ===>  Start decoding SimpleComplexType = " + this);
        super.startDecoding(dis);
    }
    
    public TypeEncoder decode(BitToBitDataInputStream dis)throws DecodingException{
        if (!isAttributeDecodingFinished()){
            TypeEncoder te = super.decode(dis);
            if(te!=null) {
                ((TypeInstance)te).setElementWriter(getElementWriter());
                return te; // besoin d'empiler un decoder
            }
        }
        
        StringWriter sw = new StringWriter();

        // encodage compatible
        if (CodingParameters.bAllowsCompatibility && getCodingContext().isSchemaMultiple()) {
            if (DECODEDEBUG) System.out.println("        (compatible codec)");

            // the first piece contains the content of this simple complex type
            if (!finished){
                ((SimpleComplexTypeDefinition)myDefinition).decode(dis,getContentWriter());
                finished = true;
            }

            // ici on essaie de relancer le compatible encoding
            while(true) {
            	
                TypeDefinition newTD = readCompatibilityHeader(dis);
                if (newTD == null) return null;
                // Start for a new compatible piece
                reinitCompatibleDecoding(dis,newTD); // change FSAs
                changeTypeDefinition(newTD); // the new TD
                withdrawSetAttributeDecodingFinished(); // new attributes are expected
                // Is there any new attributes to decode ? it could be a restriction, therefore there is no other attribute
                // to encode however we have to find the next compatible header
                if (!isAttributeDecodingFinished()){
                    TypeEncoder te = super.decode(dis);
                    if(te!=null) {
                        ((TypeInstance)te).setElementWriter(getElementWriter());
                        return te; // besoin d'empiler un decoder
                    }
                }
            }
        } else // encodage simple 
            {
                if (DECODEDEBUG) System.out.println("        (simple codec)");
                outputEndAttributeCoding();
                ((SimpleComplexTypeDefinition)myDefinition).decode(dis,getContentWriter());
            }
        return null;
    }

    public void endDecoding() throws DecodingException {
        if (DECODEDEBUG) System.out.println(" ===>  End decoding SimpleComplexType = " + this);
        if (CodingParameters.bAllowsCompatibility && getCodingContext().isSchemaMultiple()) {
            // on a vraiment fini de coder les attributs (on ferme le >)
            outputEndAttributeCoding();
            outputContent();
        }

        // on affiche l'element fermant "<\xxx>"
        super.endDecoding();
    }
    
}
