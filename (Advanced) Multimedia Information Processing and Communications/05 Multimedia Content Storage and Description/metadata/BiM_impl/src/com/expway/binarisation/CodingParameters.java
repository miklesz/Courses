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

import java.util.Hashtable;
import java.util.Iterator;
import java.io.IOException;

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;

import com.expway.util.URIRegistry;

// Codecs
import com.expway.tools.codec.Codec;


public class CodingParameters {
    public static boolean bWriteStructure=true;
    public static boolean bWriteData=true;
    public static boolean bAligned=false; 

    public static boolean bWriteXMLNSInDecompressFile=true;

    public static boolean bAllowsSkipping=false;
    public static boolean bMandatorySkipping=false;
    public static boolean bAllowsPartialInstantiation=true;
    public static boolean bAllowsSubtyping=true;

    public static boolean bAllowsCompatibility=false;

    // Codecs de feuilles
    public static boolean bSpecificCodecs=false;

    // Codecs spécifiques de certains complex types 
    public static boolean bSpecificVideoCodecs=false;
    public static boolean bSpecificTimeDatatypes=false;

    // Occurence Coding (Sony) //AM
    public static int bUnitSize=-1; //AM

    private final static boolean DEBUG=false;

    public final static String PARTIAL_INSTANTIATED_TYPE="@<\"partial_instantiated_type";
    
    static public void reset() {
        instantiatedCodecs.clear();
    }

    // Ecriture des 4 bits, conformément au Systems CD
    public static void writeHeaderInto(ChunkWriter cw) {
        testCoherent();
        if (DEBUG) System.out.println("CodingParameters: writing "+printState());
        
        if (bAllowsSkipping) {
            if (bMandatorySkipping) {
                cw.writeByte(1,1);
                cw.writeByte(0,1);
            }
            else {
                cw.writeByte(0,1);
                cw.writeByte(1,1);
            }
        }
        else {
            cw.writeByte(0,1);
            cw.writeByte(0,1);
        }

        cw.writeBoolean(bAllowsPartialInstantiation);
        cw.writeBoolean(bAllowsSubtyping);
    }

    public static void readHeaderFrom(BitToBitDataInputStream dis) throws IOException {
        boolean b1=dis.readBoolean();
        boolean b2=dis.readBoolean();

        if (b1) {
            if (b2) {
                System.out.println("Invalide code 11 for Skipping information in header !");
            }
            else {
                bAllowsSkipping=true;
                bMandatorySkipping=true;
            }
        } else {
            if (b2) {
                bAllowsSkipping=true;
                bMandatorySkipping=false;
            } else {
                bAllowsSkipping=false;
                bMandatorySkipping=false;
            }
        }

        bAllowsPartialInstantiation=dis.readBoolean();
        bAllowsSubtyping=dis.readBoolean();

        testCoherent();

        if (DEBUG) System.out.println("CodingParameters: readed "+printState());
    }

    static public String generateCSyntax() {
        return "readCodingParameters();\n";
    }

    static public String printState() {
        return "Skip="+bAllowsSkipping+" MandatorySkip="+bMandatorySkipping
                           +" PartialInstantiation="+bAllowsPartialInstantiation
                           +" Subtyping="+bAllowsSubtyping;
    }

    // Test de coherence
    // PartialInstantiation pas possible si Subtyping est pas possible
    // MandatorySkip pas possible si AllowsSkipping est pas possible
    static private boolean testCoherent() {
        boolean bValid=true;
        if (!bAllowsSubtyping && bAllowsPartialInstantiation) bValid=false;
        if (!bAllowsSkipping && bMandatorySkipping) bValid=false;
        
        if (!bValid) {
            throw new RuntimeException("CodingParameters: incoherent ! "+printState());
        }

        if (!bWriteData) System.out.println("[Warning] Data is NOT written in this file !");
        if (!bWriteStructure) System.out.println("[Warning] Structure is NOT written in this file !");
        if (bAligned) System.out.println("[Warning] Data stream is ALIGNED in this file !");

        return bValid;
    }

    static Hashtable instantiatedCodecs=new Hashtable();

    // Table d'association des codecs de la forme (primitive_type,type,nom d'une instance de codec)
    static String [] elementsCodecsTable = {
        //"ID","","DicoA",
        //"NMTOKEN","","DicoA",
        //"MIXED","","HuffmanA",
        "string","","ZLibA",
        //"string","","HuffmanA",
        //"anyURI","","ZLibA",
        //"integer","","ZLibB",
        //"double","","ZLibB",
        //"float","","ZLibB",   
        //"list","","ZLibB",  
        //"string","","DicoB",
        //"ZeroToOneFloat","","DicoA",
        //"union","","ZLibA",
        //"string","NMTOKEN","ZLibB",
        //"string","language","ZLibC",
        //"IDREF","","DicoC",
    };

    // Table d'association (nom d'une instance de codec,codec)
    // Les codecs suivant existent : ZLibCodec DictionaryCodec HuffmanCodec
    static String [] codecsTable = {
        "DicoA","DictionaryCodec",
        "DicoB","DictionaryCodec",
        "DicoC","DictionaryCodec",
        "HuffmanA","HuffmanCodec",
        "ZLibA","ZLibCodec",
        "ZLibB","ZLibCodec",
        "ZLibC","ZLibCodec",
    };

    final static String codecTypePath="com.expway.tools.codec.";

    // Gestion des codecs spécifiques
    // GP: L'appel de getCodec() a été changé; désormais on ne code que les string UTF
    static public Codec getCodec(String primitive,String type) {
        if (!bSpecificCodecs) return null;

        String codecName=null;

        // Enleve les prefixes de namespaces
        primitive=URIRegistry.getWithoutPrefix(primitive);
        type=URIRegistry.getWithoutPrefix(type);

        // Cherche si un codec existe
        for (int i=0;i<elementsCodecsTable.length;i+=3) {
            String matchPrimitive=elementsCodecsTable[i];
            String matchType=elementsCodecsTable[i+1];
            String matchCodecName=elementsCodecsTable[i+2];

            // Teste d'abord si type APRES si primitive
            if (matchType.equals(type)) {codecName=matchCodecName;}
            if (codecName==null && matchPrimitive.equals(primitive)) {codecName=matchCodecName;}
        }

        // Codec existant pour cet élement ?
        if (codecName==null) return null;
        else {
            Codec codec=(Codec)instantiatedCodecs.get(codecName);
            String codecType=null;

            // Codec pas encore instancié ?
            if (codec==null) {
                try {                    
                    for (int i=0;i<codecsTable.length;i+=2) {
                        String matchCodecName=codecsTable[i];
                        String matchCodecType=codecsTable[i+1];

                        if (codecName.equals(matchCodecName)) codecType=matchCodecType;
                    }
                    codec=(Codec)Class.forName(codecTypePath+codecType).newInstance();
                    instantiatedCodecs.put(codecName,codec);
                    System.out.println("codec: Instantiated the new codec name="+codecName+" handle="+codec+" type="+codecType);
                } catch(Exception e) {
                    System.out.println("[Warning] Cannot instantiate the codec of type "+codecType+" "+e);
                    codec=null;
                }                
            } 
            
            return codec;
        }        
    }

    static public void flushAllPendingCodecs() {
        for (Iterator i=instantiatedCodecs.values().iterator();i.hasNext();) {
            Codec codec=(Codec)i.next();
            codec.dispose();
        }
    }
}
