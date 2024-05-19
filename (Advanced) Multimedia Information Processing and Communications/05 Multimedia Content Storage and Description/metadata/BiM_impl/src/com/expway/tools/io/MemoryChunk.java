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
 * A chunk represents a part of an array of bytes. Chunks are linked to form
 * messages that may be sent from an adress space to another. Their use avoids
 * unnecessarily copying arrays of bytes, and helps recovering these arrays
 * without  resorting to garbage collection (thanks to
 * {@link ChunkFactory chunk factories}).
 * <p>
 * Chunks should not be used concurrently.
 */

public class MemoryChunk extends Chunk {
    /** The associated array of bytes. */
    private byte[] data;
    
    /** Index of the last valid (written) byte in this chunk + 1. */
    private int top;

    // The tail of a memory chunk is a chunk of bits
    private int lastByte,lastNbBits;
    
    /**
     * Constructs a new chunk with the specified data and offsets.
     * 
     * @param data the byte array containing the data.
     * @param offset the offset of the first valid byte in the chunk.
     * @param top offset + the number of valid bytes in the chunk.
     */
    public MemoryChunk (byte[] data) {
        this.data = data;
        this.top = 0;
        lastByte=0;
        lastNbBits=0;
    }
    
    /**
     * Returns a string representation of the target chunk.
     *
     * @return a string representation of the target chunk.
     */
    public String toString() {
        return "Chunk[data: " + data + " top: " + top + "bits="+lastNbBits+"]";
    }

    public int size(){ return top; }
    public long sizeInBits() {return top*8+lastNbBits;}
    
    public void writeByte(int b){
        if (isFull())
            throw new RuntimeException("Chunk is Full");
        
        data[top] = (byte)b;
        top++;

        if (top == data.length) close();
    }

    public void writeYourselfInto(ChunkWriter cw){
        if (DEBUG) System.out.println("DEBUT writeYourself : memory");
        // Il n'y a que des memorychunks de quelques bits...
        cw.write(data,0,top);
        //cw.finishCurrentChunk();
        //cw.addChunk(this);
        if (lastNbBits>0) cw.writeByte(lastByte,lastNbBits);
        if (DEBUG) System.out.println("FIN writeYourself : memory");
    }

    public void writeYourselfInto(BitOutputStream os) throws IOException {
        if (DEBUG) System.out.println("DEBUT writeYourself : memory");
        os.write(data,0,top);
        if (lastNbBits>0) {
            os.write(lastByte,lastNbBits);
        }
        if (DEBUG) System.out.println("FIN writeYourself : memory");
    }

    /**
     *  Sets the tail of the memory chunk which is a bitfield.
     */
    public void setLastByte(int lastByte,int lastNbBits) {
        this.lastByte=lastByte;
        this.lastNbBits=lastNbBits;
        close();
    }
}

