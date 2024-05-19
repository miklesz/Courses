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

package com.expway.tools.io;

import java.io.OutputStream;
import java.io.IOException;

/**
 *  Length Pending chunk. This chunk holds the size of a ChunkWriter.
 */
public class LengthPendingChunk extends PendingChunk {
    long cachedSizeInBits=-1;

    /**
     *  This chunk contains the length of the underlying contentChunk. 
     */
    public LengthPendingChunk(Chunk contentChunk) {
        super();
        this.myChunk=contentChunk;
    }

    /**
     *  Returns -1 if its size is still unknown.
     */
    public long sizeInBits() {
        if (cachedSizeInBits==-1) {
            Chunk lengthChunk=makeLengthChunk();
            if (lengthChunk!=null) cachedSizeInBits=lengthChunk.sizeInBits();
            else cachedSizeInBits=-1;
        }
        
        return cachedSizeInBits;
    }

    /**
     *  Writes the content into a BitOutputStream. Can throws an IOException.
     */
    public void writeYourselfInto(BitOutputStream os) throws IOException {
        Chunk lengthChunk=makeLengthChunk();

        if (lengthChunk!=null) {
            System.out.println("Length bitsize="+lengthChunk.sizeInBits());            
            lengthChunk.writeYourselfInto(os);
        }
        else throw new IOException("Unknown LengthPendingChunk content.");
    }

    private Chunk makeLengthChunk() {
        Chunk c=null;

        if (cachedSizeInBits==-1) {
            cachedSizeInBits=myChunk.sizeInBits();
            if (cachedSizeInBits!=-1) {
                ChunkWriter cw=new ChunkWriter();
                cw.writeInfiniteLong(cachedSizeInBits);
                // Liberation du chunk
                myChunk=null;
                return cw;
            }
        }
        
        return c;
    }
}
