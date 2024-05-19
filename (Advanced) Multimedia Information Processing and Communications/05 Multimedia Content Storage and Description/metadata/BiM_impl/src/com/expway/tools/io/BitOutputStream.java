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

import java.io.*;

/**
 *  This is a bit-aware OutputStream. It should build up on an underlying OutputStream
 *  and provides the write(int value,int nbbits) method.
 */
public class BitOutputStream extends OutputStream {
    OutputStream outputStream;

    int pendingByte;
    int pendingNumberOfBits;

    /**
     *  Create a bit-aware BitOutputStream from an underlying outpustream.
     */
    public BitOutputStream(OutputStream os) {
        outputStream=os;
        pendingByte=0;
        pendingNumberOfBits=0;
    }

    /**
     *  Flushes the last pending byte and closes the stream.
     */
    public void close() throws IOException {
        flushPendingByte();
        outputStream.close();
    }
    
    /**
     *  Tries to flush the BitOutputStream. This is not always possible if there is a pending
     *  byte. To flush the last pending byte, the BitOutputStream must be close.
     */
    public void flush() throws IOException {
        if (pendingNumberOfBits==0) outputStream.flush();
        else {
            System.out.println("BitOutputStream Flush ? pendingNumberOfBits="+pendingNumberOfBits);
        }
    }
    
    /**
     *  Writes a 8 bits value in the bitstream.
     */     
    public void write(int value) throws IOException {
        if (pendingNumberOfBits==0) emitByte(value);
        else {
            pendingByte = ((value&0xFF) >>> pendingNumberOfBits) | pendingByte;
            emitByte(pendingByte);
            pendingByte = (value << 8 - pendingNumberOfBits)&0xFF;
        }
    }

    /**
     *  Write a nbbits bits value in the bitstream. nbbits should be comprised between 0 inclusive and 8 inclusive.
     *  No checkings are made on the nbbits value.
     */
    public void write(int value,int nbbits) throws IOException {
        if (nbbits<=0) return;
        else if (nbbits==8) write(value);
        else {
            if (pendingNumberOfBits==0) {
                pendingByte = (value << (8 - nbbits))&0xFF;
                pendingNumberOfBits=nbbits;
            } else {
                value  =(byte)( value & ~masks[8 - nbbits]);
                int decalage = 8 - pendingNumberOfBits - nbbits;
                if (decalage < 0) {
                    decalage = -decalage;
                    pendingByte = pendingByte | (value >>> decalage);
                    emitByte(pendingByte);
                    pendingByte = (value << 8 - decalage)&0xFF;
                    pendingNumberOfBits = decalage;
                } else if (decalage == 0) {
                    pendingByte = pendingByte | value;
                    emitByte(pendingByte);
                    pendingNumberOfBits = 0;
                } else {
                    pendingByte = pendingByte | (value << decalage);
                    pendingNumberOfBits = 8 - decalage;
                }
            }
        }   
    }
    
    private void emitByte(int b) throws IOException {
        outputStream.write(b);
    }

    private void flushPendingByte() throws IOException {
        emitByte(pendingByte);
    }

    static private byte[] masks = null;

    static {
        masks = new byte[8];
        for (byte t=0;t<8;t++){
            masks[t] = (byte)(~(0xFF >>> t));
        }
    }

    
} 
