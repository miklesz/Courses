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

import java.util.*;
import java.io.*;

import com.expway.util.*;
import com.expway.tools.expression.*;
import com.expway.tools.compression.*;
import com.expway.tools.utils.MethodsBag;
import com.expway.tools.io.*;

import org.xml.sax.Attributes;

// CodingContext permet de gérer l'entete SchemaMode et length au début
// de chaque General Element Code
public class CodingContext {
    final static boolean DEBUG=false;

    SetOfDefinitions theSetOfDefinitions;

    // Evolutivité de ce contexte
    // On passe de l'état normal à freezing puis à frozen
    private boolean bFreezing;
    private boolean bFrozen;

    // Mode multischémas ?
    private boolean bSchemaMultiple;

    // Skip demandé ?
    private boolean bSkip;

    // Length ?
    private int iLength;

    // Embedded chunk. Used if the length is -1 (unknown).
    private Chunk bodyChunk;

    // Decodage
    private int startIndex=0;

    // ------------------------------------------------------------
    // CONSTRUCTEURS
    public CodingContext() {
        bFreezing=false;
        bFrozen=false;
        bSchemaMultiple=false;
        bSkip=false;
    }

    // On a besoin du set of definitions 
    public CodingContext(SetOfDefinitions sod) {
        this();
        theSetOfDefinitions = sod;
    }

    // D'un contexte à l'autre, on gère :
    // - le passage de freezing a frozen state
    // - la conservation du frozen state
    public CodingContext(CodingContext ct) {
        this(ct.theSetOfDefinitions);
        if (ct.isFrozen()||ct.isFreezing()) bFrozen=true;
    }

    // ------------------------------------------------------------
    // ACCESSEURS UTILES

    public SetOfDefinitions getSetOfDefinitions(){return theSetOfDefinitions;}
    public URIRegistry getURIRegistry(){return theSetOfDefinitions.getURIRegistry();}

    // ------------------------------------------------------------
    // GESTION DU SCHEMA MODE

    /**
     *  Sets the size of the context. It must be positive (i.e. not -1 (unknown)).
     *  Use setBodyForLengthValue() if the length can be unknown.
     */
    public void setLengthValue(int length) {
        if (length==-1) throw new RuntimeException("CodingContext: Unknown chunk size !");
        iLength=length;
    }

    /**
     *  Sets the nested Chunk of this context. This is used only to knows the size
     *  of the chunk ; needed if the size of the chunk is still unknown. 
     */
    public void setBodyForLengthValue(Chunk c) {
        iLength=(int)c.sizeInBits();
        if (iLength==-1) bodyChunk=c;
    }

    public int getLength() {
        return iLength;
    }

    public void setSkip() {
        bSkip=true;
    }

    private boolean isFrozen() {
        return bFrozen;
    }

    private boolean isFreezing() {
        return bFreezing;
    }

    public boolean isSchemaMultiple() {
        return bSchemaMultiple;
    }

    public void setSchemaMultiple(boolean b) {
        bSchemaMultiple=b;
    }

    public boolean isSkipping() {
        if (CodingParameters.bMandatorySkipping) return true;

        return bSkip;
    }

    public boolean severalSchemasPresent() {
        //if (DEBUG) System.out.println("@@ A FAIRE !");
        return true;
    }

    // Démarre le gel du contexte
    public void freeze() {
        if (bFrozen || bFreezing)
            throw new RuntimeException ("CodingContext : Already in frozen or freezing mode !");
        bFreezing=true;
    }

    // SchemaMode:
    // Present si frozen=false ET si backward autorisée ET si plusieurs schemas
    private boolean isSchemaModePresent() {
        if (!isFrozen() && CodingParameters.bAllowsCompatibility 
            && severalSchemasPresent()) return true;
        else return false;
    }

    // Length (pas forcément la valeur mais juste le bit optionnel) présente si :
    // (General.allowsSkipping autorisé) OU (General.compatibility ET multischema)
    private boolean isLengthPresent() {
        if ((CodingParameters.bAllowsSkipping) ||
            CodingParameters.bAllowsCompatibility && isSchemaMultiple() ) return true;
        else return false;
    }

    // Bit optionnel present si (General.optionalSkip ET monoschema) 
    private boolean isLengthOptionalBitPresent() {
        if (!CodingParameters.bMandatorySkipping && !isSchemaMultiple()) return true;
        else return false;
    }

    // ------------------------------------------------------------
    // LECTURE XML ET ECRITURE CHUNK DU SCHEMA MODE
    
    // XML
    // Cet attribut est-il reservé à CodingContext ? Commence par cc: (pour l'instant)
    static public boolean isReserved(Attributes attrs,int index) {     
        if (attrs.getQName(index).startsWith("cc:")) return true;
        return false;
    }

