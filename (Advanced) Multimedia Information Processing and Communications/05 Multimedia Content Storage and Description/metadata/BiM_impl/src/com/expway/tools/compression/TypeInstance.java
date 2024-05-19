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
import com.expway.tools.io.*;
import com.expway.binarisation.CodingParameters;

abstract public class TypeInstance implements TypeEncoder  {

    public static boolean ENCODEDEBUG = false;
    public static boolean DECODEDEBUG = false;

    public static boolean STUPIDPARSING = false;
    
	
    // Identifiant unique
    private static int cptid = 0;
    protected int id = cptid++;
    
    // la definition
    protected TypeDefinition myDefinition = null;
    protected boolean bTypeChanged = false;

    // le codage
    private Chunk myCoding = null;
    private CompressionActivityToken myCompressionActivityToken = null;
    
    // du controle
    private boolean started = false;
    private boolean ended = false;

    // un mode
    private boolean bIsPartialInstantiated=false;

    // les bits de structure

    private int bitsOfStructure = 0;
    public int getBitsOfStructure()           {return bitsOfStructure;}
    protected void setBitsOfStructure(int bs) {bitsOfStructure = bs;}
    protected void addBitsOfStructure(int bs) {bitsOfStructure += bs;}

    // ELEMENT

    private String myElement = null;
    public void setElementName(String s) {myElement = s;}
    public String getElementName()       {return myElement;}

    // CONSTRUCTEUR

    TypeInstance(TypeDefinition td){
        changeTypeDefinition(td);
        bTypeChanged = false;
    }

    // LE TYPE

    public TypeDefinition getTypeDefinition() {
        return myDefinition;
    }

    /** en cas de xsi:type */
    public void changeTypeDefinition(TypeDefinition td) {
        //          if (started && !(CodingParameters.bAllowsCompatibility))
        //              throw new DecodingException("impossible to change type when encoding already started");
        bTypeChanged = true;
        myDefinition = td;
    }

    // ACCESSEUR

    public boolean isCodingFinished(){return ended;}
    protected void setCodingFinished(){ended = true;}

    public    Chunk getCoding()        {return myCoding;}
    protected void  setCoding(Chunk c) {myCoding = c;}
    public    CompressionActivityToken getCompressionActivityToken()        {return myCompressionActivityToken;}
    protected void  setCompressionActivityToken(CompressionActivityToken c) {myCompressionActivityToken = c;}

    // ENCODAGE

    public void startEncoding() throws ParsingException {
        if (started)
            throw new ParsingException("impossible to start two times the encoding");
        started = true;
    }

    public boolean isPartialInstantiated() {
        return bIsPartialInstantiated;
    }

    public void setPartialInstantiated(boolean bit) {
        if (CodingParameters.bAllowsPartialInstantiation==false && bit==true)
            System.out.println("[Warning] PartialInstantiation is asked but not is not true in CodingParameters !");
        bIsPartialInstantiated=bit;
    }

    public Chunk getCodingWithContext(boolean bMode) {
        return getCoding();
    }

    abstract public void endEncoding() throws ParsingException ;

    // DECODAGE
    static boolean first=false;

    public void startDecoding(BitToBitDataInputStream dis) throws DecodingException {
    }
    
    public void endDecoding() throws DecodingException {
    }

    //

    public String getTypeName() {
        return myDefinition.getName();
    }

    public String toString(){
        if (myDefinition!=null)
            return "" + myDefinition.getName() + " id("+id+")";
        return "NoDefinition id("+id+")";
    }

    // DECODAGE

    private Writer myWriter = null;
    // if there is already a writer, don't change it : compatible decoding
    public    void   setElementWriter(Writer w) { myWriter = w;}
    protected Writer getElementWriter()         { return myWriter; }

    public boolean isEmpty(){return false;}

	

}