    public CodingContext makeNewContext(Attributes attrs) {
        CodingContext newCodingContext=new CodingContext(this);

        for (int i=0;i<attrs.getLength();i++) {
            String sattr=attrs.getLocalName(i);
            boolean b=(new Boolean(attrs.getValue(i))).booleanValue();
            if (isReserved(attrs,i)) {
                if (sattr.equals("freeze")) {
                    if (b) {
                        if (DEBUG) System.out.println("context: freezing asked");                        
                        newCodingContext.freeze();
                    }
                }
                else if (sattr.equals("multi")) {                    
                    if (b) {
                        if (DEBUG) System.out.println("context: multiple schema mode asked");                        
                        newCodingContext.setSchemaMultiple(true);
                    }
                }
                else if (sattr.equals("skip")) {
                    if (b) {
                        if (DEBUG) System.out.println("context: skip asked");                        
                        newCodingContext.setSkip();
                    }
                }
                else if (sattr.equals("start")) {                    
                }
                else if (sattr.equals("stop")) {                    
                }
                else if (sattr.equals("repeat")) {                    
                }
                else {
                    String s="";
                    s+=" Allowed keywords: (";
                    s+="freeze ";
                    s+="multi ";
                    s+="skip ";
                    s+="start ";
                    s+="stop ";
                    s+="repeat ";
                    s+=")";
                    throw new RuntimeException("Unknown context coding attribute :"+sattr+s);
                }
            }
        }

        newCodingContext.testCoherent();
        if (DEBUG) System.out.println("context: new context "+newCodingContext.printState());

        return newCodingContext;
    }

    // CHUNK
    // length en paramètre
    public Chunk writeContext() {
        ChunkWriter cw=new ChunkWriter();

        if (isSchemaModePresent()) {
            if (isSchemaMultiple()) {
                if (isFreezing()) {
                    cw.writeByte(6,3); // 110 - multiple + freezing 
                } else {
                    cw.writeByte(2,2); // 10  - multiple + no freezing
                }
            } else {
                if  (isFreezing()) {
                    cw.writeByte(7,3); // 111 - mono + frezzing

                } else {
                    cw.writeByte(0,1); // 0 - mono + no freezing
                }
            }
        }

        if (isLengthPresent()) {       
            if (isLengthOptionalBitPresent()) {
                if (isSkipping()) {
                    cw.writeBoolean(true);
                    if (iLength!=-1) 
                        cw.writeInfiniteLong(iLength);
                    else {
                        System.out.println("created new LengthPendingChunk");                        
                        cw.write(new LengthPendingChunk(bodyChunk));
                    }
                }
                else {
                    cw.writeBoolean(false);
                } 
            } else {
                if (iLength!=-1) 
                    cw.writeInfiniteLong(iLength);
                else {
                    System.out.println("created new LengthPendingChunk");                        
                    cw.write(new LengthPendingChunk(bodyChunk));
                }
            }
        }

        if (DEBUG) System.out.println("context: written state="+printState()+" sizebits="+cw.sizeInBits());

        return cw;
    }


    // Lit le schemaMode et à partir d'un contexte courant, et fabrique un 
    // nouveau contexte.
    public CodingContext readContext(BitToBitDataInputStream bis) throws IOException {
        CodingContext newCodingContext=new CodingContext(this);
        int bitsread=bis.getReadedBits();

        // Lecture du schemaMode
        if (newCodingContext.isSchemaModePresent()) {
            if (bis.readByte(1)==0) {
                newCodingContext.setSchemaMultiple(false); // 0 - mono + no freezing
            } else {
                if (bis.readByte(1)==0) {                   
                    newCodingContext.setSchemaMultiple(true); // 10 - multiple + no freezing
                } else {
                    if (bis.readByte(1)==0) {
                        newCodingContext.setSchemaMultiple(true); // 110 - mutliple + freezing                        
                        newCodingContext.freeze();
                    } else {                        
                        newCodingContext.setSchemaMultiple(false); // 111 - mono + freezing
                        newCodingContext.freeze();
                    }
                }
            }            
        }

        // Lecture du length
        if (newCodingContext.isLengthPresent()) {
            boolean bRead=true;
            if (newCodingContext.isLengthOptionalBitPresent()) {
                bRead=bis.readBoolean();
            }
            if (bRead) {
                int length=(int)bis.readInfiniteLong();
                newCodingContext.setLengthValue(length);
                newCodingContext.setSkip();
            }   
        }

        newCodingContext.startIndex=bis.getReadedBits();

        //System.out.println("length="+newCodingContext.getLength());

        if (DEBUG) System.out.println("context: after reading: "+newCodingContext.printState()+
                                      " readedbits="+(newCodingContext.startIndex-bitsread));

        
 
        return newCodingContext;
    }

    public int getStartIndex() {
        return startIndex;
    }


    // ------------------------------------------------------------
    // DEBUG, AFFICHAGE ET COHERENCE

    private String printState() {
        String s="";
        if (isSchemaModePresent()) s+="SMPresent "; else s+="NoSMPresent ";
        if (isFrozen()) s+="Frozen ";
        if (isFreezing()) s+="Freezing ";
        if (!isFreezing() && !isFrozen()) s+="Hot ";
        if (isSchemaMultiple()) s+="Multiple "; else s+="Mono ";
        if (isLengthPresent()) s+="L:"+((iLength!=0)?""+iLength:"unknown")+" "; else s+="NoLength ";
        if (isSkipping()) s+="Skip ";
        return s;
    }

    // Test de coherence
    // Skip pas possible si la longueur est pas présente (cause possible: CodingParemeters.allowsSkipping=false
    private void testCoherent() {
        if (isSkipping() && !isLengthPresent()) {
            System.out.println("[Warning] Context: can't encode skipping information here !");
        }
        if (isSchemaMultiple() && isFrozen() ) {
            System.out.println("[Warning] Context: can't be in multiple schema mode while frozen !");
        }
        if (isSchemaMultiple() && !CodingParameters.bAllowsCompatibility) {
            System.out.println("[Warning] Context: can't be in multiple schema while General Compatibility is not allowed !"+
                               " Resetting to mono mode");
            setSchemaMultiple(false);
        }
    }
}
